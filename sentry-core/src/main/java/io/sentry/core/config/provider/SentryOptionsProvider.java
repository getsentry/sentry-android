package io.sentry.core.config.provider;

import io.sentry.core.ILogger;
import io.sentry.core.NoOpLogger;
import io.sentry.core.SentryOptions;
import io.sentry.core.SystemOutLogger;
import io.sentry.core.config.location.CompoundResourceLocator;
import io.sentry.core.config.location.EnvironmentBasedLocator;
import io.sentry.core.config.location.StaticFileLocator;
import io.sentry.core.config.location.SystemPropertiesBasedLocator;
import io.sentry.core.config.provider.resource.CompoundResourceLoader;
import io.sentry.core.config.provider.resource.ContextClassLoaderResourceLoader;
import io.sentry.core.config.provider.resource.FileResourceLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class SentryOptionsProvider {
  private final ConfigurationProvider configurationProvider;

  public SentryOptionsProvider(ConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
  }

  public static SentryOptionsProvider create(boolean debug) {
    final ILogger logger = debug ? new SystemOutLogger() : NoOpLogger.getInstance();
    List<ConfigurationProvider> providers = new ArrayList<>();

    if (JndiSupport.isAvailable()) {
      providers.add(new JndiConfigurationProvider(logger));
    }

    providers.add(new SystemPropertiesConfigurationProvider());
    providers.add(new EnvironmentConfigurationProvider());

    try {
      providers.add(
        new LocatorBasedConfigurationProvider(
          new CompoundResourceLoader(
            new FileResourceLoader(),
            new ContextClassLoaderResourceLoader()
          ),
          new CompoundResourceLocator(
            new SystemPropertiesBasedLocator(),
            new EnvironmentBasedLocator(),
            new StaticFileLocator()
          ),
          Charset.defaultCharset(),
          logger
        )
      );
    } catch (IOException e) {
//            logger.debug("Failed to instantiate resource locator-based configuration provider.", e);
    }

    return new SentryOptionsProvider(new CompoundConfigurationProvider(providers));
  }

  public @NotNull SentryOptions resolve() {
    final SentryOptions options = new SentryOptions();

    final String dsn = configurationProvider.getProperty("dsn");
    if (dsn != null) {
      options.setDsn(dsn);
    }

    final Long shutdownTimeout = getLong("shutdownTimeout");
    if (shutdownTimeout != null) {
      options.setShutdownTimeout(shutdownTimeout);
    }

    return options;
  }

  private @Nullable Long getLong(@NotNull String property) {
    final String value = configurationProvider.getProperty(property);
    return value != null ? Long.valueOf(value) : null;
  }
}
