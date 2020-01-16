package io.sentry.core.transport;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

/**
 * This is a thread pool executor enriched for the possibility of retrying the supplied tasks.
 *
 * <p>Note that only {@link Runnable} tasks are retried, a {@link java.util.concurrent.Callable} is
 * not retry-able. Note also that the {@link Future} returned from the {@link #submit(Runnable)} or
 * {@link #submit(Runnable, Object)} methods is NOT generally usable, because it does not work when
 * the task is retried!
 *
 * <p>The {@link Runnable} instances may in addition implement the {@link Retryable} interface to
 * suggest the required delay before the next attempt.
 *
 * <p>This class is not public because it is used solely in {@link AsyncConnection}.
 */
final class RetryingThreadPoolExecutor extends ScheduledThreadPoolExecutor {
  //  private final int maxRetries;
  private final int maxQueueSize;
  private final AtomicInteger currentlyRunning;
  private final IBackOffIntervalStrategy backOffIntervalStrategy;

  private static final int HTTP_TOO_MANY_REQUESTS = 429;

  private final AtomicBoolean retryAfter = new AtomicBoolean(false);
  private final Timer timer = new Timer(true);
  private TimerTask timerTaskRetryAfter;

  /**
   * Creates a new instance of the thread pool.
   *
   * @param corePoolSize the minimum number of threads started // * @param maxRetries the maximum
   *     number of retries of failing tasks
   * @param threadFactory the thread factory to construct new threads
   * @param backOffIntervalStrategy the strategy for obtaining delays between retries if not
   *     suggested by the tasks
   * @param rejectedExecutionHandler specifies what to do with the tasks that cannot be run (e.g.
   *     during the shutdown)
   */
  public RetryingThreadPoolExecutor(
      int corePoolSize,
      //    int maxRetries,
      int maxQueueSize,
      ThreadFactory threadFactory,
      IBackOffIntervalStrategy backOffIntervalStrategy,
      RejectedExecutionHandler rejectedExecutionHandler) {
    super(corePoolSize, threadFactory, rejectedExecutionHandler);
    //    this.maxRetries = maxRetries;
    this.maxQueueSize = maxQueueSize;
    this.backOffIntervalStrategy = backOffIntervalStrategy;
    this.currentlyRunning = new AtomicInteger();
  }

  /**
   * A special overload to submit {@link Retryable} tasks.
   *
   * @param task the task to execute
   */
  @SuppressWarnings("FutureReturnValueIgnored")
  // https://errorprone.info/bugpattern/FutureReturnValueIgnored
  public void submit(Retryable task) {
    if (isSchedulingAllowed()) {
      super.submit(task);
    }
  }

  @Override
  public Future<?> submit(Runnable task) {
    if (isSchedulingAllowed()) {
      return super.submit(task);
    } else {
      return new CancelledFuture<>();
    }
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    if (isSchedulingAllowed()) {
      return super.submit(task, result);
    } else {
      return new CancelledFuture<>();
    }
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    if (isSchedulingAllowed()) {
      return super.submit(task);
    } else {
      return new CancelledFuture<>();
    }
  }

