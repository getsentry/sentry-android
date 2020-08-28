package io.sentry.core;

import static io.sentry.core.SentryOptions.DEFAULT_DIAGNOSTIC_LEVEL;

import com.jakewharton.nopen.annotation.Open;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Subset of {@link SentryOptions} properties used in 3rd party framework integrations. */
@Open
public class SentryCommonOptions {

  /**
   * The DSN tells the SDK where to send the events to. If this value is not provided, the SDK will
   * just not send any events.
   */
  private @Nullable String dsn;

  /**
   * Controls how many seconds to wait before shutting down. Sentry SDKs send events from a
   * background queue and this queue is given a certain amount to drain pending events Default is
   * 2000 = 2s
   */
  private long shutdownTimeout = 2000; // 2s

  /**
   * Controls how many seconds to wait before flushing down. Sentry SDKs cache events from a
   * background queue and this queue is given a certain amount to drain pending events Default is
   * 15000 = 15s
   */
  private long flushTimeoutMillis = 15000; // 15s

  /** connection timeout in milliseconds. */
  private int connectionTimeoutMillis = 5000;

  /** minimum LogLevel to be used if debug is enabled */
  private @NotNull SentryLevel diagnosticLevel = DEFAULT_DIAGNOSTIC_LEVEL;

  /**
   * Turns debug mode on or off. If debug is enabled SDK will attempt to print out useful debugging
   * information if something goes wrong. Default is disabled.
   */
  private boolean debug;

  /**
   * This variable controls the total amount of breadcrumbs that should be captured Default is 100
   */
  private int maxBreadcrumbs = 100;

  /** Sets the release. SDK will try to automatically configure a release out of the box */
  private @Nullable String release;

  /**
   * Sets the environment. This string is freeform and not set by default. A release can be
   * associated with more than one environment to separate them in the UI Think staging vs prod or
   * similar.
   */
  private @Nullable String environment;

  /**
   * Configures the sample rate as a percentage of events to be sent in the range of 0.0 to 1.0. if
   * 1.0 is set it means that 100% of events are sent. If set to 0.1 only 10% of events will be
   * sent. Events are picked randomly. Default is null (disabled)
   */
  private @Nullable Double sampleRate;

  /**
   * A list of string prefixes of module names that do not belong to the app, but rather third-party
   * packages. Modules considered not to be part of the app will be hidden from stack traces by
   * default.
   */
  private @NotNull List<String> inAppExcludes = new CopyOnWriteArrayList<>();

  /**
   * A list of string prefixes of module names that belong to the app. This option takes precedence
   * over inAppExcludes.
   */
  private @NotNull List<String> inAppIncludes = new CopyOnWriteArrayList<>();

  /** Sets the distribution. Think about it together with release and environment */
  private @Nullable String dist;

  /** When enabled, threads are automatically attached to all logged events. */
  private boolean attachThreads = true;

  /**
   * When enabled, stack traces are automatically attached to all threads logged. Stack traces are
   * always attached to exceptions but when this is set stack traces are also sent with threads
   */
  private boolean attachStacktrace;

  /** The server name used in the Sentry messages. */
  private String serverName;

  /** read timeout in milliseconds */
  private int readTimeoutMillis = 5000;

  /** whether to ignore TLS errors */
  private boolean bypassSecurity = false;

  /**
   * Applies configuration from this instance to the {@link SentryOptions} instance.
   *
   * @param options the instance of {@link SentryOptions} to apply the configuration to
   */
  public void applyTo(SentryOptions options) {
    if (dsn != null) {
      options.setDsn(dsn);
    }
    if (environment != null) {
      options.setEnvironment(environment);
    }
    if (sampleRate != null) {
      options.setSampleRate(sampleRate);
    }
    if (dist != null) {
      options.setDist(dist);
    }
    if (release != null) {
      options.setRelease(release);
    }
    if (sampleRate != null) {
      options.setSampleRate(sampleRate);
    }
    if (serverName != null) {
      options.setServerName(serverName);
    }
    for (String inAppExclude : inAppExcludes) {
      options.addInAppExclude(inAppExclude);
    }
    for (String inAppInclude : inAppIncludes) {
      options.addInAppInclude(inAppInclude);
    }
    options.setMaxBreadcrumbs(maxBreadcrumbs);
    options.setShutdownTimeout(shutdownTimeout);
    options.setFlushTimeoutMillis(flushTimeoutMillis);
    options.setReadTimeoutMillis(readTimeoutMillis);
    options.setBypassSecurity(bypassSecurity);
    options.setDebug(debug);
    options.setAttachThreads(attachThreads);
    options.setAttachStacktrace(attachStacktrace);
    options.setDiagnosticLevel(diagnosticLevel);
  }

