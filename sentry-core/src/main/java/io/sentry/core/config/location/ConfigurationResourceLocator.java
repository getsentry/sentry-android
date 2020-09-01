package io.sentry.core.config.location;

import org.jetbrains.annotations.Nullable;

/**
 * Tries to find the Sentry configuration file.
 */
public interface ConfigurationResourceLocator {
    /**
     * Tries to find the location of the resource containing the Sentry configuration file.
     *
     * @return the location on which some {@link io.sentry.core.config.provider.resource.ResourceLoader} can find the configuration file or null if this
     * locator could not find any.
     */
    @Nullable
    String getConfigurationResourcePath();
}
