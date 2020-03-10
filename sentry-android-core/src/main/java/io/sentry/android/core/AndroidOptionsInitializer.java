package io.sentry.android.core;

import android.content.Context;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import java.io.File;
import org.jetbrains.annotations.NotNull;

final class AndroidOptionsInitializer {
  private AndroidOptionsInitializer() {}

  static void init(SentryAndroidOptions options, Context context) {
    init(options, context, new AndroidLogger());
  }

  static void init(SentryAndroidOptions options, Context context, final @NotNull ILogger logger) {
    // Firstly set the logger, if `debug=true` configured, logging can start asap.
    options.setLogger(logger);

    options.setSentryClientName(BuildConfig.SENTRY_CLIENT_NAME + "/" + BuildConfig.VERSION_NAME);

    ManifestMetadataReader.applyMetadata(context, options);
    initializeCacheDirs(context, options);
    setDefaultInApp(context, options);

    // Integrations are registered in the same order. Watch outbox before adding NDK:
    options.addIntegration(EnvelopeFileObserverIntegration.getOutboxFileObserver());
    options.addIntegration(new NdkIntegration());
    options.addIntegration(new AnrIntegration());

    options.addEventProcessor(new DefaultAndroidEventProcessor(context, options));
    options.setSerializer(new AndroidSerializer(options.getLogger()));

    options.setTransportGate(new AndroidTransportGate(context, options.getLogger()));
  }

  private static void setDefaultInApp(Context context, final @NotNull SentryOptions options) {
    String packageName = context.getPackageName();
    if (packageName != null && !packageName.startsWith("android.")) {
      options.addInAppInclude(packageName);
    }
  }

  private static void initializeCacheDirs(Context context, SentryOptions options) {
    File cacheDir = new File(context.getCacheDir(), "sentry");
    cacheDir.mkdirs();
    options.setCacheDirPath(cacheDir.getAbsolutePath());

    if (options.getOutboxPath() != null) {
      new File(options.getOutboxPath()).mkdirs();
    } else {
      options.getLogger().log(SentryLevel.WARNING, "No outbox dir path is defined in options.");
    }
    if (options.getSessionsPath() != null) {
      new File(options.getSessionsPath()).mkdirs();
    } else {
      options.getLogger().log(SentryLevel.WARNING, "No session dir path is defined in options.");
    }
  }
}
