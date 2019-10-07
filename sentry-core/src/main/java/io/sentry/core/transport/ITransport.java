package io.sentry.core.transport;

import java.io.IOException;

import io.sentry.ISerializer;
import io.sentry.SentryEvent;

/**
 * A transport is in charge of sending the event to the Sentry server.
 */
public interface ITransport {
  TransportResult send(SentryEvent event, ISerializer serializer) throws IOException;
}
