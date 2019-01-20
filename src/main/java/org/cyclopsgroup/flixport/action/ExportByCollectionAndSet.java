package org.cyclopsgroup.flixport.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
  public void run() throws IOException, FlickrException {
    String userId = flickr.getAuth().getUser().getId();
    List<Collection> collections =
        new ArrayList<>(flickr.getCollectionsInterface().getTree(null, userId));
    Collections.sort(collections, Comparator.comparing(Collection::getTitle));
    for (Collection c : collections) {
      traverseCollection(c, userId);
    }
  }

  private void traverseCollection(Collection collection, String userId)
      throws IOException, FlickrException {
    if (isFileLimitBreached()) {
      logger.atInfo().log("Max number of files were exported, ignore collection %s.",
          collection.getTitle());
      return;
    }
    if (!collection.getCollections().isEmpty()) {
      List<Collection> children = new ArrayList<>(collection.getCollections());
      Collections.sort(children, Comparator.comparing(Collection::getTitle));
      for (Collection child : children) {
        submitJob(() -> traverseCollection(child, userId), "traverse collection %s",
            child.getTitle());
      }
      logger.atInfo().log("Not processing non-leaf collection %s.", collection.getTitle());
      return;
    }
    ImmutableMap<String, Object> params = ImmutableMap.of("c", collection);
    List<Photoset> sets = new ArrayList<>(collection.getPhotosets());
    if (sets.isEmpty()) {
      logger.atWarning().log("Leaf collection %s is empty, ignore it.", collection.getTitle());
      return;
    }
    Collections.sort(sets, Comparator.comparing(Photoset::getTitle));
    for (Photoset set : sets) {
      exportPhotoset(set, params);
    }
  }
}
