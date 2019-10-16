package io.sentry.core;

import io.sentry.core.protocol.SentryId;
import java.util.ArrayDeque;
import java.util.Deque;

public class Hub implements IHub {

  private class StackItem {
    public ISentryClient client;
    public Scope scope;

    public StackItem(ISentryClient client, Scope scope) {
      this.client = client;
      this.scope = scope;
    }
  }

  private SentryId lastEventId;
  private SentryOptions options;
  private volatile boolean isEnabled;
  private Deque<StackItem> stack = new ArrayDeque<>();
  private final Object lock = new Object();

  public Hub(SentryOptions options) {
    this.options = options;

    Scope scope = new Scope();
    ISentryClient client = new SentryClient(options);
    StackItem item = new StackItem(client, scope);
    stack.push(item);

    isEnabled = true;
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  @Override
  public SentryId captureEvent(SentryEvent event) {
    SentryId sentryId;
    synchronized (lock) {
      StackItem item = stack.peek();
      sentryId = item.client.captureEvent(event, item.scope);
      this.lastEventId = sentryId;
    }
    return sentryId;
  }

  @Override
  public SentryId captureMessage(String message) {
    SentryId sentryId;
    synchronized (lock) {
      StackItem item = stack.peek();
      sentryId = item.client.captureMessage(message, item.scope);
      this.lastEventId = sentryId;
    }
    return sentryId;
  }

  @Override
  public SentryId captureException(Throwable throwable) {
    SentryId sentryId;
    synchronized (lock) {
      StackItem item = stack.peek();
      sentryId = item.client.captureException(throwable, item.scope);
      this.lastEventId = sentryId;
    }
    return sentryId;
  }

  @Override
  public void close() {
    synchronized (lock) {
      // Close the top-most client
      StackItem item = stack.peek();
      item.client.close(options.getShutdownTimeout());
    }
    isEnabled = false;
  }

  @Override
  public void addBreadcrumb(Breadcrumb breadcrumb) {
    synchronized (lock) {
      StackItem item = stack.peek();
      item.scope.addBreadcrumb(breadcrumb);
    }
  }

  @Override
  public SentryId getLastEventId() {
    return lastEventId;
  }

  @Override
  public void pushScope() {
    synchronized (lock) {
      StackItem item = stack.peek();
      Scope clone = item.scope.clone();
      StackItem newItem = new StackItem(item.client, clone);
      stack.push(newItem);
    }
  }

  @Override
  public void popScope() {
    synchronized (lock) {
      // Don't drop the root scope
      if (stack.size() != 1) {
        stack.pop();
      }
    }
  }

  @Override
  public void withScope(ScopeCallback callback) {
    pushScope();
    try {
      synchronized (lock) {
        StackItem item = stack.peek();
        callback.run(item.scope);
      }
    } finally {
      popScope();
    }
  }

  @Override
  public void configureScope(ScopeCallback callback) {
    synchronized (lock) {
      StackItem item = stack.peek();
      callback.run(item.scope);
    }
  }

  @Override
  public void bindClient(SentryClient client) {
    synchronized (lock) {
      StackItem item = stack.peek();
      item.client = client;
    }
  }

  @Override
  public void flush(long timeoutMills) {
    synchronized (lock) {
      StackItem item = stack.peek();
      item.client.flush(options.getShutdownTimeout());
    }
  }

  @Override
  public IHub clone() {
    // TODO: Clone hub
    return new Hub(options);
  }
}
