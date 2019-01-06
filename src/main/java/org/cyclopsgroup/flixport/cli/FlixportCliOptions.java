package org.cyclopsgroup.flixport.cli;

import java.io.File;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;

@Cli(name = "seetl")
public class FlixportCliOptions {

  private String destBucketName = "jiaqi-test-2";

  public String destCredentialsFile = "";

  private String destPathPrefix = "flickr";

  private boolean dryRun;

  private String flickAppKey = "9e29c69c42532a690ed1d9cb22bd27a8";

  private String flickAppSecret = "075f83fc3bb3e6f2";

  private String flickCredentialsDirectory =
      System.getProperty("user.home") + File.separatorChar + ".flickr";

  private boolean forceToAuthenticate = false;

  private int maxFilesToCopy = 100;

  private int threads = 5;

  public String getDestBucketName() {
    return destBucketName;
  }

  public String getDestCredentialsFile() {
    return destCredentialsFile;
  }

  public String getDestPathPrefix() {
    return destPathPrefix;
  }

  public String getFlickAppKey() {
    return flickAppKey;
  }

  public String getFlickAppSecret() {
    return flickAppSecret;
  }

  public String getFlickCredentialsDirectory() {
    return flickCredentialsDirectory;
  }

  public int getMaxFilesToCopy() {
    return maxFilesToCopy;
  }

  public int getThreads() {
    return threads;
  }

  public boolean isDryRun() {
    return dryRun;
  }

  public boolean isForceToAuthenticate() {
    return forceToAuthenticate;
  }

  @Option(name = "b", longName = "bucket_name", description = "Google storage bucket name")
  public void setDestBucketName(String destBucketName) {
    this.destBucketName = destBucketName;
  }

  @Option(name = "g", longName = "dest_creds", description = "Destination storage credentials file")
  public void setDestCredentialsFile(String destCredentialsFile) {
    this.destCredentialsFile = destCredentialsFile;
  }

  @Option(name = "p", longName = "path_prefix", description = "Destination path prefix")
  public void setDestPathPrefix(String destPathPrefix) {
    this.destPathPrefix = destPathPrefix;
  }

  @Option(name = "d", longName = "dry_run",
      description = "Log action without actually copying files")
  public void setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
  }

  @Option(name = "k", longName = "flickr_app_key", description = "Flickr application key")
  public void setFlickAppKey(String flickAppKey) {
    this.flickAppKey = flickAppKey;
  }

  @Option(name = "s", longName = "flickr_app_secret", description = "Flickr application secret")
  public void setFlickAppSecret(String flickAppSecret) {
    this.flickAppSecret = flickAppSecret;
  }

  @Option(name = "c", longName = "flickr_creds_dir",
      description = "Directory of flickr credentials")
  public void setFlickCredentialsDirectory(String flickCredentialsDirectory) {
    this.flickCredentialsDirectory = flickCredentialsDirectory;
  }

  @Option(name = "a", longName = "force_to_authenticate", description = "Force to authenticate")
  public void setForceToAuthenticate(boolean forceToAuthenticate) {
    this.forceToAuthenticate = forceToAuthenticate;
  }

  @Option(name = "l", longName = "max_files", description = "Max number of files to copy")
  public void setMaxFilesToCopy(int maxFilesToCopy) {
    this.maxFilesToCopy = maxFilesToCopy;
  }

  @Option(name = "t", longName = "threads", description = "Number of threads to use")
  public void setThreads(int threads) {
    this.threads = threads;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
