package org.cyclopsgroup.flixport.store.fs;

import com.google.api.client.util.Preconditions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.flogger.FluentLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.cyclopsgroup.flixport.store.DestinationStorage;

public class LocalFileStorage implements DestinationStorage {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String TEMP_SUFFIX = ".tmp";

  private static File assureDirectory(File directory, boolean dryRun) {
    if (directory.isDirectory()) {
      return directory;
    }
    if (dryRun) {
      logger.atWarning().log(
          "Directory %s would be created since it doesn't exist, but it's not because of dry run.");
      return directory;
    }
    Preconditions.checkState(
        directory.mkdirs(), "Root directory %s doesn't exist and can't be created.", directory);
    logger.atInfo().log("Made new directory %s since it didn't exist.", directory);
    return directory;
  }

  private final File rootDirectory;

  LocalFileStorage(File rootDirectory) {
    this.rootDirectory =
        assureDirectory(Preconditions.checkNotNull(rootDirectory).getAbsoluteFile(), false);
  }

  @Override
  public void createObject(String path, String mimeType, InputStream content, boolean dryRun)
      throws IOException {
    File destFile = new File(rootDirectory, path).getAbsoluteFile();
    assureDirectory(destFile.getParentFile(), dryRun);
    logger.atInfo().log("Copying %s content to local file %s.", mimeType, destFile);
    if (dryRun) {
      logger.atInfo().log("Skip actual copying since it's dry run.");
      return;
    }
    File tempFile = new File(destFile.getAbsolutePath() + TEMP_SUFFIX);
    if (tempFile.isFile() && !tempFile.delete()) {
      logger.atWarning().log("Couldn't delete the pre-existing temp file %s.", tempFile);
    }
    Files.copy(content, tempFile.toPath());
    Files.move(tempFile.toPath(), destFile.toPath());
  }

  @VisibleForTesting
  File getRootDirectory() {
    return rootDirectory;
  }

  @Override
  public Set<String> listObjects(String path) {
    File directory = new File(rootDirectory, path);
    if (!directory.exists()) {
      return Collections.emptySet();
    }
    return Arrays.asList(directory.listFiles(File::isFile)).stream()
        .map(f -> f.getName())
        .filter(n -> !n.endsWith(TEMP_SUFFIX))
        .collect(Collectors.toSet());
  }
}
