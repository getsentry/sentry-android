package io.sentry.core.config.location;

import org.jetbrains.annotations.Nullable;

/**
 * Tries to find the location of the Sentry configuration file in some environment variable.
 */
public final class EnvironmentBasedLocator implements ConfigurationResourceLocator {
    /**
     * The default environment variable to use for obtaining the location of the Sentry configuration file.
     */
    public static final String DEFAULT_ENV_VAR_NAME = "SENTRY_PROPERTIES_FILE";

    private final String envVarName;

    /**
     * Constructs a new instance that will use the environment variable defined in {@link #DEFAULT_ENV_VAR_NAME}.
     */
    public EnvironmentBasedLocator() {
        this(DEFAULT_ENV_VAR_NAME);
    }

    /**
     * Constructs a new instance that will use the provided environment variable.
     *
     * @param envVarName the name of the environment variable to use
     */
    public EnvironmentBasedLocator(String envVarName) {
        this.envVarName = envVarName;
    }

    @Override
    @Nullable
    public String getConfigurationResourcePath() {
        return System.getenv(envVarName);
    }
}
