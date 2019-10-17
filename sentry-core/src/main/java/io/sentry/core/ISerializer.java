package io.sentry.core;

import java.io.IOException;
import java.io.Writer;

public interface ISerializer {
  SentryEvent deserializeEvent(String envelope);

  void serialize(SentryEvent event, Writer writer) throws IOException;

  SentryEnvelopeHeader deserializeEnvelopeHeader(byte[] buffer, int offset, int length);

  SentryEnvelopeItemHeader deserializeEnvelopeItemHeader(byte[] buffer, int offset, int length);
}
