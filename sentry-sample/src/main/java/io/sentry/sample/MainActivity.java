package io.sentry.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.sentry.core.Sentry;
import io.sentry.core.protocol.User;
import io.sentry.sample.databinding.ActivityMainBinding;
import java.util.Collections;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

    Timber.i("Sentry.isEnabled() = %s", Sentry.isEnabled());

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

    setContentView(binding.getRoot());
  }
}
