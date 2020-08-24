package io.sentry.android.core;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ProcessLifecycleOwner;
import io.sentry.android.core.util.MainThreadChecker;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

public final class AppLifecycleIntegration implements Integration, Closeable {

  @TestOnly @Nullable LifecycleWatcher watcher;

  private @Nullable SentryAndroidOptions options;

  @Override
  public void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    Objects.requireNonNull(hub, "Hub is required");
    this.options =
        Objects.requireNonNull(
            (options instanceof SentryAndroidOptions) ? (SentryAndroidOptions) options : null,
            "SentryAndroidOptions is required");

    this.options
        .getLogger()
        .log(
            SentryLevel.DEBUG,
            "enableSessionTracking enabled: %s",
            this.options.isEnableSessionTracking());

    this.options
        .getLogger()
        .log(
            SentryLevel.DEBUG,
            "enableAppLifecycleBreadcrumbs enabled: %s",
            this.options.isEnableAppLifecycleBreadcrumbs());

    if (this.options.isEnableSessionTracking() || this.options.isEnableAppLifecycleBreadcrumbs()) {
      try {
        Class.forName("androidx.lifecycle.DefaultLifecycleObserver");
        Class.forName("androidx.lifecycle.ProcessLifecycleOwner");
        if (MainThreadChecker.isMainThread()) {
          addObserver(hub);
          options.getLogger().log(SentryLevel.DEBUG, "AppLifecycleIntegration installed.");
        } else {
          // some versions of the androidx lifecycle-process require this to be executed on the main
          // thread.
          final Handler handler = new Handler(Looper.getMainLooper());
          handler.post(() -> addObserver(hub));
        }
      } catch (ClassNotFoundException e) {
        options
            .getLogger()
            .log(
                SentryLevel.INFO,
                "androidx.lifecycle is not available, AppLifecycleIntegration won't be installed",
                e);
      }
    }
  }

  private void addObserver(final @NotNull IHub hub) {
    watcher =
        new LifecycleWatcher(
            hub,
            this.options.getSessionTrackingIntervalMillis(),
            this.options.isEnableSessionTracking(),
            this.options.isEnableAppLifecycleBreadcrumbs());
    ProcessLifecycleOwner.get().getLifecycle().addObserver(watcher);
  }

  @Override
  public void close() throws IOException {
    if (watcher != null) {
      ProcessLifecycleOwner.get().getLifecycle().removeObserver(watcher);
      watcher = null;
      if (options != null) {
        options.getLogger().log(SentryLevel.DEBUG, "AppLifecycleIntegration removed.");
      }
    }
  }
}
