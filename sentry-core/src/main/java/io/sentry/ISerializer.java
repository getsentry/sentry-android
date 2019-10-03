package io.sentry;

import io.sentry.internal.SentryEnvelope;

public interface ISerializer {
  SentryEnvelope DeserializeEnvelope(String envelope);
  String Serialize(SentryEvent event);
}
