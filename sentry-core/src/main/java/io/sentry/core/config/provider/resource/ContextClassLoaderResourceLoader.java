package io.sentry.core.config.provider.resource;

import java.io.InputStream;

/**
 * A {@link ResourceLoader} that considers the paths to be resource locations in the context classloader of the current
 * thread.
 */
public final class ContextClassLoaderResourceLoader implements ResourceLoader {
    @Override
    public InputStream getInputStream(String filepath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return classLoader.getResourceAsStream(filepath);
    }
}
