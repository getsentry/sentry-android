package io.sentry.android;

import android.content.Context;
import io.sentry.SentryOptions;
import io.sentry.internal.AndroidSerializer;

class AndroidOptionsInitializer {
  static void init(SentryOptions options, Context context) {
    // Firstly set the logger, if `debug=true` configured, logging can start asap.
    options.setLogger(new AndroidLogger());
    ManifestMetadataReader.applyMetadata(context, options);
    options.addEventProcessor(new DefaultAndroidEventProcessor(context));
    options.setSerializer(new AndroidSerializer());
  }
}
