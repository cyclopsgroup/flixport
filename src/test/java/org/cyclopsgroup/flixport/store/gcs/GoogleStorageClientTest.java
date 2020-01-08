package org.cyclopsgroup.flixport.store.gcs;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GoogleStorageClientTest {
  private GoogleStorageClient client;
  @Mock private Blob returnedBlob;
  @Mock private Page<Blob> returnedPage;
  @Mock private Storage storage;

  @Before
  public void setUp() throws IOException {
    client = new GoogleStorageClient("bucket", storage);
  }

  @Test
  public void testListObjects() {
    when(storage.list("bucket", BlobListOption.prefix("a/b"))).thenReturn(returnedPage);
    when(returnedPage.iterateAll()).thenReturn(ImmutableList.of(returnedBlob, returnedBlob));
    when(returnedBlob.getName()).thenReturn("f1.txt", "f2.txt");
    assertThat(client.listObjects("a/b")).containsExactly("f1.txt", "f2.txt");
  }

  @After
  public void verifyInvocations() {
    verifyNoMoreInteractions(ignoreStubs(storage, returnedPage, returnedBlob));
  }
}
