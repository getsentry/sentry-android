package io.sentry;

import java.util.ArrayList;
import java.util.List;

public class SentryOptions {
  private List<EventProcessor> eventProcessors = new ArrayList<>();

  public void AddEventProcessor(EventProcessor eventProcessor) {
    eventProcessors.add(eventProcessor);
  }
}
