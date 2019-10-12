package io.sentry.core;

import io.sentry.core.protocol.Contexts;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SentryEvent {
  private io.sentry.core.protocol.SentryId eventId;
  private Date timestamp;
  private Throwable throwable;
  private io.sentry.core.protocol.Message message;
  private String serverName;
  private String platform;
  private String release;
  private String dist;
  private String logger;
  private SentryValues<io.sentry.core.protocol.SentryThread> threads;
  private SentryValues<io.sentry.core.protocol.SentryException> exceptions;
  private SentryLevel level;
  private String transaction;
  private String environment;
  private io.sentry.core.protocol.User user;
  private io.sentry.core.protocol.Request request;
  private io.sentry.core.protocol.SdkVersion sdkVersion;
  private io.sentry.core.protocol.Contexts contexts = new io.sentry.core.protocol.Contexts();
  private List<String> fingerprint = new ArrayList<>();
  private List<Breadcrumb> breadcrumbs = new ArrayList<>();
  private Map<String, String> tags = new HashMap<>();
  private Map<String, Object> extra = new HashMap<>();

  SentryEvent(io.sentry.core.protocol.SentryId eventId, Date timestamp) {
    this.eventId = eventId;
    this.timestamp = timestamp;
  }

  public SentryEvent(Throwable throwable) {
    this();
    this.throwable = throwable;
  }

  public SentryEvent() {
    this(new io.sentry.core.protocol.SentryId(), DateUtils.getCurrentDateTime());
  }

  public io.sentry.core.protocol.SentryId getEventId() {
    return eventId;
  }

  public Date getTimestamp() {
    return (Date) timestamp.clone();
  }

  String getTimestampIsoFormat() {
    return DateUtils.getTimestampIsoFormat(timestamp);
  }

  Throwable getThrowable() {
    return throwable;
  }

  public io.sentry.core.protocol.Message getMessage() {
    return message;
  }

  public void setMessage(io.sentry.core.protocol.Message message) {
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

  public List<io.sentry.core.protocol.SentryThread> getThreads() {
    return threads.getValues();
  }

  public void setThreads(List<io.sentry.core.protocol.SentryThread> threads) {
    this.threads = new SentryValues<>(threads);
  }

  public List<io.sentry.core.protocol.SentryException> getExceptions() {
    return exceptions.getValues();
  }

  public void setExceptions(List<io.sentry.core.protocol.SentryException> exceptions) {
    this.exceptions = new SentryValues<>(exceptions);
  }

  public void setEventId(io.sentry.core.protocol.SentryId eventId) {
    this.eventId = eventId;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public void setThreads(SentryValues<io.sentry.core.protocol.SentryThread> threads) {
    this.threads = threads;
  }

  public void setExceptions(SentryValues<io.sentry.core.protocol.SentryException> exceptions) {
    this.exceptions = exceptions;
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

  public io.sentry.core.protocol.User getUser() {
    return user;
  }

  public void setUser(io.sentry.core.protocol.User user) {
    this.user = user;
  }

  public io.sentry.core.protocol.Request getRequest() {
    return request;
  }

  public void setRequest(io.sentry.core.protocol.Request request) {
    this.request = request;
  }

  public io.sentry.core.protocol.SdkVersion getSdkVersion() {
    return sdkVersion;
  }

  public void setSdkVersion(io.sentry.core.protocol.SdkVersion sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  public List<String> getFingerprint() {
    return fingerprint;
  }

  public void setFingerprint(List<String> fingerprint) {
    this.fingerprint = fingerprint;
  }

  public List<Breadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  public void setBreadcrumbs(ArrayList<Breadcrumb> breadcrumbs) {
    this.breadcrumbs = breadcrumbs;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(HashMap<String, String> tags) {
    this.tags = tags;
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(HashMap<String, Object> extra) {
    this.extra = extra;
  }

  public io.sentry.core.protocol.Contexts getContexts() {
    return contexts;
  }

  public void setContexts(Contexts contexts) {
    this.contexts = contexts;
  }
}
