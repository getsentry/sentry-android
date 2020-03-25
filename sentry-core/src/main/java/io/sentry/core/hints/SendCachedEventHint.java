package io.sentry.core.hints;

import static io.sentry.core.SentryLevel.ERROR;

import io.sentry.core.ILogger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class SendCachedEventHint implements Cached, Retryable, SubmissionResult, Flushable {
  boolean retry = false;
  boolean succeeded = false;

  private final CountDownLatch latch;
  private final long timeoutMillis;
  private final @NotNull ILogger logger;

  public SendCachedEventHint(final long timeoutMillis, final @NotNull ILogger logger) {
    this.timeoutMillis = timeoutMillis;
    this.latch = new CountDownLatch(1);
    this.logger = logger;
  }

  @Override
  public boolean isRetry() {
    return retry;
  }

  @Override
  public void setRetry(boolean retry) {
    this.retry = retry;
  }

  @Override
  public boolean waitFlush() {
    try {
      return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.log(ERROR, "Exception while awaiting on lock.", e);
    }
    return false;
  }

  @Override
  public void setResult(boolean succeeded) {
    this.succeeded = succeeded;
    latch.countDown();
  }

  @Override
  public boolean isSuccess() {
    return succeeded;
  }
}
