package io.sentry.core.hints;

/** marker interface that resets/restarts a Flushable operation. */
public interface Resettable {

  void reset();
}
