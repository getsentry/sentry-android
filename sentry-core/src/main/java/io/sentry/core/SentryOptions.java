package io.sentry.core;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.cache.IEnvelopeCache;
import io.sentry.core.cache.IEventCache;
import io.sentry.core.protocol.SdkVersion;
import io.sentry.core.transport.ITransport;
import io.sentry.core.transport.ITransportGate;
import io.sentry.core.transport.NoOpEnvelopeCache;
import io.sentry.core.transport.NoOpEventCache;
import io.sentry.core.transport.NoOpTransport;
import io.sentry.core.transport.NoOpTransportGate;
import java.io.File;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Sentry SDK options */
@Open
public class SentryOptions extends SentryCommonOptions {

  /** Default Log level if not specified Default is DEBUG */
  static final SentryLevel DEFAULT_DIAGNOSTIC_LEVEL = SentryLevel.DEBUG;

  /**
   * Are callbacks that run for every event. They can either return a new event which in most cases
   * means just adding data OR return null in case the event will be dropped and not sent.
   */
  private final @NotNull List<EventProcessor> eventProcessors = new CopyOnWriteArrayList<>();

  /**
   * Code that provides middlewares, bindings or hooks into certain frameworks or environments,
   * along with code that inserts those bindings and activates them.
   */
  private final @NotNull List<Integration> integrations = new CopyOnWriteArrayList<>();

  /** Turns NDK on or off. Default is enabled. */
  private boolean enableNdk = true;

  /** Logger interface to log useful debugging information if debug is enabled */
  private @NotNull ILogger logger = NoOpLogger.getInstance();

  /** Serializer interface to serialize/deserialize json events */
  private @NotNull ISerializer serializer = NoOpSerializer.getInstance();

  private @NotNull IEnvelopeReader envelopeReader = new EnvelopeReader();

  /**
   * Sentry client name used for the HTTP authHeader and userAgent eg
   * sentry.{language}.{platform}/{version} eg sentry.java.android/2.0.0 would be a valid case
   */
  private @Nullable String sentryClientName;

  /**
   * This function is called with an SDK specific event object and can return a modified event
   * object or nothing to skip reporting the event
   */
  private @Nullable BeforeSendCallback beforeSend;

  /**
   * This function is called with an SDK specific breadcrumb object before the breadcrumb is added
   * to the scope. When nothing is returned from the function, the breadcrumb is dropped
   */
  private @Nullable BeforeBreadcrumbCallback beforeBreadcrumb;

  /** The cache dir. path for caching offline events */
  private @Nullable String cacheDirPath;

  /** The cache dir. size for capping the number of events Default is 10 */
  private int cacheDirSize = 10;

  /** The sessions dir. size for capping the number of envelopes Default is 100 */
  private int sessionsDirSize = 100;

  /** Max. queue size before flushing events/envelopes to the disk */
  private int maxQueueSize = cacheDirSize + sessionsDirSize;

  /**
   * When set, a proxy can be configured that should be used for outbound requests. This is also
   * used for HTTPS requests
   */
  private @Nullable Proxy proxy;

  /** The transport is an internal construct of the client that abstracts away the event sending. */
  private @NotNull ITransport transport = NoOpTransport.getInstance();

  /**
   * Implementations of this interface serve as gatekeepers that allow or disallow sending of the
   * events
   */
  private @NotNull ITransportGate transportGate = NoOpTransportGate.getInstance();

  /** Whether to enable automatic session tracking. */
  private boolean enableSessionTracking;

  /**
   * The session tracking interval in millis. This is the interval to end a session if the App goes
   * to the background.
   */
  private long sessionTrackingIntervalMillis = 30000; // 30s

  /** The distinct Id (generated Guid) used for session tracking */
  private String distinctId;

  /*
  When enabled, Sentry installs UncaughtExceptionHandlerIntegration.
   */
  private boolean enableUncaughtExceptionHandler = true;

  /** Sentry Executor Service that sends cached events and envelopes on App. start. */
  private @NotNull ISentryExecutorService executorService;

  /** Reads and caches event json files in the disk */
  private @NotNull IEventCache eventDiskCache = NoOpEventCache.getInstance();

  /** Reads and caches envelope files in the disk */
  private @NotNull IEnvelopeCache envelopeDiskCache = NoOpEnvelopeCache.getInstance();

