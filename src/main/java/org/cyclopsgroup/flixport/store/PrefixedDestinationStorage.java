package org.cyclopsgroup.flixport.store;

import com.google.api.client.util.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nullable;

public class PrefixedDestinationStorage implements DestinationStorage {
  public static DestinationStorage decorate(DestinationStorage storage, @Nullable String prefix) {
    if (Strings.isNullOrEmpty(prefix)) {
      return storage;
    }
    return new PrefixedDestinationStorage(storage, prefix);
  }

  private final DestinationStorage delegate;
  private final String prefix;

  PrefixedDestinationStorage(DestinationStorage delegate, String prefix) {
    this.delegate = Preconditions.checkNotNull(delegate);
    this.prefix = Preconditions.checkNotNull(prefix);
  }

  @Override
  public void createObject(String path, String mimeType, InputStream content, boolean dryRun)
      throws IOException {
    delegate.createObject(prefix + path, mimeType, content, dryRun);
  }

  public DestinationStorage getDelegate() {
    return delegate;
  }

  public String getPrefix() {
    return prefix;
  }

  @Override
  public Set<String> listObjects(String path) {
    return delegate.listObjects(prefix + path);
  }
}
