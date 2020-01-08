package org.cyclopsgroup.flixport.action;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import org.cyclopsgroup.flixport.store.DestinationStorage;

/**
 * Exports photos by scanning through all photosets and dump photos based on photoset identifiers in
 * directory.
 */
public class ExportByPhotoset extends AbstractExportSupport implements AutoFlickrAction {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public ExportByPhotoset(Flickr flickr, DestinationStorage storage, ExportOptions options) {
    super(flickr, storage, options);
  }

  @Override
  String getDefaultDestDir() {
    return "/${s.title}";
  }

  @Override
  public void run() throws IOException, FlickrException {
    String userId = flickr.getAuth().getUser().getId();
    for (int i = 0; ; i++) {
      Photosets sets = flickr.getPhotosetsInterface().getList(userId, PAGE_SIZE, i, null);
      if (sets.getPhotosets().isEmpty()) {
        logger.atInfo().log("Reached empty page of fileset at page %s, exits.", i);
        break;
      }
      if (isFileLimitBreached()) {
        logger.atInfo().log("Max number of files were exported, stop at page %s.", i);
        break;
      }
      for (Photoset set : sets.getPhotosets()) {
        logger.atInfo().log("Found photoset %s and submitting a job to export it.", set.getTitle());
        exportPhotoset(set, ImmutableMap.of());
      }
      if (sets.getPhotosets().size() < PAGE_SIZE) {
        break;
      }
    }
  }
}
