package io.sentry.android.core;

import io.sentry.core.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

abstract class EnvelopeFileObserverIntegration implements Integration {
  private @Nullable EnvelopeFileObserver observer;

  protected EnvelopeFileObserverIntegration() {}

  @Override
  public void register(IHub hub, SentryOptions options) {
    ILogger logger = options.getLogger();
    String path = getPath(options);
    if (path == null) {
      logger.log(SentryLevel.WARNING, "Null given as a path to %s. Nothing will be registered.");
    } else {
      logger.log(SentryLevel.DEBUG, "Registering CachedEventReaderIntegration for path: %s", path);

      EnvelopeSender envelopeSender = new EnvelopeSender(hub, new io.sentry.core.EnvelopeReader(), options.getSerializer(), logger);

      observer = new EnvelopeFileObserver(path, envelopeSender, logger);
      observer.startWatching();
    }
  }

  public static EnvelopeFileObserverIntegration getOutboxFileObserver() {
    return new OutboxEnvelopeFileObserverIntegration();
  }

  public static EnvelopeFileObserverIntegration getCachedEnvelopeFileObserver() {
    return new JavaCachedEnvelopeFileObserverIntegration();
  }

  @TestOnly abstract String getPath(SentryOptions options);

  private static class OutboxEnvelopeFileObserverIntegration extends EnvelopeFileObserverIntegration {
    @Override
    protected String getPath(SentryOptions options) {
      return options.getOutboxPath();
    }
  }

  private static class JavaCachedEnvelopeFileObserverIntegration extends EnvelopeFileObserverIntegration {
    @Override
    protected String getPath(SentryOptions options) {
      // TODO: Wherever we're caching events from the Java layer
      return options.getCacheDirPath() + "/cached";
    }
  }
}
