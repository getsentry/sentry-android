package io.sentry.core.config.provider;


/**
 * A helper class to check whether JNDI is available in the current JVM.
 */
final class JndiSupport {
//    private static final Logger logger = LoggerFactory.getLogger(JndiSupport.class);

    private JndiSupport() {
    }

    /**
     * Checks whether JNDI is available.
     * @return true if JNDI is available, false otherwise
     */
    public static boolean isAvailable() {
        try {
            Class.forName("javax.naming.InitialContext", false, JndiSupport.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
//            logger.trace("JNDI is not available: " + e.getMessage());
            return false;
        }
    }
}
