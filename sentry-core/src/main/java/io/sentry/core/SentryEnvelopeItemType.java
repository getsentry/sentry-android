package io.sentry.core;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum SentryEnvelopeItemType {
  Session("session"),
  Event("event"), // DataCategory.Error
  Attachment("attachment"),
  Transaction("transaction"),
  Csp("csp"), // DataCategory.Security
  Hpkp("hpkp"), // DataCategory.Security
  Expectct("expectct"), // DataCategory.Security
  Expectstaple("expectstaple"), // DataCategory.Security
  Unknown("__unknown__"); // DataCategory.Unknown

  private final String type;

  SentryEnvelopeItemType(final String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
