package io.sentry.android.core.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.TimeZone;

public class TImeZoneDeserializerAdapter implements JsonDeserializer<TimeZone> {
  @Override
  public TimeZone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return json == null ? null : TimeZone.getTimeZone(json.getAsString());
  }
}
