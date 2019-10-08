package io.sentry.core.transport;

import static io.sentry.SentryLevel.DEBUG;
import static io.sentry.SentryLevel.ERROR;

import io.sentry.ISerializer;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import io.sentry.util.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

import io.sentry.ISerializer;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import io.sentry.util.Nullable;

/**
 * An implementation of the {@link ITransport} interface that sends the events to the Sentry server
 * over HTTP(S) in UTF-8 encoding.
 */
public class HttpTransport implements ITransport {
  public static final int HTTP_TOO_MANY_REQUESTS = 429;

  @Nullable private final Proxy proxy;
  private final Consumer<URLConnection> requestUpdater;
  private final int connectionTimeout;
  private final int readTimeout;
  private final boolean bypassSecurity;
  private final URL sentryUrl;
  private final SentryOptions options;

  /**
   * Constructs a new HTTP transport instance. Notably, the provided {@code requestUpdater} must set
   * the appropriate content encoding header for the {@link io.sentry.ISerializer} instance obtained
   * from the options.
   *
   * @param options sentry options to read the config from
   * @param proxy the proxy to use, if any
   * @param requestUpdater this consumer is given a chance to set up the request before it is sent
   * @param connectionTimeout connection timeout
   * @param readTimeout read timeout
   * @param bypassSecurity whether to ignore TLS errors
   * @throws URISyntaxException when options contain invalid DSN
   * @throws MalformedURLException when options contain invalid DSN
   */
  public HttpTransport(
      SentryOptions options,
      @Nullable Proxy proxy,
      Consumer<URLConnection> requestUpdater,
      int connectionTimeout,
      int readTimeout,
      boolean bypassSecurity)
      throws URISyntaxException, MalformedURLException {
    this.proxy = proxy;
    this.requestUpdater = requestUpdater;
    this.connectionTimeout = connectionTimeout;
    this.readTimeout = readTimeout;
    this.options = options;
    this.sentryUrl = new URI(options.getDsn()).toURL();
    this.bypassSecurity = bypassSecurity;
  }

  // visible for testing
  // giving up on testing this method is probably the simplest way of having the rest of the class
  // testable...
  protected HttpURLConnection open(URL url, Proxy proxy) throws IOException {
    return (HttpURLConnection)
        (proxy == null ? sentryUrl.openConnection() : sentryUrl.openConnection(proxy));
  }

  @Override
  public TransportResult send(SentryEvent event, ISerializer serializer) throws IOException {
    HttpURLConnection connection = open(sentryUrl, proxy);
    requestUpdater.accept(connection);

    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Encoding", "UTF-8");
    connection.setConnectTimeout(connectionTimeout);
    connection.setReadTimeout(readTimeout);

    if (bypassSecurity && connection instanceof HttpsURLConnection) {
      ((HttpsURLConnection) connection).setHostnameVerifier((__, ___) -> true);
    }

    connection.connect();

    try (OutputStream outputStream = connection.getOutputStream()) {
      serializer.serialize(event, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

      // need to also close the input stream of the connection
      connection.getInputStream().close();
      return TransportResult.success();
    } catch (IOException e) {
      long retryAfterMs = 1000; // the default is 1s
      String retryAfterHeader = connection.getHeaderField("Retry-After");
      if (retryAfterHeader != null) {
        try {
          retryAfterMs =
              (long) (Double.parseDouble(retryAfterHeader) * 1000L); // seconds -> milliseconds
        } catch (NumberFormatException __) {
          // let's use the default then
        }
      }

      int responseCode = -1;
      try {
        responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
          if (options.isDebug()) {
            log(
                DEBUG,
                "Event '"
                    + event.getEventId()
                    + "' was rejected by the Sentry server due to a filter.");
          }
        }
        return TransportResult.error(retryAfterMs, responseCode);
      } catch (IOException responseCodeException) {
        // pass
      }

      if (options.isDebug()) {
        String errorMessage = null;
        final InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
          errorMessage = getErrorMessageFromStream(errorStream);
        }
        if (null == errorMessage || errorMessage.isEmpty()) {
          errorMessage = "An exception occurred while submitting the event to the Sentry server.";
        }

        log(DEBUG, errorMessage);
      }

      return TransportResult.error(retryAfterMs, responseCode);
    } finally {
      connection.disconnect();
    }
  }

  private String getErrorMessageFromStream(InputStream errorStream) {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    try {
      String line;
      // ensure we do not add "\n" to the last line
      boolean first = true;
      while ((line = reader.readLine()) != null) {
        if (!first) {
          sb.append("\n");
        }
        sb.append(line);
        first = false;
      }
    } catch (Exception e2) {
      log(
          ERROR,
          "Exception while reading the error message from the connection: " + e2.getMessage());
    }
    return sb.toString();
  }

  private final void log(SentryLevel logLevel, String message) {
    if (options.isDebug()) {
      options.getLogger().log(logLevel, message);
    }
  }
}
