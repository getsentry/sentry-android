package io.sentry.core.transport;

import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import io.sentry.util.VisibleForTesting;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;

/** A connection to Sentry that sends the events asynchronously. */
public class AsyncConnection {
  private final ITransport transport;
  private final ITransportGate transportGate;
  private final ExecutorService executor;
  private final IEventCache eventCache;
  private final SentryOptions options;

  public AsyncConnection(
      ITransport transport,
      ITransportGate transportGate,
      int maxRetries,
      IBackOffIntervalStrategy backOffIntervalStrategy,
      IEventCache eventCache,
      SentryOptions options) {
    this(transport, transportGate, eventCache, initExecutor(maxRetries, backOffIntervalStrategy, eventCache), options);
  }

  @VisibleForTesting
  AsyncConnection(ITransport transport,
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
      int maxRetries, IBackOffIntervalStrategy backOffIntervalStrategy, IEventCache eventCache) {

    RejectedExecutionHandler storeEvents =
        (r, executor) -> {
          if (r instanceof EventSender) {
            eventCache.store(((EventSender) r).event);
          }
        };

    return new RetryingThreadPoolExecutor(
        1, maxRetries, new AsyncConnectionThreadFactory(), backOffIntervalStrategy, storeEvents);
  }

  /**
   * Tries to send the event to the Sentry server.
   *
   * @param event the event to send
   * @throws IOException on error
   */
  public void send(SentryEvent event) throws IOException {
    executor.submit(new EventSender(event));
  }

  private static final class AsyncConnectionThreadFactory implements ThreadFactory {
    private int cnt;

    @Override
    public Thread newThread(Runnable r) {
      Thread ret = new Thread(r, "SentryAsyncConnection-" + cnt++);
      ret.setDaemon(true);
      return ret;
    }
  }

  private final class EventSender implements io.sentry.core.transport.Retryable {
    final SentryEvent event;
    long suggestedRetryDelay;

    EventSender(SentryEvent event) {
      this.event = event;
    }

    @Override
    public void run() {
      if (transportGate.isSendingAllowed()) {
        try {
          TransportResult result = transport.send(event);
          if (result.isSuccess()) {
            eventCache.discard(event);
          } else {
            eventCache.store(event);
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
          eventCache.store(event);
          throw new IllegalStateException("Sending the event failed.", e);
        }
      } else {
        eventCache.store(event);
      }
    }

    @Override
    public long getSuggestedRetryDelayMillis() {
      return suggestedRetryDelay;
    }
  }
}