  /** SdkVersion object that contains the Sentry Client Name and its version */
  private @Nullable SdkVersion sdkVersion;

  /**
   * Adds an event processor
   *
   * @param eventProcessor the event processor
   */
  public void addEventProcessor(@NotNull EventProcessor eventProcessor) {
    eventProcessors.add(eventProcessor);
  }

  /**
   * Returns the list of event processors
   *
   * @return the event processor list
   */
  public @NotNull List<EventProcessor> getEventProcessors() {
    return eventProcessors;
  }

  /**
   * Adds an integration
   *
   * @param integration the integration
   */
  public void addIntegration(@NotNull Integration integration) {
    integrations.add(integration);
  }

  /**
   * Returns the list of integrations
   *
   * @return the integration list
   */
  public @NotNull List<Integration> getIntegrations() {
    return integrations;
  }

  /**
   * Returns the Logger interface
   *
   * @return the logger
   */
  public @NotNull ILogger getLogger() {
    return logger;
  }

  /**
   * Sets the Logger interface if null, logger will be NoOp
   *
   * @param logger the logger interface
   */
  public void setLogger(final @Nullable ILogger logger) {
    this.logger = (logger == null) ? NoOpLogger.getInstance() : new DiagnosticLogger(this, logger);
  }

  /**
   * Returns the Serializer interface
   *
   * @return the serializer
   */
  public @NotNull ISerializer getSerializer() {
    return serializer;
  }

  /**
   * Sets the Serializer interface if null, Serializer will be NoOp
   *
   * @param serializer the serializer
   */
  public void setSerializer(@Nullable ISerializer serializer) {
    this.serializer = serializer != null ? serializer : NoOpSerializer.getInstance();
  }

  public @NotNull IEnvelopeReader getEnvelopeReader() {
    return envelopeReader;
  }

  public void setEnvelopeReader(final @Nullable IEnvelopeReader envelopeReader) {
    this.envelopeReader =
        envelopeReader != null ? envelopeReader : NoOpEnvelopeReader.getInstance();
  }

  /**
   * Check if NDK is ON or OFF Default is ON
   *
   * @return true if ON or false otherwise
   */
  public boolean isEnableNdk() {
    return enableNdk;
  }

  /**
   * Sets NDK to ON or OFF
   *
   * @param enableNdk true if ON or false otherwise
   */
  public void setEnableNdk(boolean enableNdk) {
    this.enableNdk = enableNdk;
  }

  /**
   * Returns the Sentry client name
   *
   * @return the Sentry client name or null if not set
   */
  public @Nullable String getSentryClientName() {
    return sentryClientName;
  }

  /**
   * Sets the Sentry client name
   *
   * @param sentryClientName the Sentry client name
   */
  public void setSentryClientName(@Nullable String sentryClientName) {
    this.sentryClientName = sentryClientName;
  }

  /**
   * Returns the BeforeSend callback
   *
   * @return the beforeSend callback or null if not set
   */
  public @Nullable BeforeSendCallback getBeforeSend() {
    return beforeSend;
  }

  /**
   * Sets the beforeSend callback
   *
   * @param beforeSend the beforeSend callback
   */
  public void setBeforeSend(@Nullable BeforeSendCallback beforeSend) {
    this.beforeSend = beforeSend;
  }

  /**
   * Returns the beforeBreadcrumb callback
   *
   * @return the beforeBreadcrumb callback or null if not set
   */
  public @Nullable BeforeBreadcrumbCallback getBeforeBreadcrumb() {
    return beforeBreadcrumb;
  }

  /**
   * Sets the beforeBreadcrumb callback
   *
   * @param beforeBreadcrumb the beforeBreadcrumb callback
   */
  public void setBeforeBreadcrumb(@Nullable BeforeBreadcrumbCallback beforeBreadcrumb) {
    this.beforeBreadcrumb = beforeBreadcrumb;
  }

  /**
   * Returns the cache dir. path if set
   *
   * @return the cache dir. path or null if not set
   */
  public @Nullable String getCacheDirPath() {
    return cacheDirPath;
  }

  /**
   * Returns the outbox path if cacheDirPath is set
   *
   * @return the outbox path or null if not set
   */
  public @Nullable String getOutboxPath() {
    if (cacheDirPath == null || cacheDirPath.isEmpty()) {
      return null;
    }
    return cacheDirPath + File.separator + "outbox";
  }

