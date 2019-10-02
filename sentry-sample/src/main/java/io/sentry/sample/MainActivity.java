package io.sentry.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Timber.plant(new Timber.DebugTree());

    Timber.i("Sentry.isEnabled() = %s", Sentry.isEnabled());

    Sentry.captureEvent(new SentryEvent());
    Sentry.captureMessage("test");
    Sentry.captureException(new Exception("test"));
  }
}
