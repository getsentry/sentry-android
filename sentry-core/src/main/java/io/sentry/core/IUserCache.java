package io.sentry.core;

import io.sentry.core.protocol.User;
import org.jetbrains.annotations.Nullable;

/** The UserCache interface */
public interface IUserCache {

  /**
   * Caches the user (id, email and username) in the disk if the cacheUserForSessions flag is
   * enabled. This method overwrites previous values. A null user cleans the cache.
   *
   * @param user the user object or null.
   */
  void setUser(@Nullable User user);

  /**
   * Returns the user that was cached in the disk if cacheUserForSessions flag was enabled
   *
   * @return the user (id, email and username) or null
   */
  @Nullable
  User getUser();
}
