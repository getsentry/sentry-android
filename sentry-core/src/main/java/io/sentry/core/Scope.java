package io.sentry.core;

import io.sentry.core.protocol.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Scope implements Cloneable {
  private SentryLevel level;
  private String transaction;
  private User user;
  private List<String> fingerprint = new ArrayList<>();
  private final CircularFifoQueue<Breadcrumb> breadcrumbs;
  private Map<String, String> tags = new ConcurrentHashMap<>();
  private Map<String, Object> extra = new ConcurrentHashMap<>();

  public Scope(int maxBreadcrumb) {
    this.breadcrumbs = new CircularFifoQueue<>(maxBreadcrumb);
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public List<String> getFingerprint() {
    return fingerprint;
  }

  public void setFingerprint(List<String> fingerprint) {
    this.fingerprint = fingerprint;
  }

  public Collection<Breadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  public void addBreadcrumb(Breadcrumb breadcrumb) {
    if (breadcrumb == null) {
      return;
    }
    this.breadcrumbs.add(breadcrumb);
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTag(String key, String value) {
    this.tags.put(key, value);
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(String key, String value) {
    this.extra.put(key, value);
  }

  @Override
  protected Scope clone() throws CloneNotSupportedException {
    return (Scope) super.clone();
  }
}
