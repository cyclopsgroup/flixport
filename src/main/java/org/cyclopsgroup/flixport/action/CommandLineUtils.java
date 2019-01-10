package org.cyclopsgroup.flixport.action;

import java.io.IOException;
import com.flickr4java.flickr.FlickrException;
import com.google.common.flogger.FluentLogger;

class CommandLineUtils {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  interface Call<T> {
    T execute() throws FlickrException, IOException;
  }

  static <T> T printCall(String message, Call<T> call) {
    T result;
    try {
      result = call.execute();
    } catch (FlickrException | IOException e) {
      throw new RuntimeException("Can't proceed the call that produces " + message, e);
    }
    logger.atInfo().log("%s: %s.", message, result);
    return result;
  }
}
