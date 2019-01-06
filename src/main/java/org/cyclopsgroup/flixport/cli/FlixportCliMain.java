package org.cyclopsgroup.flixport.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cyclopsgroup.flixport.cli.FlickrClient.CollectionFn;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.gcs.GoogleStorageClient;
import org.cyclopsgroup.jcli.ArgumentProcessor;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.common.flogger.FluentLogger;

public class FlixportCliMain {
  private static class CopyToGoogleStorageFn implements CollectionFn<String> {
    private final DestinationStorage dest;
    private final FlickrClient fc;
    private final int maxFilesToCopy;
    private final AtomicInteger processedPhotos = new AtomicInteger(0);
    private final int threads;
    private final boolean dryRun;

    private CopyToGoogleStorageFn(FlickrClient fc, DestinationStorage gcs, int maxFilesToCopy,
        int threads, boolean dryRun) {
      this.fc = fc;
      this.dest = gcs;
      this.maxFilesToCopy = maxFilesToCopy;
      this.threads = threads;
      this.dryRun = dryRun;
    }

    @Override
    public String apply(Collection t, String parentPath) throws FlickrException, IOException {
      if (!t.getCollections().isEmpty()) {
        logger.atInfo().log("Ignoring abstract collection %s since it has children.", t.getTitle());
        return null;
      }
      if (t.getPhotosets().isEmpty()) {
        logger.atWarning().log("Collection %s has no photo set.", t.getTitle());
        return null;
      }
      String path = parentPath + "/" + t.getTitle();
      logger.atInfo().log("Dumping %s sets to dir %s.", t.getPhotosets().size(), path);

      for (Photoset set : t.getPhotosets()) {
        String setPath = path + "/" + set.getTitle();
        copyPhotos(set, setPath);
      }
      return path;
    }

    private void copyPhoto(Photo photo, String parentPath) throws IOException, FlickrException {
      if (processedPhotos.get() > maxFilesToCopy) {
        logger.atInfo().log("Processed %s photos, stop now.", maxFilesToCopy);
        return;
      }
      String destPath = parentPath + "/" + photo.getTitle() + "." + photo.getOriginalFormat();
      String mimeType = Files.probeContentType(new File(destPath).toPath());
      if (mimeType == null) {
        mimeType = "image/" + photo.getOriginalFormat();
      }

      try (InputStream in = fc.openPhoto(photo)) {
        dest.createObject(destPath, mimeType, in, dryRun);
      }
      processedPhotos.incrementAndGet();
    }

    private void copyPhotos(Photoset set, String path) throws FlickrException, IOException {
      if (processedPhotos.get() > maxFilesToCopy) {
        logger.atInfo().log("No copy for set %s.", set);
        return;
      }
      List<Photo> photos = fc.getPhotos(set.getId());
      Map<String, Photo> photosToCopy = new HashMap<>();
      for (Photo p : photos) {
        String fileName = p.getTitle() + "." + p.getOriginalFormat();
        if (photosToCopy.containsKey(fileName)) {
          logger.atWarning().log("Photo %s already appeared in set %s, ignore it.", fileName,
              set.getTitle());
          continue;
        }
        photosToCopy.put(fileName, p);
      }
      Set<String> existingNames = dest.listObjects(path);
      logger.atInfo().log("Found %s photos to copy in set %s while %s already exist in path %s.",
          photosToCopy.size(), set.getTitle(), existingNames.size(), path);
      if (existingNames.containsAll(photosToCopy.keySet())) {
        logger.atInfo().log("All %s photos in set %s are already copied to %s, skip the set.",
            photosToCopy.size(), set.getTitle(), path);
        return;
      }
      existingNames.forEach(photosToCopy::remove);
      logger.atInfo().log("Start copying the missing %s photos in set %s to path %s.",
          photosToCopy.size(), set.getTitle(), path);
      ExecutorService scheduler = Executors.newFixedThreadPool(threads);
      for (Photo photo : photosToCopy.values()) {
        scheduler.execute(() -> {
          try {
            copyPhoto(photo, path);
          } catch (IOException | FlickrException e) {
            logger.atSevere().withCause(e).log("Can't copy photo %s to %s.", photo.getTitle(),
                path);
          }
        });
      }
      scheduler.shutdown();
      try {
        scheduler.awaitTermination(300, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException("Can't shutodown thread pool.", e);
      }
    }
  }

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static void main(String[] args) throws IOException, FlickrException {
    FlixportCliOptions options = new FlixportCliOptions();
    ArgumentProcessor.forType(FlixportCliOptions.class).process(args, options);
    logger.atInfo().log("Start command line with options %s.", options);

    FlickrClient fc = new FlickrClient(options.getFlickAppKey(), options.getFlickAppSecret());
    fc.authenticate(new File(options.getFlickCredentialsDirectory()),
        options.isForceToAuthenticate());
    GoogleStorageClient gcs =
        new GoogleStorageClient(options.getDestBucketName(), options.getDestPathPrefix(),
            options.destCredentialsFile.isEmpty() ? null : new File(options.destCredentialsFile));
    fc.traverseCollections(new CopyToGoogleStorageFn(fc, gcs, options.getMaxFilesToCopy(),
        options.getThreads(), options.isDryRun()), "");
  }
}
