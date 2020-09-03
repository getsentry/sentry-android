package io.sentry.spring;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.HubAdapter;
import io.sentry.core.IHub;
import io.sentry.core.Sentry;
import io.sentry.core.SentryOptions;
import io.sentry.core.protocol.SdkVersion;
import io.sentry.core.transport.ITransport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Registers beans required to use Sentry core features. */
@Configuration
@Open
public class SentryCoreConfiguration {

  /**
   * Creates {@link SentryOptions} using properties from Spring {@link Environment}.
   *
   * @param environment - the environment
   * @param transport - optional Sentry transport - used primarily in testing scenarios
   * @return SentryOptions
   */
  @Bean
  public @NotNull SentryOptions sentryOptions(
      final @NotNull Environment environment, final @NotNull ObjectProvider<ITransport> transport) {
    final SentryOptions options = new SentryOptions();
    options.setDsn(environment.getProperty("sentry.dsn"));
    options.setSdkVersion(createSdkVersion(options));

    final String sendDefaultPii = environment.getProperty("sentry.send-default-pii");
    if (sendDefaultPii != null) {
      options.setSendDefaultPii(Boolean.parseBoolean(sendDefaultPii));
    }
    final String enableUncaughtExceptionHandler =
        environment.getProperty("sentry.enable-uncaught-exception-handler");
    if (enableUncaughtExceptionHandler != null) {
      options.setEnableUncaughtExceptionHandler(
          Boolean.parseBoolean(enableUncaughtExceptionHandler));
    }
    transport.ifAvailable(options::setTransport);
    return options;
  }

  @Bean
  public @NotNull IHub sentryHub(final @NotNull SentryOptions options) {
    options.setSentryClientName(BuildConfig.SENTRY_SPRING_SDK_NAME);
    options.setSdkVersion(createSdkVersion(options));
    Sentry.init(options);
    return HubAdapter.getInstance();
  }

  private static @NotNull SdkVersion createSdkVersion(final @NotNull SentryOptions sentryOptions) {
    SdkVersion sdkVersion = sentryOptions.getSdkVersion();

    if (sdkVersion == null) {
      sdkVersion = new SdkVersion();
    }

    sdkVersion.setName(BuildConfig.SENTRY_SPRING_SDK_NAME);
    final String version = BuildConfig.VERSION_NAME;
    sdkVersion.setVersion(version);
    sdkVersion.addPackage("maven:sentry-spring", version);

    return sdkVersion;
  }
}
