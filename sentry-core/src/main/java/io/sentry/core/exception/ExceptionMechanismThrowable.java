package io.sentry.core.exception;

import io.sentry.core.protocol.Mechanism;

/**
 * A throwable decorator that holds an {@link io.sentry.core.protocol.Mechanism} related to the decorated {@link Throwable}.
 */
public final class ExceptionMechanismThrowable extends Throwable {
  private static final long serialVersionUID = 100L;

  private final Mechanism exceptionMechanism;
  private final Throwable throwable;

  /**
   * A {@link Throwable} that decorates another with a Sentry {@link Mechanism}.
   * @param mechanism The {@link Mechanism}.
   * @param throwable The {@link java.lang.Throwable}.
   */
  public ExceptionMechanismThrowable(Mechanism mechanism, Throwable throwable) {
    this.exceptionMechanism = mechanism;
    this.throwable = throwable;
  }

  public Mechanism getExceptionMechanism() {
    return exceptionMechanism;
  }

  public Throwable getThrowable() {
    return throwable;
  }
}
