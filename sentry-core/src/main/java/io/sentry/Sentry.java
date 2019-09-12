package io.sentry;

import java.util.function.Consumer;

public class Sentry {
  public static void init() {
    init(new SentryOptions());
  }

  public static void init(SentryOptions options) {}

  public static void init(Consumer<SentryOptions> optionsConfiguration) {}
}
