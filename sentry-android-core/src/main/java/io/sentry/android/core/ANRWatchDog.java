// https://github.com/SalomonBrys/ANR-WatchDog/blob/1969075f75f5980e9000eaffbaa13b0daf282dcb/anr-watchdog/src/main/java/com/github/anrwatchdog/ANRWatchDog.java
// Based on the class above. The API unnecessary here was removed.
package io.sentry.android.core;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;

/** A watchdog timer thread that detects when the UI thread has frozen. */
@SuppressWarnings("UnusedReturnValue")
final class ANRWatchDog extends Thread {

  public interface ANRListener {
    /**
     * Called when an ANR is detected.
     *
     * @param error The error describing the ANR.
     */
    void onAppNotResponding(ApplicationNotResponding error);
  }

  private boolean reportInDebug;
  private ANRListener anrListener;
  private final Handler uiHandler = new Handler(Looper.getMainLooper());
  private final int timeoutIntervalMills;
  private ILogger logger;

  private AtomicLong tick = new AtomicLong(0);
  private volatile boolean reported = false;

  private final Runnable ticker =
      () -> {
        tick = new AtomicLong(0);
        reported = false;
      };

  /**
   * Constructs a watchdog that checks the ui thread every given interval
   *
   * @param timeoutIntervalMills The interval, in milliseconds, between to checks of the UI thread.
   *     It is therefore the maximum time the UI may freeze before being reported as ANR.
   * @param listener The new listener or null
   */
  ANRWatchDog(
      int timeoutIntervalMills,
      boolean reportInDebug,
      @NotNull ANRListener listener,
      @NotNull ILogger logger) {
    super();
    this.reportInDebug = reportInDebug;
    this.anrListener = listener;
    this.timeoutIntervalMills = timeoutIntervalMills;
    this.logger = logger;
  }

  @SuppressWarnings("NonAtomicOperationOnVolatileField")
  @Override
  public void run() {
    setName("|ANR-WatchDog|");

    long interval = timeoutIntervalMills;
    while (!isInterrupted()) {
      boolean needPost = tick.get() == 0;
      tick.addAndGet(interval);
      if (needPost) {
        uiHandler.post(ticker);
      }

      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        logger.log(SentryLevel.WARNING, "Interrupted: %s", e.getMessage());
        return;
      }

      // If the main thread has not handled ticker, it is blocked. ANR.
      if (tick.get() != 0 && !reported) {
        //noinspection ConstantConditions
        if (!reportInDebug && (Debug.isDebuggerConnected() || Debug.waitingForDebugger())) {
          logger.log(
              SentryLevel.DEBUG,
              "An ANR was detected but ignored because the debugger is connected.");
          reported = true;
          continue;
        }

        logger.log(SentryLevel.INFO, "Raising ANR");
        final String message =
            "Application Not Responding for at least " + timeoutIntervalMills + " ms.";

        final ApplicationNotResponding error = new ApplicationNotResponding(message);
        anrListener.onAppNotResponding(error);
        interval = timeoutIntervalMills;
        reported = true;
      }
    }
  }
}
