package org.cyclopsgroup.flixport.action;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.cyclopsgroup.flixport.store.DestinationStorage;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.google.common.flogger.FluentLogger;

public class ExportFlickrByPhotoset extends AbstractExportAction {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final int TASK_QUEUE_SIZE = 20;

  public ExportFlickrByPhotoset(Flickr flickr) {
    super(flickr);
  }

  public void export(DestinationStorage storage, ExportOptions options)
      throws FlickrException, InterruptedException {
    BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(TASK_QUEUE_SIZE);
    ExecutorService executor = new ThreadPoolExecutor(options.getThreads(), options.getThreads(), 0,
        TimeUnit.SECONDS, taskQueue, new ThreadPoolExecutor.CallerRunsPolicy());

    String userId = flickr.getAuth().getUser().getId();
    for (int i = 0;; i++) {
      Photosets sets = flickr.getPhotosetsInterface().getList(userId, PAGE_SIZE, i, null);
      if (sets.getPhotosets().isEmpty()) {
        break;
      }
      for (Photoset set : sets.getPhotosets()) {
        executor.submit(() -> {
          exportPhotoset(storage, set, options, executor);
        });
      }
    }
    executor.shutdown();
    executor.awaitTermination(300, TimeUnit.SECONDS);
  }
}
