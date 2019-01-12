package org.cyclopsgroup.flixport.action;

public interface ExportOptions {
  String getDestinationDirectory();

  String getDestinationFileName();

  int getMaxFilesToExport();

  int getThreads();

  boolean isDryRun();
}
