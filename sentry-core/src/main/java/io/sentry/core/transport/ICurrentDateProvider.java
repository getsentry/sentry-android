package io.sentry.core.transport;

import java.util.Date;

/** Date Provider to make the Transport unit testable */
interface ICurrentDateProvider {

  /**
   * Returns the current Date (local)
   *
   * @return the Date
   */
  Date getCurrentDate();

  /**
   * Returns a new Date based on the given timeMillis
   *
   * @param timeMillis the Time in millis
   * @return the given Date
   */
  Date getDate(long timeMillis);
}
