package org.cyclopsgroup.flixport.store.fs;

import static com.google.common.truth.Truth.assertThat;
import java.io.File;
import org.apache.commons.lang3.SystemUtils;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.junit.Test;

public class LocalFileStorageFactoryTest {
  private static void verifyStorage(File expectedDirectory, DestinationStorage actualStorage) {
    LocalFileStorage storage = (LocalFileStorage) actualStorage;
    assertThat(storage.getRootDirectory()).isEqualTo(expectedDirectory.getAbsoluteFile());
  }

  @Test
  public void testWithEmptyPath() {
    verifyStorage(new File(""), new LocalFileStorageFactory().createStorage("file:", null));
  }

  @Test
  public void testWithTempFile() {
    File tempDir = SystemUtils.getJavaIoTmpDir();
    verifyStorage(tempDir,
        new LocalFileStorageFactory().createStorage("file:" + tempDir.getAbsolutePath(), null));
  }
}
