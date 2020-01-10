package io.sentry.android.core;

import static io.sentry.core.ILogger.logIfNotNull;

import io.sentry.core.IHub;
import io.sentry.core.ILogger;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.exception.ExceptionMechanismException;
import io.sentry.core.protocol.Mechanism;
import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.TestOnly;

final class AnrIntegration implements Integration, Closeable {

  private static ANRWatchDog anrWatchDog;

  @Override
  public void register(IHub hub, SentryOptions options) {
    register(hub, (SentryAndroidOptions) options);
  }

  private void register(IHub hub, SentryAndroidOptions options) {
    logIfNotNull(options.getLogger(), SentryLevel.DEBUG, "ANR enabled: %s", options.isAnrEnabled());

    if (options.isAnrEnabled() && anrWatchDog == null) {
      logIfNotNull(
          options.getLogger(),
          SentryLevel.DEBUG,
          "ANR timeout in milliseconds: %d",
          options.getAnrTimeoutIntervalMills());

      anrWatchDog =
          new ANRWatchDog(
              options.getAnrTimeoutIntervalMills(),
              options.isAnrReportInDebug(),
              error -> reportANR(hub, options.getLogger(), error),
              options.getLogger());
      anrWatchDog.start();
    }
  }

  @TestOnly
  void reportANR(IHub hub, ILogger logger, ApplicationNotResponding error) {
    logIfNotNull(logger, SentryLevel.INFO, "ANR triggered with message: %s", error.getMessage());

    Mechanism mechanism = new Mechanism();
    mechanism.setType("ANR");
    ExceptionMechanismException throwable =
        new ExceptionMechanismException(mechanism, error, Thread.currentThread());

    hub.captureException(throwable);
  }

  @TestOnly
  ANRWatchDog getANRWatchDog() {
    return anrWatchDog;
  }

  @Override
  public void close() throws IOException {
    if (anrWatchDog != null) {
      anrWatchDog.interrupt();
      anrWatchDog = null;
    }
  }
}
