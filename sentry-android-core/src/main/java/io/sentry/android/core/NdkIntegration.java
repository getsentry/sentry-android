package io.sentry.android.core;

import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/** Enables the NDK error reporting for Android */
public final class NdkIntegration implements Integration {

  public static final String SENTRY_NDK_CLASS_NAME = "io.sentry.android.ndk.SentryNdk";

  @Override
  public final void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    Objects.requireNonNull(hub, "Hub is required");
    Objects.requireNonNull(options, "SentryOptions is required");

    final boolean enabled = options.isEnableNdk();
    options.getLogger().log(SentryLevel.DEBUG, "NdkIntegration enabled: %s", enabled);

    // Note: `hub` isn't used here because the NDK integration writes files to disk which are picked
    // up by another integration (EnvelopeFileObserverIntegration).
    if (enabled) {
      try {
        final Class<?> cls = Class.forName(SENTRY_NDK_CLASS_NAME);

        final Method method = cls.getMethod("init", SentryOptions.class);
        final Object[] args = new Object[1];
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
