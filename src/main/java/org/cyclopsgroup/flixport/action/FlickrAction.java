package org.cyclopsgroup.flixport.action;

import java.io.IOException;
import com.flickr4java.flickr.FlickrException;

public interface FlickrAction {
  void run() throws FlickrException, IOException;
}
