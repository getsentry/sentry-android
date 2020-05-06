package io.sentry.core.transport;

import java.util.Date;
import org.jetbrains.annotations.NotNull;

final class CurrentDateProvider implements ICurrentDateProvider {

  @Override
  public final @NotNull Date getCurrentDate() {
    return new Date();
  }

  @Override
  public final @NotNull Date getDate(final long timeMillis) {
    return new Date(timeMillis);
  }
}
