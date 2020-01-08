package org.cyclopsgroup.flixport.store.gcs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import org.cyclopsgroup.flixport.store.DestinationStorageOptions;
import org.cyclopsgroup.flixport.store.PrefixedDestinationStorage;

public class GoogleStorageFactory extends DestinationStorageFactory {
  private static final Pattern GCS_SPEC =
      Pattern.compile("^gs:((\\w|\\.|-)+)(/((\\w|\\.|-|/)*))?$");

  private static StorageOptions createOptions(@Nullable File credentials) throws IOException {
    if (credentials == null) {
      return StorageOptions.getDefaultInstance();
    }
    try (FileInputStream in = new FileInputStream(credentials)) {
      GoogleCredentials creds =
          GoogleCredentials.fromStream(in)
              .createScoped(ImmutableList.of("https://www.googleapis.com/auth/cloud-platform"));
      return StorageOptions.newBuilder().setCredentials(creds).build();
    }
  }

  @Override
  public DestinationStorage createStorage(DestinationStorageOptions options) throws IOException {
    String storageSpec = options.getDestSpec();
    Matcher m = GCS_SPEC.matcher(storageSpec);
    Preconditions.checkArgument(
        m.matches() && m.groupCount() == 5, "Input %s isn't a valid GCS path", storageSpec);
    StorageOptions gcsOptions =
        Strings.isNullOrEmpty(options.getDestCredentialsFile())
            ? StorageOptions.getDefaultInstance()
            : createOptions(new File(options.getDestCredentialsFile()));
    String prefix = m.group(4);
    return PrefixedDestinationStorage.decorate(
        new GoogleStorageClient(m.group(1), gcsOptions.getService()), prefix);
  }

  @Override
  public boolean matchesSpec(String storageSpec) {
    return storageSpec.startsWith("gs:");
  }
}
