package io.sentry.core.transport;

import com.google.gson.Gson;
import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.Breadcrumb;
import io.sentry.core.SentryEnvelope;
import io.sentry.core.SentryEvent;
import io.sentry.core.SentryOptions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Open
public class FakeHttpTransport implements ITransport {

  private final SentryOptions options;

  public FakeHttpTransport(SentryOptions options) {
    this.options = options;
  }

  @Override
  public TransportResult send(SentryEvent event) throws IOException {
    // if dsn is invalid return error
    String DSN = "https://ABC123@sentry.io/canva";
    if (!DSN.equals(options.getDsn())) {
      System.out.println("Sentry: request status_code=403, data={\"error\": \"Forbidden\"}");
      options
          .getLogger()
          .log(io.sentry.core.SentryLevel.DEBUG, "Request data: dsn=" + options.getDsn());
      return TransportResult.error(403);
    }

    // if data too big return error
    String data = new Gson().toJson(event);
    int limit = 100 * 1024;
    if (data.getBytes(Charset.defaultCharset()).length > limit) {
      System.out.println(
          "Sentry: request status_code=400, data={\"error\": "
              + "\"Payload too large, actual "
              + data.getBytes(Charset.defaultCharset()).length
              + " bytes, expected less than "
              + limit
              + " bytes\"}");
      options.getLogger().log(io.sentry.core.SentryLevel.DEBUG, "Request data: payload=" + data);
      return TransportResult.error(400);
    }

    System.out.println(
        "Sentry: request status_code=200, data={"
            + "\"throwable\": \""
            + (event.getThrowable() != null ? event.getThrowable().getMessage() : "null")
            + "\","
            + "\"message\": \""
            + (event.getMessage() != null ? event.getMessage().getMessage() : "null")
            + "\","
            + "\"level\": \""
            + event.getLevel()
            + "\","
            + "\"user\": \""
            + (event.getUser() != null ? event.getUser().getId() : "null")
            + "\","
            + "\"tags\": \""
            + printMap(event.tags)
            + "\","
            + "\"extras\": \""
            + printMap(event.extra)
            + "\","
            + "\"breadcrumbs\": \""
            + printBreadcrumbs(event.getBreadcrumbs())
            + "\"}");
    return TransportResult.success();
  }

  @Override
  public boolean isRetryAfter(String type) {
    return false;
  }

  @Override
  public TransportResult send(SentryEnvelope envelope) throws IOException {
    return null;
  }

  private String printBreadcrumbs(List<Breadcrumb> breadcrumbs) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (Breadcrumb breadcrumb : breadcrumbs) {
      sb.append("{\"")
          .append("message")
          .append("\":\"")
          .append(breadcrumb.getMessage())
          .append("\"}");
    }
    sb.append("]");
    return sb.toString();
  }

  private <K, V> String printMap(Map<K, V> map) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (Map.Entry<K, V> entry : map.entrySet()) {
      sb.append("{\"")
          .append(entry.getKey())
          .append("\":\"")
          .append(entry.getValue())
          .append("\"}");
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public void close() throws IOException {}
}
