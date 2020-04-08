package io.sentry.android.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class ActivityBreadcrumbsIntegration implements Integration, Closeable {

  private final @NotNull Context context;
  private @Nullable IHub hub;

  public ActivityBreadcrumbsIntegration(final @NotNull Context context) {
    this.context = context;
  }

  @Override
  public void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    this.hub = hub;
    ((Application) context).registerActivityLifecycleCallbacks(lifecycleCallbacks);

    // TODO: check for fragments
  }

  @Override
  public void close() throws IOException {
    ((Application) context).unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
  }

  private final Application.ActivityLifecycleCallbacks lifecycleCallbacks =
      new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(
            @NonNull Activity activity, @Nullable Bundle savedInstanceState) {
          addBreadcrumb(activity, "created");
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
          addBreadcrumb(activity, "started");
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
          addBreadcrumb(activity, "resumed");
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
          addBreadcrumb(activity, "paused");
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
          addBreadcrumb(activity, "stopped");
        }

        @Override
        public void onActivitySaveInstanceState(
            @NonNull Activity activity, @NonNull Bundle outState) {
          addBreadcrumb(activity, "saveInstanceState");
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
          addBreadcrumb(activity, "destroyed");
        }
      };

  private void addBreadcrumb(final @NonNull Activity activity, final @NotNull String state) {
    if (hub != null) {
      final Breadcrumb breadcrumb = new Breadcrumb();
      breadcrumb.setType("navigation");
      breadcrumb.setData("state", state);
      breadcrumb.setData("screen", activity.getClass().getSimpleName());
      breadcrumb.setCategory("ui.lifecycle");
      breadcrumb.setLevel(SentryLevel.DEBUG);
      hub.addBreadcrumb(breadcrumb);
    }
  }
}
