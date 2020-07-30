package io.sentry.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/** Util class for Collections */
@ApiStatus.Internal
public final class CollectionUtils {

  private CollectionUtils() {}

  /**
   * Returns an Iterator size
   *
   * @param data the Iterable
   * @return iterator size
   */
  public static int size(Iterable<?> data) {
    if (data instanceof Collection) {
      return ((Collection<?>) data).size();
    }
    int counter = 0;
    for (Object ignored : data) {
      counter++;
    }
    return counter;
  }

  /**
   * Creates a shallow copy of map given by parameter.
   *
   * @param map the map to copy
   * @param <K> the type of map keys
   * @param <V> the type of map values
   * @return the shallow copy of map
   */
  public static <K, V> @Nullable Map<K, V> shallowCopy(@Nullable Map<K, V> map) {
    if (map != null) {
      final Map<K, V> clone = new HashMap<>();

      for (Map.Entry<K, V> item : map.entrySet()) {
        if (item != null) {
          clone.put(item.getKey(), item.getValue()); // shallow copy
        }
      }

      return clone;
    } else {
      return null;
    }
  }
}
