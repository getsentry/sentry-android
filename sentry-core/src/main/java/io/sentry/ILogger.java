package io.sentry;

public interface ILogger {
  void log(SentryLevel level, String message, Object... args);

  void log(SentryLevel level, String message, Throwable throwable);
}
