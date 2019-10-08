package io.sentry.android;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.sentry.DateUtils;
import java.lang.reflect.Type;
import java.util.Date;

class DateSerializerAdapter implements JsonSerializer<Date> {
  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    return src == null ? null : new JsonPrimitive(DateUtils.getTimestamp(src));
  }
}
