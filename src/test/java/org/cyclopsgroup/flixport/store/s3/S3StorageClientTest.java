package org.cyclopsgroup.flixport.store.s3;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class S3StorageClientTest {
  private static final String BUCKET_NAME = "some_bucket";
  private static S3ObjectSummary newSummary(String key) {
    S3ObjectSummary s = new S3ObjectSummary();
    s.setKey(key);
    return s;
  }
  @Mock
  private ObjectListing listing;

  @Captor
  private ArgumentCaptor<PutObjectRequest> requestCaptor;

  @Mock
  private AmazonS3 s3;

  private S3StorageClient storage;

  @Before
  public void setUp() {
    storage = new S3StorageClient(s3, BUCKET_NAME);
  }

  @Test
  public void testCreateObject() throws IOException {
    storage.createObject("a/b/c.txt", "plain/txt", new ByteArrayInputStream("xyz".getBytes()),
        false);
    verify(s3).putObject(requestCaptor.capture());
    PutObjectRequest r = requestCaptor.getValue();
    assertThat(r.getKey()).isEqualTo("a/b/c.txt");
    assertThat(r.getMetadata().getContentType()).isEqualTo("plain/txt");
  }

  @Test
  public void testListObject() {
    when(s3.listObjects(BUCKET_NAME, "some/path")).thenReturn(listing);
    when(listing.getObjectSummaries())
        .thenReturn(ImmutableList.of(newSummary("some/path/a.txt"), newSummary("some/path/b.txt")));
    assertThat(storage.listObjects("some/path")).containsExactly("a.txt", "b.txt");
  }

  @After
  public void verifyInvocations() {
    verifyNoMoreInteractions(ignoreStubs(s3));
  }
}
