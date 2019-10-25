package io.sentry.android.ndk;

import java.io.File;

import io.sentry.core.SentryOptions;

public class SentryNdk {
  static {
    System.loadLibrary("sentry");
  }

  static {
    System.loadLibrary("sentry-android");
  }

  private static native void initSentryNative(SentryOptions cacheDirPath);

  public static void notifyNewSerializedEnvelope(String path) {
    System.out.println("envelope written to " + path);
  }

  public static void init(SentryOptions options) {
    initSentryNative(options);
  }
}
