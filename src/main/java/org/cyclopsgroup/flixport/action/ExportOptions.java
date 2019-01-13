package org.cyclopsgroup.flixport.action;

public interface ExportOptions {
  String getDestDir();

  String getDestFileName();

  int getMaxFilesToExport();

  int getThreads();

  boolean isDryRun();
}
