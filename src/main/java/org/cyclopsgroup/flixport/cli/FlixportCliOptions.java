package org.cyclopsgroup.flixport.cli;

import java.io.File;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;

@Cli(name = "flixport")
class FlixportCliOptions {
  @Option(name = "c", longName = "credential_spec",
      description = "Destination storage credentials file")
  String destCredentialSpec = "";

  @Option(name = "p", longName = "dest_dir", description = "Directory of destination")
  String destDir = "/${c.title}/${s.title}";

  @Option(name = "n", longName = "dest_file_name", description = "File name of the output file")
  String destFileName = "${f.title}.${f.type}";

  @Option(name = "d", longName = "dest_spec", description = "Destination spec")
  String destSpec = "gs:jiaqi-test-2/flickr";

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

  @Option(name = "t", longName = "threads", description = "Number of threads to use")
  int threads = 5;
}
