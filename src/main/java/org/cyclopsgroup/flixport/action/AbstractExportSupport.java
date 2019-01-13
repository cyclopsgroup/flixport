package org.cyclopsgroup.flixport.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.MoreExecutors;

class AbstractExportSupport implements AutoCloseable {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  static final int PAGE_SIZE = 250;
  private final Executor executor;
  final Flickr flickr;
  final ExportOptions options;
  private final AtomicInteger photoCount = new AtomicInteger();
  final DestinationStorage storage;
  private final VelocityEngine ve = new VelocityEngine();

  AbstractExportSupport(Flickr flickr, DestinationStorage storage, ExportOptions options) {
    this.flickr = Preconditions.checkNotNull(flickr);
    this.storage = Preconditions.checkNotNull(storage);
    this.options = Preconditions.checkNotNull(options);
    BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(20);
    if (options.getThreads() == 1) {
      logger.atInfo().log("Will run in single-thread mode.");
      executor = MoreExecutors.directExecutor();
    } else {
      logger.atInfo().log("Will run in %s threads.", options.getThreads());
      executor = new ThreadPoolExecutor(options.getThreads(), options.getThreads(), 0,
          TimeUnit.SECONDS, taskQueue, new ThreadPoolExecutor.CallerRunsPolicy());
    }
  }

  @Override
  public void close() throws InterruptedException {
    if (executor instanceof ExecutorService) {
      MoreExecutors.shutdownAndAwaitTermination((ExecutorService) executor, 300, TimeUnit.SECONDS);
    }
  }

  String evaluateString(String template, ImmutableMap<String, Object> variables, String logTag) {
    try (StringWriter out = new StringWriter()) {
      ve.evaluate(new VelocityContext(variables), out, logTag, template);
      out.flush();
      return out.toString();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Can't render template " + template + " with variables " + variables);
    }
  }

  void exportFile(Photo file, String destPath) throws FlickrException, IOException {
    if (photoCount.incrementAndGet() > options.getMaxFilesToExport()) {
      logger.atInfo().log("Ignoring the %s th photo %s since it breaches the limit %s.",
          photoCount.get(), file.getTitle(), options.getMaxFilesToExport());
      return;
    }

    String mimeType = Files.probeContentType(new File(destPath).toPath());
    if (mimeType == null) {
      mimeType = "image/" + file.getOriginalFormat();
    }
    try (InputStream in = flickr.getPhotosInterface().getImageAsStream(file, Size.ORIGINAL)) {
      storage.createObject(destPath, mimeType, in, options.isDryRun());
    }
  }

  void exportPhotoset(Photoset set) throws FlickrException {
    if (isFileLimitBreached()) {
      logger.atInfo().log(
          "Ignoring photoset %s since number of exported photo breaches the limit %s.",
          set.getTitle(), options.getMaxFilesToExport());
      return;
    }

    String destDir = evaluateString(options.getDestDir(), ImmutableMap.of("s", set), "destDir");
    Set<String> existingFileNames = storage.listObjects(destDir);
    logger.atInfo().log("Found %s files in destination of set %s, %s.", existingFileNames.size(),
        set.getTitle(), destDir);

    List<Photo> allPhotos = new ArrayList<>();
    for (int i = 0;; i++) {
      PhotoList<Photo> list = flickr.getPhotosetsInterface().getPhotos(set.getId(), PAGE_SIZE, i);
      if (list.isEmpty()) {
        break;
      }
      logger.atInfo().log("Found %s photos from set %s in page %s.", list.size(), set.getTitle(),
          i);
      allPhotos.addAll(list);
      if (list.size() < PAGE_SIZE) {
        break;
      }
    }

    Map<String, Photo> photosToExport = new HashMap<>();
    for (Photo photo : allPhotos) {
      String fileName =
          evaluateString(options.getDestFileName(), ImmutableMap.of("f", photo), "destFile");
      if (!existingFileNames.contains(fileName)) {
        photosToExport.put(fileName, photo);
      }
    }

    if (photosToExport.isEmpty()) {
      logger.atInfo().log("All %s photos in set %s already exist in destination %s.",
          allPhotos.size(), set.getTitle(), destDir);
      return;
    } else {
      logger.atInfo().log("Exporting %s out of total %s photos from set %s to destination %s.",
          photosToExport.size(), allPhotos.size(), set.getTitle(), destDir);
    }

    for (Map.Entry<String, Photo> e : photosToExport.entrySet()) {
      String destFile = destDir + "/" + e.getKey();
      submitJob(() -> exportFile(e.getValue(), destFile), "export photo %s to %s",
          e.getValue().getTitle(), destFile);
    }
  }

  boolean isFileLimitBreached() {
    return photoCount.get() > options.getMaxFilesToExport();
  }

  void submitJob(FlickrAction action, String format, Object... args) {
    executor.execute(() -> {
      try {
        action.run();
      } catch (FlickrException | IOException e) {
        String actionName = String.format(format, args);
        logger.atSevere().withCause(e).log("Action %s failed: %s.", actionName, e.getMessage());
        throw new RuntimeException("Can't execute action [" + actionName + "]: " + action, e);
      }
    });
  }
}
