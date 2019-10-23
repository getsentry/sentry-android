package io.sentry.core;

import static io.sentry.core.ILogger.log;

import io.sentry.core.protocol.SentryId;
import io.sentry.core.util.Nullable;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class Hub implements IHub, Cloneable {

  private static final class StackItem {
    private volatile ISentryClient client;
    private volatile Scope scope;

    public StackItem(ISentryClient client, Scope scope) {
      this.client = client;
      this.scope = scope;
    }
  }

  private volatile SentryId lastEventId;
  private final SentryOptions options;
  private volatile boolean isEnabled;
  private final Deque<StackItem> stack = new LinkedBlockingDeque<>();

  public Hub(SentryOptions options) {
    this(options, createRootStackItem(options));
  }

  private Hub(SentryOptions options, @Nullable StackItem rootStackItem) {
    this.options = options;
    if (rootStackItem != null) {
      this.stack.push(rootStackItem);
    }
    this.isEnabled = true;
  }

  static StackItem createRootStackItem(SentryOptions options) {
    Scope scope = new Scope();
    ISentryClient client = new SentryClient(options);
    return new StackItem(client, scope);
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  @Override
  public SentryId captureEvent(SentryEvent event) {
    SentryId sentryId;
    StackItem item = stack.peek();
    sentryId = item != null ? item.client.captureEvent(event, item.scope) : SentryId.EMPTY_ID;
    this.lastEventId = sentryId;
    return sentryId;
  }

  @Override
  public SentryId captureMessage(String message) {
    SentryId sentryId;
    StackItem item = stack.peek();
    sentryId = item != null ? item.client.captureMessage(message, item.scope) : SentryId.EMPTY_ID;
    this.lastEventId = sentryId;
    return sentryId;
  }

  @Override
  public SentryId captureException(Throwable throwable) {
    SentryId sentryId;
    StackItem item = stack.peek();
    sentryId =
        item != null ? item.client.captureException(throwable, item.scope) : SentryId.EMPTY_ID;
    this.lastEventId = sentryId;
    return sentryId;
  }

  @Override
  public void close() {
    // Close the top-most client
    StackItem item = stack.peek();
    if (item != null) {
      item.client.close();
    }
    isEnabled = false;
  }

  @Override
  public void addBreadcrumb(Breadcrumb breadcrumb) {
    StackItem item = stack.peek();
    if (item != null) {
      item.scope.addBreadcrumb(breadcrumb);
    }
  }

  @Override
  public SentryId getLastEventId() {
    return lastEventId;
  }

  @Override
  public void pushScope() {
    StackItem item = stack.peek();
    if (item != null) {
      Scope clone = null;
      try {
        clone = item.scope.clone();
      } catch (CloneNotSupportedException e) {
        log(
            options.getLogger(),
            SentryLevel.ERROR,
            "An error has occurred when cloning a Scope",
            e);
      }
      if (clone != null) {
        StackItem newItem = new StackItem(item.client, clone);
        stack.push(newItem);
      }
    }
  }

  @Override
  public void popScope() {
    // Don't drop the root scope
    synchronized (stack) {
      if (stack.size() != 1) {
        stack.pop();
      }
    }
  }

  @Override
  public void withScope(ScopeCallback callback) {
    pushScope();
    try {
      StackItem item = stack.peek();
      if (item != null) {
        callback.run(item.scope);
      }
    } finally {
      popScope();
    }
  }

  @Override
  public void configureScope(ScopeCallback callback) {
    StackItem item = stack.peek();
    if (item != null) {
      callback.run(item.scope);
    }
  }

  @Override
  public void bindClient(SentryClient client) {
    StackItem item = stack.peek();
    if (item != null) {
      item.client = client != null ? client : NoOpSentryClient.getInstance();
    }
  }

  @Override
  public void flush(long timeoutMills) {
    StackItem item = stack.peek();
    if (item != null) {
      item.client.flush(timeoutMills);
    }
  }

  @Override
  public IHub clone() {
    // Clone will be invoked in parallel
    Hub clone = new Hub(this.options, null);
    for (StackItem item : this.stack) {
      clone.stack.push(item);
    }
    return clone;
  }
}
