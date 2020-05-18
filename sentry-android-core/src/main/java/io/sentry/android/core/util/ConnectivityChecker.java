package io.sentry.android.core.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ConnectivityChecker {

  private ConnectivityChecker() {}

  /**
   * Check whether the application has internet access at a point in time.
   *
   * @return true if the application has internet access
   */
  public static @Nullable Boolean isConnected(
      final @NotNull Context context, final @NotNull ILogger logger) {
    final ConnectivityManager connectivityManager = getConnectivityManager(context, logger);
    if (connectivityManager == null) {
      return null;
    }
    try {
      return isConnected(context, connectivityManager, logger);
    } catch (SecurityException ignored) {
      // if no permission, we assume its connected.
      return true;
    }
    // getActiveNetworkInfo might return null if VPN doesn't specify its
    // underlying network

    // when min. API 24, use:
    // connectivityManager.registerDefaultNetworkCallback(...)
  }

  @SuppressWarnings({"deprecation", "MissingPermission"})
  private static boolean isConnected(
      final @NotNull Context context,
      final @NotNull ConnectivityManager connectivityManager,
      final @NotNull ILogger logger) {
    final android.net.NetworkInfo activeNetwork =
        getActiveNetworkInfo(context, connectivityManager, logger);

    if (activeNetwork == null) {
      logger.log(SentryLevel.INFO, "NetworkInfo is null, there's no active network.");
      return false;
    }
    return activeNetwork.isConnected();
  }

  @SuppressWarnings({"deprecation", "MissingPermission"})
  private static @Nullable android.net.NetworkInfo getActiveNetworkInfo(
      final @NotNull Context context,
      final @NotNull ConnectivityManager connectivityManager,
      final @NotNull ILogger logger)
      throws SecurityException {
    if (!Permissions.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
      logger.log(SentryLevel.INFO, "No permission (ACCESS_NETWORK_STATE) to check network status.");
      throw new SecurityException("No ACCESS_NETWORK_STATE permission");
    }

    return connectivityManager.getActiveNetworkInfo();
  }

  /**
   * Check the connection type of the active network
   *
   * @param context the App. context
   * @param logger the logger from options
   * @return the connection type wifi, ethernet, cellular or null
   */
  @SuppressLint({"ObsoleteSdkInt", "MissingPermission"})
  public static @Nullable String getConnectionType(
      final @NotNull Context context, final @NotNull ILogger logger) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      final ConnectivityManager connectivityManager = getConnectivityManager(context, logger);
      if (connectivityManager == null) {
        return null;
      }
      if (!Permissions.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
        logger.log(
            SentryLevel.INFO, "No permission (ACCESS_NETWORK_STATE) to check network status.");
        return null;
      }
      final Network activeNetwork = connectivityManager.getActiveNetwork();
      if (activeNetwork == null) {
        logger.log(SentryLevel.INFO, "Network is null and cannot check network status");
        return null;
      }
      final NetworkCapabilities networkCapabilities =
          connectivityManager.getNetworkCapabilities(activeNetwork);
      if (networkCapabilities == null) {
        logger.log(SentryLevel.INFO, "NetworkCapabilities is null and cannot check network type");
        return null;
      }
      if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        return "wifi";
      }
      if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
        return "ethernet";
      }
      if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
        return "cellular";
      }
    }
    return null;
  }

  private static @Nullable ConnectivityManager getConnectivityManager(
      final @NotNull Context context, final @NotNull ILogger logger) {
    final ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivityManager == null) {
      logger.log(SentryLevel.INFO, "ConnectivityManager is null and cannot check network status");
    }
    return connectivityManager;
  }
}
