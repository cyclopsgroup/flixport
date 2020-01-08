package org.cyclopsgroup.flixport.store.gcs;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.cyclopsgroup.flixport.store.PrefixedDestinationStorage;
import org.cyclopsgroup.flixport.store.SimpleDestinationStorageOptions;
import org.junit.Test;

public class GoogleStorageFactoryTest {
  private static void verifyStorageWithoutPrefix(
      String expectedBucket, DestinationStorage actualStorage) {
    assertThat(actualStorage).isInstanceOf(GoogleStorageClient.class);
    GoogleStorageClient storage = (GoogleStorageClient) actualStorage;
    assertThat(storage.getBucketName()).isEqualTo(expectedBucket);
  }

  private static void verifyStorageWithPrefix(
      String expectedBucket, String expectedPrefix, DestinationStorage actualStorage) {
    assertThat(actualStorage).isInstanceOf(PrefixedDestinationStorage.class);
    PrefixedDestinationStorage storage = (PrefixedDestinationStorage) actualStorage;
    assertThat(storage.getPrefix()).isEqualTo(expectedPrefix);
    GoogleStorageClient delegate = (GoogleStorageClient) storage.getDelegate();
    assertThat(delegate.getBucketName()).isEqualTo(expectedBucket);
  }

  @Test
  public void testWithoutPrefix() throws IOException {
    verifyStorageWithoutPrefix(
        "somewhere",
        new GoogleStorageFactory()
            .createStorage(SimpleDestinationStorageOptions.forSpec("gs:somewhere")));
    verifyStorageWithoutPrefix(
        "somewhere",
        new GoogleStorageFactory()
            .createStorage(SimpleDestinationStorageOptions.forSpec("gs:somewhere/")));
    verifyStorageWithoutPrefix(
        "some-where",
        new GoogleStorageFactory()
            .createStorage(SimpleDestinationStorageOptions.forSpec("gs:some-where")));
  }

  @Test
  public void testWithPath() throws IOException {
    verifyStorageWithPrefix(
        "somewhere",
        "a/b",
        new GoogleStorageFactory()
            .createStorage(SimpleDestinationStorageOptions.forSpec("gs:somewhere/a/b")));
    verifyStorageWithPrefix(
        "somewhere",
        "a",
        new GoogleStorageFactory()
            .createStorage(SimpleDestinationStorageOptions.forSpec("gs:somewhere/a")));
    verifyStorageWithPrefix(
        "somewhere",
        "a-b",
        new GoogleStorageFactory()
            .createStorage(SimpleDestinationStorageOptions.forSpec("gs:somewhere/a-b")));
  }
}
