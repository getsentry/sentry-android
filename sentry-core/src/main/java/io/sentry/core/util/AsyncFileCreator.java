package io.sentry.core.util;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
Class to create a file async. on disk if it doesn't exist.
 */
public final class AsyncFileCreator {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  /**
   * Method used to create a new file
   * @param path on disk to be created
   * @return a Future with the file
   */
  public Future<File> createFile(final String path) {
    return executor.submit(() -> {
      File file = new File(path);
      file.mkdirs();
      return file;
    });
  }
}
