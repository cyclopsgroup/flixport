package org.cyclopsgroup.flixport.action;

import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.google.common.flogger.FluentLogger;

public class ExportFlickrByPhotoset extends AbstractExportSupport implements FlickrAction {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public ExportFlickrByPhotoset(Flickr flickr, DestinationStorage storage, ExportOptions options) {
    super(flickr, storage, options);
  }

  @Override
  public void run() throws FlickrException {
    String userId = flickr.getAuth().getUser().getId();
    for (int i = 0;; i++) {
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
        submitJob(() -> {
          exportPhotoset(set);
        }, "export set %s", set.getTitle());
      }
      if (sets.getPhotosets().size() < PAGE_SIZE) {
        break;
      }
    }
  }
}
