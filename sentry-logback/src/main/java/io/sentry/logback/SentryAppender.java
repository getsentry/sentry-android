package io.sentry.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.sentry.core.Sentry;
import io.sentry.core.SentryEvent;
import io.sentry.core.SentryLevel;
import io.sentry.core.protocol.Message;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Appender for logback in charge of sending the logged events to a Sentry server. */
public final class SentryAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
  private @Nullable String dsn;
  private @Nullable String environment;
  private @Nullable Integer maxBreadcrumbs;
  private @Nullable Integer shutdownTimeoutMillis;
  private @Nullable Integer flushTimeoutMillis;
  private @Nullable Integer readTimeoutMillis;
  private @Nullable Double sampleRate;
  private @Nullable Boolean bypassSecurity;
  private @Nullable Boolean debug;
  private @Nullable Boolean attachThreads;
  private @Nullable Boolean attachStacktrace;

  @Override
  public void start() {
    if (dsn != null) {
      Sentry.init(
          options -> {
            options.setDsn(dsn);
            Optional.ofNullable(maxBreadcrumbs).ifPresent(options::setMaxBreadcrumbs);
            Optional.ofNullable(environment).ifPresent(options::setEnvironment);
            Optional.ofNullable(shutdownTimeoutMillis).ifPresent(options::setShutdownTimeout);
            Optional.ofNullable(flushTimeoutMillis).ifPresent(options::setFlushTimeoutMillis);
            Optional.ofNullable(readTimeoutMillis).ifPresent(options::setReadTimeoutMillis);
            Optional.ofNullable(sampleRate).ifPresent(options::setSampleRate);
            Optional.ofNullable(bypassSecurity).ifPresent(options::setBypassSecurity);
            Optional.ofNullable(debug).ifPresent(options::setDebug);
            Optional.ofNullable(attachThreads).ifPresent(options::setAttachThreads);
            Optional.ofNullable(attachStacktrace).ifPresent(options::setAttachStacktrace);
          });
    }
    super.start();
  }

  @Override
  protected void append(@NotNull ILoggingEvent eventObject) {
    if (Sentry.isEnabled()) {
      Sentry.captureEvent(createEvent(eventObject));
    }
  }

  /**
   * Creates {@link SentryEvent} from Logback's {@link ILoggingEvent}.
   *
   * @param loggingEvent the logback event
   * @return the sentry event
   */
  @SuppressWarnings("JdkObsolete")
  final @NotNull SentryEvent createEvent(@NotNull ILoggingEvent loggingEvent) {
    final SentryEvent event = new SentryEvent(new Date(loggingEvent.getTimeStamp()));
    final Message message = new Message();
    message.setFormatted(loggingEvent.getFormattedMessage());
    message.setParams(toParams(loggingEvent.getArgumentArray()));
    event.setMessage(message);
    event.setLogger(loggingEvent.getLoggerName());
    event.setLevel(formatLevel(loggingEvent.getLevel()));

    final ThrowableProxy throwableInformation = (ThrowableProxy) loggingEvent.getThrowableProxy();
    if (throwableInformation != null) {
      event.setThrowable(throwableInformation.getThrowable());
    }

    if (loggingEvent.getThreadName() != null) {
      event.setExtra("thread_name", loggingEvent.getThreadName());
    }

    for (Map.Entry<String, String> entry : loggingEvent.getMDCPropertyMap().entrySet()) {
      event.setTag(entry.getKey(), entry.getValue());
    }

    return event;
  }

  private @NotNull List<String> toParams(@Nullable Object[] arguments) {
    if (arguments != null) {
      return Arrays.stream(arguments)
          .filter(Objects::nonNull)
          .map(Object::toString)
          .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Transforms a {@link Level} into an {@link SentryLevel}.
   *
   * @param level original level as defined in log4j.
   * @return log level used within sentry.
   */
  private static @NotNull SentryLevel formatLevel(@NotNull Level level) {
    if (level.isGreaterOrEqual(Level.ERROR)) {
      return SentryLevel.ERROR;
    } else if (level.isGreaterOrEqual(Level.WARN)) {
      return SentryLevel.WARNING;
    } else if (level.isGreaterOrEqual(Level.INFO)) {
      return SentryLevel.INFO;
    } else if (level.isGreaterOrEqual(Level.ALL)) {
      return SentryLevel.DEBUG;
    } else {
      return SentryLevel.LOG;
    }
  }

  public void setDsn(@Nullable String dsn) {
    this.dsn = dsn;
  }

  public void setEnvironment(@Nullable String environment) {
    this.environment = environment;
  }

  public void setMaxBreadcrumbs(@Nullable Integer maxBreadcrumbs) {
    this.maxBreadcrumbs = maxBreadcrumbs;
  }

  public void setShutdownTimeoutMillis(@Nullable Integer shutdownTimeoutMillis) {
    this.shutdownTimeoutMillis = shutdownTimeoutMillis;
  }

  public void setFlushTimeoutMillis(@Nullable Integer flushTimeoutMillis) {
    this.flushTimeoutMillis = flushTimeoutMillis;
  }

  public void setReadTimeoutMillis(@Nullable Integer readTimeoutMillis) {
    this.readTimeoutMillis = readTimeoutMillis;
  }

  public void setSampleRate(@Nullable Double sampleRate) {
    this.sampleRate = sampleRate;
  }

  public void setBypassSecurity(@Nullable Boolean bypassSecurity) {
    this.bypassSecurity = bypassSecurity;
  }

  public void setDebug(@Nullable Boolean debug) {
    this.debug = debug;
  }

  public void setAttachThreads(@Nullable Boolean attachThreads) {
    this.attachThreads = attachThreads;
  }

  public void setAttachStacktrace(@Nullable Boolean attachStacktrace) {
    this.attachStacktrace = attachStacktrace;
  }
}
