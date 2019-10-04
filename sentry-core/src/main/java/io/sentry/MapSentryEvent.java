package io.sentry;

import io.sentry.protocol.*;

import java.text.ParseException;
import java.util.*;

public class MapSentryEvent extends SentryEvent {

  private final Map<String, Object> map;
  private static final String sentryIdKey = "sentry_id";
  private static final String timestampKey = "timestamp";
  private static final String messageKey = "message";

  public MapSentryEvent(Map<String, Object> map) {
    super();
    if (map == null) {
      throw new IllegalArgumentException("Original map required.");
    }

    // Make sure the map is available before using getter/setter
    this.map = map;

    // Initialize default fields if not backed by the map
    if (getEventId() == SentryId.EMPTY_ID) {
      setEventId(new SentryId());
    }
    if (getTimestamp() == null) {
      setTimestamp(DateUtils.getCurrentDateTime());
    }
  }

  @Override
  public SentryId getEventId() {
    if (map.containsKey(sentryIdKey)) {
      Object sentryIdObject = map.get(sentryIdKey);
      if (sentryIdObject != null) {
        try {
          return new SentryId(sentryIdObject.toString());
        } catch (Exception e) {
        }
      }
    }
    return SentryId.EMPTY_ID;
  }

  @Override
  public void setEventId(SentryId eventId) {
    this.map.put(sentryIdKey, eventId.toString());
  }

  @Override
  public Date getTimestamp() {
      try {
        String dateString = getTimestampIsoFormat();
        if (dateString != null) {
          return DateUtils.getTimestampIsoFormat(dateString);
        }
      } catch (ParseException e) {
      }
    return null;
  }

  @Override
  public void setTimestamp(Date timestamp) {
    this.map.put(timestampKey, DateUtils.getTimestampIsoFormat(timestamp));
  }

  @Override
  String getTimestampIsoFormat() {
    if (map.containsKey(timestampKey)) {
      Object timestampObject = map.get(timestampKey);
      if (timestampObject != null) {
        return timestampObject.toString();
      }
    }
    return null;
  }

  @Override
  Throwable getThrowable() {
    return super.getThrowable();
  }

  <T> T getObject(String key, Class<T> cls) {
    if (map.containsKey(key)) {
      Object object = map.get(key);
      if (object != null && cls.isInstance(object)) {
        return cls.cast(object);
      }
    }
    return null;
  }

  void setObject(String key, Object object) {
    map.put(key, object);
  }

  @Override
  public Message getMessage() {
    return getObject(messageKey, Message.class);
  }

  @Override
  public void setMessage(Message message) {
    setObject(messageKey, message);
  }

  @Override
  public String getServerName() {
    return super.getServerName();
  }

  @Override
  public void setServerName(String serverName) {
    super.setServerName(serverName);
  }

  @Override
  public String getPlatform() {
    return super.getPlatform();
  }

  @Override
  public void setPlatform(String platform) {
    super.setPlatform(platform);
  }

  @Override
  public String getRelease() {
    return super.getRelease();
  }

  @Override
  public void setRelease(String release) {
    super.setRelease(release);
  }

  @Override
  public String getLogger() {
    return super.getLogger();
  }

  @Override
  public void setLogger(String logger) {
    super.setLogger(logger);
  }

  @Override
  public List<SentryThread> getThreads() {
    return super.getThreads();
  }

  @Override
  public void setThreads(List<SentryThread> threads) {
    super.setThreads(threads);
  }

  @Override
  public List<SentryException> getExceptions() {
    return super.getExceptions();
  }

  @Override
  public void setExceptions(List<SentryException> exceptions) {
    super.setExceptions(exceptions);
  }

  @Override
  public void setThrowable(Throwable throwable) {
    super.setThrowable(throwable);
  }

  @Override
  public void setThreads(SentryValues<SentryThread> threads) {
    super.setThreads(threads);
  }

  @Override
  public void setExceptions(SentryValues<SentryException> exceptions) {
    super.setExceptions(exceptions);
  }

  @Override
  public SentryLevel getLevel() {
    return super.getLevel();
  }

  @Override
  public void setLevel(SentryLevel level) {
    super.setLevel(level);
  }

  @Override
  public String getTransaction() {
    return super.getTransaction();
  }

  @Override
  public void setTransaction(String transaction) {
    super.setTransaction(transaction);
  }

  @Override
  public String getEnvironment() {
    return super.getEnvironment();
  }

  @Override
  public void setEnvironment(String environment) {
    super.setEnvironment(environment);
  }

  @Override
  public User getUser() {
    return super.getUser();
  }

  @Override
  public void setUser(User user) {
    super.setUser(user);
  }

  @Override
  public Request getRequest() {
    return super.getRequest();
  }

  @Override
  public void setRequest(Request request) {
    super.setRequest(request);
  }

  @Override
  public SdkVersion getSdkVersion() {
    return super.getSdkVersion();
  }

  @Override
  public void setSdkVersion(SdkVersion sdkVersion) {
    super.setSdkVersion(sdkVersion);
  }

  @Override
  public List<String> getFingerprint() {
    return super.getFingerprint();
  }

  @Override
  public void setFingerprint(List<String> fingerprint) {
    super.setFingerprint(fingerprint);
  }

  @Override
  public List<Breadcrumb> getBreadcrumbs() {
    return super.getBreadcrumbs();
  }

  @Override
  public void setBreadcrumbs(ArrayList<Breadcrumb> breadcrumbs) {
    super.setBreadcrumbs(breadcrumbs);
  }

  @Override
  public Map<String, String> getTags() {
    return super.getTags();
  }

  @Override
  public void setTags(HashMap<String, String> tags) {
    super.setTags(tags);
  }

  @Override
  public Map<String, Object> getExtra() {
    return super.getExtra();
  }

  @Override
  public void setExtra(HashMap<String, Object> extra) {
    super.setExtra(extra);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
}