  /**
   * Returns the DSN
   *
   * @return the DSN or null if not set
   */
  public @Nullable String getDsn() {
    return dsn;
  }

  /**
   * Sets the DSN
   *
   * @param dsn the DSN
   */
  public void setDsn(@Nullable String dsn) {
    this.dsn = dsn;
  }

  /**
   * Check if debug mode is ON Default is OFF
   *
   * @return true if ON or false otherwise
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Sets the debug mode to ON or OFF Default is OFF
   *
   * @param debug true if ON or false otherwise
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Returns the minimum LogLevel
   *
   * @return the log level
   */
  public @NotNull SentryLevel getDiagnosticLevel() {
    return diagnosticLevel;
  }

  /**
   * Sets the minimum LogLevel if null, it uses the default min. LogLevel Default is DEBUG
   *
   * @param diagnosticLevel the log level
   */
  public void setDiagnosticLevel(@Nullable final SentryLevel diagnosticLevel) {
    this.diagnosticLevel = (diagnosticLevel != null) ? diagnosticLevel : DEFAULT_DIAGNOSTIC_LEVEL;
  }

  /**
   * Returns the shutdown timeout in Millis
   *
   * @return the timeout in Millis
   */
  public long getShutdownTimeout() {
    return shutdownTimeout;
  }

  /**
   * Sets the shutdown timeout in Millis Default is 2000 = 2s
   *
   * @param shutdownTimeoutMillis the shutdown timeout in millis
   */
  public void setShutdownTimeout(long shutdownTimeoutMillis) {
    this.shutdownTimeout = shutdownTimeoutMillis;
  }

  /**
   * Returns the max Breadcrumbs Default is 100
   *
   * @return the max breadcrumbs
   */
  public int getMaxBreadcrumbs() {
    return maxBreadcrumbs;
  }

  /**
   * Sets the max breadcrumbs Default is 100
   *
   * @param maxBreadcrumbs the max breadcrumbs
   */
  public void setMaxBreadcrumbs(int maxBreadcrumbs) {
    this.maxBreadcrumbs = maxBreadcrumbs;
  }

  /**
   * Returns the release
   *
   * @return the release or null if not set
   */
  public @Nullable String getRelease() {
    return release;
  }

  /**
   * Sets the release
   *
   * @param release the release
   */
  public void setRelease(@Nullable String release) {
    this.release = release;
  }

  /**
   * Returns the environment
   *
   * @return the environment or null if not set
   */
  public @Nullable String getEnvironment() {
    return environment;
  }

  /**
   * Sets the environment
   *
   * @param environment the environment
   */
  public void setEnvironment(@Nullable String environment) {
    this.environment = environment;
  }

  /**
   * Returns the sample rate Default is null (disabled)
   *
   * @return the sample rate
   */
  public @Nullable Double getSampleRate() {
    return sampleRate;
  }

  /**
   * Sets the sampleRate Can be anything between 0.01 and 1.0 or null (default), to disable it.
   *
   * @param sampleRate the sample rate
   */
  public void setSampleRate(Double sampleRate) {
    if (sampleRate != null && (sampleRate > 1.0 || sampleRate <= 0.0)) {
      throw new IllegalArgumentException(
          "The value "
              + sampleRate
              + " is not valid. Use null to disable or values between 0.01 (inclusive) and 1.0 (exclusive).");
    }
    this.sampleRate = sampleRate;
  }

  /**
   * the list of inApp excludes
   *
   * @return the inApp excludes list
   */
  public @NotNull List<String> getInAppExcludes() {
    return inAppExcludes;
  }

