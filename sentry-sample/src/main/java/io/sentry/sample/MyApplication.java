package io.sentry.sample;

import android.app.Application;
import android.os.StrictMode;
import io.sentry.core.Sentry;

// import io.sentry.android.core.SentryAndroid;

/** Apps. main Application. */
public class MyApplication extends Application {

  @Override
  public void onCreate() {
    strictMode();
    super.onCreate();

    // Example how to initialize the SDK manually which allows access to SentryOptions callbacks.
    // Make sure you disable the auto init via manifest meta-data: io.sentry.auto-init=false
    // SentryAndroid.init(
    // this,
    // options -> {
    //   options.setBeforeSend(event -> {
    //     event.setTag("sample-key", "before-send");
    //   });
    //   options.setAnrTimeoutIntervalMillis(2000);
    // });
    Sentry.init(
        options -> {
          options.setDebug(true);
          options.setDsn("https://f7f320d5c3a54709be7b28e0f2ca7081@sentry.io/1808954");
          options.setRelease("io.sentry.100sessions-sample@4.0.15+1");
          options.setDistinctId("ab96942b-1e54-45c0-99d5-c59d5ab5e959");
        });
  }

  private void strictMode() {
    //    https://developer.android.com/reference/android/os/StrictMode
    //    StrictMode is a developer tool which detects things you might be doing by accident and
    //    brings them to your attention so you can fix them.
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(
          new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }
  }
}
