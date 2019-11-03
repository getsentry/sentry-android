package io.sentry.core.transport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** This interface is not public because it is provided solely for the ease of testing. */
interface FlushableExecutorService extends ExecutorService {

  /**
   * In essence, this is very similar to {@link #awaitTermination(long, TimeUnit)} but keeps the
   * executor active.
   *
   * <p>New tasks can be submitted to the executor service. The return future completes when all the
   * tasks queued at the time this method was called are finished.
   *
   * @param timeout the amount of time to wait for the flushing to finish
   * @param unit the unit of the time
   * @return a future that will complete as soon as all the tasks in the executor service are
   *     completed or the timeout happens, whichever comes first.
   */
  Future<Void> flush(long timeout, TimeUnit unit);
}
