package org.cyclopsgroup.flixport.store;

import javax.annotation.Nullable;

public interface DestinationStorageOptions {
  @Nullable
  String getAwsKey();

  String getAwsRegion();

  @Nullable
  String getAwsSecret();

  @Nullable
  String getDestCredentialsFile();

  String getDestSpec();
}
