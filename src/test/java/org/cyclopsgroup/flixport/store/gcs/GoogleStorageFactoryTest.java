package org.cyclopsgroup.flixport.store.gcs;

import static com.google.common.truth.Truth.assertThat;
import java.io.IOException;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.PrefixedDestinationStorage;
import org.junit.Test;

public class GoogleStorageFactoryTest {
  private static void verifyStorageWithoutPrefix(String expectedBucket,
      DestinationStorage actualStorage) {
    assertThat(actualStorage).isInstanceOf(GoogleStorageClient.class);
    GoogleStorageClient storage = (GoogleStorageClient) actualStorage;
    assertThat(storage.getBucketName()).isEqualTo(expectedBucket);
  }

  private static void verifyStorageWithPrefix(String expectedBucket, String expectedPrefix,
      DestinationStorage actualStorage) {
    assertThat(actualStorage).isInstanceOf(PrefixedDestinationStorage.class);
    PrefixedDestinationStorage storage = (PrefixedDestinationStorage) actualStorage;
    assertThat(storage.getPrefix()).isEqualTo(expectedPrefix);
    GoogleStorageClient delegate = (GoogleStorageClient) storage.getDelegate();
    assertThat(delegate.getBucketName()).isEqualTo(expectedBucket);
  }

  @Test
  public void testWithoutPrefix() throws IOException {
    verifyStorageWithoutPrefix("somewhere",
        new GoogleStorageFactory().createStorage("gs:somewhere", null));
    verifyStorageWithoutPrefix("somewhere",
        new GoogleStorageFactory().createStorage("gs:somewhere/", null));
    verifyStorageWithoutPrefix("some-where",
        new GoogleStorageFactory().createStorage("gs:some-where", null));
  }

  @Test
  public void testWithPath() throws IOException {
    verifyStorageWithPrefix("somewhere", "a/b",
        new GoogleStorageFactory().createStorage("gs:somewhere/a/b", null));
    verifyStorageWithPrefix("somewhere", "a",
        new GoogleStorageFactory().createStorage("gs:somewhere/a", null));
    verifyStorageWithPrefix("somewhere", "a-b",
        new GoogleStorageFactory().createStorage("gs:somewhere/a-b", null));
  }
}
