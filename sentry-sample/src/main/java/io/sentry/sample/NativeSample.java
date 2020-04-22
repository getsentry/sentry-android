package io.sentry.sample;

public class NativeSample {
  public static native void crash();

  public static native void message();

  static {
    System.loadLibrary("log");
    System.loadLibrary("sentry");
    System.loadLibrary("native-sample");
  }
}
