package io.sentry.core;

public class SentryEnvelopeItem {
  private final SentryEnvelopeItemHeader header;
  // TODO: Can we have a slice or a reader here instead?
  private final Byte[] data;

  public SentryEnvelopeItem(SentryEnvelopeItemHeader header, Byte[] data) {
    this.header = header;
    this.data = data;
  }
}
