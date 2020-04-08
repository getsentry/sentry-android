package io.sentry.android.core;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.SentryLevel;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LifecycleWatcher implements DefaultLifecycleObserver {

  private long lastStartedSession = 0L;

  private final long sessionIntervalMillis;

  private @Nullable TimerTask timerTask;
  private final @NotNull Timer timer = new Timer(true);
  private final @NotNull IHub hub;
  private final boolean enableSessionTracking;

  LifecycleWatcher(
      final @NotNull IHub hub, final long sessionIntervalMillis, boolean enableSessionTracking) {
    this.sessionIntervalMillis = sessionIntervalMillis;
    this.enableSessionTracking = enableSessionTracking;
    this.hub = hub;
  }

  // App goes to foreground
  @Override
  public void onStart(final @NotNull LifecycleOwner owner) {
    addAppBreadcrumb("foreground");
    if (enableSessionTracking) {
      final long currentTimeMillis = System.currentTimeMillis();
      cancelTask();
      if (lastStartedSession == 0L
          || (lastStartedSession + sessionIntervalMillis) <= currentTimeMillis) {
        addSessionBreadcrumb("start");
        hub.startSession();
      }
      lastStartedSession = currentTimeMillis;
    }
  }

  // App went to background and triggered this callback after 700ms
  // as no new screen was shown
  @Override
  public void onStop(final @NotNull LifecycleOwner owner) {
    addAppBreadcrumb("background");
    if (enableSessionTracking) {
      scheduleEndSession();
    }
  }

  private void scheduleEndSession() {
    cancelTask();
    timerTask =
        new TimerTask() {
          @Override
          public void run() {
            addSessionBreadcrumb("end");
            hub.endSession();
          }
        };

    timer.schedule(timerTask, sessionIntervalMillis);
  }

  private void cancelTask() {
    if (timerTask != null) {
      timerTask.cancel();
    }
  }

  private void addAppBreadcrumb(final @NotNull String state) {
    final Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.setType("navigation");
    breadcrumb.setData("state", state);
    breadcrumb.setCategory("ui.lifecycle");
    breadcrumb.setLevel(SentryLevel.DEBUG);
    hub.addBreadcrumb(breadcrumb);
  }

  private void addSessionBreadcrumb(final @NotNull String state) {
    final Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.setType("session");
    breadcrumb.setData("state", state);
    breadcrumb.setCategory("session.lifecycle");
    breadcrumb.setLevel(SentryLevel.DEBUG);
    hub.addBreadcrumb(breadcrumb);
  }
}
