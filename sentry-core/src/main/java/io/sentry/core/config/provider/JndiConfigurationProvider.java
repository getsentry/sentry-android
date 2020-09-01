package io.sentry.core.config.provider;


import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

/**
 * A configuration provider that looks up the configuration in the JNDI registry.
 *
 * @see JndiSupport to check whether Jndi is available in the current JVM without needing to load this class
 */
final class JndiConfigurationProvider implements ConfigurationProvider {
    /**
     * The default prefix of the JNDI names of Sentry configuration. This, concatenated with the configuration key name
     * provided to the {@link #getProperty(String)} method, must form a valid JNDI name.
     */
    public static final String DEFAULT_JNDI_PREFIX = "java:comp/env/sentry/";

    private final String prefix;
    private final JndiContextProvider contextProvider;
    private final ILogger logger;

    /**
     * Constructs a new instance using the {@link #DEFAULT_JNDI_PREFIX} and {@link JndiContextProvider} that returns
     * a new {@link InitialContext} each time it is asked for a context. This ensures that any changes in the JNDI
     * environment are taken into account on the next configuration property lookup.
     */
    public JndiConfigurationProvider(ILogger logger) {
        this(DEFAULT_JNDI_PREFIX, new JndiContextProvider() {
            @Override
            public Context getContext() throws NamingException {
                return new InitialContext();
            }
        }, logger);
    }

    /**
     * Constructs a new instance that will look up the Sentry configuration keys using the provided prefix and using
     * the context obtained from the JNDI context provider. Because the JNDI environment is dynamic, Sentry uses the
     * JNDI context provider to obtain a fresh copy of the environment (if the provider chooses to return such).
     *  @param jndiNamePrefix The prefix of the JNDI names of Sentry configuration. This, concatenated with
     *                       the configuration key name provided to the {@link #getProperty(String)} method, must form
     *                       a valid JNDI name.
     * @param contextProvider an object able to provide instances of the JNDI context
     * @param logger the logger
     */
    public JndiConfigurationProvider(String jndiNamePrefix, JndiContextProvider contextProvider, ILogger logger) {
        this.prefix = jndiNamePrefix;
        this.contextProvider = contextProvider;
      this.logger = logger;
    }

    @Override
    public String getProperty(String key) {
        String value = null;
        try {
            Context ctx = contextProvider.getContext();
            value = (String) ctx.lookup(prefix + key);
        } catch (NoInitialContextException e) {
            logger.log(SentryLevel.DEBUG, "JNDI not configured for Sentry (NoInitialContextEx)");
        } catch (NamingException e) {
            logger.log(SentryLevel.DEBUG, "No " + prefix + key + " in JNDI");
        } catch (RuntimeException e) {
            logger.log(SentryLevel.WARNING, "Odd RuntimeException while testing for JNDI", e);
        }
        return value;
    }

    /**
     * A helper interface to be able to obtain application-specific JNDI context during Sentry configuration lookup.
     */
    public interface JndiContextProvider {
        /**
         * Returns the context to use when looking for a property in JNDI. Note that it is supposed that this method
         * returns a new context each time, but that might not be required or even correct depending on that
         * requirements of the supplier of the implementation of this interface.
         *
         * @return a possibly new instance of the JNDI context
         * @throws NamingException on JNDI error
         */
        Context getContext() throws NamingException;
    }
}
