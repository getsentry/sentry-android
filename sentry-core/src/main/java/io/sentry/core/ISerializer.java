package io.sentry.core;

public interface ISerializer {
  SentryEvent deserializeEvent(String envelope);

  String serialize(SentryEvent event);

  SentryEnvelopeHeader deserializeEnvelopeHeader(byte[] buffer, int offset, int length);

  SentryEnvelopeItemHeader deserializeEnvelopeItemHeader(byte[] buffer, int offset, int length);
}