  /**
   * Returns the sessions path if cacheDirPath is set
   *
   * @return the sessions path or null if not set
   */
  public @Nullable String getSessionsPath() {
    if (cacheDirPath == null || cacheDirPath.isEmpty()) {
      return null;
    }
    return cacheDirPath + File.separator + "sessions";
  }

  /**
   * Sets the cache dir. path
   *
   * @param cacheDirPath the cache dir. path
   */
  public void setCacheDirPath(@Nullable String cacheDirPath) {
    this.cacheDirPath = cacheDirPath;
  }

  /**
   * Returns the cache dir. size Default is 10
   *
   * @return the cache dir. size
   */
  public int getCacheDirSize() {
    return cacheDirSize;
  }

  /**
   * Sets the cache dir. size Default is 10
   *
   * @param cacheDirSize the cache dir. size
   */
  public void setCacheDirSize(int cacheDirSize) {
    this.cacheDirSize = cacheDirSize;
  }

  /**
   * Returns the proxy if set
   *
   * @return the proxy or null if not set
   */
  public @Nullable Proxy getProxy() {
    return proxy;
  }

  /**
   * Sets the proxy
   *
   * @param proxy the proxy
   */
  public void setProxy(@Nullable Proxy proxy) {
    this.proxy = proxy;
  }

  /**
   * Returns the Transport interface
   *
   * @return the transport
   */
  public @NotNull ITransport getTransport() {
    return transport;
  }

  /**
   * Sets the Transport interface
   *
   * @param transport the transport
   */
  public void setTransport(@Nullable ITransport transport) {
    this.transport = transport != null ? transport : NoOpTransport.getInstance();
  }

  /**
   * Returns the TransportGate interface
   *
   * @return the transport gate
   */
  public @NotNull ITransportGate getTransportGate() {
    return transportGate;
  }

  /**
   * Sets the TransportGate interface
   *
   * @param transportGate the transport gate
   */
  public void setTransportGate(@Nullable ITransportGate transportGate) {
    this.transportGate = (transportGate != null) ? transportGate : NoOpTransportGate.getInstance();
  }

  /**
   * Returns if the automatic session tracking is enabled or not
   *
   * @return true if enabled or false otherwise
   */
  public boolean isEnableSessionTracking() {
    return enableSessionTracking;
  }

  /**
   * Enable or disable the automatic session tracking
   *
   * @param enableSessionTracking true if enabled or false otherwise
   */
  public void setEnableSessionTracking(boolean enableSessionTracking) {
    this.enableSessionTracking = enableSessionTracking;
  }

  /**
   * Returns the sessions dir size
   *
   * @return the dir size
   */
  public int getSessionsDirSize() {
    return sessionsDirSize;
  }

  /**
   * Sets the sessions dir size
   *
   * @param sessionsDirSize the sessions dir size
   */
  public void setSessionsDirSize(int sessionsDirSize) {
    this.sessionsDirSize = sessionsDirSize;
  }

  /**
   * Returns the session tracking interval in millis
   *
   * @return the interval in millis
   */
  public long getSessionTrackingIntervalMillis() {
    return sessionTrackingIntervalMillis;
  }

  /**
   * Sets the session tracking interval in millis
   *
   * @param sessionTrackingIntervalMillis the interval in millis
   */
  public void setSessionTrackingIntervalMillis(long sessionTrackingIntervalMillis) {
    this.sessionTrackingIntervalMillis = sessionTrackingIntervalMillis;
  }

  /**
   * Returns the distinct Id
   *
   * @return the distinct Id
   */
  @ApiStatus.Internal
  public String getDistinctId() {
    return distinctId;
  }

  /**
   * Sets the distinct Id
   *
   * @param distinctId the distinct Id
   */
  @ApiStatus.Internal
  public void setDistinctId(String distinctId) {
    this.distinctId = distinctId;
  }

  /**
   * Checks if the default UncaughtExceptionHandlerIntegration is enabled or not.
   *
   * @return true if enabled or false otherwise.
   */
  public boolean isEnableUncaughtExceptionHandler() {
    return enableUncaughtExceptionHandler;
  }

  /**
   * Enable or disable the default UncaughtExceptionHandlerIntegration.
   *
   * @param enableUncaughtExceptionHandler true if enabled or false otherwise.
   */
  public void setEnableUncaughtExceptionHandler(boolean enableUncaughtExceptionHandler) {
    this.enableUncaughtExceptionHandler = enableUncaughtExceptionHandler;
  }

