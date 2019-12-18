package io.sentry.android.core;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks;
import io.sentry.core.Sentry;

// its called integration, but might not be possible to hook it automatically, each fragment has its
// own fragmentmanager
public final class FragmentLifeCycleIntegration extends FragmentLifecycleCallbacks {

  @Override
  public void onFragmentCreated(
      @NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
    Sentry.setTag("fragment", f.getClass().getName());
  }

  @Override
  public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
    Sentry.addBreadcrumb(f.getClass().getName(), "started");
  }

  // many other lifecycles
}
