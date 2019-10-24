package io.sentry.core.exception;

import io.sentry.core.protocol.Mechanism;
import io.sentry.core.protocol.SentryException;
import io.sentry.core.protocol.SentryStackTrace;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SentryExceptionReader {

  /**
   * Creates a new instance from the given {@code throwable}.
   *
   * @param throwable the {@link Throwable} to build this instance from
   */
  public static List<SentryException> sentryExceptionReader(final Throwable throwable) {
    return sentryExceptionReader(extractExceptionQueue(throwable));
  }

  /**
   * Creates a new instance from the given {@code exceptions}.
   *
   * @param exceptions a {@link Deque} of {@link SentryException} to build this instance from
   */
  private static List<SentryException> sentryExceptionReader(final Deque<SentryException> exceptions) {
    return new ArrayList<>(exceptions);
  }

  /**
   * Creates a Sentry exception based on a Java Throwable.
   * <p>
   * The {@code childExceptionStackTrace} parameter is used to define the common frames with the child exception
   * (Exception caused by {@code throwable}).
   *
   * @param throwable                Java exception to send to Sentry.
   * @param childExceptionStackTrace StackTrace of the exception caused by {@code throwable}.
   * @param exceptionMechanism The optional {@link Mechanism} of the {@code throwable}.
   *                           Or null if none exist.
   */
  private static SentryException sentryExceptionReader(
    Throwable throwable,
    StackTraceElement[] childExceptionStackTrace, // TODO: do we need that?
    Mechanism exceptionMechanism) {

    Package exceptionPackage = throwable.getClass().getPackage();
    String fullClassName = throwable.getClass().getName();

    SentryException exception = new SentryException();

//    this.exceptionMessage = throwable.getMessage();
    String exceptionClassName = exceptionPackage != null
      ? fullClassName.replace(exceptionPackage.getName() + ".", "")
      : fullClassName;

    String exceptionPackageName = exceptionPackage != null
      ? exceptionPackage.getName()
      : null;
    // TODO: whats about those missing fields? message, classname, packagename, ...

    SentryStackTrace sentryStackTrace = new SentryStackTrace();
    sentryStackTrace.setFrames(
      Arrays.asList(SentryStackFrameReader.fromStackTraceElements(
        throwable.getStackTrace(), null))); // TODO: cached frames

    exception.setStacktrace(sentryStackTrace);
    exception.setType("ValueError"); // TODO ?
//    exception.setValue(); type or value is mandatory

    exception.setMechanism(exceptionMechanism);
    return exception;
  }

  /**
   * Transforms a {@link Throwable} into a Queue of {@link SentryException}.
   * <p>
   * Exceptions are stored in the queue from the most recent one to the oldest one.
   *
   * @param throwable throwable to transform in a queue of exceptions.
   * @return a queue of exception with StackTrace.
   */
  private static Deque<SentryException> extractExceptionQueue(Throwable throwable) {
    Deque<SentryException> exceptions = new ArrayDeque<>();
    Set<Throwable> circularityDetector = new HashSet<>();
    StackTraceElement[] childExceptionStackTrace = new StackTraceElement[0];
    Mechanism exceptionMechanism = null;

    //Stack the exceptions to send them in the reverse order
    while (throwable != null && circularityDetector.add(throwable)) {
      if (throwable instanceof ExceptionMechanismThrowable) {
        ExceptionMechanismThrowable exceptionMechanismThrowable = (ExceptionMechanismThrowable) throwable;
        exceptionMechanism = exceptionMechanismThrowable.getExceptionMechanism();
        throwable = exceptionMechanismThrowable.getThrowable();
      } else {
        exceptionMechanism = null;
      }

      SentryException exception = sentryExceptionReader(throwable, childExceptionStackTrace, exceptionMechanism);
      exceptions.add(exception);
      childExceptionStackTrace = throwable.getStackTrace();
      throwable = throwable.getCause();
    }

    return exceptions;
  }
}
