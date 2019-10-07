package io.sentry.core.transport;

import io.sentry.ISerializer;
import io.sentry.SentryEvent;
import java.io.IOException;

/** A transport is in charge of sending the event to the Sentry server. */
public interface ITransport {
  TransportResult send(SentryEvent event, ISerializer serializer) throws IOException;
}
