package org.cyclopsgroup.flixport.action;

import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
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
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.collections.CollectionsInterface;
import com.flickr4java.flickr.people.User;
import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class ExportByCollectionAndSetTest {
  private static final Auth authWithUser = new Auth();
  private static final String USER_ID = "user_xyz";

  static {
    User user = new User();
    user.setId(USER_ID);
    authWithUser.setUser(user);
  }
  private ExportByCollectionAndSet action;

  @Mock
  private Flickr flickr;

  @Mock
  private CollectionsInterface collectionsInterface;

  @Mock
  private DestinationStorage storage;

  @Before
  public void setUp() {
    when(flickr.getAuth()).thenReturn(authWithUser);
    when(flickr.getCollectionsInterface()).thenReturn(collectionsInterface);
    action = new ExportByCollectionAndSet(flickr, storage,
        SimpleExportOptions.forDestination("$c.id/$s.id", "$f.id"));
  }

  @Test
  public void test() throws FlickrException, IOException {
    when(collectionsInterface.getTree(null, USER_ID))
        .thenReturn(ImmutableList.of(newCollection("a")));
    when(collectionsInterface.getTree("a", USER_ID))
        .thenReturn(ImmutableList.of(newCollection("b")));
    when(collectionsInterface.getTree("b", USER_ID)).thenReturn(ImmutableList.of());
    when(collectionsInterface.getInfo("b")).thenReturn(newCollection("b"));
    action.run();
  }

  @After
  public void verifyInvocations() {
    verifyNoMoreInteractions(ignoreStubs(storage, flickr, collectionsInterface));
  }

  private static Collection newCollection(String id) {
    Collection c = new Collection();
    c.setId(id);
    return c;
  }
}
