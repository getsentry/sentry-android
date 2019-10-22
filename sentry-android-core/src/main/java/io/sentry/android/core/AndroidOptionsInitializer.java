package io.sentry.android.core;

import android.content.Context;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class AndroidOptionsInitializer {
  static void init(SentryOptions options, Context context) {
    // Firstly set the logger, if `debug=true` configured, logging can start asap.
    options.setLogger(new AndroidLogger());

    // TODO this needs to fetch the data from somewhere - defined at build time?
    options.setSentryClientName("sentry-android/0.0.1");
    ManifestMetadataReader.applyMetadata(context, options);

    if (options.isEnableNdk()) {
      try {
        // TODO: Create Integrations interface and use that to initialize NDK
        Class cls = Class.forName("io.sentry.android.ndk.SentryNdk");

        // XXX: temporary hack
        String cacheDirPath = context.getCacheDir().getAbsolutePath() + "/sentry-envelopes";
        File f = new File(cacheDirPath);
        f.mkdirs();

        Method method = cls.getMethod("init", SentryOptions.class, String.class);
        Object[] args = new Object[2];
        args[0] = options;
        args[1] = cacheDirPath;
        method.invoke(null, args);
      } catch (ClassNotFoundException exc) {
        options.getLogger().log(SentryLevel.ERROR, "Failed to load SentryNdk.");
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        options.getLogger().log(SentryLevel.ERROR, "Failed to initialize SentryNdk.", e);
      }
    }

    options.addEventProcessor(new DefaultAndroidEventProcessor(context));
    options.setSerializer(new AndroidSerializer());
  }
}
