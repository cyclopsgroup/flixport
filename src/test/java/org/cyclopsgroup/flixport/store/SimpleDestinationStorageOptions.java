package org.cyclopsgroup.flixport.store;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SimpleDestinationStorageOptions implements DestinationStorageOptions {
  public static SimpleDestinationStorageOptions forS3(String spec, String awsKey,
      String awsSecret) {
    return new AutoValue_SimpleDestinationStorageOptions(awsKey, awsSecret, null, spec);
  }

  public static SimpleDestinationStorageOptions forSpec(String spec) {
    return new AutoValue_SimpleDestinationStorageOptions(null, null, null, spec);
  }
}
