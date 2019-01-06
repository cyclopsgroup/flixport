package org.cyclopsgroup.flixport.store.gcs;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.DestinationStorageFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class GoogleStorageFactory extends DestinationStorageFactory {
  private static final Pattern GCS_SPEC =
      Pattern.compile("^gs:((\\w|\\.|-)+)(/((\\w|\\.|-|/)*))?$");

  @Override
  public DestinationStorage createStorage(String storageSpec, @Nullable String credentialSpec)
      throws IOException {
    Matcher m = GCS_SPEC.matcher(storageSpec);
    Preconditions.checkArgument(m.matches() && m.groupCount() == 5,
        "Input %s isn't a valid GCS path", storageSpec);
    return new GoogleStorageClient(m.group(1), Strings.nullToEmpty(m.group(4)),
        Strings.isNullOrEmpty(credentialSpec) ? null : new File(credentialSpec));
  }

  @Override
  public boolean matchesSpec(String storageSpec) {
    return storageSpec.startsWith("gs:");
  }
}
