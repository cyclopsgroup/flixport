package org.cyclopsgroup.flixport.store.gcs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.google.api.client.util.Preconditions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.flogger.FluentLogger;

public class GoogleStorageClient implements DestinationStorage {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final String bucketName;
  private final Storage storage;

  GoogleStorageClient(String bucketName, Storage storage) throws IOException {
    this.storage = Preconditions.checkNotNull(storage, "GCS storage can't be null.");
    this.bucketName = Preconditions.checkNotNull(bucketName, "Bucket name can't be null");
  }

  @SuppressWarnings("deprecation")
  @Override
  public void createObject(String path, String mimeType, InputStream in, boolean dryRun) {
    Preconditions.checkNotNull(in, "Input content can't be null.");
    Preconditions.checkNotNull(path, "Path can't be null.");
    logger.atInfo().log("Storing %s content to location %s.",
        Preconditions.checkNotNull(mimeType, "Mime type can't be null."), path);
    if (dryRun) {
      logger.atInfo().log("Skip actual copying of %s file to %s since it's dry run.", mimeType,
          path);
      return;
    }
    storage.create(
        BlobInfo.newBuilder(BlobId.of(bucketName, path)).setContentType(mimeType).build(), in);
  }

  @VisibleForTesting
  String getBucketName() {
    return bucketName;
  }

  @Override
  public Set<String> listObjects(String path) {
    return StreamSupport
        .stream(storage.list(bucketName, BlobListOption.prefix(path)).iterateAll().spliterator(),
            false)
        .map(b -> new File(b.getName()).getName()).collect(Collectors.toSet());
  }
}
