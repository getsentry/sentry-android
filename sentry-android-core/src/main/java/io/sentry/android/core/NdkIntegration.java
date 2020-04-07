package io.sentry.android.core;

import android.os.Build;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import java.lang.reflect.Method;

/** Enables the NDK error reporting for Android */
public final class NdkIntegration implements Integration {
  private boolean isNdkAvailable() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }

  @Override
  public final void register(IHub hub, SentryOptions options) {
    boolean enabled = options.isEnableNdk() && isNdkAvailable();
    options.getLogger().log(SentryLevel.DEBUG, "NdkIntegration enabled: %s", enabled);

    // Note: `hub` isn't used here because the NDK integration writes files to disk which are picked
    // up by another integration (EnvelopeFileObserverIntegration).
    if (enabled) {
      try {
        Class<?> cls = Class.forName("io.sentry.android.ndk.SentryNdk");

        Method method = cls.getMethod("init", SentryOptions.class);
        Object[] args = new Object[1];
        args[0] = options;
        method.invoke(null, args);

        options.getLogger().log(SentryLevel.DEBUG, "NdkIntegration installed.");
      } catch (ClassNotFoundException e) {
        options.setEnableNdk(false);
        options.getLogger().log(SentryLevel.ERROR, "Failed to load SentryNdk.", e);
      } catch (UnsatisfiedLinkError e) {
        options.setEnableNdk(false);
        options
            .getLogger()
            .log(SentryLevel.ERROR, "Failed to load (UnsatisfiedLinkError) SentryNdk.", e);
      } catch (Throwable e) {
        options.setEnableNdk(false);
        options.getLogger().log(SentryLevel.ERROR, "Failed to initialize SentryNdk.", e);
      }
    } else {
      options.setEnableNdk(false);
    }
  }
}
