package io.sentry.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;

/** Sends cached events over when your App. is starting. */
public final class SendCachedEventFireAndForgetIntegration implements Integration {

  private final SendFireAndForgetFactory factory;

  public interface SendFireAndForget {
    void send();
  }

  public interface SendFireAndForgetFactory {
    SendFireAndForget create(IHub hub, SentryOptions options);
  }

  public SendCachedEventFireAndForgetIntegration(SendFireAndForgetFactory factory) {
    this.factory = factory;
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Override
  public final void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    final String cachedDir = options.getCacheDirPath();
    if (cachedDir == null) {
      options
          .getLogger()
          .log(
              SentryLevel.WARNING,
              "No cache dir path is defined in options to SendCachedEventFireAndForgetIntegration.");
      return;
    }

    final SendFireAndForget sender = factory.create(hub, options);

    try {
      final ExecutorService es = Executors.newSingleThreadExecutor();
      es.submit(
          () -> {
            try {
              sender.send();
            } catch (Exception e) {
              options.getLogger().log(SentryLevel.ERROR, "Failed trying to send cached events.", e);
            } finally {
              es.shutdown();
            }
          });
      options
          .getLogger()
          .log(SentryLevel.DEBUG, "Scheduled sending cached files from %s", cachedDir);
      //      es.shutdown();

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