  public void setInAppExcludes(List<String> inAppExcludes) {
    this.inAppExcludes = new CopyOnWriteArrayList<>(inAppExcludes);
  }

  /**
   * Adds an inApp exclude
   *
   * @param exclude the inApp exclude module/package
   */
  public void addInAppExclude(@NotNull String exclude) {
    inAppExcludes.add(exclude);
  }

  /**
   * Returns the inApp includes list
   *
   * @return the inApp includes list
   */
  public @NotNull List<String> getInAppIncludes() {
    return inAppIncludes;
  }

  /**
   * Adds an inApp include
   *
   * @param include the inApp include module/package
   */
  public void addInAppInclude(@NotNull String include) {
    inAppIncludes.add(include);
  }

  public void setInAppIncludes(List<String> inAppIncludes) {
    this.inAppIncludes = new CopyOnWriteArrayList<>(inAppIncludes);
  }

  /**
   * Sets the distribution
   *
   * @return the distribution or null if not set
   */
  public @Nullable String getDist() {
    return dist;
  }

  /**
   * Sets the distribution
   *
   * @param dist the distribution
   */
  public void setDist(@Nullable String dist) {
    this.dist = dist;
  }

  /**
   * Checks if the AttachStacktrace is enabled or not
   *
   * @return true if enabled or false otherwise
   */
  public boolean isAttachStacktrace() {
    return attachStacktrace;
  }

  /**
   * Sets the attachStacktrace to enabled or disabled
   *
   * @param attachStacktrace true if enabled or false otherwise
   */
  public void setAttachStacktrace(boolean attachStacktrace) {
    this.attachStacktrace = attachStacktrace;
  }

  /**
   * Checks if the AttachThreads is enabled or not
   *
   * @return true if enabled or false otherwise
   */
  public boolean isAttachThreads() {
    return attachThreads;
  }

  /**
   * Sets the attachThreads to enabled or disabled
   *
   * @param attachThreads true if enabled or false otherwise
   */
  public void setAttachThreads(boolean attachThreads) {
    this.attachThreads = attachThreads;
  }

  /**
   * Gets the default server name to be used in Sentry events.
   *
   * @return the default server name or null if none set
   */
  public @Nullable String getServerName() {
    return serverName;
  }

  /**
   * Sets the default server name to be used in Sentry events.
   *
   * @param serverName the default server name or null if none should be used
   */
  public void setServerName(@Nullable String serverName) {
    this.serverName = serverName;
  }

  /**
   * Returns the flush timeout in millis
   *
   * @return the timeout in millis
   */
  public long getFlushTimeoutMillis() {
    return flushTimeoutMillis;
  }

  /**
   * Sets the flush timeout in millis
   *
   * @param flushTimeoutMillis the timeout in millis
   */
  public void setFlushTimeoutMillis(long flushTimeoutMillis) {
    this.flushTimeoutMillis = flushTimeoutMillis;
  }

  /**
   * Returns the connection timeout in milliseconds.
   *
   * @return the connectionTimeoutMillis
   */
  public int getConnectionTimeoutMillis() {
    return connectionTimeoutMillis;
  }

  /**
   * Sets the connection timeout in milliseconds.
   *
   * @param connectionTimeoutMillis the connectionTimeoutMillis
   */
  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  /**
   * Returns the read timeout in milliseconds
   *
   * @return the readTimeoutMillis
   */
  public int getReadTimeoutMillis() {
    return readTimeoutMillis;
  }

  /**
   * Sets the read timeout in milliseconds
   *
   * @param readTimeoutMillis the readTimeoutMillis
   */
  public void setReadTimeoutMillis(int readTimeoutMillis) {
    this.readTimeoutMillis = readTimeoutMillis;
  }

  /**
   * Returns whether to ignore TLS errors
   *
   * @return the bypassSecurity
   */
  public boolean isBypassSecurity() {
    return bypassSecurity;
  }

  /**
   * Sets whether to ignore TLS errors
   *
   * @param bypassSecurity the bypassSecurity
   */
  public void setBypassSecurity(boolean bypassSecurity) {
    this.bypassSecurity = bypassSecurity;
  }
}
