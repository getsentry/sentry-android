package io.sentry.core;

import io.sentry.core.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Registers hook that closes {@link Hub} when main thread shuts down. */
public final class ShutdownHookIntegration implements Integration {

  private final @NotNull Runtime runtime;

  public ShutdownHookIntegration(final @NotNull Runtime runtime) {
    this.runtime = Objects.requireNonNull(runtime, "Runtime is required");
  }

  @Override
  public void register(@NotNull IHub hub, @NotNull SentryOptions options) {
    Objects.requireNonNull(hub, "Hub is required");

    runtime.addShutdownHook(new Thread(() -> hub.close()));
  }
}
