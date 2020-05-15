package io.sentry.core;

import java.util.concurrent.Future;

interface ISentryExecutorService {

  Future<?> submit(Runnable runnable);

  void close(long timeoutMillis);
}
