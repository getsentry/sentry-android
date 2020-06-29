package io.sentry.console;

import io.sentry.core.Sentry;

public final class Main {

  public static void main(String[] args) {
    Sentry.init(options -> {
      options.setDebug(true);
      options.setDsn("https://f7f320d5c3a54709be7b28e0f2ca7081@sentry.io/1808954");
        options.setRelease("io.sentry.console-sample@2.2.0+1");
        options.setDistinctId("ab96942b-1e54-45c0-99d5-c59d5ab5e959");
    });
    System.out.println("capture test");
    Sentry.captureMessage("test");

//    System.console().readLine();
    try {
      Thread.sleep(100000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
