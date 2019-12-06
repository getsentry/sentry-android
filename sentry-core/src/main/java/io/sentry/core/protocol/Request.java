package io.sentry.core.protocol;

import io.sentry.core.IUnknownPropertiesConsumer;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public final class Request implements IUnknownPropertiesConsumer {
  private String url;
  private String method;
  private String queryString;
  private Object data;
  private String cookies;
  private Map<String, String> headers;
  private Map<String, String> env;
  private Map<String, String> other;

  @SuppressWarnings("unused")
  private Map<String, Object> unknown;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public String getCookies() {
    return cookies;
  }

  public void setCookies(String cookies) {
    this.cookies = cookies;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public Map<String, String> getEnvs() {
    return env;
  }

  public void setEnvs(Map<String, String> env) {
    this.env = env;
  }

  public Map<String, String> getOthers() {
    return other;
  }

  public void setOthers(Map<String, String> other) {
    this.other = other;
  }

  @ApiStatus.Internal
  @Override
  public void acceptUnknownProperties(Map<String, Object> unknown) {
    this.unknown = unknown;
  }
}
