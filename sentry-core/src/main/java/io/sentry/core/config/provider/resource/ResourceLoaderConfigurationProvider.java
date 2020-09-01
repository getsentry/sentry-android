package io.sentry.core.config.provider.resource;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import io.sentry.core.config.provider.ConfigurationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * A configuration provider that loads the properties from a {@link ResourceLoader}.
 */
@Open
public class ResourceLoaderConfigurationProvider implements ConfigurationProvider {

    @Nullable
    private final Properties properties;

    @NotNull
    private final ILogger logger;

    /**
     * Instantiates a new resource loader based configuration provider.
     * @param rl the resource loader used to load the configuration file
     * @param filePath the path to the configuration file as understood by the resource loader
     * @param charset the charset of the configuration file
     * @param logger the logger
     * @throws IOException on failure to process the configuration file contents
     */
    public ResourceLoaderConfigurationProvider(ResourceLoader rl, @Nullable String filePath, Charset charset, @NotNull ILogger logger)
            throws IOException {
      this.logger = logger;
      properties = loadProperties(rl, filePath, charset);
    }

    @Nullable
    private static Properties loadProperties(ResourceLoader rl, @Nullable String filePath, Charset charset)
            throws IOException {
        if (filePath == null) {
            return null;
        }

        InputStream is = rl.getInputStream(filePath);

        if (is == null) {
            return null;
        }

        try (InputStreamReader rdr = new InputStreamReader(is, charset)) {
            Properties props = new Properties();
            props.load(rdr);
            return props;
        }
    }

    @Override
    public String getProperty(String key) {
        if (properties == null) {
            return null;
        }

        String ret = properties.getProperty(key);

        if (ret != null) {
            logger.log(SentryLevel.DEBUG, "Found %s=%s in properties file.", key, ret);
        }

        return ret;
    }

}
