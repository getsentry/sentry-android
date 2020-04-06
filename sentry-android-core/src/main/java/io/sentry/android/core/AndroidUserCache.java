package io.sentry.android.core;

import android.content.SharedPreferences;
import io.sentry.core.IUserCache;
import io.sentry.core.protocol.User;
import io.sentry.core.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: to think, this could be a SentryOptionsCache that could be expanded, does it make sense?

/** the Android User Cache class. It caches the user data in the Android SharedPreferences */
public final class AndroidUserCache implements IUserCache {

  // TODO: only these 3 fields make sense to me, otherwise caching a raw json would be better, but I
  // don't want to do that.
  private static final String EMAIL = "email";
  private static final String ID = "id";
  private static final String USERNAME = "user_name";

  private final @NotNull SharedPreferences sharedPreferences;
  private final @NotNull SentryAndroidOptions options;
  private final @Nullable String sentryDeviceId;

  public AndroidUserCache(
      final @NotNull SentryAndroidOptions options,
      final @NotNull SharedPreferences sharedPreferences,
      final @Nullable String sentryDeviceId) {
    this.options = Objects.requireNonNull(options, "SentryAndroidOptions is required.");
    this.sharedPreferences =
        Objects.requireNonNull(sharedPreferences, "SharedPreferences is required.");
    this.sentryDeviceId = sentryDeviceId;
  }

  //  TODO: I believe this makes sense only for global hub SDKs, but if not,
  //   how do we cache multiple users (because we have multiple scopes)?

  @Override
  public void setUser(final @Nullable User user) {
    if (options.isCacheUserForSessions()) {
      final SharedPreferences.Editor edit = sharedPreferences.edit();
      if (user != null) {
        edit.putString(ID, user.getId())
            .putString(EMAIL, user.getEmail())
            .putString(USERNAME, user.getUsername())
            .apply(); // it does in-memory cache and flush to the disk async.
      } else {
        cleanUserFields(edit);
      }
    }
  }

  @Override
  public @Nullable User getUser() {
    // sharedPreferences has in-memory cache and flush to the disk async., so it's not expensive
    if (options.isCacheUserForSessions()) {
      final String id = sharedPreferences.getString(ID, null);
      final String email = sharedPreferences.getString(EMAIL, null);
      final String userName = sharedPreferences.getString(USERNAME, null);

      boolean hasCachedUser = true;
      if (id == null && email == null && userName == null) {
        hasCachedUser = false;
      }

      final User user = new User();

      if (hasCachedUser) {
        // returns previous set user, even if it has no id
        user.setId(id);
        user.setEmail(email);
        user.setUsername(userName);
      } else {
        user.setId(sentryDeviceId);
      }
      return user;
    } else {
      final SharedPreferences.Editor edit = sharedPreferences.edit();
      // is it a good idea to do it here? this guarantees that it cleans up on restart if the flag
      // has been swapped
      cleanUserFields(edit);
      return null;
    }
  }

  private void cleanUserFields(final @NotNull SharedPreferences.Editor edit) {
    // do not clean SENTRY_DEVICE_ID
    edit.remove(ID).remove(EMAIL).remove(USERNAME).apply();
  }
}
