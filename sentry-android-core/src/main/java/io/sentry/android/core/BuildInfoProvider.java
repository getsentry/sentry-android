package io.sentry.android.core;

import android.os.Build;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class BuildInfoProvider implements IBuildInfoProvider {

  private static final BuildInfoProvider instance = new BuildInfoProvider();

  public static IBuildInfoProvider getInstance() {
    return instance;
  }

  private BuildInfoProvider() {}

  @Override
  public int getSdkInfoVersion() {
    return Build.VERSION.SDK_INT;
  }
}
