package io.sentry.core.transport;

import java.util.Date;
import org.jetbrains.annotations.NotNull;

/** Date Provider to make the Transport unit testable */
final class CurrentDateProvider implements ICurrentDateProvider {

  @Override
  public final @NotNull Date getCurrentDate() {
    return new Date();
  }

  @Override
  public final @NotNull Date getDate(long timeMillis) {
    return new Date(timeMillis);
  }
}
