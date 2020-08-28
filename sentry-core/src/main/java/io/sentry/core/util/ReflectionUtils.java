package io.sentry.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class ReflectionUtils {

  private ReflectionUtils() {}

  /**
   * Copies non-null properties from source object to target using direct field access.
   *
   * @param source - object to copy properties from
   * @param target - object to copy properties to
   * @param <T> - the type of source and target
   */
  public static <T> void copyNonNullFields(final @NotNull T source, final @NotNull T target) {
    final List<Field> fields =
        new ArrayList<>(Arrays.asList(source.getClass().getDeclaredFields()));
    if (source.getClass().getSuperclass() != null) {
      fields.addAll(Arrays.asList(source.getClass().getSuperclass().getDeclaredFields()));
    }

    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isFinal(field.getModifiers())
          && hasField(target.getClass(), field)) {
        try {
          field.setAccessible(true);
          if (field.get(source) != null) {
            field.set(target, field.get(source));
          }
          field.setAccessible(false);
        } catch (IllegalAccessException e) {
          System.err.println(
              "Failed to copy field " + field.getName() + ". Error: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Checks if given class or its super class contains a field.
   *
   * @param clazz - the class
   * @param field - the field
   * @param <T> - the type of class
   * @return boolean - if class or its super class contains a field
   */
  private static <T> boolean hasField(final @NotNull Class<T> clazz, final @NotNull Field field) {
    final List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
    if (clazz.getSuperclass() != null) {
      fields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
    }
    return fields.contains(field);
  }
}
