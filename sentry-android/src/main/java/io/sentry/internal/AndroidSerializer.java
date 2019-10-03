package io.sentry.internal;

import io.sentry.ISerializer;
import io.sentry.SentryEvent;

public class AndroidSerializer implements ISerializer {
  @Override
  public SentryEnvelope DeserializeEnvelope(String envelope) {
    return null;
  }

  @Override
  public String Serialize(SentryEvent event) {
    return null;
  }
}
