package org.cyclopsgroup.flixport.cli;

import java.io.File;
import javax.annotation.Nullable;
import org.cyclopsgroup.flixport.action.ExportOptions;
import org.cyclopsgroup.flixport.store.DestinationStorageOptions;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;

@Cli(name = "flixport")
class FlixportCliOptions implements ExportOptions, DestinationStorageOptions {
  @Option(name = "x", longName = "aws_key", description = "AWS credentials key")
  private String awsKey;

  @Option(name = "z", longName = "aws_secret", description = "AWS credentials secret")
  private String awsSecret;

  @Option(name = "c", longName = "dest_creds", displayName = "file",
      description = "Destination storage credentials file or spec")
  private String destCredentialsFile = "";

  @Option(name = "p", longName = "dest_dir", displayName = "dir",
      description = "Directory of destination")
  private String destDir = "/${s.title}";

  @Option(name = "n", longName = "dest_file_name", displayName = "format",
      description = "File name of the output file")
  private String destFileName = "${f.title}.${f.originalFormat}";

  @Option(name = "d", longName = "dest_spec", description = "Destination spec", required = true)
  private String destSpec;

  @Option(name = "r", longName = "dry_run",
      description = "Log action without actually copying files")
  private boolean dryRun;

  @Option(name = "k", longName = "app_key", description = "Flickr application key", required = true)
  String flickrAppKey;

  @Option(name = "s", longName = "app_secret", description = "Flickr application secret",
      required = true)
  String flickrAppSecret;

  @Option(name = "x", longName = "flixport_dir", displayName = "dir",
      description = "Directory for application specific local files")
  String flixportDir = System.getProperty("user.home") + File.separatorChar + ".flixport";

  @Option(name = "a", longName = "force_auth", description = "Force to authenticate")
  boolean forceAuthenticate = false;

  @Option(name = "m", longName = "max_files", displayName = "integer",
      description = "Max number of files to copy")
  private int maxFilesToCopy = 100;

  @Option(name = "h", longName = "help", description = "Show help message")
  boolean showHelp = false;

  @Option(name = "t", longName = "threads", displayName = "integer",
      description = "Number of threads to use")
  private int threads = 1;

  @Override
  @Nullable
  public String getAwsKey() {
    return awsKey;
  }

  @Override
  @Nullable
  public String getAwsSecret() {
    return awsSecret;
  }

  @Override
  @Nullable
  public String getDestCredentialsFile() {
    return destCredentialsFile;
  }

  @Override
  public String getDestDir() {
    return destDir;
  }

  @Override
  public String getDestFileName() {
    return destFileName;
  }

  @Override
  public String getDestSpec() {
    return destSpec;
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
