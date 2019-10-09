package io.sentry;

import java.util.HashMap;
import java.util.Map;

public class UnknownField {
  private Map<String, Object> unknown = new HashMap<>();

  public Map<String, Object> getUnknown() {
    return unknown;
  }

  public void setUnknown(Map<String, Object> unknown) {
    this.unknown = unknown;
  }
}
