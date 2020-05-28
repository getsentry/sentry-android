package io.sentry.android.ndk;

import io.sentry.core.SentryOptions;
import org.jetbrains.annotations.ApiStatus;

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

  private static native void initSentryNative(SentryOptions options);

  public static void init(SentryOptions options) {
    initSentryNative(options);
  }
}
