package io.sentry.core.protocol;

import io.sentry.core.IUnknownPropertiesConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

/** The user affected by an event. */
public final class User implements Cloneable, IUnknownPropertiesConsumer {

  /** User's email */
  private @Nullable String email;

  /** User's id */
  private @Nullable String id;

  /** User's username */
  private @Nullable String username;

  /** User's ipAddress */
  private @Nullable String ipAddress;

  /** User's others map */
  private @Nullable Map<String, String> other;

  /** unknown fields, only internal usage. */
  private @Nullable Map<String, Object> unknown;

  /**
   * Gets the e-mail address of the user.
   *
   * @return the e-mail.
   */
  public @Nullable String getEmail() {
    return email;
  }

  /**
   * Gets the e-mail address of the user.
   *
   * @param email the e-mail.
   */
  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  /**
   * Gets the id of the user.
   *
   * @return the id.
   */
  public @Nullable String getId() {
    return id;
  }

  /**
   * Sets the id of the user.
   *
   * @param id the user id.
   */
  public void setId(@Nullable String id) {
    this.id = id;
  }

  /**
   * Gets the username of the user.
   *
   * @return the username.
   */
  public @Nullable String getUsername() {
    return username;
  }

  /**
   * Sets the username of the user.
   *
   * @param username the username.
   */
  public void setUsername(@Nullable String username) {
    this.username = username;
  }

  /**
   * Gets the IP address of the user.
   *
   * @return the IP address of the user.
   */
  public @Nullable String getIpAddress() {
    return ipAddress;
  }

  /**
   * Sets the IP address of the user.
   *
   * @param ipAddress the IP address of the user.
   */
  public void setIpAddress(@Nullable String ipAddress) {
    this.ipAddress = ipAddress;
  }

  /**
   * Gets other user related data.
   *
   * @return the other user data.
   */
  public @Nullable Map<String, String> getOthers() {
    return other;
  }

  /**
   * Sets other user related data.
   *
   * @param other the other user related data..
   */
  public void setOthers(@Nullable Map<String, String> other) {
    this.other = other;
  }

  /**
   * User's unknown fields, only internal usage
   *
   * @param unknown the unknown fields
   */
  @ApiStatus.Internal
  @Override
  public void acceptUnknownProperties(Map<String, Object> unknown) {
    this.unknown = unknown;
  }

  /**
   * the User's unknown fields
   *
   * @return
   */
  @TestOnly
  Map<String, Object> getUnknown() {
    return unknown;
  }

  /**
   * Clones an User aka deep copy
   *
   * @return the cloned User
   * @throws CloneNotSupportedException if the User is not cloneable
   */
  @Override
  public @NotNull User clone() throws CloneNotSupportedException {
    final User clone = (User) super.clone();

    final Map<String, String> otherRef = other;
    if (otherRef != null) {
      final Map<String, String> otherClone = new ConcurrentHashMap<>();

      for (Map.Entry<String, String> item : otherRef.entrySet()) {
        if (item != null) {
          otherClone.put(item.getKey(), item.getValue()); // shallow copy
        }
      }

      clone.other = otherClone;
    } else {
      clone.other = null;
    }

    final Map<String, Object> unknownRef = unknown;
    if (unknownRef != null) {
      final Map<String, Object> unknownClone = new HashMap<>();

      for (Map.Entry<String, Object> item : unknownRef.entrySet()) {
        if (item != null) {
          unknownClone.put(item.getKey(), item.getValue()); // shallow copy
        }
      }

      clone.unknown = unknownClone;
    } else {
      clone.unknown = null;
    }

    return clone;
  }
}
