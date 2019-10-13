package io.sentry.core.transport;

import io.sentry.core.SentryEvent;
import java.io.IOException;

/** A transport is in charge of sending the event to the Sentry server. */
public interface ITransport {
  TransportResult send(SentryEvent event) throws IOException;
}
