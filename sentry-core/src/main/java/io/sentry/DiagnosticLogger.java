package io.sentry;

class DiagnosticLogger implements ILogger {
  private SentryOptions options;
  private ILogger logger;

  DiagnosticLogger(SentryOptions options, ILogger logger) {
    if (options == null) {
      throw new IllegalArgumentException("SentryOptions is required.");
    }
    this.options = options;
    this.logger = logger;
  }

  public boolean isEnabled(SentryLevel level) {
    SentryLevel diagLevel = options.getDiagnosticLevel();
    if (level == null || diagLevel == null) {
      return false;
    }
    return options.isDebug() && level.ordinal() >= diagLevel.ordinal();
  }

  @Override
  public void log(SentryLevel level, String message, Object... args) {
    if (logger != null && isEnabled(level)) {
      logger.log(level, message, args);
    }
  }

  @Override
  public void log(SentryLevel level, String message, Throwable throwable) {
    if (logger != null && isEnabled(level)) {
      logger.log(level, message, throwable);
    }
  }
}
