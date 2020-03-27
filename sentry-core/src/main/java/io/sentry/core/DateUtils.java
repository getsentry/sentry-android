package io.sentry.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jetbrains.annotations.ApiStatus;

/** Utilities to deal with dates */
@ApiStatus.Internal
public final class DateUtils {
  private static final String UTC = "UTC";
  private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final String ISO_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  private DateUtils() {}

  /**
   * Get date formatted as expected by Sentry.
   *
   * @param date the current date with local timezone
   * @return the ISO formatted UTC date with millis precision.
   */
  public static String getTimestampIsoFormat(Date date) {
    TimeZone tz = TimeZone.getTimeZone(UTC);
    DateFormat df = new SimpleDateFormat(ISO_FORMAT_WITH_MILLIS, Locale.US);
    df.setTimeZone(tz);
    return df.format(date);
  }

  /**
   * Get the current date and time as ISO UTC
   *
   * @return the ISO UTC date and time
   */
  public static Date getCurrentDateTime() {
    String timestampIsoFormat = getTimestampIsoFormat(new Date());
    return getDateTime(timestampIsoFormat);
  }

  /**
   * Get Java Date from UTC timestamp format
   *
   * @param timestamp UTC format eg 2000-12-31T23:59:58Z or 2000-12-31T23:59:58.123Z
   * @return the Date
   */
  public static Date getDateTime(String timestamp) throws IllegalArgumentException {
    try {
      return new SimpleDateFormat(ISO_FORMAT_WITH_MILLIS, Locale.US).parse(timestamp);
    } catch (ParseException e) {
      try {
        // to keep compatibility with older envelopes
        return new SimpleDateFormat(ISO_FORMAT, Locale.US).parse(timestamp);
      } catch (ParseException ignored) {
      }
      throw new IllegalArgumentException("timestamp is not ISO format " + timestamp);
    }
  }

  /**
   * Get Java Date from millis timestamp format
   *
   * @param timestamp millis format eg 1581410911.988 (1581410911 seconds and 988 millis)
   * @return the Date
   */
  public static Date getDateTimeWithMillisPrecision(String timestamp)
      throws IllegalArgumentException {
    try {
      String[] times = timestamp.split("\\.", -1);
      long seconds = Long.parseLong(times[0]);
      long millis = times.length > 1 ? Long.parseLong(times[1]) : 0;

      return new Date((seconds * 1000) + millis);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("timestamp is not millis format " + timestamp);
    }
  }

  /**
   * Get date formatted as expected by Sentry.
   *
   * @param date already UTC format
   * @return the ISO formatted date with millis precision.
   */
  public static String getTimestamp(Date date) {
    DateFormat df = new SimpleDateFormat(ISO_FORMAT_WITH_MILLIS, Locale.US);
    return df.format(date);
  }

  /**
   * Converts the given Date and time to UTC timezone
   *
   * @param date the Date with local timezone
   * @return the Date UTC timezone
   */
  public static Date getDateTime(Date date) {
    String timestampIsoFormat = getTimestampIsoFormat(date);
    return getDateTime(timestampIsoFormat);
  }
}
