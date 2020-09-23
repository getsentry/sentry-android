package io.sentry.core.config.provider;

import io.sentry.core.ILogger;
import io.sentry.core.config.provider.resource.ResourceLoader;
import io.sentry.core.config.location.ConfigurationResourceLocator;
import io.sentry.core.config.provider.resource.ResourceLoaderConfigurationProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Similar to {@link ResourceLoaderConfigurationProvider} but uses a {@link ConfigurationResourceLocator} to find
 * the path to the configuration file.
 */
final class LocatorBasedConfigurationProvider extends ResourceLoaderConfigurationProvider {
    /**
     * Instantiates a new configuration provider using the parameters.
     *
     * @param rl the resource loader to load the contents of the configuration file with
     * @param locator the locator to find the configuration file with
     * @param charset the charset of the configuration file
     * @param logger the logger
     * @throws IOException on failure to process the configuration file
     */
    public LocatorBasedConfigurationProvider(ResourceLoader rl, ConfigurationResourceLocator locator, Charset charset, @NotNull ILogger logger)
            throws IOException {
        super(rl, locator.getConfigurationResourcePath(), charset, logger);
    }
}
