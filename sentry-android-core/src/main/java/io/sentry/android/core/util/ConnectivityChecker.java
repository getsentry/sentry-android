package io.sentry.android.core.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import io.sentry.android.core.IBuildInfoProvider;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ConnectivityChecker {

  public enum Status {
    CONNECTED,
    NOT_CONNECTED,
    NO_PERMISSION,
    UNKNOWN
  }

  private ConnectivityChecker() {}

  /**
   * Return the Connection status
   *
   * @return the ConnectionStatus
   */
  public static @NotNull ConnectivityChecker.Status getConnectionStatus(
      final @NotNull Context context, final @NotNull ILogger logger) {
    final ConnectivityManager connectivityManager = getConnectivityManager(context, logger);
    if (connectivityManager == null) {
      return Status.UNKNOWN;
    }
    return getConnectionStatus(context, connectivityManager, logger);
    // getActiveNetworkInfo might return null if VPN doesn't specify its
    // underlying network

    // when min. API 24, use:
    // connectivityManager.registerDefaultNetworkCallback(...)
  }

  /**
   * Return the Connection status
   *
   * @param context the Context
   * @param connectivityManager the ConnectivityManager
   * @param logger the Logger
   * @return true if connected or no permission to check, false otherwise
   */
  @SuppressWarnings({"deprecation", "MissingPermission"})
  private static @NotNull ConnectivityChecker.Status getConnectionStatus(
      final @NotNull Context context,
      final @NotNull ConnectivityManager connectivityManager,
      final @NotNull ILogger logger) {
    if (!Permissions.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
      logger.log(SentryLevel.INFO, "No permission (ACCESS_NETWORK_STATE) to check network status.");
      return Status.NO_PERMISSION;
    }
    final android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

    if (activeNetworkInfo == null) {
      logger.log(SentryLevel.INFO, "NetworkInfo is null, there's no active network.");
      return Status.NOT_CONNECTED;
    }
    return activeNetworkInfo.isConnected() ? Status.CONNECTED : Status.NOT_CONNECTED;
  }

  /**
   * Check the connection type of the active network
   *
   * @param context the App. context
   * @param logger the logger from options
   * @param buildInfoProvider the BuildInfoProvider provider
   * @return the connection type wifi, ethernet, cellular or null
   */
  @SuppressLint({"ObsoleteSdkInt", "MissingPermission", "NewApi"})
  public static @Nullable String getConnectionStatus(
      final @NotNull Context context,
      final @NotNull ILogger logger,
      final @NotNull IBuildInfoProvider buildInfoProvider) {
    final ConnectivityManager connectivityManager = getConnectivityManager(context, logger);
    if (connectivityManager == null) {
      return null;
    }
    if (!Permissions.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
      logger.log(SentryLevel.INFO, "No permission (ACCESS_NETWORK_STATE) to check network status.");
      return null;
    }

    NetworkCapabilities networkCapabilities = null;
    if (buildInfoProvider.getSdkInfoVersion() >= Build.VERSION_CODES.M) {
      final Network activeNetwork = connectivityManager.getActiveNetwork();
      if (activeNetwork == null) {
        logger.log(SentryLevel.INFO, "Network is null and cannot check network status");
        return null;
      }
      networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
    } else {
      // NetworkInfo won't be able to detect if multiple networks, so fallback to getAllNetworks
      final Network[] allNetworks = connectivityManager.getAllNetworks();

      for (final Network network : allNetworks) {
        networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        if (networkCapabilities == null) {
          continue;
        }
        break;
      }
    }
    if (networkCapabilities == null) {
      logger.log(SentryLevel.INFO, "NetworkCapabilities is null and cannot check network type");
      return null;
    }
    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
      return "ethernet";
    }
    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
      return "wifi";
    }
    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
      return "cellular";
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
