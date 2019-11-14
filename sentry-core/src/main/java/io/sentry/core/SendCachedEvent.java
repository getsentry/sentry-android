package io.sentry.core;

import static io.sentry.core.ILogger.logIfNotNull;

import io.sentry.core.cache.DiskCache;
import io.sentry.core.hints.Cached;
import io.sentry.core.hints.Retryable;
import io.sentry.core.util.Objects;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;

final class SendCachedEvent extends DirectoryProcessor {
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  private final ISerializer serializer;
  private final IHub hub;
  private final ILogger logger;

  SendCachedEvent(@NotNull ISerializer serializer, @NotNull IHub hub, @NotNull ILogger logger) {
    super(logger);
    this.serializer = Objects.requireNonNull(serializer, "Serializer is required.");
    this.hub = Objects.requireNonNull(hub, "Hub is required.");
    this.logger = Objects.requireNonNull(logger, "Logger is required.");
  }

  @Override
  protected void processFile(@NotNull File file) {
    if (!isRelevantFileName(file.getName())) {
      logIfNotNull(
          logger, SentryLevel.DEBUG, "File '%s' doesn't match extension expected.", file.getName());
      return;
    }

    if (!file.isFile()) {
      logIfNotNull(logger, SentryLevel.DEBUG, "'%s' is not a file.", file.getAbsolutePath());
      return;
    }

    if (!file.getParentFile().canWrite()) {
      logIfNotNull(
          logger,
          SentryLevel.WARNING,
          "File '%s' cannot be delete so it will not be processed.",
          file.getName());
      return;
    }

    SendCachedEventHint hint = new SendCachedEventHint();
    try (Reader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
      SentryEvent event = serializer.deserializeEvent(reader);
      hub.captureEvent(event, hint);
    } catch (FileNotFoundException e) {
      logIfNotNull(logger, SentryLevel.ERROR, "File '%s' cannot be found.", file.getName(), e);
    } catch (IOException e) {
      logIfNotNull(logger, SentryLevel.ERROR, "I/O on file '%s' failed.", file.getName(), e);
    } catch (Exception e) {
      logIfNotNull(logger, SentryLevel.ERROR, "Failed to capture cached event.", file.getName(), e);
      hint.setRetry(false);
    } finally {
      // Unless the transport marked this to be retried, it'll be deleted.
      if (!hint.getRetry()) {
        safeDelete(file, "after trying to capture it");
      }
    }
  }

  @Override
  protected boolean isRelevantFileName(String fileName) {
    return fileName.endsWith(DiskCache.FILE_SUFFIX);
  }

  private void safeDelete(File file, String errorMessageSuffix) {
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

  private static final class SendCachedEventHint implements Cached, Retryable {
    boolean retry = false;

    @Override
    public boolean getRetry() {
      return retry;
    }

    @Override
    public void setRetry(boolean retry) {
      this.retry = retry;
    }
  }
}
