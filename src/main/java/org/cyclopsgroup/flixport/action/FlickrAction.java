package org.cyclopsgroup.flixport.action;

import com.flickr4java.flickr.FlickrException;
import java.io.IOException;

public interface FlickrAction {
  void run() throws FlickrException, IOException;
}
