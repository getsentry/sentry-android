package io.sentry.android.core;

/** To make the Build class testable */
public interface IBuildInfoProvider {

  /**
   * Returns Build.VERSION.SDK_INT
   *
   * @return the Build.VERSION.SDK_INT
   */
  int getSdkInfoVersion();
}
