package io.sentry.android.core;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import io.sentry.core.Sentry;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class LifecycleWatcher implements DefaultLifecycleObserver {

  private static long lastStartedSession = 0L;
  private final long sessionIntervalMillis;

  private TimerTask timerTask;
  private final Timer timer = new Timer(true);

  LifecycleWatcher(final long sessionIntervalMillis) {
    this.sessionIntervalMillis = sessionIntervalMillis;
  }

  // App goes to foreground
  @Override
  public void onStart(@NotNull LifecycleOwner owner) {
    final long currentTimeMillis = System.currentTimeMillis();
    cancelTask();
    if (lastStartedSession == 0L
        || (lastStartedSession + sessionIntervalMillis) <= currentTimeMillis) {
      Sentry.startSession();
    }
    lastStartedSession = currentTimeMillis;
  }

  // App went to background and triggered this callback after 700ms
  // as no new screen was shown
  @Override
  public void onStop(@NotNull LifecycleOwner owner) {
    scheduleEndSession();
  }

  private void scheduleEndSession() {
    cancelTask();
    timerTask =
        new TimerTask() {
          @Override
          public void run() {
            Sentry.endSession();
          }
        };

    timer.schedule(timerTask, sessionIntervalMillis);
  }

  private void cancelTask() {
    if (timerTask != null) {
      timerTask.cancel();
    }
  }
}
