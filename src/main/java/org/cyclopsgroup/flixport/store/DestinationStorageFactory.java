package org.cyclopsgroup.flixport.store;

import java.io.IOException;
import javax.annotation.Nullable;

public abstract class DestinationStorageFactory {
  public abstract DestinationStorage createStorage(String storageSpec,
      @Nullable String credentialSpec) throws IOException;

  public abstract boolean matchesSpec(String storageSpec);
}
