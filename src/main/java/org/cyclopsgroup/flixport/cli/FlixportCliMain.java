package org.cyclopsgroup.flixport.cli;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import org.cyclopsgroup.flixport.action.ExportFlickrByPhotoset;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.jcli.ArgumentProcessor;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;

public class FlixportCliMain {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static void main(String[] args) throws IOException, FlickrException, InterruptedException {
    FlixportCliOptions options = new FlixportCliOptions();
    ArgumentProcessor<FlixportCliOptions> processor =
        ArgumentProcessor.forType(FlixportCliOptions.class);
    processor.process(args, options);
    if (options.showHelp) {
      try (PrintWriter out = new PrintWriter(System.out)) {
        processor.printHelp(out);
        out.flush();
      }
      return;
    }

    File appDir = new File(options.flixportDir).getAbsoluteFile();
    if (!appDir.isDirectory()) {
      if (appDir.mkdirs()) {
        logger.atInfo().log("Made working directory %s.", appDir);
      } else {
        logger.atWarning().log("Can't make working directory %s.", appDir);
      }
    }
    File propFile = new File(appDir, "cli.properties");
    if (propFile.isFile()) {
      logger.atInfo().log("Reading additional options from properties file %s.", propFile);
      Properties props = new Properties();
      try (FileReader in = new FileReader(propFile)) {
        props.load(in);
      }
      processor.process(props, options);
      // Processing arguments again in case properties overlaps with command line arguments.
      processor.process(args, options);
    }
    Preconditions.checkNotNull(options.destSpec, "Destination must be specified.");

    logger.atInfo().log("Start command line with options %s.", options);

    Flickr flickr = new Flickr(options.flickrAppKey, options.flickrAppSecret, new REST());
    FlickrClient fc = new FlickrClient(flickr);
    fc.authenticate(appDir, options.forceAuthenticate);

    DestinationStorage storage =
        new DynamicDestinationStorage(options.destSpec, options.destCredentialSpec);
    logger.atInfo().log("Destination storage is %s.", storage);
    try (ExportFlickrByPhotoset action = new ExportFlickrByPhotoset(flickr, storage, options)) {
      action.run();
    }
  }
}
