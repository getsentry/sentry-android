package io.sentry.core;

/** Registers hook that closes {@link Sentry Sentry SDK} when main thread shuts down. */
public final class ShutdownHookIntegration implements Integration {

  @Override
  public void register(IHub hub, SentryOptions options) {
    Runtime.getRuntime().addShutdownHook(new Thread(Sentry::close));
  }
}
