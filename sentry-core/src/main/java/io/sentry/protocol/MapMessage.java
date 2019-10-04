package io.sentry.protocol;

import java.util.List;
import java.util.Map;

// https://docs.sentry.io/development/sdk-dev/event-payloads/message/

public class MapMessage {
  private String formatted;
  private String message;
  private List<String> params;
  private final Map<String, Object> map;

  public MapMessage(Map<String, Object> map) {
    this.map = map;
  }

  public String getFormatted() {
    return formatted;
  }

  /** @param formatted */
  public void setFormatted(String formatted) {
    this.formatted = formatted;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<String> getParams() {
    return params;
  }

  public void setParams(List<String> params) {
    this.params = params;
  }
}
