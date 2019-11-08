package io.sentry.android.core;

import android.os.FileObserver;
import io.sentry.core.*;
import io.sentry.core.util.Objects;
import java.io.File;
import org.jetbrains.annotations.Nullable;

final class EnvelopeFileObserver extends FileObserver {

  private String rootPath;
  private final IEnvelopeSender envelopeSender;
  private ILogger logger;

  // The preferred overload (Taking File instead of String) is only available from API 29
  @SuppressWarnings("deprecation")
  EnvelopeFileObserver(String path, IEnvelopeSender envelopeSender, ILogger logger) {
    super(path);
    this.rootPath = Objects.requireNonNull(path, "File path is required.");
    this.envelopeSender = Objects.requireNonNull(envelopeSender, "Envelope sender is required.");
    this.logger = Objects.requireNonNull(logger, "Logger is required.");
  }

  @Override
  public void onEvent(int eventType, @Nullable String relativePath) {
    if (relativePath == null
      // Protocol with sentry-native is a rename
      // TODO: Check file extension matches once sentry-native starts renaming the file
      // && eventType != FileObserver.
      // event types: https://developer.android.com/reference/android/os/FileObserver.html
      // TODO: Currently sentry-native simply writes so watch for close-write
      || eventType != FileObserver.CLOSE_WRITE
    ) {
      return;
    }

    logger.log(
        SentryLevel.DEBUG,
        "onEvent fired for EnvelopeFileObserver with event type %d on path: %s for file %s.",
        eventType,
        this.rootPath,
        relativePath);


    envelopeSender.processEnvelopeFile(this.rootPath + File.separator + relativePath);
  }
}
