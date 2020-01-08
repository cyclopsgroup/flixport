package org.cyclopsgroup.flixport.store.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.api.client.util.Preconditions;
import com.google.common.flogger.FluentLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Collectors;
import org.cyclopsgroup.flixport.store.DestinationStorage;

class S3StorageClient implements DestinationStorage {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final AmazonS3 s3;
  private final String bucketName;

  S3StorageClient(AmazonS3 s3, String bucketName) {
    this.s3 = Preconditions.checkNotNull(s3);
    this.bucketName = Preconditions.checkNotNull(bucketName);
  }

  @Override
  public void createObject(String path, String mimeType, InputStream content, boolean dryRun)
      throws IOException {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(mimeType);

    if (dryRun) {
      logger.atInfo().log(
          "Would store %s content to s3 %s:%s, but this is dry run mode.",
          mimeType, bucketName, path);
      return;
    }

    File tmpFile = File.createTempFile("flickrport-s3", ".tmp");
    logger.atInfo().log("Copying content to %s via a temporary file %s.", path, tmpFile);
    try {
      Files.copy(content, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      PutObjectRequest put = new PutObjectRequest(bucketName, path, tmpFile).withMetadata(metadata);
      s3.putObject(put);
    } finally {
      tmpFile.delete();
    }
  }

  @Override
  public Set<String> listObjects(String path) {
    return s3.listObjects(bucketName, path).getObjectSummaries().stream()
        .map(s -> new File(s.getKey()).getName())
        .collect(Collectors.toSet());
  }
}
