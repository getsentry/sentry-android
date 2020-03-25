package io.sentry.core;

import io.sentry.core.hints.SendCachedEventHint;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class DirectoryProcessor {

  private final @NotNull ILogger logger;
  private final long flushTimeoutMillis;

  DirectoryProcessor(final @NotNull ILogger logger, final long flushTimeoutMillis) {
    this.logger = logger;
    this.flushTimeoutMillis = flushTimeoutMillis;
  }

  void processDirectory(@NotNull File directory) {
    try {
      if (!directory.exists()) {
        logger.log(
            SentryLevel.WARNING,
            "Directory '%s' doesn't exist. No cached events to send.",
            directory.getAbsolutePath());
        return;
      }
      if (!directory.isDirectory()) {
        logger.log(
            SentryLevel.ERROR, "Cache dir %s is not a directory.", directory.getAbsolutePath());
        return;
      }

      File[] listFiles = directory.listFiles();
      if (listFiles == null) {
        logger.log(SentryLevel.ERROR, "Cache dir %s is null.", directory.getAbsolutePath());
        return;
      }

      File[] filteredListFiles = directory.listFiles((d, name) -> isRelevantFileName(name));

      logger.log(
          SentryLevel.DEBUG,
          "Processing %d items from cache dir %s",
          filteredListFiles != null ? filteredListFiles.length : 0,
          directory.getAbsolutePath());

      for (File file : listFiles) {
        final SendCachedEventHint hint = new SendCachedEventHint(flushTimeoutMillis, logger);
        processFile(file, hint);
      }
    } catch (Exception e) {
      logger.log(SentryLevel.ERROR, e, "Failed processing '%s'", directory.getAbsolutePath());
    }
  }

  protected abstract void processFile(File file, @Nullable Object hint);

  protected abstract boolean isRelevantFileName(String fileName);
}
