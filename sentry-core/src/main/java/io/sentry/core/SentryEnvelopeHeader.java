package io.sentry.core;

import io.sentry.core.protocol.SentryId;

public final class SentryEnvelopeHeader {
  private final SentryId eventId;
  private final String auth;

  public SentryEnvelopeHeader(SentryId sentryId, String auth) {
    this.eventId = sentryId;
    this.auth = auth;
  }

  public SentryId getEventId() {
    return eventId;
  }

  public String getAuth() {
    return auth;
  }
}
