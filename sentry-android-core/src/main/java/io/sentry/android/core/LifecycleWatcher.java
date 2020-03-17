package io.sentry.android.core;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import io.sentry.core.Sentry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LifecycleWatcher implements LifecycleObserver {

  //  private long lastStartedSession;
  //  private static final long DEFAULT_SESSION_INTERVAL_MILLIS = 5000; // 5s
  //  private long lastEndedSession;

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void foreground() {
    //    lastStartedSession = System.currentTimeMillis();
    Sentry.startSession();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void background() {
    //    lastEndedSession = System.currentTimeMillis();
    Sentry.endSession();
  }
}
