package org.cyclopsgroup.flixport.action;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExportOptions {
  public static ExportOptions of(String destDir, String destFileName, int maxFiles, int threads,
      boolean dryRun) {
    return new AutoValue_ExportOptions(destDir, destFileName, maxFiles, threads, dryRun);
  }

  public abstract String getDestinationDirectory();

  public abstract String getDestinationFileName();

  public abstract int getMaxFilesToExport();

  public abstract int getThreads();

  public abstract boolean isDryRun();
}
