package io.sentry.core.transport;

import static io.sentry.core.ILogger.logIfNotNull;

import io.sentry.core.*;
import io.sentry.core.cache.IEventCache;
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
  private final boolean storeBeforeSend;

  public AsyncConnection(
      ITransport transport,
      ITransportGate transportGate,
      IBackOffIntervalStrategy backOffIntervalStrategy,
      IEventCache eventCache,
      int maxRetries,
      int maxQueueSize,
      boolean storeBeforeSend,
      SentryOptions options) {
    this(
        transport,
        transportGate,
        eventCache,
        initExecutor(maxRetries, maxQueueSize, backOffIntervalStrategy, eventCache),
        storeBeforeSend,
        options);
  }

  @TestOnly
  AsyncConnection(
      ITransport transport,
      ITransportGate transportGate,
      IEventCache eventCache,
      ExecutorService executorService,
      boolean storeBeforeSend,
      SentryOptions options) {
    this.transport = transport;
    this.transportGate = transportGate;
    this.eventCache = eventCache;
    this.options = options;
    this.executor = executorService;
    this.storeBeforeSend = storeBeforeSend;
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
  public void send(SentryEvent event, Object hint) throws IOException {
    CachedEvent cachedEvent = null;
    if (hint instanceof CachedEvent) {
      cachedEvent = (CachedEvent) hint;
    }
    EventSender sender = new EventSender(event, cachedEvent);

    // TODO: A temp hack to get the event sending sync with the calling thread
    // Calling thread here is something we've spawn to read files on app start
    // or a FileObserver thread that detected a file written by sentry-native
    // We might need a sychronization mechanism within the `CachedEvent` instead
    // which the caller will wait on and the transport will set once event submission
    // is completed
    if (cachedEvent != null) {
      sender.run();
    } else {
      executor.submit(sender);
    }
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
    private CachedEvent cachedEvent;
    long suggestedRetryDelay;

    EventSender(SentryEvent event, @Nullable CachedEvent cachedEvent) {
      this.event = event;
      this.cachedEvent = cachedEvent;
    }

    @Override
    public void run() {
      if (transportGate.isSendingAllowed()) {
        try {
          if (cachedEvent == null && storeBeforeSend) {
            eventCache.store(event);
          }

          TransportResult result = transport.send(event);
          if (result.isSuccess()) {
            eventCache.discard(event);
          } else {
            if (cachedEvent == null && !storeBeforeSend) {
              eventCache.store(event);
            }
            // TODO: Here we could inspect result and decide whether to mark
            // eventCache.setResend(true)
            suggestedRetryDelay = result.getRetryMillis();

            String message =
                "The transport failed to send the event with response code "
                    + result.getResponseCode()
                    + ". Retrying in "
                    + suggestedRetryDelay
                    + "ms.";

            if (options.isDebug()) {
              options.getLogger().log(SentryLevel.DEBUG, message);
            }

            throw new IllegalStateException(message);
          }
        } catch (IOException e) {
          if (cachedEvent == null) {
            eventCache.store(event);
          } else {
            // This is a cached event and the error was I/O related, we can retry later.
            cachedEvent.setResend(true);
          }
          throw new IllegalStateException("Sending the event failed.", e);
        }
      } else {
        if (cachedEvent == null) {
          eventCache.store(event);
        } else {
          // Device is offline, keep event to retry later
          cachedEvent.setResend(true);
        }
      }
    }

    @Override
    public long getSuggestedRetryDelayMillis() {
      return suggestedRetryDelay;
    }
  }
}
