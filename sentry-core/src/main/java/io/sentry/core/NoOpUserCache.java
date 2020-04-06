package io.sentry.core;

import io.sentry.core.protocol.User;
import org.jetbrains.annotations.Nullable;

final class NoOpUserCache implements IUserCache {

  private static final NoOpUserCache instance = new NoOpUserCache();

  private NoOpUserCache() {}

  public static NoOpUserCache getInstance() {
    return instance;
  }

  @Override
  public void setUser(@Nullable User user) {}

  @Override
  public @Nullable User getUser() {
    return null;
  }
}
