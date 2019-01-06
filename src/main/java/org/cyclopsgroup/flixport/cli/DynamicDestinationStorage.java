package org.cyclopsgroup.flixport.cli;

import java.io.IOException;
import javax.annotation.Nullable;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import org.cyclopsgroup.flixport.store.ForwardingDestinationStorage;
import org.cyclopsgroup.flixport.store.fs.LocalFileStorageFactory;
import org.cyclopsgroup.flixport.store.gcs.GoogleStorageFactory;
import com.google.common.collect.ImmutableList;

class DynamicDestinationStorage extends ForwardingDestinationStorage {
  private static final ImmutableList<DestinationStorageFactory> FACTORIES =
      ImmutableList.of(new LocalFileStorageFactory(), new GoogleStorageFactory());

  private static DestinationStorage createMatchingOrFail(String storageSpec,
      @Nullable String credentialSpec) throws IOException {
    DestinationStorageFactory factory = FACTORIES.stream().filter(f -> f.matchesSpec(storageSpec))
        .findAny().orElseThrow(() -> new IllegalArgumentException(
            "Can't find a storage factory for spec " + storageSpec));
    return factory.createStorage(storageSpec, credentialSpec);
  }

  DynamicDestinationStorage(String storageSpec, @Nullable String credentialSpec)
      throws IOException {
    super(createMatchingOrFail(storageSpec, credentialSpec));
  }
}
