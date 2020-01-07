package io.sentry.core;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class HubWrapper implements IHub {

  public boolean isIntegrationAvailable(Integration integration) {
    SentryOptions sentryOptions = getSentryClient().getSentryOptions();

    for (Integration item : sentryOptions.getIntegrations()) {
      if (item.equals(integration)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IHub clone() {
    throw new NotImplementedException();
  }
}
