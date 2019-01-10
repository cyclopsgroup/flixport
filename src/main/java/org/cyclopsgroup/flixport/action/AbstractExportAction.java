package org.cyclopsgroup.flixport.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableMap;

class AbstractExportAction {
  static final int PAGE_SIZE = 200;
  final Flickr flickr;
  private final VelocityEngine ve = new VelocityEngine();

  AbstractExportAction(Flickr flickr) {
    this.flickr = Preconditions.checkNotNull(flickr);
  }

  String evaluateString(String template, ImmutableMap<String, Object> variables, String logTag) {
    try (StringWriter out = new StringWriter()) {
      ve.evaluate(new VelocityContext(variables), out, logTag, template);
      out.flush();
      return out.toString();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Can't render template " + template + " with variables " + variables);
    }
  }

  void exportFile(Photo file, String destPath, DestinationStorage storage, boolean dryRun) {
    try {
      String mimeType = Files.probeContentType(new File(destPath).toPath());
      if (mimeType == null) {
        mimeType = "image/" + file.getOriginalFormat();
      }
      try (InputStream in = flickr.getPhotosInterface().getImageAsStream(file, Size.ORIGINAL)) {
        storage.createObject(destPath, mimeType, in, dryRun);
      }
    } catch (IOException | FlickrException e) {
      throw new IllegalStateException("Can't storage file " + destPath, e);
    }
  }

  void exportPhotoset(DestinationStorage storage, Photoset set, ExportOptions options,
      ExecutorService executor) {
    String destDir =
        evaluateString(options.getDestinationDirectory(), ImmutableMap.of("s", set), "destDir");
    Set<String> existingFileNames = storage.listObjects(destDir);

    List<Photo> allPhotos = new ArrayList<>();
    for (int i = 0;; i++) {
      PhotoList<Photo> list;
      try {
        list = flickr.getPhotosetsInterface().getPhotos(set.getId(), PAGE_SIZE, i);
      } catch (FlickrException e) {
        throw new IllegalStateException(
            "Can't list photos in page " + i + " of set " + set.getTitle());
      }
      if (list.isEmpty()) {
        break;
      }
      allPhotos.addAll(list);
    }

    Map<String, Photo> photosToExport = new HashMap<>();
    for (Photo photo : allPhotos) {
      String fileName =
          evaluateString(options.getDestinationFileName(), ImmutableMap.of("f", photo), "destFile");
      if (!existingFileNames.contains(fileName)) {
        photosToExport.put(fileName, photo);
      }
    }

    for (Map.Entry<String, Photo> e : photosToExport.entrySet()) {
      String destFile = destDir + "/" + e.getKey();
      executor.submit(() -> exportFile(e.getValue(), destFile, storage, options.isDryRun()));
    }
  }
}
