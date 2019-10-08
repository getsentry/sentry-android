package io.sentry.android.ndk;

public class SentryNdk {
  static {
    System.loadLibrary("sentry");
  }

  static {
    System.loadLibrary("sentry-android");
  }

  public static native void example();

  public static void init() {
    // Java_example
    example();
  }
}
