package io.sentry.core;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * needed because of isIntegrationAvailable otherwise custom integrations will need to do it
 * manually.
 */
public abstract class HubWrapper implements IHub {

  public boolean isIntegrationAvailable(Integration integration) {
    for (Integration item : getSentryClient().getSentryOptions().getIntegrations()) {
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
