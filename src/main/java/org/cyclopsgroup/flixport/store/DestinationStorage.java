package org.cyclopsgroup.flixport.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface DestinationStorage {
  void createObject(String path, String mimeType, InputStream content, boolean dryRun)
      throws IOException;

  Set<String> listObjects(String path);
}
