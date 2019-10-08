package io.sentry.android.ndk;

public class SentryNdk {
  static {
    System.loadLibrary("sentry");
  }

  public static void init() {}
}
