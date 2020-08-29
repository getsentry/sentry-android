package io.sentry.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.sentry.core.DateUtils;
import io.sentry.core.Sentry;
import io.sentry.core.SentryEvent;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.protocol.Message;
import io.sentry.core.protocol.SdkVersion;
import io.sentry.core.transport.ITransport;
import io.sentry.core.util.CollectionUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Appender for logback in charge of sending the logged events to a Sentry server. */
public final class SentryAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
  private @Nullable SentryOptions options;
  private @Nullable ITransport transport;

  @Override
  public void start() {
    if (options != null && options.getDsn() != null) {
      options.setSentryClientName(BuildConfig.SENTRY_LOGBACK_SDK_NAME);
      options.setSdkVersion(createSdkVersion(options));
      Optional.ofNullable(transport).ifPresent(options::setTransport);
      Sentry.init(options);
    }
    super.start();
  }

  @Override
  protected void append(@NotNull ILoggingEvent eventObject) {
    Sentry.captureEvent(createEvent(eventObject));
  }

  /**
   * Creates {@link SentryEvent} from Logback's {@link ILoggingEvent}.
   *
   * @param loggingEvent the logback event
   * @return the sentry event
   */
  @SuppressWarnings("JdkObsolete")
  final @NotNull SentryEvent createEvent(@NotNull ILoggingEvent loggingEvent) {
    final SentryEvent event =
        new SentryEvent(DateUtils.getDateTime(new Date(loggingEvent.getTimeStamp())));
    final Message message = new Message();
    message.setMessage(loggingEvent.getMessage());
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

    final Map<String, String> mdcProperties =
        CollectionUtils.shallowCopy(loggingEvent.getMDCPropertyMap());
    if (!mdcProperties.isEmpty()) {
      event.getContexts().put("MDC", mdcProperties);
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
    } else {
      return SentryLevel.DEBUG;
    }
  }

  private @NotNull SdkVersion createSdkVersion(@NotNull SentryOptions sentryOptions) {
    SdkVersion sdkVersion = sentryOptions.getSdkVersion();

    if (sdkVersion == null) {
      sdkVersion = new SdkVersion();
    }

    sdkVersion.setName(BuildConfig.SENTRY_LOGBACK_SDK_NAME);
    final String version = BuildConfig.VERSION_NAME;
    sdkVersion.setVersion(version);
    sdkVersion.addPackage("maven:sentry-logback", version);

    return sdkVersion;
  }

  public void setOptions(SentryOptions options) {
    this.options = options;
  }

  @ApiStatus.Internal
  void setTransport(@Nullable ITransport transport) {
    this.transport = transport;
  }
}
