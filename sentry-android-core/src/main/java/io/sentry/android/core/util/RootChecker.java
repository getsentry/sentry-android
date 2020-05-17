package io.sentry.android.core.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import io.sentry.core.util.Objects;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class RootChecker {

  /** the UTF-8 Charset */
  @SuppressWarnings("CharsetObjectCanBeUsed")
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private RootChecker() {}

  /**
   * Check if the device is rooted or not
   * https://medium.com/@thehimanshugoel/10-best-security-practices-in-android-applications-that-every-developer-must-know-99c8cd07c0bb
   *
   * @param context the Context
   * @param logger the Logger
   * @return whether the device is rooted or not
   */
  public static boolean isDeviceRooted(
      final @NotNull Context context, final @NotNull ILogger logger) {
    Objects.requireNonNull(context, "The Context is required.");
    Objects.requireNonNull(logger, "The Logger is required.");

    return checkTestKeys()
        || checkRootFiles(logger)
        || checkSUExist(logger)
        || checkRootPackages(context);
  }

  /**
   * Android Roms from Google are build with release-key tags. If test-keys are present, this can
   * mean that the Android build on the device is either a developer build or an unofficial Google
   * build.
   *
   * @return whether if it contains test keys or not
   */
  private static boolean checkTestKeys() {
    final String buildTags = Build.TAGS;
    return buildTags != null && buildTags.contains("test-keys");
  }

  /**
   * Often the rooted device have the following files . This method will check whether the device is
   * having these files or not
   *
   * @param logger the Logger
   * @return whether if the root files exist or not
   */
  private static boolean checkRootFiles(final @NotNull ILogger logger) {
    final String[] paths = {
      "/system/app/Superuser.apk",
      "/sbin/su",
      "/system/bin/su",
      "/system/xbin/su",
      "/data/local/xbin/su",
      "/data/local/bin/su",
      "/system/sd/xbin/su",
      "/system/bin/failsafe/su",
      "/data/local/su",
      "/su/bin/su",
      "/su/bin",
      "/system/xbin/daemonsu"
    };

    for (final String path : paths) {
      try {
        if (new File(path).exists()) {
          return true;
        }
      } catch (Exception e) {
        logger.log(
            SentryLevel.ERROR, e, "Error when trying to check if root file %s exists.", path);
      }
    }
    return false;
  }

  /**
   * this will check if SU(Super User) exist or not
   *
   * @param logger the Logger
   * @return whether su exists or not
   */
  private static boolean checkSUExist(final @NotNull ILogger logger) {
    Process process = null;
    final String[] su = {"/system/xbin/which", "su"};

    try {
      process = Runtime.getRuntime().exec(su);

      try (final BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
        return reader.readLine() != null;
      }
    } catch (Throwable e) {
      logger.log(SentryLevel.ERROR, "Error when trying to check if SU exists.", e);
      return false;
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  /**
   * some application hide the root status of the android device. This will check for those files
   *
   * @param context the Context
   * @return whether the root packages exist or not
   */
  private static boolean checkRootPackages(final @NotNull Context context) {
    final PackageManager pm = context.getPackageManager();
    if (pm != null) {
      final String[] packages = {
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu", // SuperSU
        "com.noshufou.android.su" // superuser
      };

      for (final String pkg : packages) {
        try {
          pm.getPackageInfo(pkg, 0);
          return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
      }
    }
    return false;
  }
}
