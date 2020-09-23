package io.sentry.core.config.provider;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Wraps a couple of other configuration providers to act as one, returning the first non-null value for given
 * configuration key, in the iteration order of the wrapped providers.
 */
final class CompoundConfigurationProvider implements ConfigurationProvider {
    private final Collection<ConfigurationProvider> providers;

    /**
     * Instantiates the new compound provider by wrapping the provided collection of providers.
     * @param providers the providers to wrap
     */
    public CompoundConfigurationProvider(Collection<ConfigurationProvider> providers) {
        this.providers = providers;
    }

    @Nullable
    @Override
    public String getProperty(String key) {
        for (ConfigurationProvider p : providers) {
            String val = p.getProperty(key);
            if (val != null) {
                return val;
            }
        }

        return null;
    }
}
