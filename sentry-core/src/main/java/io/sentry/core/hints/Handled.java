package io.sentry.core.hints;

// Marker interface for a capture involving data cached from disk
// This means applying data relevant to the current execution should be done
// as the App. has handled the error and App won't crash.
// like applying threads or current app version.
public interface Handled {
  // this could also be in Cached hint as well, just making a new to to exemplify easily
  void setHandled(boolean handled);

  boolean isHandled();
}
