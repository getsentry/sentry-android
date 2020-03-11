package io.sentry.core.cache;

import io.sentry.core.SentryEnvelope;

public interface IEnvelopeCache extends Iterable<SentryEnvelope> {

  void store(SentryEnvelope envelope);

  void discard(SentryEnvelope envelope);
}
