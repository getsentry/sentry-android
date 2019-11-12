package io.sentry.core.transport;

import static io.sentry.core.ILogger.logIfNotNull;

import io.sentry.core.SentryEvent;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.cache.IEventCache;
import io.sentry.core.hints.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.*;

/** A connection to Sentry that sends the events asynchronously. */
public final class AsyncConnection implements Closeable, Connection {
  private final ITransport transport;
  private final ITransportGate transportGate;
  private final ExecutorService executor;
  private final IEventCache eventCache;
  private final SentryOptions options;

  public AsyncConnection(
      ITransport transport,
      ITransportGate transportGate,
      IBackOffIntervalStrategy backOffIntervalStrategy,
      IEventCache eventCache,
      int maxRetries,
      int maxQueueSize,
      SentryOptions options) {
    this(
        transport,
        transportGate,
        eventCache,
        initExecutor(maxRetries, maxQueueSize, backOffIntervalStrategy, eventCache),
        options);
  }

  @TestOnly
  AsyncConnection(
      ITransport transport,
      ITransportGate transportGate,
      IEventCache eventCache,
      ExecutorService executorService,
      SentryOptions options) {
    this.transport = transport;
    this.transportGate = transportGate;
    this.eventCache = eventCache;
    this.options = options;
    this.executor = executorService;
  }

  private static RetryingThreadPoolExecutor initExecutor(
      int maxRetries,
      int maxQueueSize,
      IBackOffIntervalStrategy backOffIntervalStrategy,
      IEventCache eventCache) {

    RejectedExecutionHandler storeEvents =
        (r, executor) -> {
          if (r instanceof EventSender) {
            eventCache.store(((EventSender) r).event);
          }
        };

    return new RetryingThreadPoolExecutor(
        1,
        maxRetries,
        maxQueueSize,
        new AsyncConnectionThreadFactory(),
        backOffIntervalStrategy,
        storeEvents);
  }

  /**
   * Tries to send the event to the Sentry server.
   *
   * @param event the event to send
   * @throws IOException on error
   */
  @SuppressWarnings("FutureReturnValueIgnored") // TODO:
  // https://errorprone.info/bugpattern/FutureReturnValueIgnored
  @Override
  public void send(SentryEvent event, @Nullable Object hint) throws IOException {
    IEventCache currentEventCache = eventCache;
    if (hint instanceof Cached) {
      currentEventCache = NoOpEventCache.getInstance();
    }
    executor.submit(new EventSender(event, hint, currentEventCache));
  }

  @Override
  public void close() throws IOException {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
        logIfNotNull(
            options.getLogger(),
            SentryLevel.WARNING,
            "Failed to shutdown the async connection async sender within 1 minute. Trying to force it now.");
        executor.shutdownNow();
      }
      transport.close();
    } catch (InterruptedException e) {
      // ok, just give up then...
      logIfNotNull(
          options.getLogger(),
          SentryLevel.DEBUG,
          "Thread interrupted while closing the connection.");
      Thread.currentThread().interrupt();
    }
  }

  private static final class AsyncConnectionThreadFactory implements ThreadFactory {
    private int cnt;

    @Override
    public Thread newThread(@NotNull Runnable r) {
      Thread ret = new Thread(r, "SentryAsyncConnection-" + cnt++);
      ret.setDaemon(true);
      return ret;
    }
  }

  private final class EventSender implements Retryable {
    final SentryEvent event;
    private Object hint;
    private IEventCache eventCache;
    long suggestedRetryDelay;

    EventSender(SentryEvent event, Object hint, IEventCache eventCache) {
      this.event = event;
      this.hint = hint;
      this.eventCache = eventCache;
    }

    @Override
    public void run() {
      try {
        flush();
      } finally {
        if (hint instanceof Flushable) {
          ((Flushable) hint).flushed();
        }
      }
    }

    private void flush() {
      if (transportGate.isSendingAllowed()) {
        try {
          eventCache.store(event);

          TransportResult result = transport.send(event);
          if (result.isSuccess()) {
            eventCache.discard(event);
          } else {
            suggestedRetryDelay = result.getRetryMillis();

            String message =
                "The transport failed to send the event with response code "
                    + result.getResponseCode()
                    + ". Retrying in "
                    + suggestedRetryDelay
                    + "ms.";

            if (options.isDebug()) {
              options.getLogger().log(SentryLevel.ERROR, message);
            }

            throw new IllegalStateException(message);
          }
        } catch (IOException e) {
          eventCache.store(event);
          // Failure due to IO is allowed to retry the event
          if (hint instanceof io.sentry.core.hints.Retryable) {
            ((io.sentry.core.hints.Retryable) hint).setRetry(true);
          }
          throw new IllegalStateException("Sending the event failed.", e);
        }
      } else {
        eventCache.store(event);
        // If transportGate is blocking from sending, allowed to retry
        if (hint instanceof io.sentry.core.hints.Retryable) {
          ((io.sentry.core.hints.Retryable) hint).setRetry(true);
        }
      }
    }

    @Override
    public long getSuggestedRetryDelayMillis() {
      return suggestedRetryDelay;
    }
  }
}
