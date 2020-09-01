package io.sentry.core.config.provider;

import org.jetbrains.annotations.Nullable;

/**
 * Tries to find the configuration properties in the environment.
 */
final class EnvironmentConfigurationProvider implements ConfigurationProvider {
    /**
     * The prefix of the environment variables holding Sentry configuration.
     */
    public static final String ENV_VAR_PREFIX = "SENTRY_";

//    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfigurationProvider.class);

    @Nullable
    @Override
    public String getProperty(String key) {
        String ret = System.getenv(ENV_VAR_PREFIX + key.replace(".", "_").toUpperCase());

        if (ret != null) {
//            logger.debug("Found {}={} in System Environment Variables.", key, ret);
        }

        return ret;
    }
}
