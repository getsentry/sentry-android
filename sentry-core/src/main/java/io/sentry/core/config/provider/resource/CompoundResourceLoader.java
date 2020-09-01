package io.sentry.core.config.provider.resource;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * A compound resource loader that returns the first non-null stream of the list of the loaders provided at
 * the instantiation time.
 */
public final class CompoundResourceLoader implements ResourceLoader {
    private final Collection<ResourceLoader> loaders;

    /**
     * Instantiates new resource loader using the provided loaders.
     *
     * @param loaders the loaders to wrap
     */
    public CompoundResourceLoader(ResourceLoader ... loaders) {
        this.loaders = Arrays.asList(loaders);
    }

    /**
     * This goes through the wrapped resource loaders in the iteration order and returns the first non-null input stream
     * obtained from the wrapped resource loaders.
     *
     * @param filepath  Path of the resource to open
     * @return the input stream with the contents of the resource on given path or null if none could be found
     */
    @Nullable
    @Override
    public InputStream getInputStream(String filepath) {
        for (ResourceLoader l : loaders) {
            InputStream is = l.getInputStream(filepath);
            if (is != null) {
                return is;
            }
        }

        return null;
    }
}
