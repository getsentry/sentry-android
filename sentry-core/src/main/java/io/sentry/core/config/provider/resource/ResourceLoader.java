package io.sentry.core.config.provider.resource;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

/**
 * Interface for platform-specific resource loaders.
 */
public interface ResourceLoader {
    /**
     * Returns an InputStream from the resource at {@code filepath} or null, if it was not possible
     * to open the resource.
     *
     * @param filepath  Path of the resource to open
     * @return  Resource's input stream or null
     */
    @Nullable
    InputStream getInputStream(String filepath);
}
