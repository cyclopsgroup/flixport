package org.cyclopsgroup.flixport.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import com.google.common.base.Preconditions;

public class ForwardingDestinationStorage implements DestinationStorage {
  private DestinationStorage delegate;

  protected ForwardingDestinationStorage(DestinationStorage delegate) {
    this.delegate = Preconditions.checkNotNull(delegate);
  }

  @Override
  public void createObject(String path, String mimeType, InputStream content, boolean dryRun)
      throws IOException {
    delegate.createObject(path, mimeType, content, dryRun);
  }

  @Override
  public Set<String> listObjects(String path) {
    return delegate.listObjects(path);
  }
}
