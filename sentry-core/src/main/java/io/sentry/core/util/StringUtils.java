package io.sentry.core.util;

import java.util.Locale;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class StringUtils {

  private StringUtils() {}

  public static @Nullable String getStringAfterDot(final @Nullable String str) {
    if (str != null) {
      final int lastDotIndex = str.lastIndexOf(".");
      if (lastDotIndex >= 0 && str.length() > (lastDotIndex + 1)) {
        return str.substring(lastDotIndex + 1);
      } else {
        return str;
      }
    }
    return null;
  }

  /**
   * Returns a Capitalized String and all remaining chars to lower case. eg seSSioN = Session
   *
   * @param str the String to capitalize
   * @return the capitalized String or itself if empty or null
   */
  public static @Nullable String capitalize(final @Nullable String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1).toLowerCase(Locale.ROOT);
  }
}
