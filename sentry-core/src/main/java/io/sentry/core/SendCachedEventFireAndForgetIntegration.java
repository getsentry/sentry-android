package io.sentry.core;

import static io.sentry.core.ILogger.logIfNotNull;

import java.util.concurrent.*;
import org.jetbrains.annotations.NotNull;

final class SendCachedEventFireAndForgetIntegration implements Integration {

  private SendFireAndForgetFactory factory;

  interface SendFireAndForget {
    void send();
  }

  interface SendFireAndForgetFactory {
    SendFireAndForget create(IHub hub, SentryOptions options);
  }

  SendCachedEventFireAndForgetIntegration(SendFireAndForgetFactory factory) {
    this.factory = factory;
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Override
  public void register(@NotNull IHub hub, @NotNull SentryOptions options) {
    String cachedDir = options.getCacheDirPath();
    if (cachedDir == null) {
      logIfNotNull(
          options.getLogger(), SentryLevel.WARNING, "No cache dir path is defined in options.");
      return;
    }

    SendFireAndForget sender = factory.create(hub, options);

    try {
      ExecutorService es = Executors.newSingleThreadExecutor();
      es.submit(
          () -> {
            try {
              sender.send();
            } catch (Exception e) {
              logIfNotNull(
                  options.getLogger(),
                  SentryLevel.ERROR,
                  "Failed trying to send cached events.",
                  e);
            }
          });
      es.shutdown();
    } catch (Exception e) {
      logIfNotNull(
          options.getLogger(),
          SentryLevel.ERROR,
          "Failed to call the executor. Cached events will not be sent",
          e);
    }
  }
}
