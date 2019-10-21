package io.sentry.android.core;

import static io.sentry.core.ILogger.log;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.sentry.core.DateUtils;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import java.lang.reflect.Type;
import java.util.Date;

class DateSerializerAdapter implements JsonSerializer<Date> {

  private final ILogger logger;

  public DateSerializerAdapter(ILogger logger) {
    this.logger = logger;
  }

  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    try {
      return src == null ? null : new JsonPrimitive(DateUtils.getTimestamp(src));
    } catch (Exception e) {
      log(logger, SentryLevel.ERROR, "Error when serializing Date", e);
    }
    return null;
  }
}
