package io.sentry.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.sentry.android.core.FragmentLifeCycleIntegration;

public class MyFragment extends Fragment {

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.my_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (getFragmentManager() != null) {
      getFragmentManager() // added support lib in version 25.1.0
          .registerFragmentLifecycleCallbacks(new FragmentLifeCycleIntegration(), true);
    }
  }
}
