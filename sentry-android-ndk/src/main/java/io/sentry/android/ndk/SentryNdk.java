package io.sentry.android.ndk;

public class SentryNdk {
  static {
    System.loadLibrary("libsentry");
  }

  public static void init() {}
}
