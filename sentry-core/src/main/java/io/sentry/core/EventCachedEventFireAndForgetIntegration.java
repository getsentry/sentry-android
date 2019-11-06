package io.sentry.core;

import static io.sentry.core.ILogger.logIfNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

public final class EventCachedEventFireAndForgetIntegration implements Integration {
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  @Override
  public void register(IHub hub, SentryOptions options) {
    String cachedDir = options.getCacheDirPath();
    if (cachedDir == null) {
      logIfNotNull(
          options.getLogger(), SentryLevel.WARNING, "No cache dir path is defined in options.");
      return;
    }

    File outbox = new File(options.getCacheDirPath());
    try {
      Executors.callable(
              () -> {
                SendCachedFiles(hub, options.getLogger(), options.getSerializer(), outbox);
              })
          .call();
    } catch (Exception e) {
      logIfNotNull(
          options.getLogger(),
          SentryLevel.ERROR,
          "Failed trying to send cached events at %s",
          outbox);
    }
  }

  private static void SendCachedFiles(
      IHub hub, ILogger logger, ISerializer serializer, File directory) {
    if (!directory.exists()) {
      logIfNotNull(
          logger,
          SentryLevel.INFO,
          "Directory '%s' doesn't exist. Nothing no cached events to send.",
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

    logIfNotNull(
        logger,
        SentryLevel.DEBUG,
        "Processing %d items from cache dir %s",
        directory.length(),
        directory.getAbsolutePath());

    for (File file : directory.listFiles()) {
      // TODO: postfix should ve a const, shared with the caching code (still to be merged)
      if (!file.getName().endsWith(".sentry-event")) {
        logIfNotNull(
            logger,
            SentryLevel.DEBUG,
            "File '%s' doesn't match extension expected.",
            file.getName());
        continue;
      }

      if (!directory.isFile()) {
        logIfNotNull(logger, SentryLevel.DEBUG, "'%s' is not a file.", directory.getAbsolutePath());
        return;
      }

      if (!file.canRead()) {
        logIfNotNull(logger, SentryLevel.WARNING, "File '%s' cannot be read.", file.getName());
        safeDelete(file, "which can't be read but has expected file extension.", logger);
        continue;
      }

      CachedEvent hint = new CachedEvent();
      try (Reader reader =
          new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
        SentryEvent event = serializer.deserializeEvent(reader);
        // TODO: Run a sync capture? The SentryId returned could be the indication to whether delete
        // or not.
        // Hub doesn't throw so we wouldn't know if it was successful or not.
        // Hint will be used by the caching layer to avoid persisting this event
        hub.captureEvent(event /*, hint); // when hint PR is merged */);
      } catch (FileNotFoundException e) {
        logIfNotNull(logger, SentryLevel.ERROR, "File '%s' cannot be found.", file.getName(), e);
      } catch (IOException e) {
        logIfNotNull(logger, SentryLevel.ERROR, "I/O on file '%s' failed.", file.getName(), e);
      } finally {
        // Unless the transport marked this to be retried, it'll be deleted.
        if (!hint.isResend()) {
          safeDelete(file, "after trying to capture it", logger);
        }
      }
    }
  }

  private static void safeDelete(File file, String errorMessageSuffix, ILogger logger) {
    try {
      file.delete();
    } catch (Exception e) {
      logIfNotNull(
          logger,
          SentryLevel.ERROR,
          "Failed to delete '%s' " + errorMessageSuffix,
          file.getName(),
          e);
    }
  }
}
