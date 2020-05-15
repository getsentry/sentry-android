package io.sentry.core;

import io.sentry.core.transport.HttpTransport;
import io.sentry.core.transport.IConnectionConfigurator;
import io.sentry.core.transport.ITransport;
import java.net.MalformedURLException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

final class HttpTransportFactory {

  private HttpTransportFactory() {}

  static ITransport create(@NotNull SentryOptions options) {
    Dsn parsedDsn = new Dsn(options.getDsn());
    IConnectionConfigurator credentials =
        new CredentialsSettingConfigurator(parsedDsn, options.getSentryClientName());

    URL sentryUrl;
    try {
      sentryUrl = parsedDsn.getSentryUri().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Failed to compose the Sentry's server URL.", e);
    }

    return new HttpTransport(
        options,
        credentials,
        options.getConnectionTimeoutMillis(),
        options.getReadTimeoutMillis(),
        options.isBypassSecurity(),
        sentryUrl);
  }
}
