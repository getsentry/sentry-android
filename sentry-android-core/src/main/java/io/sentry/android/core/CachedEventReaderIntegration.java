package io.sentry.android.core;

import io.sentry.core.EnvelopeReader;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryOptions;
import org.jetbrains.annotations.Nullable;

final class CachedEventReaderIntegration implements Integration {
  private @Nullable EnvelopeFileObserver observer;
  @Override
  public void register(IHub hub, SentryOptions options) {
    String path = options.getCacheDirPath();
    if (path != null) {
      observer = new EnvelopeFileObserver(path, hub, new EnvelopeReader(), options.getSerializer(), options.getLogger());
      observer.startWatching();
    }
  }
}
