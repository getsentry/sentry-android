package io.sentry.core.exception;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO: should we use that? why?
public class SentryFrame {
  /**
   * Method that this frame originated in.
   */
  private Method method;
  /**
   * Local variable information for this frame.
   */
  private final LocalVariable[] locals;

  /**
   * Construct a {@link SentryFrame}.
   *
   * @param method Method that this frame originated in.
   * @param locals Local variable information for this frame.
   */
  public SentryFrame(Method method, LocalVariable[] locals) {
    this.method = method;
    this.locals = locals;
  }

  public Method getMethod() {
    return method;
  }

  /**
   * Converts the locals array to a Map of variable-name -> variable-value.
   *
   * @return Map of variable-name -> variable-value.
   */
  public Map<String, Object> getLocals() {
    if (locals == null || locals.length == 0) {
      return Collections.emptyMap();
    }

    Map<String, Object> localsMap = new HashMap<>();
    for (SentryFrame.LocalVariable localVariable : locals) {
      if (localVariable != null) {
        localsMap.put(localVariable.getName(), localVariable.getValue());
      }
    }

    return localsMap;
  }

  /**
   * Class representing a single local variable.
   */
  public static final class LocalVariable {
    /**
     * Variable name.
     */
    final String name;
    /**
     * Variable value.
     */
    final Object value;

    /**
     * Construct a {@link LocalVariable} for a live object.
     *
     * @param name Variable name.
     * @param value Variable value.
     */
    public LocalVariable(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }
  }
}
