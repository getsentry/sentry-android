package io.sentry.android.core;

import android.content.Context;
import io.sentry.core.ILogger;
import io.sentry.core.OptionsContainer;
import io.sentry.core.Sentry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/** Sentry initialization class */
public final class SentryAndroid {

  private SentryAndroid() {}

  /**
   * Sentry initialization method if auto-init is disabled
   *
   * @param context Application. context
   */
  public static void init(@NotNull final Context context) {
    init(context, new AndroidLogger());
  }

  static void init(@NotNull final Context context, @NotNull ILogger logger) {
    try {
      Sentry.init(
          OptionsContainer.create(SentryAndroidOptions.class),
          options -> AndroidOptionsInitializer.init(options, context, logger));
    } catch (IllegalAccessException e) {
      // This is awful. Should we have this all on the interface and let the caller deal with these?
      // They mean bug in the SDK.
      throw new RuntimeException("Failed to initialize Sentry's SDK", e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Failed to initialize Sentry's SDK", e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Failed to initialize Sentry's SDK", e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Failed to initialize Sentry's SDK", e);
    }
  }
}
