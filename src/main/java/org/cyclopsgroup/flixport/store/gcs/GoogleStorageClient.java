package org.cyclopsgroup.flixport.store.gcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.google.api.client.util.Preconditions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;

public class GoogleStorageClient implements DestinationStorage {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static StorageOptions createOptions(@Nullable File credentials) throws IOException {
    if (credentials == null) {
      return StorageOptions.getDefaultInstance();
    }
    try (FileInputStream in = new FileInputStream(credentials)) {
      GoogleCredentials creds = GoogleCredentials.fromStream(in)
          .createScoped(ImmutableList.of("https://www.googleapis.com/auth/cloud-platform"));
      return StorageOptions.newBuilder().setCredentials(creds).build();
    }
  }

  private final String bucketName;
  private final String prefix;
  private final Storage storage;

  GoogleStorageClient(String bucketName, String prefix, @Nullable File credentials)
      throws IOException {
    this.storage = createOptions(credentials).getService();
    this.bucketName = bucketName;
    this.prefix = prefix;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void createObject(String path, String mimeType, InputStream in, boolean dryRun) {
    Preconditions.checkNotNull(in, "Input content can't be null.");
    String location = prefix + Preconditions.checkNotNull(path, "Path can't be null.");
    logger.atInfo().log("Storing %s content to location %s.",
        Preconditions.checkNotNull(mimeType, "Mime type can't be null."), location);
    if (dryRun) {
      logger.atInfo().log("Skip actual copying of %s file to %s since it's dry run.", mimeType,
          path);
      return;
    }
    storage.create(
        BlobInfo.newBuilder(BlobId.of(bucketName, location)).setContentType(mimeType).build(), in);
  }

  @VisibleForTesting
  String getBucketName() {
    return bucketName;
  }

  @VisibleForTesting
  String getPrefix() {
    return prefix;
  }

  @Override
  public Set<String> listObjects(String path) {
    return StreamSupport
        .stream(storage.list(bucketName, BlobListOption.prefix(prefix + path)).iterateAll()
            .spliterator(), false)
        .map(b -> new File(b.getName()).getName()).collect(Collectors.toSet());
  }
}
