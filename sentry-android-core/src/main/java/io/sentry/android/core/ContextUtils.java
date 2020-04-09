package io.sentry.android.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.provider.Settings;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ContextUtils {

  private ContextUtils() {}

  /**
   * Return the Application's PackageInfo if possible, or null.
   *
   * @return the Application's PackageInfo if possible, or null
   */
  @Nullable
  static PackageInfo getPackageInfo(final @NotNull Context context, final @NotNull ILogger logger) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    } catch (Exception e) {
      logger.log(SentryLevel.ERROR, "Error getting package info.", e);
      return null;
    }
  }

  /**
   * Returns the App's version code based on the PackageInfo
   *
   * @param packageInfo the PackageInfo class
   * @return the versionCode or LongVersionCode based on your API version
   */
  @NotNull
  static String getVersionCode(final @NotNull PackageInfo packageInfo) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      return Long.toString(packageInfo.getLongVersionCode());
    }
    return getVersionCodeDep(packageInfo);
  }

  @SuppressWarnings("deprecation")
  private static @NotNull String getVersionCodeDep(final @NotNull PackageInfo packageInfo) {
    return Integer.toString(packageInfo.versionCode);
  }

  /**
   * Returns the Settings.Secure.ANDROID_ID and if not valid, fallback to defaultValue
   *
   * @param defaultValue the defaultValue
   * @return Settings.Secure.ANDROID_ID if valid or defaultValue
   */
  @SuppressWarnings("HardwareIds")
  static @Nullable String getAndroidId(
      final @NotNull Context context, final @Nullable String defaultValue) {
    String androidId =
        Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

    if (androidId == null
        || androidId.isEmpty()
        || androidId.equalsIgnoreCase("9774d56d682e549c")) {
      return defaultValue;
    }
    return androidId;
  }
}
