package org.cyclopsgroup.flixport.store;

import java.io.IOException;

public abstract class DestinationStorageFactory {
  public abstract DestinationStorage createStorage(DestinationStorageOptions options)
      throws IOException;

  public abstract boolean matchesSpec(String storageSpec);
}
