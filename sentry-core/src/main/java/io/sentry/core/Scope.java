package io.sentry.core;

import io.sentry.core.protocol.User;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Scope implements Cloneable {
  private @Nullable SentryLevel level;
  private @Nullable String transaction;
  private @Nullable User user;
  private @NotNull List<String> fingerprint = new CopyOnWriteArrayList<>();
  private @NotNull Queue<Breadcrumb> breadcrumbs;
  private @NotNull Map<String, String> tags = new ConcurrentHashMap<>();
  private @NotNull Map<String, Object> extra = new ConcurrentHashMap<>();
  private final int maxBreadcrumb;
  private @Nullable final SentryOptions.BeforeBreadcrumbCallback beforeBreadcrumbCallback;
  private @NotNull List<EventProcessor> eventProcessors = new CopyOnWriteArrayList<>();

  public Scope(
      final int maxBreadcrumb,
      @Nullable final SentryOptions.BeforeBreadcrumbCallback beforeBreadcrumbCallback) {
    this.maxBreadcrumb = maxBreadcrumb;
    this.beforeBreadcrumbCallback = beforeBreadcrumbCallback;
    this.breadcrumbs = createBreadcrumbsList(this.maxBreadcrumb);
  }

  public Scope(final int maxBreadcrumb) {
    this(maxBreadcrumb, null);
  }

  public @Nullable SentryLevel getLevel() {
    return level;
  }

  public void setLevel(@Nullable SentryLevel level) {
    this.level = level;
  }

  public @Nullable String getTransaction() {
    return transaction;
  }

  public void setTransaction(@Nullable String transaction) {
    this.transaction = transaction;
  }

  public @Nullable User getUser() {
    return user;
  }

  public void setUser(@Nullable User user) {
    this.user = user;
  }

  public @NotNull List<String> getFingerprint() {
    return fingerprint;
  }

  public void setFingerprint(@NotNull List<String> fingerprint) {
    this.fingerprint = fingerprint;
  }

  @NotNull
  Queue<Breadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  public void addBreadcrumb(@NotNull Breadcrumb breadcrumb) {
    addBreadcrumb(breadcrumb, true);
  }

  void addBreadcrumb(@NotNull Breadcrumb breadcrumb, boolean executeBeforeBreadcrumb) {
    if (executeBeforeBreadcrumb && beforeBreadcrumbCallback != null) {
      try {
        breadcrumb =
            beforeBreadcrumbCallback.execute(breadcrumb, null); // TODO: whats about hint here?
      } catch (Exception e) {
        // TODO: log it

        Map<String, String> data = breadcrumb.getData();
        if (breadcrumb.getData() == null) {
          data = new HashMap<>();
        }
        data.put("sentry:message", e.getMessage());
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        data.put("sentry:stacktrace", sw.toString());
        breadcrumb.setData(data);
      }

      if (breadcrumb == null) {
        return;
      }
    }

    this.breadcrumbs.add(breadcrumb);
  }

  public void clearBreadcrumbs() {
    breadcrumbs.clear();
  }

  public @NotNull Map<String, String> getTags() {
    return tags;
  }

  public void setTag(@NotNull String key, @NotNull String value) {
    this.tags.put(key, value);
  }

  public @NotNull Map<String, Object> getExtras() {
    return extra;
  }

  public void setExtra(@NotNull String key, @NotNull String value) {
    this.extra.put(key, value);
  }

  private @NotNull Queue<Breadcrumb> createBreadcrumbsList(final int maxBreadcrumb) {
    return SynchronizedQueue.synchronizedQueue(new CircularFifoQueue<>(maxBreadcrumb));
  }

  @Override
  public Scope clone() throws CloneNotSupportedException {
    final Scope clone = (Scope) super.clone();

    final SentryLevel levelRef = level;
    clone.level =
        levelRef != null ? SentryLevel.valueOf(levelRef.name().toUpperCase(Locale.ROOT)) : null;

    final User userRef = user;
    clone.user = userRef != null ? userRef.clone() : null;

    clone.fingerprint = new CopyOnWriteArrayList<>(fingerprint);
    clone.eventProcessors = new CopyOnWriteArrayList<>(eventProcessors);

    final Queue<Breadcrumb> breadcrumbsRef = breadcrumbs;
    //    if (breadcrumbs != null) {
    Queue<Breadcrumb> breadcrumbsClone = createBreadcrumbsList(maxBreadcrumb);

    for (Breadcrumb item : breadcrumbsRef) {
      final Breadcrumb breadcrumbClone = item.clone();
      breadcrumbsClone.add(breadcrumbClone);
    }
    clone.breadcrumbs = breadcrumbsClone;
    //    } else {
    //      clone.breadcrumbs = null;
    //    }

    final Map<String, String> tagsRef = tags;
    //    if (tags != null) {
    final Map<String, String> tagsClone = new ConcurrentHashMap<>();

    for (Map.Entry<String, String> item : tagsRef.entrySet()) {
      if (item != null) {
        tagsClone.put(item.getKey(), item.getValue());
      }
    }

    clone.tags = tagsClone;
    //    } else {
    //      clone.tags = null;
    //    }

    final Map<String, Object> extraRef = extra;
    //        if (extra != null) {
    Map<String, Object> extraClone = new HashMap<>();

    for (Map.Entry<String, Object> item : extraRef.entrySet()) {
      if (item != null) {
        extraClone.put(item.getKey(), item.getValue());
      }
    }

    clone.extra = extraClone;
    //        } else {
    //            clone.extra = null;
    //        }

    return clone;
  }

  @NotNull
  List<EventProcessor> getEventProcessors() {
    return eventProcessors;
  }

  public void addEventProcessor(@NotNull EventProcessor eventProcessor) {
    eventProcessors.add(eventProcessor);
  }
}
