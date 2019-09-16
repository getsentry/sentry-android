package io.sentry;

import java.util.UUID;

class NoOpSentryClient implements ISentryClient {

  private static final UUID emptyId = UUID.fromString("00000000-0000-0000-0000-000000000000");

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public UUID captureEvent(SentryEvent event) {
    return emptyId;
  }

  @Override
  public void close() {}
}
