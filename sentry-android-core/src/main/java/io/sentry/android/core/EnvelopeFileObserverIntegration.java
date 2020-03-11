package io.sentry.android.core;

import io.sentry.core.EnvelopeReader;
import io.sentry.core.EnvelopeSender;
import io.sentry.core.IHub;
import io.sentry.core.ILogger;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import java.io.Closeable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

/** Watches the envelope dir. and send them (events) over. */
public abstract class EnvelopeFileObserverIntegration implements Integration, Closeable {
  private @Nullable EnvelopeFileObserver observer;

  EnvelopeFileObserverIntegration() {}

  public static EnvelopeFileObserverIntegration getOutboxFileObserver() {
    return new OutboxEnvelopeFileObserverIntegration();
  }

  @Override
  public final void register(IHub hub, SentryOptions options) {
    ILogger logger = options.getLogger();
    String path = getPath(options);
    if (path == null) {
      logger.log(
          SentryLevel.WARNING,
          "Null given as a path to EnvelopeFileObserverIntegration. Nothing will be registered.");
    } else {
      logger.log(
          SentryLevel.DEBUG, "Registering EnvelopeFileObserverIntegration for path: %s", path);

      EnvelopeSender envelopeSender =
          new EnvelopeSender(hub, new EnvelopeReader(), options.getSerializer(), logger);

      observer = new EnvelopeFileObserver(path, envelopeSender, logger);
      observer.startWatching();

      options.getLogger().log(SentryLevel.DEBUG, "EnvelopeFileObserverIntegration installed.");
    }
  }

  @Override
  public void close() {
    if (observer != null) {
      observer.stopWatching();
    }
  }

  @TestOnly
  abstract String getPath(SentryOptions options);

  private static final class OutboxEnvelopeFileObserverIntegration
      extends EnvelopeFileObserverIntegration {
    @Override
    protected String getPath(final SentryOptions options) {
      return options.getOutboxPath();
    }
  }
}
