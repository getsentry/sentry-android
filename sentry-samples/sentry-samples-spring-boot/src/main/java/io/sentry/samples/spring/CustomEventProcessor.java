package io.sentry.samples.spring;

import io.sentry.core.EventProcessor;
import io.sentry.core.SentryEvent;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Custom {@link EventProcessor} implementation lets modifying {@link SentryEvent}s before they are sent to Sentry.
 */
@Component
public class CustomEventProcessor implements EventProcessor {
  @Override
  public SentryEvent process(SentryEvent event, @Nullable Object hint) {
    event.setTag("Java Version", System.getProperty("java.version"));
    return event;
  }
}
