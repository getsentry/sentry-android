package io.sentry.core;

import io.sentry.core.protocol.SentryId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SentryEnvelopeHeader {
  private final @NotNull SentryId eventId;
  // TODO: I noticed this dropped from the spec
  // Should be safe to delete since this was an optional field which was never used
  // Nothing serialized anywhere should have this value, if it's there we can just drop it.
  private final @Nullable String auth;

  SentryEnvelopeHeader(@NotNull SentryId sentryId, @Nullable String auth) {
    this.eventId = sentryId;
    this.auth = auth;
  }

  public SentryEnvelopeHeader(@NotNull SentryId sentryId) {
    this(sentryId, null);
  }

  public SentryEnvelopeHeader() {
    this(new SentryId(), null);
  }

  // TODO Should be renamed to EnvelopeId
  public SentryId getEventId() {
    return eventId;
  }

  public String getAuth() {
    return auth;
  }
}
