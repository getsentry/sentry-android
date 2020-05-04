package io.sentry.android.core;

import io.sentry.core.SentryOptions;
import org.jetbrains.annotations.ApiStatus;

/** Sentry SDK options for Android */
public final class SentryAndroidOptions extends SentryOptions {

  /**
   * Enable or disable ANR (Application Not Responding) Default is enabled Used by AnrIntegration
   */
  private boolean anrEnabled = true;

  /** ANR Timeout internal in Millis Default is 4000 = 4s Used by AnrIntegration */
  private long anrTimeoutIntervalMillis = 4000;

  /** Enable or disable ANR on Debug mode Default is disabled Used by AnrIntegration */
  private boolean anrReportInDebug = false;

  /**
   * Enable or disable automatic breadcrumbs for Activity lifecycle. Using
   * Application.ActivityLifecycleCallbacks
   */
  private boolean enableActivityLifecycleBreadcrumbs = true;

  /** Enable or disable automatic breadcrumbs for App's lifecycle Using ProcessLifecycleOwner */
  private boolean enableAppLifecycleBreadcrumbs = true;

  /** Enable or disable automatic breadcrumbs for SystemEvents Registering a BroadcastReceiver */
  private boolean enableSystemEventBreadcrumbs = true;

  /** Enable or disable automatic breadcrumbs for App Components Using ComponentCallbacks */
  private boolean enableAppComponentBreadcrumbs = true;

  /**
   * Checks if ANR (Application Not Responding) is enabled or disabled Default is enabled
   *
   * @return true if enabled or false otherwise
   */
  public boolean isAnrEnabled() {
    return anrEnabled;
  }

  /**
   * Sets ANR (Application Not Responding) to enabled or disabled Default is enabled
   *
   * @param anrEnabled true for enabled and false for disabled
   */
  public void setAnrEnabled(boolean anrEnabled) {
    this.anrEnabled = anrEnabled;
  }

  /**
   * @deprecated use {@link #getAnrTimeoutIntervalMillis , #getAnrTimeoutIntervalMillis} instead.
   */
  @ApiStatus.ScheduledForRemoval
  @Deprecated
  public long getAnrTimeoutIntervalMills() {
    return getAnrTimeoutIntervalMillis();
  }

  /**
   * @deprecated use {@link #setAnrTimeoutIntervalMillis , #setAnrTimeoutIntervalMillis} instead.
   */
  @ApiStatus.ScheduledForRemoval
  @Deprecated
  public void setAnrTimeoutIntervalMills(long anrTimeoutIntervalMillis) {
    setAnrTimeoutIntervalMillis(anrTimeoutIntervalMillis);
  }

  /**
   * Returns the ANR timeout internal in Millis Default is 4000 = 4s
   *
   * @return the timeout in millis
   */
  public long getAnrTimeoutIntervalMillis() {
    return anrTimeoutIntervalMillis;
  }

  /**
   * Sets the ANR timeout internal in Millis Default is 4000 = 4s
   *
   * @param anrTimeoutIntervalMillis the timeout internal in Millis
   */
  public void setAnrTimeoutIntervalMillis(long anrTimeoutIntervalMillis) {
    this.anrTimeoutIntervalMillis = anrTimeoutIntervalMillis;
  }

  /**
   * Checks if ANR (Application Not Responding) is enabled or disabled on Debug mode Default is
   * disabled
   *
   * @return true if enabled or false otherwise
   */
  public boolean isAnrReportInDebug() {
    return anrReportInDebug;
  }

  /**
   * Sets ANR (Application Not Responding) to enabled or disabled on Debug mode Default is disabled
   *
   * @param anrReportInDebug true for enabled and false for disabled
   */
  public void setAnrReportInDebug(boolean anrReportInDebug) {
    this.anrReportInDebug = anrReportInDebug;
  }

  public boolean isEnableActivityLifecycleBreadcrumbs() {
    return enableActivityLifecycleBreadcrumbs;
  }

  public void setEnableActivityLifecycleBreadcrumbs(boolean enableActivityLifecycleBreadcrumbs) {
    this.enableActivityLifecycleBreadcrumbs = enableActivityLifecycleBreadcrumbs;
  }

  public boolean isEnableAppLifecycleBreadcrumbs() {
    return enableAppLifecycleBreadcrumbs;
  }

  public void setEnableAppLifecycleBreadcrumbs(boolean enableAppLifecycleBreadcrumbs) {
    this.enableAppLifecycleBreadcrumbs = enableAppLifecycleBreadcrumbs;
  }

  public boolean isEnableSystemEventBreadcrumbs() {
    return enableSystemEventBreadcrumbs;
  }

  public void setEnableSystemEventBreadcrumbs(boolean enableSystemEventBreadcrumbs) {
    this.enableSystemEventBreadcrumbs = enableSystemEventBreadcrumbs;
  }

  public boolean isEnableAppComponentBreadcrumbs() {
    return enableAppComponentBreadcrumbs;
  }

  public void setEnableAppComponentBreadcrumbs(boolean enableAppComponentBreadcrumbs) {
    this.enableAppComponentBreadcrumbs = enableAppComponentBreadcrumbs;
  }

  /**
   * Enable or disable all the automatic breadcrumbs
   *
   * @param enable true if enable or false otherwise
   */
  public void enableAllAutoBreadcrumbs(boolean enable) {
    enableActivityLifecycleBreadcrumbs = enable;
    enableAppComponentBreadcrumbs = enable;
    enableSystemEventBreadcrumbs = enable;
    enableAppLifecycleBreadcrumbs = enable;
  }
}
