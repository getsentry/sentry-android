package io.sentry.android.core;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import io.sentry.core.Sentry;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LifecycleWatcher implements LifecycleObserver {

  private static long lastStartedSession = 0L;
  private final long sessionIntervalMillis;

  private TimerTask timerTask;
  private final Timer timer = new Timer(true);

  LifecycleWatcher(final long sessionIntervalMillis) {
    this.sessionIntervalMillis = sessionIntervalMillis;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void foreground() {
    final long currentTimeMillis = System.currentTimeMillis();
    cancelTask();
    if (lastStartedSession == 0L
        || (lastStartedSession + sessionIntervalMillis) <= currentTimeMillis) {
      Sentry.startSession();
    }
    lastStartedSession = currentTimeMillis;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void background() {
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
