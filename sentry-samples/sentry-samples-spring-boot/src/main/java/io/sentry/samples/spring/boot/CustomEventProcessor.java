package io.sentry.samples.spring.boot;

import io.sentry.core.EventProcessor;
import io.sentry.core.SentryEvent;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Custom {@link EventProcessor} implementation lets modifying {@link SentryEvent}s before they are
 * sent to Sentry.
 */
@Component
public class CustomEventProcessor implements EventProcessor {
  private final String javaVersion;
  private final String javaVendor;

  public CustomEventProcessor(String javaVersion, String javaVendor) {
    this.javaVersion = javaVersion;
    this.javaVendor = javaVendor;
  }

  public CustomEventProcessor() {
    this(System.getProperty("java.version"), System.getProperty("java.vendor"));
  }

  @Override
  public SentryEvent process(SentryEvent event, @Nullable Object hint) {
    event.setTag("Java-Version", javaVersion);
    event.setTag("Java-Vendor", javaVendor);
    return event;
  }
}
