package org.cyclopsgroup.flixport.action;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class ExportByPhotosetTest {
  private static final Auth authWithUser = new Auth();
  private static final String USER_ID = "user_xyz";

  static {
    User user = new User();
    user.setId(USER_ID);
    authWithUser.setUser(user);
  }

  @Mock
  private Flickr flickr;

  @Mock
  private PhotosetsInterface photosetsInterface;
  @Mock
  private PhotosInterface photosInterface;

  @Mock
  private DestinationStorage storage;

  private ExportByPhotoset newAction(ExportOptions options) {
    return new ExportByPhotoset(flickr, storage, options);
  }

  @Before
  public void setUp() {
    when(flickr.getAuth()).thenReturn(authWithUser);
    when(flickr.getPhotosetsInterface()).thenReturn(photosetsInterface);
    when(flickr.getPhotosInterface()).thenReturn(photosInterface);
  }

  @Test
  public void testRunAndSaveFile() throws FlickrException, IOException {
    Photoset set = new Photoset();
    set.setId("some_set_id");
    Photosets sets = new Photosets();
    sets.setPhotosets(ImmutableList.of(set));
    when(photosetsInterface.getList(eq(USER_ID), anyInt(), anyInt(), isNull())).thenReturn(sets);

    Photo p = new Photo();
    p.setId("some_photo");
    p.setOriginalFormat("jpeg");
    PhotoList<Photo> list = new PhotoList<>();
    list.add(p);
    when(photosetsInterface.getPhotos(eq("some_set_id"), anyInt(), anyInt())).thenReturn(list);
    when(storage.listObjects("some_set_id")).thenReturn(ImmutableSet.of());

    InputStream in = new ByteArrayInputStream("".getBytes());
    when(photosInterface.getImageAsStream(p, Size.ORIGINAL)).thenReturn(in);

    newAction(SimpleExportOptions.forDestination("$s.id", "${f.id}.${f.originalFormat}")).run();
    verify(storage).createObject("some_set_id/some_photo.jpeg", "image/jpeg", in, false);
  }

  @Test
  public void testRunWithFileAlreadyExists() throws FlickrException, IOException {
    Photoset set = new Photoset();
    set.setId("some_set_id");
    Photosets sets = new Photosets();
    sets.setPhotosets(ImmutableList.of(set));
    when(photosetsInterface.getList(eq(USER_ID), anyInt(), anyInt(), isNull())).thenReturn(sets);

    Photo p = new Photo();
    p.setId("some_photo");
    p.setOriginalFormat("jpg");
    PhotoList<Photo> list = new PhotoList<>();
    list.add(p);
    when(photosetsInterface.getPhotos(eq("some_set_id"), anyInt(), anyInt())).thenReturn(list);
    when(storage.listObjects("some_set_id")).thenReturn(ImmutableSet.of("some_photo.jpg"));

    newAction(SimpleExportOptions.forDestination("$s.id", "${f.id}.${f.originalFormat}")).run();
  }

  @After
  public void verifyInvocations() {
    verifyNoMoreInteractions(ignoreStubs(storage, flickr, photosetsInterface, photosInterface));
  }
}