  /**
   * Returns the SentryExecutorService
   *
   * @return the SentryExecutorService
   */
  @NotNull
  ISentryExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Sets the SentryExecutorService
   *
   * @param executorService the SentryExecutorService
   */
  void setExecutorService(final @NotNull ISentryExecutorService executorService) {
    if (executorService != null) {
      this.executorService = executorService;
    }
  }

  /**
   * Returns the EventCache interface
   *
   * @return the EventCache object
   */
  public @NotNull IEventCache getEventDiskCache() {
    return eventDiskCache;
  }

  /**
   * Sets the EventCache interface
   *
   * @param eventDiskCache the EventCache object
   */
  public void setEventDiskCache(final @Nullable IEventCache eventDiskCache) {
    this.eventDiskCache = eventDiskCache != null ? eventDiskCache : NoOpEventCache.getInstance();
  }

  /**
   * Returns the EnvelopeCache interface
   *
   * @return the EnvelopeCache object
   */
  public @NotNull IEnvelopeCache getEnvelopeDiskCache() {
    return envelopeDiskCache;
  }

  /**
   * Sets the EnvelopeCache interface
   *
   * @param envelopeDiskCache the EnvelopeCache object
   */
  public void setEnvelopeDiskCache(final @Nullable IEnvelopeCache envelopeDiskCache) {
    this.envelopeDiskCache =
        envelopeDiskCache != null ? envelopeDiskCache : NoOpEnvelopeCache.getInstance();
  }

  /**
   * Returns the Max queue size
   *
   * @return the max queue size
   */
  public int getMaxQueueSize() {
    return maxQueueSize;
  }

  /**
   * Sets the max queue size if maxQueueSize is bigger than 0
   *
   * @param maxQueueSize max queue size
   */
  public void setMaxQueueSize(int maxQueueSize) {
    if (maxQueueSize > 0) {
      this.maxQueueSize = maxQueueSize;
    }
  }

  /**
   * Returns the SdkVersion object
   *
   * @return the SdkVersion object or null
   */
  public @Nullable SdkVersion getSdkVersion() {
    return sdkVersion;
  }

  /**
   * Sets the SdkVersion object
   *
   * @param sdkVersion the SdkVersion object or null
   */
  @ApiStatus.Internal
  public void setSdkVersion(final @Nullable SdkVersion sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  /** The BeforeSend callback */
  public interface BeforeSendCallback {

    /**
     * Mutates or drop an event before being sent
     *
     * @param event the event
     * @param hint the hint, usually the source of the event
     * @return the original event or the mutated event or null if event was dropped
     */
    @Nullable
    SentryEvent execute(@NotNull SentryEvent event, @Nullable Object hint);
  }

  /** The BeforeBreadcrumb callback */
  public interface BeforeBreadcrumbCallback {

    /**
     * Mutates or drop a callback before being added
     *
     * @param breadcrumb the breadcrumb
     * @param hint the hint, usually the source of the breadcrumb
     * @return the original breadcrumb or the mutated breadcrumb of null if breadcrumb was dropped
     */
    @Nullable
    Breadcrumb execute(@NotNull Breadcrumb breadcrumb, @Nullable Object hint);
  }

  /** SentryOptions ctor It adds and set default things */
  public SentryOptions() {
    // SentryExecutorService should be inited before any SendCachedEventFireAndForgetIntegration
    executorService = new SentryExecutorService();

    // UncaughtExceptionHandlerIntegration should be inited before any other Integration.
    // if there's an error on the setup, we are able to capture it
    integrations.add(new UncaughtExceptionHandlerIntegration());

    integrations.add(new ShutdownHookIntegration());

    eventProcessors.add(new MainEventProcessor(this));

    setSentryClientName(BuildConfig.SENTRY_JAVA_SDK_NAME + "/" + BuildConfig.VERSION_NAME);
    setSdkVersion(createSdkVersion());
  }

  private @NotNull SdkVersion createSdkVersion() {
    final SdkVersion sdkVersion = new SdkVersion();

    sdkVersion.setName(BuildConfig.SENTRY_JAVA_SDK_NAME);
    String version = BuildConfig.VERSION_NAME;
    sdkVersion.setVersion(version);
    sdkVersion.addPackage("maven:sentry-core", version);

    return sdkVersion;
  }
}
