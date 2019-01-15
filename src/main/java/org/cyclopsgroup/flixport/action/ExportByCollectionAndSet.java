package org.cyclopsgroup.flixport.action;

import java.io.IOException;
import java.util.List;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;

public class ExportByCollectionAndSet extends AbstractExportSupport implements AutoFlickrAction {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public ExportByCollectionAndSet(Flickr flickr, DestinationStorage storage,
      ExportOptions options) {
    super(flickr, storage, options);
  }

  @Override
  String getDefaultDestDir() {
    return "/${c.title}/${s.title}";
  }

  @Override
  public void run() throws FlickrException, IOException {
    String userId = flickr.getAuth().getUser().getId();
    for (Collection collection : flickr.getCollectionsInterface().getTree(null, userId)) {
      submitJob(() -> traverseCollection(collection, userId), "traverse collection %s",
          collection.getTitle());
    }
  }

  private void traverseCollection(Collection collection, String userId) throws FlickrException {
    List<Collection> children =
        flickr.getCollectionsInterface().getTree(collection.getId(), userId);
    if (!children.isEmpty()) {
      for (Collection child : children) {
        submitJob(() -> traverseCollection(child, userId), "traverse collection %s",
            child.getTitle());
      }
      logger.atInfo().log("Not processing sets in collection %s since it has children collections.",
          collection.getTitle());
      return;
    }
    ImmutableMap<String, Object> params = ImmutableMap.of("c", collection);
    for (Photoset set : flickr.getCollectionsInterface().getInfo(collection.getId())
        .getPhotosets()) {
      submitJob(() -> exportPhotoset(set, params), "export set %s", set.getTitle());
    }
  }
}
