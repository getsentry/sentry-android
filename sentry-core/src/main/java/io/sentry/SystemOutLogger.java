package io.sentry;

class SystemOutLogger implements ILogger {
  @Override
  public void log(SentryLevel level, String message, Object... args) {
    System.out.println(String.format("%s: %s", level, String.format(message, args)));
  }

  @Override
  public void log(SentryLevel level, String message, Throwable throwable) {
    System.out.println(
        String.format(
            "%s: %s\n%s",
            level, String.format(message, throwable.toString(), throwable.getStackTrace())));
  }
}
