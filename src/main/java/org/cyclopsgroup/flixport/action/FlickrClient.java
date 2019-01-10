package org.cyclopsgroup.flixport.action;

import static org.cyclopsgroup.flixport.action.CommandLineUtils.printCall;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;

public class FlickrClient {
  interface CollectionFn<T> {
    T apply(Collection colleciton, T parent) throws FlickrException, IOException;
  }

  private final Flickr flickr;

  public FlickrClient(String appId, String appSecret) {
    flickr = new Flickr(appId, appSecret, new REST());
  }

  private Auth authenticate() throws IOException {
    Token requestToken =
        printCall("Request token", () -> flickr.getAuthInterface().getRequestToken());
    printCall("Auth URL",
        () -> flickr.getAuthInterface().getAuthorizationUrl(requestToken, Permission.READ));
    System.out.println("Verifier: ");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      String in = printCall("You typed", () -> reader.readLine());
      Token accessToken = printCall("Access token",
          () -> flickr.getAuthInterface().getAccessToken(requestToken, new Verifier(in)));

      return printCall("Auth", () -> flickr.getAuthInterface().checkToken(accessToken));
    }
  }

  public void authenticate(File authDir, boolean forceToAuthenticate)
      throws FlickrException, IOException {
    AuthStore authStore = new FileAuthStore(
        new File(System.getProperty("user.home") + File.separatorChar + ".flickr"));
    List<Auth> auths = printCall("Existing auths", () -> Arrays.asList(authStore.retrieveAll()));
    Auth auth;
    if (auths.isEmpty() || forceToAuthenticate) {
      auth = authenticate();
      authStore.store(auth);
    } else {
      auth = auths.get(0);
    }
    RequestContext.getRequestContext().setAuth(auth);
    flickr.setAuth(auth);
  }

  PhotoList<Photo> getPhotos(String setId) throws FlickrException {
    return flickr.getPhotosetsInterface().getPhotos(setId, 500, 0);
  }

  InputStream openPhoto(Photo photo) throws FlickrException {
    return flickr.getPhotosInterface().getImageAsStream(photo, Size.ORIGINAL);
  }

  private <T> T traverseCollection(@Nullable T parent, Collection collection,
      CollectionFn<T> collectionFn) throws FlickrException, IOException {
    List<Collection> children = new ArrayList<>(collection.getCollections());
    children.sort(Comparator.comparing(Collection::getTitle));
    T node = collectionFn.apply(collection, parent);
    if (node == null) {
      return null;
    }
    for (Collection child : children) {
      traverseCollection(node, child, collectionFn);
    }
    return node;
  }

  <T> void traverseCollections(CollectionFn<T> collectionFn, @Nullable T root)
      throws FlickrException, IOException {
    String userId = flickr.getAuth().getUser().getId();
    List<Collection> allCollections =
        new ArrayList<>(flickr.getCollectionsInterface().getTree(null, userId));
    allCollections.sort(Comparator.comparing(Collection::getTitle));
    for (Collection child : allCollections) {
      traverseCollection(root, child, collectionFn);
    }
  }
}
