package io.sentry.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public interface IEnvelopeReader {
  @Nullable SentryEnvelope read(@NotNull InputStream stream) throws IOException;
}
