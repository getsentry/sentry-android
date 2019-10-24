package io.sentry.core.exception;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Utility class used by the Sentry Java Agent to store per-frame local variable
 * information for the last thrown exception.
 */
public final class FrameCache {
  private static Set<String> appPackages = new HashSet<>();

  private static ThreadLocal<WeakHashMap<Throwable, SentryFrame[]>> cache =
    new ThreadLocal<WeakHashMap<Throwable, SentryFrame[]>>() {
      @Override
      protected WeakHashMap<Throwable, SentryFrame[]> initialValue() {
        return new WeakHashMap<>();
      }
    };

  /**
   * Utility class, no public ctor.
   */
  private FrameCache() {

  }

  /**
   * Store the per-frame local variable information for the last exception thrown on this thread.
   *
   * @param throwable Throwable that the provided {@link SentryFrame}s represent.
   * @param frames Array of {@link SentryFrame}s to store
   */
  public static void add(Throwable throwable, SentryFrame[] frames) {
    Map<Throwable, SentryFrame[]> weakMap = cache.get();
    weakMap.put(throwable, frames);
  }

  /**
   * Retrieve the per-frame local variable information for the last exception thrown on this thread.
   *
   * @param throwable Throwable to look up cached {@link SentryFrame}s for.
   * @return Array of {@link SentryFrame}s
   */
  public static SentryFrame[] get(Throwable throwable) {
    Map<Throwable, SentryFrame[]> weakMap = cache.get();
    return weakMap.get(throwable);
  }

  /**
   * Check whether the provided {@link Throwable} should be cached or not. Called by
   * the native agent code so that the Java side (this code) can check the existing
   * cache and user configuration, such as which packages are "in app".
   *
   * @param throwable Throwable to be checked
   * @param numFrames Number of frames in the Throwable's stacktrace
   * @return true if the Throwable should be processed and cached
   */
  public static boolean shouldCacheThrowable(Throwable throwable, int numFrames) {
    // only cache frames when 'in app' packages are provided
    if (appPackages.isEmpty()) {
      return false;
    }

    // many libraries/frameworks seem to rethrow the same object with trimmed
    // stacktraces, which means later ("smaller") throws would overwrite the existing
    // object in cache. for this reason we prefer the throw with the greatest stack
    // length...
    Map<Throwable, SentryFrame[]> weakMap = cache.get();
    SentryFrame[] existing = weakMap.get(throwable);
    if (existing != null && numFrames <= existing.length) {
      return false;
    }

    // check each frame against all "in app" package prefixes
    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      for (String appFrame : appPackages) {
        if (stackTraceElement.getClassName().startsWith(appFrame)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Add an "in app" package prefix to the set of packages for which exception
   * local variables will be cached.
   *
   * When an exception is thrown it must contain at least one frame that originates
   * from a package in this set, otherwise local variable information will not be
   * cached. See {@link FrameCache#shouldCacheThrowable(Throwable, int)}.
   *
   * @param newAppPackage package prefix to add to the set
   */
  public static void addAppPackage(String newAppPackage) {
    appPackages.add(newAppPackage);
  }

  /**
   * This method is meant for cleaner testing. Don't attempt to use it in production.
   */
  // visible for testing
  static void reset() {
    cache.get().clear();
    appPackages.clear();
  }
}
