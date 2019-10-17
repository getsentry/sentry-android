package io.sentry.core;

import io.sentry.core.protocol.SentryId;
import io.sentry.core.util.Nullable;

public class SentryEnvelope {

  private final SentryEnvelopeHeader header;
  private final Iterable<SentryEnvelopeItem> items;

  public Iterable<SentryEnvelopeItem> getItems() {
    return items;
  }

  public SentryEnvelope(SentryEnvelopeHeader header, Iterable<SentryEnvelopeItem> items) {
    this.header = header;
    this.items = items;
  }

  public SentryEnvelope(
      SentryId sentryId, @Nullable String auth, Iterable<SentryEnvelopeItem> items) {
    header = new SentryEnvelopeHeader(sentryId, auth);
    this.items = items;
  }
}