  @Override
  protected <V> RunnableScheduledFuture<V> decorateTask(
      Runnable runnable, RunnableScheduledFuture<V> task) {

    //    int attempt = 0;

    if (runnable instanceof NextAttempt) {
      //      attempt = ((NextAttempt) runnable).attempt;
      runnable = ((NextAttempt) runnable).runnable;
    }

    return new AttemptedRunnable<>(task, runnable);
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
    currentlyRunning.incrementAndGet();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  // https://errorprone.info/bugpattern/FutureReturnValueIgnored
  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    try {
      super.afterExecute(r, t);

      if (!(r instanceof AttemptedRunnable)) {
        return;
      }

      AttemptedRunnable<?> ar = (AttemptedRunnable) r;

      // taken verbatim from the javadoc of the method in ThreadPoolExecutor - this makes sure we
      // capture the exceptions from the tasks
      if (t == null) {
        try {
          ar.get();
        } catch (CancellationException ce) {
          t = ce;
        } catch (ExecutionException ee) {
          t = ee.getCause();
        } catch (InterruptedException ie) {
          // ok, we're interrupted - mark the thread again and give up
          Thread.currentThread().interrupt();
          return;
        }
      }

      if (t != null) {
        //        int attempt = ar.attempt.get();
        //        if (attempt < maxRetries) {
        long delayMillis = -1;
        int responseCode = -1;
        if (ar.suppliedAction instanceof Retryable) {
          delayMillis = ((Retryable) ar.suppliedAction).getSuggestedRetryDelayMillis();
          responseCode = ((Retryable) ar.suppliedAction).getResponseCode();
        }

        if (delayMillis < 0) {
          delayMillis = backOffIntervalStrategy.nextDelayMillis(1);
        }

        if (responseCode == HTTP_TOO_MANY_REQUESTS) {
          // if I delay or await this thread to finish respecting delayMillis, next ones might not
          // have the chance to cache the event
          // eg
          // https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadPoolExecutor.html
          // using a ReentrantLock pauseLock

          //            List<Runnable> runnables = shutdownNow();
          // it should be possible to reschedule again with the delay, but again, we delay the
          // caching of events
          //          shutdownNow();
          getQueue().clear();

          scheduleRetryAfterDelay(delayMillis);
        }
        //          schedule(new NextAttempt(attempt, ar.suppliedAction), delayMillis,
        // TimeUnit.MILLISECONDS);
        //        }
      }
    } finally {
      currentlyRunning.decrementAndGet();
    }
  }

  private void scheduleRetryAfterDelay(long delayMillis) {
    if (!retryAfter.getAndSet(true)) {
      if (timerTaskRetryAfter != null) {
        timerTaskRetryAfter.cancel();
      }
      timerTaskRetryAfter =
          new TimerTask() {
            @Override
            public void run() {
              retryAfter.set(false);
            }
          };

      timer.schedule(timerTaskRetryAfter, delayMillis);
    }
  }

  private boolean isSchedulingAllowed() {
    return getQueue().size() + currentlyRunning.get() < maxQueueSize && !retryAfter.get();
  }

  private static final class NextAttempt implements Runnable {
    //    private final int attempt;
    private final Runnable runnable;

    private NextAttempt(Runnable runnable) {
      //      this.attempt = attempt;
      this.runnable = runnable;
    }

    @Override
    public void run() {
      runnable.run();
    }
  }

  private static final class AttemptedRunnable<V> implements RunnableScheduledFuture<V> {
    private final RunnableScheduledFuture<?> task;
    private final Runnable suppliedAction;
    //    final AtomicInteger attempt;

    AttemptedRunnable(RunnableScheduledFuture<?> task, Runnable suppliedAction) {
      this.task = task;
      this.suppliedAction = suppliedAction;
      //      this.attempt = new AtomicInteger(attempt);
    }

    @Override
    public boolean isPeriodic() {
      return task.isPeriodic();
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      return task.getDelay(unit);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
      return task.compareTo(o);
    }

    @Override
    public void run() {
      //      attempt.incrementAndGet();
      task.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return task.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
      return task.isCancelled();
    }

    @Override
    public boolean isDone() {
      return task.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      task.get();
      return null;
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      task.get(timeout, unit);
      return null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      AttemptedRunnable<?> that = (AttemptedRunnable<?>) o;
      return task.equals(that.task);
    }

    @Override
    public int hashCode() {
      return task.hashCode();
    }

    @Override
    public String toString() {
      return task.toString();
    }
  }

  private static final class CancelledFuture<T> implements Future<T> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return true;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public T get() {
      throw new CancellationException();
    }

    @Override
    public T get(long timeout, @NotNull TimeUnit unit) {
      throw new CancellationException();
    }
  }
}
