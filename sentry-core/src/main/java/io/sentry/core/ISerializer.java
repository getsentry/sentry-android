package io.sentry.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface ISerializer {
  SentryEvent deserializeEvent(Reader reader);

  void serialize(SentryEvent event, Writer writer) throws IOException;

  void serialize(Session session, Writer writer) throws IOException;

  void serialize(SentryEnvelope envelope, OutputStream outputStream) throws Exception;
}
