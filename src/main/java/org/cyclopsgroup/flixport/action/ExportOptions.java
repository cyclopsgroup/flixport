package org.cyclopsgroup.flixport.action;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExportOptions {
  public static ExportOptions of(String destinationPath, int maxFiles, int threads) {
    return new AutoValue_ExportOptions(destinationPath, maxFiles, threads);
  }

  public abstract String getDestinationPath();

  public abstract int getMaxFilesToExport();

  public abstract int getThreads();
}
