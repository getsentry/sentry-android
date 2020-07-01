package io.sentry.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.sentry.core.Sentry;
import io.sentry.core.SentryEvent;
import io.sentry.core.SentryLevel;
import io.sentry.core.protocol.User;
import io.sentry.sample.databinding.ActivityMainBinding;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

    binding.crashFromJava.setOnClickListener(
        view -> {
          throw new RuntimeException("Uncaught Exception from Java.");
        });

    binding.sendMessage.setOnClickListener(view -> Sentry.captureMessage("Some message."));

    binding.captureException.setOnClickListener(
        view ->
            Sentry.captureException(
                new Exception(new Exception(new Exception("Some exception.")))));

    binding.breadcrumb.setOnClickListener(
        view -> {
          Sentry.addBreadcrumb("Breadcrumb");
          Sentry.setExtra("extra", "extra");
          Sentry.setFingerprint(Collections.singletonList("fingerprint"));
          Sentry.setTransaction("transaction");
          User user = new User();
          user.setUsername("username");
          Sentry.setUser(user);
          Sentry.setTag("tag", "tag");
          Sentry.captureException(new Exception("Some exception with scope."));
        });

    binding.nativeCrash.setOnClickListener(view -> NativeSample.crash());

    binding.nativeCapture.setOnClickListener(view -> NativeSample.message());

    binding.anr.setOnClickListener(
        view -> {
          // Try cause ANR by blocking for 2.5 seconds.
          // By default the SDK sends an event if blocked by at least 4 seconds.
          // The time was configurable (see manifest) to 1 second for demo purposes.
          // NOTE: By default it doesn't raise if the debugger is attached. That can also be
          // configured.
          try {
            Thread.sleep(2500);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });

    binding.sessions.setOnClickListener(
        view -> {
          for (int i = 0; i < 100; i++) {
            System.out.println("session start: i = " + i);

            // if all capture event and exceptions are commented, it means 100% healthy
            Sentry.startSession();

            // only crashed sessions
            //                                    SentryEvent event = new SentryEvent(new
            // Throwable());
            //                                    event.setLevel(SentryLevel.FATAL);
            //                                    Sentry.captureEvent(event);

            // 100% errored
            //                        Sentry.captureException(new Throwable());

            // 50% healthy 50% errored
            //                        if (i % 2 == 0) {
            //                            Sentry.captureException(new Throwable());
            //                        }

            // 50% healthy and 50% crashed
            if (i % 2 == 0) {
              SentryEvent event = new SentryEvent(new Throwable());
              event.setLevel(SentryLevel.FATAL);
              Sentry.captureEvent(event);
            }

            // 50% crashed and 50% errored
            //                        if (i % 2 == 0) {
            //                            SentryEvent event = new SentryEvent(new Throwable());
            //                            event.setLevel(SentryLevel.FATAL);
            //                            Sentry.captureEvent(event);
            //                        } else {
            //                            Sentry.captureException(new Throwable());
            //                        }

            // for having at least 1s duration
            //                        try {
            //                            Thread.sleep(1000);
            //                        } catch (InterruptedException e) {
            //                            System.out.println("InterruptedException");
            //                        }

            Sentry.endSession();
            System.out.println("session end: i = " + i);
          }
        });

    setContentView(binding.getRoot());
  }
}
