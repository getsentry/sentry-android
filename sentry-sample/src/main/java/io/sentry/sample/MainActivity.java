package io.sentry.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.sentry.core.Sentry;
import io.sentry.core.protocol.User;
import io.sentry.sample.databinding.ActivityMainBinding;

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
          User user = new User();
          user.setUsername("username");
          Sentry.setUser(user);
          Sentry.setTag("tag", "tag");
          Sentry.captureException(new Exception("Some exception with scope and breadcrumbs."));
        });

    binding.nativeCrash.setOnClickListener(view -> NativeSample.crash());

    binding.nativeCapture.setOnClickListener(view -> NativeSample.message());

    binding.anr.setOnClickListener(
        view -> {
          // Try cause ANR by blocking for 10 seconds.
          // By default the SDK sends an event if blocked by at least 5 seconds.
          // you must keep clicking on the UI, so OS will detect that the UI is not responding.
          // NOTE: By default it doesn't raise if the debugger is attached. That can also be
          // configured.
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });

    setContentView(binding.getRoot());
  }
}
