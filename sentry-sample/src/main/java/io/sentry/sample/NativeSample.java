package io.sentry.sample;

public class NativeSample {
  public static native void crash();

  public static native void ndk_integration();

  static {
    System.loadLibrary("native-sample");
  }

  public static void verificationEvent() {
    ndk_integration();
  }
}
