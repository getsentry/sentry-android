package io.sentry.android.core;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.*;
import android.util.DisplayMetrics;
import io.sentry.core.EventProcessor;
import io.sentry.core.SentryEvent;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.protocol.Device;
import io.sentry.core.protocol.SdkVersion;
import io.sentry.core.util.Objects;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DefaultAndroidEventProcessor implements EventProcessor {
  Context context;
  SentryOptions options;

  public DefaultAndroidEventProcessor(Context context, SentryOptions options) {
    Objects.requireNonNull(context, "The application context is required.");
    Objects.requireNonNull(options, "The SentryOptions is required.");

    this.context = context.getApplicationContext();
    this.options = options;
  }

  @Override
  public SentryEvent process(SentryEvent event) {
    if (event.getSdkVersion() == null) {
      SdkVersion sdkVersion = new SdkVersion();
      sdkVersion.setName("android");
      // version, don't we have getRelease() for that?
      // packages, which packages?

      event.setSdkVersion(sdkVersion);
    }
    PackageInfo packageInfo = getPackageInfo();
    if (packageInfo != null) {
      if (event.getRelease() == null) {
        event.setRelease(packageInfo.packageName + "-" + packageInfo.versionName);
      }
      if (event.getDist() == null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          event.setDist(Long.toString(packageInfo.getLongVersionCode()));
        } else {
          event.setDist(getVersionCode(packageInfo));
        }
      }
    }
    // sentry interface? we dont need anymore, only user

    // proguard uuids? check if we need sentry-debug-meta.properties
    //

    if (event.getContexts().getDevice() == null) {
      event.getContexts().setDevice(getDevice());
    }

    return event;
  }

  @SuppressWarnings("deprecation")
  private String getVersionCode(PackageInfo packageInfo) {
    return Integer.toString(packageInfo.versionCode);
  }

  @SuppressWarnings("deprecation")
  private String getAbi() {
    return Build.CPU_ABI;
  }

  /**
   * Return the Application's PackageInfo if possible, or null.
   *
   * @return the Application's PackageInfo if possible, or null
   */
  private PackageInfo getPackageInfo() {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting package info.", e);
      return null;
    }
  }

  @SuppressWarnings("ObsoleteSdkInt")
  private void setArchitectures(Device device) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      device.setArchitectures(Build.SUPPORTED_ABIS);
    } else {
      device.setArchitecture(getAbi());
    }
  }

  @SuppressWarnings("ObsoleteSdkInt")
  private Long getMemorySize(ActivityManager.MemoryInfo memInfo) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      return memInfo.totalMem;
    }
    // TODO: find a way?
    return null;
  }

  // we can get some inspiration here
  // https://github.com/flutter/plugins/blob/master/packages/device_info/android/src/main/java/io/flutter/plugins/deviceinfo/DeviceInfoPlugin.java
  private Device getDevice() {
    Device device = new Device();
    // name of what? maybe from the BluetoothAdapter
    device.setManufacturer(Build.MANUFACTURER);
    device.setBrand(Build.BRAND);
    device.setFamily(getFamily());
    device.setModel(Build.MODEL);
    device.setModelId(Build.ID); // or DISPLAY which is user friendly id
    setArchitectures(device);

    Intent batteryIntent = getBatteryIntent();
    if (batteryIntent != null) {
      device.setBatteryLevel(getBatteryLevel(batteryIntent));
      device.setCharging(isCharging(batteryIntent));
    }
    device.setOnline(isConnected());
    device.setOrientation(getOrientation());
    device.setSimulator(isEmulator());

    ActivityManager.MemoryInfo memInfo = getMemInfo();
    if (memInfo != null) {
      device.setMemorySize(getMemorySize(memInfo));
      device.setFreeMemory(memInfo.availMem);
      device.setLowMemory(memInfo.lowMemory);
      // device.setUsableMemory(); // TODO: check that
      // do we need threshold?
    }

    StatFs internalStorageStat = getInternalStorageStat();
    device.setStorageSize(getTotalInternalStorage(internalStorageStat));
    device.setFreeStorage(getUnusedInternalStorage(internalStorageStat));

    StatFs externalStorageStat = getExternalStorageStat();
    if (externalStorageStat != null) {
      device.setExternalStorageSize(getTotalExternalStorage(externalStorageStat));
      device.setExternalFreeStorage(getUnusedExternalStorage(externalStorageStat));
    }

    DisplayMetrics displayMetrics = getDisplayMetrics();
    if (displayMetrics != null) {
      device.setScreenResolution(getResolution(displayMetrics));
      device.setScreenDensity(displayMetrics.density);
      device.setScreenDpi(displayMetrics.densityDpi);
    }

    device.setBootTime(getBootTime());
    device.setTimezone(getTimeZone());

    return device;
  }

  private TimeZone getTimeZone() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      LocaleList locales = context.getResources().getConfiguration().getLocales();
      if (!locales.isEmpty()) {
        Locale locale = locales.get(0);
        return Calendar.getInstance(locale).getTimeZone();
      }
    }
    return Calendar.getInstance().getTimeZone();
  }

  private Date getBootTime() {
    // should it be a Date?
    return new Date(
        System.currentTimeMillis()
            - SystemClock.elapsedRealtime()
            + new Date().getTime()); // check Date().getTime() and if its UTC
  }

  private String getResolution(DisplayMetrics displayMetrics) {
    // do we need to calculate the density in here?
    int largestSide = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
    int smallestSide = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
    return largestSide + "x" + smallestSide;
  }

  /**
   * Get MemoryInfo object representing the memory state of the application.
   *
   * @return MemoryInfo object representing the memory state of the application
   */
  private ActivityManager.MemoryInfo getMemInfo() {
    try {
      ActivityManager actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
      ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
      if (actManager != null) {
        actManager.getMemoryInfo(memInfo);
        return memInfo;
      }
      options.getLogger().log(SentryLevel.INFO, "Error getting MemoryInfo.");
      return null;
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting MemoryInfo.", e);
      return null;
    }
  }

  private Intent getBatteryIntent() {
    return context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
  }

  /**
   * Fake the device family by using the first word in the Build.MODEL. Works well in most cases...
   * "Nexus 6P" -> "Nexus", "Galaxy S7" -> "Galaxy".
   *
   * @return family name of the device, as best we can tell
   */
  private String getFamily() {
    try {
      return Build.MODEL
          .split(" ")[
          0]; // might be possible to do it better, should we change that or keep compatibility?
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting device family.", e);
      return null;
    }
  }

  /**
   * Get the device's current battery level (as a percentage of total).
   *
   * @return the device's current battery level (as a percentage of total), or null if unknown
   */
  private Float getBatteryLevel(Intent batteryIntent) {
    try {
      int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

      if (level == -1 || scale == -1) {
        return null;
      }

      // CHECKSTYLE.OFF: MagicNumber
      float percentMultiplier = 100.0f;
      // CHECKSTYLE.ON: MagicNumber

      return ((float) level / (float) scale) * percentMultiplier;
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting device battery level.", e);
      return null;
    }
  }

  /**
   * Checks whether or not the device is currently plugged in and charging, or null if unknown.
   *
   * @return whether or not the device is currently plugged in and charging, or null if unknown
   */
  private Boolean isCharging(Intent batteryIntent) {
    try {
      int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
      return plugged == BatteryManager.BATTERY_PLUGGED_AC
          || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting device charging state.", e);
      return null;
    }
  }

  /**
   * Check whether the application has internet access at a point in time.
   *
   * @return true if the application has internet access
   */
  private Boolean isConnected() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivityManager == null) {
      options
          .getLogger()
          .log(SentryLevel.INFO, "ConnectivityManager is null and cannot check network status");
      return null;
    }
    return getActiveNetworkInfo(connectivityManager);
    // does the connection type matters? wifi, lte...
    // do we care about VPNs? getActiveNetworkInfo might return null if VPN doesn't specify its
    // underlying network

    // when min. API 24, use:
    // connectivityManager.registerDefaultNetworkCallback(...)
  }

  @SuppressWarnings("deprecation")
  private Boolean getActiveNetworkInfo(ConnectivityManager connectivityManager) {
    // do not import class or deprecation lint will throw
    android.net.NetworkInfo activeNetwork =
        connectivityManager.getActiveNetworkInfo(); // it requires ACCESS_NETWORK_STATE

    if (activeNetwork != null) {
      return activeNetwork.isConnected();
    }
    options
        .getLogger()
        .log(SentryLevel.INFO, "NetworkInfo is null and cannot check network status");
    return null;
  }

  /**
   * Get the device's current screen orientation.
   *
   * @return the device's current screen orientation, or null if unknown
   */
  private Device.DeviceOrientation getOrientation() {
    try {
      switch (context.getResources().getConfiguration().orientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
          return Device.DeviceOrientation.LANDSCAPE;
        case Configuration.ORIENTATION_PORTRAIT:
          return Device.DeviceOrientation.PORTRAIT;
        default:
          options
              .getLogger()
              .log(SentryLevel.INFO, "No device orientation available (ORIENTATION_UNDEFINED)");
          return null;
      }
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting device orientation.", e);
      return null;
    }
  }

  /**
   * Check whether the application is running in an emulator.
   * https://github.com/flutter/plugins/blob/master/packages/device_info/android/src/main/java/io/flutter/plugins/deviceinfo/DeviceInfoPlugin.java#L105
   *
   * @return true if the application is running in an emulator, false otherwise
   */
  private Boolean isEmulator() {
    try {
      return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
          || Build.FINGERPRINT.startsWith("generic")
          || Build.FINGERPRINT.startsWith("unknown")
          || Build.HARDWARE.contains("goldfish")
          || Build.HARDWARE.contains("ranchu")
          || Build.MODEL.contains("google_sdk")
          || Build.MODEL.contains("Emulator")
          || Build.MODEL.contains("Android SDK built for x86")
          || Build.MANUFACTURER.contains("Genymotion")
          || Build.PRODUCT.contains("sdk_google")
          || Build.PRODUCT.contains("google_sdk")
          || Build.PRODUCT.contains("sdk")
          || Build.PRODUCT.contains("sdk_x86")
          || Build.PRODUCT.contains("vbox86p")
          || Build.PRODUCT.contains("emulator")
          || Build.PRODUCT.contains("simulator");
    } catch (Exception e) {
      options
          .getLogger()
          .log(
              SentryLevel.ERROR,
              "Error checking whether application is running in an emulator.",
              e);
      return null;
    }
  }

  /**
   * Get the total amount of internal storage, in bytes.
   *
   * @return the total amount of internal storage, in bytes
   */
  private Long getTotalInternalStorage(StatFs stat) {
    try {
      long blockSize = getBlockSizeLong(stat);
      long totalBlocks = getBlockCountLong(stat);
      return totalBlocks * blockSize;
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting total internal storage amount.", e);
      return null;
    }
  }

  @SuppressWarnings("ObsoleteSdkInt")
  private long getBlockSizeLong(StatFs stat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      return stat.getBlockSizeLong();
    }
    return getBlockSizeDep(stat);
  }

  @SuppressWarnings("deprecation")
  private int getBlockSizeDep(StatFs stat) {
    return stat.getBlockSize();
  }

  @SuppressWarnings("ObsoleteSdkInt")
  private long getBlockCountLong(StatFs stat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      return stat.getBlockCountLong();
    }
    return getBlockCountDep(stat);
  }

  @SuppressWarnings("deprecation")
  private int getBlockCountDep(StatFs stat) {
    return stat.getBlockCount();
  }

  @SuppressWarnings("ObsoleteSdkInt")
  private long getAvailableBlocksLong(StatFs stat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      return stat.getAvailableBlocksLong();
    }
    return getAvailableBlocksDep(stat);
  }

  @SuppressWarnings("deprecation")
  private int getAvailableBlocksDep(StatFs stat) {
    return stat.getAvailableBlocks();
  }

  /**
   * Get the unused amount of internal storage, in bytes.
   *
   * @return the unused amount of internal storage, in bytes
   */
  private Long getUnusedInternalStorage(StatFs stat) {
    try {
      long blockSize = getBlockSizeLong(stat);
      long availableBlocks = getAvailableBlocksLong(stat);
      return availableBlocks * blockSize;
    } catch (Exception e) {
      options
          .getLogger()
          .log(SentryLevel.ERROR, "Error getting unused internal storage amount.", e);
      return null;
    }
  }

  private StatFs getInternalStorageStat() {
    File path = Environment.getDataDirectory();
    return new StatFs(path.getPath());
  }

  private StatFs getExternalStorageStat() {
    if (!isExternalStorageMounted()) {
      File path = context.getExternalFilesDir(null);
      if (path != null) {
        return new StatFs(path.getPath());
      }
      options.getLogger().log(SentryLevel.INFO, "Not possible to read external files directory");
      return null;
    }
    options.getLogger().log(SentryLevel.INFO, "External storage is not mounted or emulated.");
    return null;
  }

  /**
   * Get the total amount of external storage, in bytes, or null if no external storage is mounted.
   *
   * @return the total amount of external storage, in bytes, or null if no external storage is
   *     mounted
   */
  private Long getTotalExternalStorage(StatFs stat) {
    try {
      long blockSize = getBlockSizeLong(stat);
      long totalBlocks = getBlockCountLong(stat);
      return totalBlocks * blockSize;
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting total external storage amount.", e);
      return null;
    }
  }

  private boolean isExternalStorageMounted() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        && !Environment.isExternalStorageEmulated();
  }

  /**
   * Get the unused amount of external storage, in bytes, or null if no external storage is mounted.
   *
   * @return the unused amount of external storage, in bytes, or null if no external storage is
   *     mounted
   */
  private Long getUnusedExternalStorage(StatFs stat) {
    try {
      long blockSize = getBlockSizeLong(stat);
      long availableBlocks = getAvailableBlocksLong(stat);
      return availableBlocks * blockSize;
    } catch (Exception e) {
      options
          .getLogger()
          .log(SentryLevel.ERROR, "Error getting unused external storage amount.", e);
      return null;
    }
  }

  /**
   * Get the DisplayMetrics object for the current application.
   *
   * @return the DisplayMetrics object for the current application
   */
  private DisplayMetrics getDisplayMetrics() {
    try {
      return context.getResources().getDisplayMetrics();
    } catch (Exception e) {
      options.getLogger().log(SentryLevel.ERROR, "Error getting DisplayMetrics.", e);
      return null;
    }
  }
}
