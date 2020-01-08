package org.cyclopsgroup.flixport.cli;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;
import com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

public class FlickrClient {
  interface CollectionFn<T> {
    T apply(Collection colleciton, T parent) throws FlickrException, IOException;
  }

  private final Flickr flickr;

  public FlickrClient(Flickr flickr) {
    this.flickr = Preconditions.checkNotNull(flickr);
  }

  private Auth authenticate() throws IOException, FlickrException {
    Token requestToken = flickr.getAuthInterface().getRequestToken();
    String authUrl = flickr.getAuthInterface().getAuthorizationUrl(requestToken, Permission.READ);
    System.out.println("Visit this URL to get auth code: " + authUrl);
    System.out.println("Type the auth code here: ");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      String in = reader.readLine();
      Token accessToken = flickr.getAuthInterface().getAccessToken(requestToken, new Verifier(in));
      return flickr.getAuthInterface().checkToken(accessToken);
    }
  }

  void authenticate(File authDir, boolean forceToAuthenticate) throws FlickrException, IOException {
    AuthStore authStore =
        new FileAuthStore(
            new File(System.getProperty("user.home") + File.separatorChar + ".flickr"));
    ImmutableList<Auth> auths = ImmutableList.copyOf(authStore.retrieveAll());
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
}
