package org.cyclopsgroup.flixport.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.amazonaws.endpointdiscovery.DaemonThreadFactory;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.api.client.util.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

abstract class AbstractExportSupport implements AutoCloseable {
  private interface ActionExecutor {
    Future<Boolean> submitJob(FlickrAction action) throws FlickrException, IOException;
  }

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  static final int PAGE_SIZE = 250;

  final String destDir;
  private final ActionExecutor executor;
  final Flickr flickr;
  final ExportOptions options;
  private final AtomicInteger photoCount = new AtomicInteger();
  final DestinationStorage storage;
  private final VelocityEngine ve = new VelocityEngine();

  AbstractExportSupport(Flickr flickr, DestinationStorage storage, ExportOptions options) {
    this.flickr = Preconditions.checkNotNull(flickr);
    this.storage = Preconditions.checkNotNull(storage);
    this.options = Preconditions.checkNotNull(options);
    if (options.getThreads() == 1) {
      logger.atInfo().log("Will run in single-thread mode.");
      executor = new ActionExecutor() {
        public Future<Boolean> submitJob(FlickrAction action) throws FlickrException, IOException {
          action.run();
          return Futures.immediateFuture(true);
        }
      };
    } else {
      logger.atInfo().log("Will run in %s threads.", options.getThreads());
      ExecutorService executorService = new ThreadPoolExecutor(options.getThreads(),
          options.getThreads(), 0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
          DaemonThreadFactory.INSTANCE, new ThreadPoolExecutor.CallerRunsPolicy());
      executor = new ActionExecutor() {
        public Future<Boolean> submitJob(FlickrAction action) {
          return executorService.<Boolean>submit(() -> {
            try {
              action.run();
              return true;
            } catch (Throwable e) {
              logger.atSevere().withCause(e).log("Action failed.");
              return false;
            }
          });
        }
      };
    }
    this.destDir =
        Strings.isNullOrEmpty(options.getDestDir()) ? getDefaultDestDir() : options.getDestDir();
    logger.atInfo().log("Destination file path is %s.", destDir);
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

  void exportPhotoset(Photoset set, ImmutableMap<String, Object> params)
      throws IOException, FlickrException {
    for (int i = 0; i < options.getMaxAttempts(); i++) {
      try {
        if (exportPhotosetInternally(set, params)) {
          return;
        }
      } catch (Throwable e) {
        logger.atWarning().withCause(e).log("Can't export photoset %s at the %s th try.",
            set.getTitle(), i);
      }
      try {
        Thread.sleep((long) (Math.pow(1.5, i) * 200));
      } catch (InterruptedException e) {
        throw new RuntimeException("Sleeping thread is interrupted.", e);
      }
    }
    logger.atSevere().log("Quit exporting set %s after 5 tries.", set.getTitle());
  }

  private boolean exportPhotosetInternally(Photoset set, ImmutableMap<String, Object> params)
      throws IOException, FlickrException {
    if (isFileLimitBreached()) {
      logger.atInfo().log(
          "Ignoring photoset %s since number of exported photo breaches the limit %s.",
          set.getTitle(), options.getMaxFilesToExport());
      return true;
    }

    ImmutableMap<String, Object> setParams =
        ImmutableMap.<String, Object>builder().putAll(params).put("s", set).build();
    String fullDestDir = evaluateString(this.destDir, setParams, "destDir");
    Set<String> existingFileNames = storage.listObjects(fullDestDir);
    logger.atInfo().log("Found %s files in destination of set %s, %s.", existingFileNames.size(),
        set.getTitle(), fullDestDir);

    List<Photo> allPhotos = new ArrayList<>();
    for (int i = 0;; i++) {
      String id = set.getId();
      PhotoList<Photo> list = flickr.getPhotosetsInterface().getPhotos(id, PAGE_SIZE, i);
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

    Map<String, Photo> photosToExport = new TreeMap<>();
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
      return true;
    } else {
      logger.atInfo().log("Exporting %s out of total %s photos from set %s to destination %s.",
          photosToExport.size(), allPhotos.size(), set.getTitle(), destDir);
    }

    List<Future<Boolean>> results = new ArrayList<>();
    for (Map.Entry<String, Photo> entry : photosToExport.entrySet()) {
      String destFile = fullDestDir + "/" + entry.getKey();
      results.add(submitJob(() -> exportFile(entry.getValue(), destFile), "export photo %s to %s",
          entry.getValue().getTitle(), destFile));
    }

    boolean finalResult = true;
    for (Future<Boolean> result : results) {
      try {
        finalResult &= result.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.atSevere().withCause(e).log("Can't get result of a file export.");
        finalResult = false;
      }
    }
    return finalResult;
  }

  abstract String getDefaultDestDir();

  boolean isFileLimitBreached() {
    return photoCount.get() > options.getMaxFilesToExport();
  }

  Future<Boolean> submitJob(FlickrAction action, String format, Object... args)
      throws IOException, FlickrException {
    String actionName = String.format(format, args);
    return executor.submitJob(() -> {
      logger.atInfo().log("Starting action %s.", actionName);
      action.run();
    });
  }
}
