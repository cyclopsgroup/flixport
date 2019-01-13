package org.cyclopsgroup.flixport.store.fs;

import java.io.File;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import org.cyclopsgroup.flixport.store.DestinationStorageOptions;

public class LocalFileStorageFactory extends DestinationStorageFactory {
  @Override
  public DestinationStorage createStorage(DestinationStorageOptions options) {
    File rootDirectory = new File(options.getDestSpec().replaceFirst("^file:", ""));
    return new LocalFileStorage(rootDirectory);
  }

  @Override
  public boolean matchesSpec(String storageSpec) {
    return storageSpec.startsWith("file:");
  }
}
