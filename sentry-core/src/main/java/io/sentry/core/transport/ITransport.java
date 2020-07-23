package io.sentry.core.transport;

import io.sentry.core.SentryEnvelope;
import java.io.Closeable;
import java.io.IOException;

/** A transport is in charge of sending the event to the Sentry server. */
public interface ITransport extends Closeable {
  boolean isRetryAfter(String type);

  TransportResult send(SentryEnvelope envelope) throws IOException;
}
