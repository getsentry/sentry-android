package io.sentry.core.config.provider.resource;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A {@link ResourceLoader} that considers the paths to be filesystem locations.
 */
public final class FileResourceLoader implements ResourceLoader {
//    private static final Logger logger = LoggerFactory.getLogger(io.sentry.config.FileResourceLoader.class);

    @Nullable
    @Override
    public InputStream getInputStream(String filepath) {
        File f = new File(filepath);
        if (f.isFile() && f.canRead()) {
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
//                logger.debug("Configuration file {} could not be found even though we just checked it can be read...",
//                        filepath);
            }
        } else {
//            logger.debug("The configuration file {} (which resolves to absolute path {}) doesn't exist, is not a file"
//                    + " or is not readable.", f, f.getAbsolutePath());
        }

        return null;
    }
}
