package io.sentry.core.cache;

import static io.sentry.core.SentryLevel.ERROR;

import io.sentry.core.ISerializer;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

abstract class CacheStrategy {

  @SuppressWarnings("CharsetObjectCanBeUsed")
  protected static final Charset UTF_8 = Charset.forName("UTF-8");

  protected final @NotNull SentryOptions options;
  protected final @NotNull ISerializer serializer;
  protected @NotNull File directory;
  protected int maxSize;

  CacheStrategy(
      final @NotNull SentryOptions options,
      final @NotNull String directoryPath,
      final int maxSize) {
    this.options = Objects.requireNonNull(options, "SentryOptions is required.");
    this.serializer = Objects.requireNonNull(options.getSerializer(), "Serializer is required.");

    Objects.requireNonNull(directoryPath, "Directory is required.");
    this.directory = new File(directoryPath);

    this.maxSize = maxSize;
  }

  /**
   * Check if a Dir. is valid, has write and read permission
   *
   * @return true if valid and has permission or false otherwise
   */
  protected boolean isDirectoryValid() {
    if (!directory.isDirectory() || !directory.canWrite() || !directory.canRead()) {
      options
          .getLogger()
          .log(
              ERROR,
              "The directory for caching files is inaccessible.: %s",
              directory.getAbsolutePath());
      return false;
    }
    return true;
  }

  /**
   * Sort files from oldest to the newest using the lastModified method
   *
   * @param files the Files
   */
  void sortFilesOldestToNewest(@NotNull File[] files) {
    // just sort it if more than 1 file
    if (files.length > 1) {
      // sort by the oldest to the newest
      Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
    }
  }

  /**
   * Rotates the caching folder if full, deleting the oldest files first
   *
   * @param files the Files
   */
  protected void rotateCacheIfNeeded(final @NotNull File[] files) {
    final int length = files.length;
    if (length >= maxSize) {
      options
          .getLogger()
          .log(SentryLevel.WARNING, "Cache folder if full (respecting maxSize). Rotating files");
      final int totalToBeDeleted = (length - maxSize) + 1;

      sortFilesOldestToNewest(files);

      // delete files from the top of the Array as its sorted by the oldest to the newest
      for (int i = 0; i < totalToBeDeleted; i++) {
        final File file = files[i];
        // sanity check if the file actually exists.
        if (file.exists()) {
          if (!file.delete()) {
            options
                .getLogger()
                .log(SentryLevel.WARNING, "File can't be deleted: %s", file.getAbsolutePath());
          }
        }
      }
    }
  }
}
