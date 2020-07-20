package io.sentry.android.ndk;

import io.sentry.core.SentryOptions;
import io.sentry.core.protocol.SdkVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class SentryNdk {

  private SentryNdk() {}

  static {
    // On older Android versions, it was necessary to manually call "`System.loadLibrary` on all
    // transitive dependencies before loading [the] main library."
    // The dependencies of `libsentry.so` are currently `lib{c,m,dl,log}.so`.
    // See
    // https://android.googlesource.com/platform/bionic/+/master/android-changes-for-ndk-developers.md#changes-to-library-dependency-resolution
    System.loadLibrary("log");
    System.loadLibrary("sentry");
    System.loadLibrary("sentry-android");
  }

  private static native void initSentryNative(@NotNull final SentryOptions options);

  public static void init(@NotNull final SentryOptions options) {
    addPackage(options);
    initSentryNative(options);
  }

  private static void addPackage(@NotNull final SentryOptions options) {
    final SdkVersion sdkVersion = options.getSdkVersion();
    if (sdkVersion == null) {
      return;
    }
    sdkVersion.addPackage("maven:sentry-android-ndk", BuildConfig.VERSION_NAME);
  }
}
