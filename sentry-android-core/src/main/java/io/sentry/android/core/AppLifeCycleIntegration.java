package io.sentry.android.core;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryOptions;

public final class AppLifeCycleIntegration implements Integration, LifecycleObserver {

  private IHub hub;

  @Override
  public void register(IHub hub, SentryOptions options) {
    this.hub = hub;

    // TODO: check on options if integration should be on?
    ProcessLifecycleOwner.get()
        .getLifecycle()
        .addObserver(this); // they also use ContentProvider :P
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void onBackground() {
    // we could stop a session
    hub.addBreadcrumb("App is background", "background");
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void onForeground() {
    // we could start a session
    hub.addBreadcrumb("App is foreground", "foreground");
  }
}
