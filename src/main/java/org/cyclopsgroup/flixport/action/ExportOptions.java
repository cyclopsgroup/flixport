package org.cyclopsgroup.flixport.action;

import javax.annotation.Nullable;

public interface ExportOptions {
  @Nullable
  String getDestDir();

  String getDestFileName();

  int getMaxAttempts();

  int getMaxFilesToExport();

  int getThreads();

  boolean isDryRun();
}
