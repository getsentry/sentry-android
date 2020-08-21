package io.sentry.spring.boot;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.EventProcessor;
import io.sentry.core.HubAdapter;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.Sentry;
import io.sentry.core.SentryOptions;
import io.sentry.core.protocol.SdkVersion;
import io.sentry.core.transport.ITransport;
import io.sentry.core.transport.ITransportGate;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "sentry.enabled", havingValue = "true", matchIfMissing = true)
@Open
public class SentryAutoConfiguration {

  /** Registers general purpose Sentry related beans. */
  @Configuration
  @ConditionalOnProperty("sentry.dsn")
  @EnableConfigurationProperties(SentryProperties.class)
  @Open
  static class HubConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Sentry.OptionsConfiguration<SentryOptions> optionsOptionsConfiguration(
      ObjectProvider<SentryOptions.BeforeSendCallback> beforeSendCallback,
      ObjectProvider<SentryOptions.BeforeBreadcrumbCallback> beforeBreadcrumbCallback,
      ObjectProvider<EventProcessor> eventProcessors,
      ObjectProvider<Integration> integrations,
      ObjectProvider<ITransportGate> transportGate,
      ObjectProvider<ITransport> transport) {
      return options -> {
        beforeSendCallback.ifAvailable(options::setBeforeSend);
        beforeBreadcrumbCallback.ifAvailable(options::setBeforeBreadcrumb);
        eventProcessors.stream().forEach(options::addEventProcessor);
        integrations.stream().forEach(options::addIntegration);
        transportGate.ifAvailable(options::setTransportGate);
        transport.ifAvailable(options::setTransport);
      };
    }

    @Bean
    public SentryOptions sentryOptions(Sentry.OptionsConfiguration<SentryOptions> optionsConfiguration,
                                       SentryProperties properties) {
      final SentryOptions options = new SentryOptions();
      optionsConfiguration.configure(options);
      properties.applyTo(options);
      options.setSentryClientName(BuildConfig.SENTRY_SPRING_BOOT_SDK_NAME);
      options.setSdkVersion(createSdkVersion(options));
      return options;
    }

    @Bean
    IHub sentryHub(SentryOptions sentryOptions) {
      Sentry.init(sentryOptions);
      return HubAdapter.getInstance();
    }

    /** Registers beans specific to Spring MVC. */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Open
    static class SentryWebMvcConfiguration {

      @Bean
      SentryRequestHttpServletRequestProcessor sentryEventHttpServletRequestProcessor() {
        return new SentryRequestHttpServletRequestProcessor();
      }

      @Bean
      SentryUserHttpServletRequestProcessor sentryUserHttpServletRequestProcessor() {
        return new SentryUserHttpServletRequestProcessor();
      }

      @Bean
      public FilterRegistrationBean<SentryRequestFilter> sentryRequestFilter(IHub sentryHub) {
        return new FilterRegistrationBean<>(new SentryRequestFilter(sentryHub));
      }
    }

    private static @NotNull SdkVersion createSdkVersion(@NotNull SentryOptions sentryOptions) {
      SdkVersion sdkVersion = sentryOptions.getSdkVersion();

      if (sdkVersion == null) {
        sdkVersion = new SdkVersion();
      }

      sdkVersion.setName(BuildConfig.SENTRY_SPRING_BOOT_SDK_NAME);
      final String version = BuildConfig.VERSION_NAME;
      sdkVersion.setVersion(version);
      sdkVersion.addPackage("maven:sentry-spring-boot-starter", version);

      return sdkVersion;
    }
  }
}
