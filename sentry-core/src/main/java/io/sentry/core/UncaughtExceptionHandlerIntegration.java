package io.sentry.core;

import static io.sentry.core.SentryLevel.ERROR;

import io.sentry.core.exception.ExceptionMechanismException;
import io.sentry.core.hints.DiskFlushNotification;
import io.sentry.core.protocol.Mechanism;
import io.sentry.core.util.Objects;
import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Sends any uncaught exception to Sentry, then passes the exception on to the pre-existing uncaught
 * exception handler.
 */
public final class UncaughtExceptionHandlerIntegration
    implements Integration, Thread.UncaughtExceptionHandler, Closeable {
  /** Reference to the pre-existing uncaught exception handler. */
  private Thread.UncaughtExceptionHandler defaultExceptionHandler;

  private IHub hub;
  private SentryOptions options;

  private boolean registered = false;
  private final UncaughtExceptionHandler threadAdapter;

  UncaughtExceptionHandlerIntegration() {
    this(UncaughtExceptionHandler.Adapter.getInstance());
  }

  UncaughtExceptionHandlerIntegration(UncaughtExceptionHandler threadAdapter) {
    this.threadAdapter = Objects.requireNonNull(threadAdapter, "threadAdapter is required.");
  }

  @Override
  public final void register(IHub hub, SentryOptions options) {
    if (registered) {
      options
          .getLogger()
          .log(
              SentryLevel.ERROR,
              "Attempt to register a UncaughtExceptionHandlerIntegration twice.");
      return;
    }
    registered = true;

    this.hub = hub;
    this.options = options;
    Thread.UncaughtExceptionHandler currentHandler =
        threadAdapter.getDefaultUncaughtExceptionHandler();
    if (currentHandler != null) {
      options
          .getLogger()
          .log(
              SentryLevel.DEBUG,
              "default UncaughtExceptionHandler class='"
                  + currentHandler.getClass().getName()
                  + "'");
      defaultExceptionHandler = currentHandler;
    }

    threadAdapter.setDefaultUncaughtExceptionHandler(this);

    options.getLogger().log(SentryLevel.DEBUG, "UncaughtExceptionHandlerIntegration installed.");
  }

  @Override
  public void uncaughtException(Thread thread, Throwable thrown) {
    options.getLogger().log(SentryLevel.INFO, "Uncaught exception received.");

    try {
      UncaughtExceptionHint hint =
          new UncaughtExceptionHint(options.getFlushTimeoutMillis(), options.getLogger());
      Throwable throwable = getUnhandledThrowable(thread, thrown);
      SentryEvent event = new SentryEvent(throwable);
      event.setLevel(SentryLevel.FATAL);
      this.hub.captureEvent(event, hint);
      // Block until the event is flushed to disk
      if (!hint.waitFlush()) {
        options
            .getLogger()
            .log(
                SentryLevel.WARNING,
                "Timed out waiting to flush event to disk before crashing. Event: %s",
                event.getEventId());
      }
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error sending uncaught exception to Sentry.", e);
    }

    if (defaultExceptionHandler != null) {
      options.getLogger().log(SentryLevel.INFO, "Invoking inner uncaught exception handler.");
      defaultExceptionHandler.uncaughtException(thread, thrown);
    }
  }

  @TestOnly
  @NotNull
  static Throwable getUnhandledThrowable(Thread thread, Throwable thrown) {
    Mechanism mechanism = new Mechanism();
    mechanism.setHandled(false);
    mechanism.setType("UncaughtExceptionHandler");
    return new ExceptionMechanismException(mechanism, thrown, thread);
  }

  @Override
  public void close() {
    if (defaultExceptionHandler != null
        && this == threadAdapter.getDefaultUncaughtExceptionHandler()) {
      threadAdapter.setDefaultUncaughtExceptionHandler(defaultExceptionHandler);
    }
  }

  private static final class UncaughtExceptionHint implements DiskFlushNotification {

    private final CountDownLatch latch;
    private final long timeoutMillis;
    private final @NotNull ILogger logger;

    UncaughtExceptionHint(final long timeoutMillis, final @NotNull ILogger logger) {
      this.timeoutMillis = timeoutMillis;
      this.latch = new CountDownLatch(1);
      this.logger = logger;
    }

    boolean waitFlush() {
      try {
        return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.log(ERROR, "Exception while awaiting for flush in UncaughtExceptionHint", e);
      }
      return false;
    }

    @Override
    public void markFlushed() {
      latch.countDown();
    }
  }
}
