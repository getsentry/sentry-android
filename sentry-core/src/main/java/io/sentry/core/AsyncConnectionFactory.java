package io.sentry.core;

import io.sentry.core.transport.*;
import java.net.MalformedURLException;
import java.net.URL;

class AsyncConnectionFactory {
  public static AsyncConnection create(SentryOptions options) {
    try {
      IConnectionConfigurator setCredentials = new CredentialsSettingConfigurator(options);

      URL sentryUrl = new Dsn(options.getDsn()).getSentryUri().toURL();
      // TODO: Take configuration values from SentryOptions
      HttpTransport transport = new HttpTransport(options, null, setCredentials, 5000, 5000, false, sentryUrl);

      // TODO this should be made configurable at least for the Android case where we can
      // just not attempt to send if the device is offline.
      ITransportGate alwaysOn =
          new ITransportGate() {
            @Override
            public boolean isSendingAllowed() {
              return true;
            }
          };

      IBackOffIntervalStrategy linearBackoff =
          new IBackOffIntervalStrategy() {
            @Override
            public long nextDelayMillis(int attempt) {
              return attempt * 500;
            }
          };

      // TODO this is obviously provisional and should be constructed based on the config in options
      IEventCache blackHole =
          new IEventCache() {
            @Override
            public void store(SentryEvent event) {}

            @Override
            public void discard(SentryEvent event) {}
          };

      // the connection doesn't do any retries of failed sends and can hold at most 10
      // pending events. The rest is dropped.
      return new AsyncConnection(transport, alwaysOn, linearBackoff, blackHole, 0, 10, options);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          "Failed to compose the connection to the Sentry server.", e);
    }
  }
}
