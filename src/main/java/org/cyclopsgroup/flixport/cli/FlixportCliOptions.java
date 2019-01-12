package org.cyclopsgroup.flixport.cli;

import java.io.File;
import org.cyclopsgroup.flixport.action.ExportOptions;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;

@Cli(name = "flixport")
class FlixportCliOptions implements ExportOptions {
  @Option(name = "c", longName = "credential_spec",
      description = "Destination storage credentials file")
  String destCredentialSpec = "";

  @Option(name = "p", longName = "dest_dir", description = "Directory of destination")
  String destDir = "/${s.title}";

  @Option(name = "n", longName = "dest_file_name", description = "File name of the output file")
  String destFileName = "${f.title}.${f.originalFormat}";

  @Option(name = "d", longName = "dest_spec", description = "Destination spec", required = true)
  String destSpec;

  @Option(name = "r", longName = "dry_run",
      description = "Log action without actually copying files")
  boolean dryRun;

  @Option(name = "k", longName = "flickr_app_key", description = "Flickr application key",
      required = true)
  String flickrAppKey;

  @Option(name = "s", longName = "flickr_app_secret", description = "Flickr application secret",
      required = true)
  String flickrAppSecret;

  @Option(name = "x", longName = "flixport_dir",
      description = "Directory for application specific local files")
  String flixportDir = System.getProperty("user.home") + File.separatorChar + ".flixport";

  @Option(name = "a", longName = "force_authenticate", description = "Force to authenticate")
  boolean forceAuthenticate = false;

  @Option(name = "m", longName = "max_files", description = "Max number of files to copy")
  int maxFilesToCopy = 100;

  @Option(name = "h", longName = "help", description = "Show help message")
  boolean showHelp = false;

  @Option(name = "t", longName = "threads", description = "Number of threads to use")
  int threads = 1;

  @Override
  public String getDestinationDirectory() {
    return destDir;
  }

  @Override
  public String getDestinationFileName() {
    return destFileName;
  }

  @Override
  public int getMaxFilesToExport() {
    return maxFilesToCopy;
  }

  @Override
  public int getThreads() {
    return threads;
  }

  @Override
  public boolean isDryRun() {
    return dryRun;
  }
}
