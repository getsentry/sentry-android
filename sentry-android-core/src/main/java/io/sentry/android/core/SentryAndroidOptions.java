package io.sentry.android.core;

import io.sentry.core.SentryOptions;

public final class SentryAndroidOptions extends SentryOptions {
  private boolean anrEnabled = false;
  private int anrTimeoutIntervalMills = 5000;

  public boolean isAnrEnabled() {
    return anrEnabled;
  }

  public void setAnrEnabled(boolean anrEnabled) {
    this.anrEnabled = anrEnabled;
  }

  public int getAnrTimeoutIntervalMills() {
    return anrTimeoutIntervalMills;
  }

  public void setAnrTimeoutIntervalMills(int anrTimeoutIntervalMills) {
    this.anrTimeoutIntervalMills = anrTimeoutIntervalMills;
  }
}
