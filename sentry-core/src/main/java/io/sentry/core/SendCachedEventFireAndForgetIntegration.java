package io.sentry.core;

import io.sentry.core.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Sends cached events over when your App. is starting. */
public final class SendCachedEventFireAndForgetIntegration implements Integration {

  private final SendFireAndForgetFactory factory;

  public interface SendFireAndForget {
    void send();
  }

  public interface SendFireAndForgetDirPath {
    @Nullable
    String getDirPath();
  }

  public interface SendFireAndForgetFactory {
    @Nullable
    SendFireAndForget create(IHub hub, SentryOptions options);
  }

  public SendCachedEventFireAndForgetIntegration(final @NotNull SendFireAndForgetFactory factory) {
    this.factory = Objects.requireNonNull(factory, "SendFireAndForgetFactory is required");
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Override
  public final void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    Objects.requireNonNull(hub, "Hub is required");
    Objects.requireNonNull(options, "SentryOptions is required");

    final String cachedDir = options.getCacheDirPath();
    if (cachedDir == null || cachedDir.isEmpty()) {
      options
          .getLogger()
          .log(
              SentryLevel.INFO,
              "No cache dir path is defined in options to SendCachedEventFireAndForgetIntegration.");
      return;
    }

    final SendFireAndForget sender = factory.create(hub, options);

    try {
      options
          .getExecutorService()
          .submit(
              () -> {
                try {
                  sender.send();
                } catch (Exception e) {
                  options
                      .getLogger()
                      .log(SentryLevel.ERROR, "Failed trying to send cached events.", e);
                }
              });

      options
          .getLogger()
          .log(SentryLevel.DEBUG, "SendCachedEventFireAndForgetIntegration installed.");
    } catch (Exception e) {
      options
          .getLogger()
          .log(SentryLevel.ERROR, "Failed to call the executor. Cached events will not be sent", e);
    }
  }
}
