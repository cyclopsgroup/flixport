package org.cyclopsgroup.flixport.store.gcs;

import static com.google.common.truth.Truth.assertThat;
import java.io.IOException;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.junit.Test;

public class GoogleStorageFactoryTest {
  private static void verifyStorage(String expectedBucket, String expectedPrefix,
      DestinationStorage actualStorage) {
    GoogleStorageClient storage = (GoogleStorageClient) actualStorage;
    assertThat(storage.getBucketName()).isEqualTo(expectedBucket);
    assertThat(storage.getPrefix()).isEqualTo(expectedPrefix);
  }

  @Test
  public void testWithoutPath() throws IOException {
    verifyStorage("somewhere", "", new GoogleStorageFactory().createStorage("gs:somewhere", null));
    verifyStorage("somewhere", "", new GoogleStorageFactory().createStorage("gs:somewhere/", null));
    verifyStorage("some-where", "",
        new GoogleStorageFactory().createStorage("gs:some-where", null));
  }

  @Test
  public void testWithPath() throws IOException {
    verifyStorage("somewhere", "a/b",
        new GoogleStorageFactory().createStorage("gs:somewhere/a/b", null));
    verifyStorage("somewhere", "a",
        new GoogleStorageFactory().createStorage("gs:somewhere/a", null));
    verifyStorage("somewhere", "a-b",
        new GoogleStorageFactory().createStorage("gs:somewhere/a-b", null));
  }
}
