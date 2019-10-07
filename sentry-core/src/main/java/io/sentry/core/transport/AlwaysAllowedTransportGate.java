package io.sentry.core.transport;

/**
 * A dummy implementation of the {@link io.sentry.core.transport.ITransportGate} interface that always allows sending the events.
 */
public class AlwaysAllowedTransportGate implements ITransportGate {
  @Override
  public boolean isSendingAllowed() {
    return true;
  }
}
