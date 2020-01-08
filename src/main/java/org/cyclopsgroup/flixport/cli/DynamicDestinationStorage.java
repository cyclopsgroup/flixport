package org.cyclopsgroup.flixport.cli;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import org.cyclopsgroup.flixport.store.DestinationStorageOptions;
import org.cyclopsgroup.flixport.store.ForwardingDestinationStorage;
import org.cyclopsgroup.flixport.store.fs.LocalFileStorageFactory;
import org.cyclopsgroup.flixport.store.gcs.GoogleStorageFactory;
import org.cyclopsgroup.flixport.store.s3.S3StorageFactory;

class DynamicDestinationStorage extends ForwardingDestinationStorage {
  private static final ImmutableList<DestinationStorageFactory> FACTORIES =
      ImmutableList.of(
          new LocalFileStorageFactory(), new GoogleStorageFactory(), new S3StorageFactory());

  private static DestinationStorage createMatchingOrFail(DestinationStorageOptions options)
      throws IOException {
    DestinationStorageFactory factory =
        FACTORIES.stream()
            .filter(f -> f.matchesSpec(options.getDestSpec()))
            .findAny()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Can't find a storage factory for spec " + options.getDestSpec()));
    return factory.createStorage(options);
  }

  DynamicDestinationStorage(DestinationStorageOptions options) throws IOException {
    super(createMatchingOrFail(options));
  }
}
