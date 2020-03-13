package io.sentry.core.transport;

import io.sentry.core.ISerializer;
import io.sentry.core.SentryEvent;
import java.io.IOException;
import java.io.PrintWriter;

public final class StdoutTransport implements ITransport {

  private final ISerializer serializer;

  public StdoutTransport(final ISerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public TransportResult send(SentryEvent event) throws IOException {
    PrintWriter writer = new PrintWriter(System.out);
    serializer.serialize(event, writer);

    return TransportResult.success();
  }

  @Override
  public void close() throws IOException {}
}
