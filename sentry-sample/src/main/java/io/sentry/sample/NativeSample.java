package io.sentry.sample;

public class NativeSample {
  public static native void crash();

  public static native String message();

  static {
    System.loadLibrary("native-sample");
  }
}
