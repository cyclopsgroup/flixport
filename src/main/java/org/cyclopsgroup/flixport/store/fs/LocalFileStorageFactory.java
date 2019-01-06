package org.cyclopsgroup.flixport.store.fs;

import java.io.File;
import javax.annotation.Nullable;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;

public class LocalFileStorageFactory extends DestinationStorageFactory {
  @Override
  public DestinationStorage createStorage(String storageSpec, @Nullable String credentialSpec) {
    File rootDirectory = new File(storageSpec.replaceFirst("^file:", ""));
    return new LocalFileStorage(rootDirectory);
  }

  @Override
  public boolean matchesSpec(String storageSpec) {
    return storageSpec.startsWith("file:");
  }
}
