package io.sentry.android.core;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;

// API 14 ActivityLifecycleCallbacks so its ok
public final class ActivityLifeCycleIntegration
    implements Integration, Application.ActivityLifecycleCallbacks {

  private final Context context;
  private IHub hub;

  ActivityLifeCycleIntegration(Context context) {
    this.context = context;
  }

  @Override
  public void register(IHub hub, SentryOptions options) {
    this.hub = hub;

    // TODO: check on options if integration should be on?

    ((Application) context).registerActivityLifecycleCallbacks(this);

    // API 14 as well
    context.registerComponentCallbacks(
        new ComponentCallbacks2() {
          @Override
          public void onTrimMemory(int level) {
            // I'd go only for onLowMemory, but this one is also possible
            Breadcrumb breadcrumb = new Breadcrumb();
            breadcrumb.setLevel(SentryLevel.WARNING);
            breadcrumb.setMessage("App onTrimMemory: " + level);
            breadcrumb.setCategory("memory");

            // level
            //        TRIM_MEMORY_COMPLETE,
            //          TRIM_MEMORY_MODERATE,
            //          TRIM_MEMORY_BACKGROUND,
            //          TRIM_MEMORY_UI_HIDDEN,
            //          TRIM_MEMORY_RUNNING_CRITICAL,
            //          TRIM_MEMORY_RUNNING_LOW,
            //          TRIM_MEMORY_RUNNING_MODERATE

            hub.addBreadcrumb(breadcrumb);
          }

          @Override
          public void onConfigurationChanged(Configuration newConfig) {
            hub.addBreadcrumb(getOrientation(newConfig.orientation), "configuration");
          }

          @Override
          public void onLowMemory() {
            Breadcrumb breadcrumb = new Breadcrumb();
            breadcrumb.setLevel(SentryLevel.WARNING);
            breadcrumb.setMessage("App onLowMemory");
            breadcrumb.setCategory("memory");
            hub.addBreadcrumb(breadcrumb);
          }
        });
  }

  /**
   * Get the device's current screen orientation.
   *
   * @return the device's current screen orientation, or null if unknown
   */
  private String getOrientation(int orientation) {
    switch (orientation) {
      case Configuration.ORIENTATION_LANDSCAPE:
        return "LANDSCAPE";
      case Configuration.ORIENTATION_PORTRAIT:
        return "PORTRAIT";
      default:
        return "ORIENTATION_UNDEFINED";
    }
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    hub.setTag("activity", activity.getComponentName().getClassName());
  }

  @Override
  public void onActivityStarted(Activity activity) {
    hub.addBreadcrumb(activity.getComponentName().getClassName(), "started");
  }

  @Override
  public void onActivityResumed(Activity activity) {
    hub.addBreadcrumb(activity.getComponentName().getClassName(), "resumed");
  }

  @Override
  public void onActivityPaused(Activity activity) {
    hub.addBreadcrumb(activity.getComponentName().getClassName(), "paused");
  }

  @Override
  public void onActivityStopped(Activity activity) {
    hub.addBreadcrumb(activity.getComponentName().getClassName(), "stopped");
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    // could even log the hashmap of the bundle
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
    hub.addBreadcrumb(activity.getComponentName().getClassName(), "destroyed");
  }
}
