package io.sentry.core;

import io.sentry.core.protocol.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

public final class SentryEvent implements IUnknownPropertiesConsumer {
  private SentryId eventId;
  private final Date timestamp;
  private transient Throwable throwable;
  private Message message;
  private String serverName;
  private String platform;
  private String release;
  private String dist;
  private String logger;
  private SentryValues<SentryThread> threads;
  private SentryValues<SentryException> exception;
  private SentryLevel level;
  private String transaction;
  private String environment;
  private User user;
  private Request request;
  private SdkVersion sdk;
  private Contexts contexts = new Contexts();
  private List<String> fingerprint;
  private List<Breadcrumb> breadcrumbs;
  private Map<String, String> tags;
  private Map<String, Object> extra;
  private Map<String, Object> unknown;
  private Map<String, String> modules;
  private DebugMeta debugMeta;

  SentryEvent(SentryId eventId, final Date timestamp) {
    this.eventId = eventId;
    this.timestamp = timestamp;
  }

  public SentryEvent(Throwable throwable) {
    this();
    this.throwable = throwable;
  }

  public SentryEvent() {
    this(new SentryId(), DateUtils.getCurrentDateTime());
  }

  @TestOnly
  public SentryEvent(final Date timestamp) {
    this(new SentryId(), timestamp);
  }

  public SentryId getEventId() {
    return eventId;
  }

  public Date getTimestamp() {
    return (Date) timestamp.clone();
  }

  Throwable getThrowable() {
    return throwable;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public String getDist() {
    return dist;
  }

  public void setDist(String dist) {
    this.dist = dist;
  }

  public String getLogger() {
    return logger;
  }

  public void setLogger(String logger) {
    this.logger = logger;
  }

  public List<SentryThread> getThreads() {
    if (threads != null) {
      return threads.getValues();
    } else {
      return null;
    }
  }

  public void setThreads(List<SentryThread> threads) {
    this.threads = new SentryValues<>(threads);
  }

  public List<SentryException> getExceptions() {
    return exception == null ? null : exception.getValues();
  }

  public void setExceptions(List<SentryException> exception) {
    this.exception = new SentryValues<>(exception);
  }

  public void setEventId(SentryId eventId) {
    this.eventId = eventId;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public SentryLevel getLevel() {
    return level;
  }

  public void setLevel(SentryLevel level) {
    this.level = level;
  }

  public String getTransaction() {
    return transaction;
  }

  public void setTransaction(String transaction) {
    this.transaction = transaction;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Request getRequest() {
    return request;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public SdkVersion getSdk() {
    return sdk;
  }

  public void setSdk(SdkVersion sdk) {
    this.sdk = sdk;
  }

  List<String> getFingerprints() {
    return fingerprint;
  }

  public void setFingerprints(List<String> fingerprint) {
    this.fingerprint = fingerprint;
  }

  public List<Breadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  public void setBreadcrumbs(List<Breadcrumb> breadcrumbs) {
    this.breadcrumbs = breadcrumbs;
  }

  public void addBreadcrumb(Breadcrumb breadcrumb) {
    if (breadcrumbs == null) {
      breadcrumbs = new ArrayList<>();
    }
    breadcrumbs.add(breadcrumb);
  }

  Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public void removeTag(@NotNull String key) {
    if (tags != null) {
      tags.remove(key);
    }
  }

  public void setTag(String key, String value) {
    if (tags == null) {
      tags = new HashMap<>();
    }
    tags.put(key, value);
  }

  Map<String, Object> getExtras() {
    return extra;
  }

  public void setExtras(Map<String, Object> extra) {
    this.extra = extra;
  }

  public void setExtra(String key, Object value) {
    if (extra == null) {
      extra = new HashMap<>();
    }
    extra.put(key, value);
  }

  public void removeExtra(@NotNull String key) {
    if (extra != null) {
      extra.remove(key);
    }
  }

  public Contexts getContexts() {
    return contexts;
  }

  public void setContexts(Contexts contexts) {
    this.contexts = contexts;
  }

  @ApiStatus.Internal
  @Override
  public void acceptUnknownProperties(Map<String, Object> unknown) {
    this.unknown = unknown;
  }

  @TestOnly
  public Map<String, Object> getUnknown() {
    return unknown;
  }

  Map<String, String> getModules() {
    return modules;
  }

  public void setModules(Map<String, String> modules) {
    this.modules = modules;
  }

  public void setModule(String key, String value) {
    if (modules == null) {
      modules = new HashMap<>();
    }
    modules.put(key, value);
  }

  public void removeModule(@NotNull String key) {
    if (modules != null) {
      modules.remove(key);
    }
  }

  public DebugMeta getDebugMeta() {
    return debugMeta;
  }

  public void setDebugMeta(DebugMeta debugMeta) {
    this.debugMeta = debugMeta;
  }

  /**
   * Returns true if Level is Fatal or any exception was unhandled by the user.
   *
   * @return true if its crashed or false otherwise
   */
  public boolean isCrashed() {
    if (level == SentryLevel.FATAL) {
      return true;
    }
    if (exception != null) {
      for (SentryException e : exception.getValues()) {
        if (e.getMechanism() != null
            && e.getMechanism().isHandled() != null
            && !e.getMechanism().isHandled()) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns true if this event has any sort of excetion
   *
   * @return true if errored or false otherwise
   */
  public boolean isErrored() {
    return exception != null && !exception.getValues().isEmpty();
  }
}
