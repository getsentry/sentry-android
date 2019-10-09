package io.sentry.android.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.sentry.protocol.SentryId;
import java.lang.reflect.Type;

public class SentryIdSerializerAdapter implements JsonSerializer<SentryId> {
  @Override
  public JsonElement serialize(SentryId src, Type typeOfSrc, JsonSerializationContext context) {
    return src == null ? null : new JsonPrimitive(src.toString());
  }
}
