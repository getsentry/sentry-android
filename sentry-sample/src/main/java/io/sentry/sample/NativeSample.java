package io.sentry.sample;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public class NativeSample {
  public static native void crash();

  public static native void message();

  static {
    System.loadLibrary("native-sample");
  }


  @SuppressLint("NewApi")
  public static String test() {
    Map<String, String> maps = new HashMap<>();
    maps.put("test", "test");

    // RequiresApi API 24, so run on <= 23 (max Android 6) to simulate a NoClassDefFoundError, which is caught by UncaughtExceptionHandlerIntegration.uncaughtException
    maps.forEach((s, s2) -> {
      System.out.println("s = " + s + " s2 " + s2);
    });

    return "test"; // (jstring result) will be null anyway as forEach throws NoClassDefFoundError
  }
}
