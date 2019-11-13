package io.sentry.core;

import static io.sentry.core.ILogger.logIfNotNull;

import java.io.File;
import org.jetbrains.annotations.Nullable;

public abstract class DirectoryProcessor {

  private ILogger logger;

  protected DirectoryProcessor(@Nullable ILogger logger) {

    this.logger = logger;
  }

  void processDirectory(File directory) {
    if (!directory.exists()) {
      logIfNotNull(
          logger,
          SentryLevel.WARNING,
          "Directory '%s' doesn't exist. No cached events to send.",
          directory.getAbsolutePath());
      return;
    }
    if (!directory.isDirectory()) {
      logIfNotNull(
          logger,
          SentryLevel.ERROR,
          "Cache dir %s is not a directory.",
          directory.getAbsolutePath());
      return;
    }

    File[] listFiles = directory.listFiles();
    if (listFiles == null) {
      logIfNotNull(logger, SentryLevel.ERROR, "Cache dir %s is null.", directory.getAbsolutePath());
      return;
    }

    File[] filteredListFiles = directory.listFiles((d, name) -> isRelevantFileName(name));

    logIfNotNull(
        logger,
        SentryLevel.DEBUG,
        "Processing %d items from cache dir %s",
        filteredListFiles != null ? filteredListFiles.length : 0,
        directory.getAbsolutePath());

    for (File file : listFiles) {
      processFile(file);
    }
  }

  protected abstract void processFile(File file);

  protected abstract boolean isRelevantFileName(String fileName);
}
