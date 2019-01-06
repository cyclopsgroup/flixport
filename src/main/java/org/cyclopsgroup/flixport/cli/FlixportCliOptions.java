package org.cyclopsgroup.flixport.cli;

import java.io.File;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;

@Cli(name = "seetl")
public class FlixportCliOptions {

  public String destCredentialSpec = "";

  private String destPath = "flickr";

  private String destSpec = "jiaqi-test-2";

  private boolean dryRun;

  private String flickAppKey;

  private String flickAppSecret;

  private String flickCredentialsDirectory =
      System.getProperty("user.home") + File.separatorChar + ".flickr";

  private boolean forceToAuthenticate = false;

  private int maxFilesToCopy = 100;

  private int threads = 5;

  public String getDestCredentialSpec() {
    return destCredentialSpec;
  }

  public String getDestPath() {
    return destPath;
  }

  public String getDestSpec() {
    return destSpec;
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

  @Option(name = "c", longName = "credential_spec",
      description = "Destination storage credentials file")
  public void setDestCredentialSpec(String destCredentialsFile) {
    this.destCredentialSpec = destCredentialsFile;
  }

  @Option(name = "p", longName = "path_prefix", description = "Destination path prefix")
  public void setDestPath(String destPathPrefix) {
    this.destPath = destPathPrefix;
  }

  @Option(name = "d", longName = "dest_spec", description = "Destination spec")
  public void setDestSpec(String destBucketName) {
    this.destSpec = destBucketName;
  }

  @Option(name = "r", longName = "dry_run",
      description = "Log action without actually copying files")
  public void setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
  }

  @Option(name = "k", longName = "flickr_app_key", description = "Flickr application key",
      required = true)
  public void setFlickAppKey(String flickAppKey) {
    this.flickAppKey = flickAppKey;
  }

  @Option(name = "s", longName = "flickr_app_secret", description = "Flickr application secret",
      required = true)
  public void setFlickAppSecret(String flickAppSecret) {
    this.flickAppSecret = flickAppSecret;
  }

  @Option(name = "a", longName = "flickr_auth_dir",
      description = "Directory of flickr authentication credentials")
  public void setFlickCredentialsDirectory(String flickCredentialsDirectory) {
    this.flickCredentialsDirectory = flickCredentialsDirectory;
  }

  @Option(name = "f", longName = "force_to_authenticate", description = "Force to authenticate")
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
