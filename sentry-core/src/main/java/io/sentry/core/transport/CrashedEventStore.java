package io.sentry.core.transport;

import io.sentry.core.*;
import io.sentry.core.cache.IEventCache;
import io.sentry.core.protocol.SentryThread;
import io.sentry.core.util.Objects;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

// TODO: not to be public and better naming
public final class CrashedEventStore implements Connection {

  private Connection inner;
  private IEventCache eventCache;

  public CrashedEventStore(Connection inner, IEventCache eventCache) {
    this.inner = Objects.requireNonNull(inner, "The inner connection is required.");
    this.eventCache = Objects.requireNonNull(eventCache, "The EventCache is required.");
  }

  @Override
  public void send(@NotNull SentryEvent event, Object hint) throws IOException {
    if (!(hint instanceof CachedEvent)) {
      List<SentryThread> threads = event.getThreads();
      if (threads != null) {
        for (SentryThread thread : threads) {
          if (Boolean.TRUE.equals(thread.getCrashed())) {
            eventCache.store(event);
            return;
          }
        }
      }
    }

    inner.send(event, hint);
  }

  @Override
  public void close() throws IOException {
    inner.close();
  }
}
