package io.sentry.android.core.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.TimeZone;

public class TimeZoneSerializerAdapter implements JsonSerializer<TimeZone> {
  @Override
  public JsonElement serialize(TimeZone src, Type typeOfSrc, JsonSerializationContext context) {
    return src == null ? null : new JsonPrimitive(src.getID()); // is it ID?
  }
}
