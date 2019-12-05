package io.sentry.core;

import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IUnknownPropertiesConsumer {
  void acceptUnknownProperties(Map<String, Object> unknown);
}
