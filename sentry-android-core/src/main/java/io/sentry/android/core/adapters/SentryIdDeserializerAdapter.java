package io.sentry.android.core.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.sentry.core.protocol.SentryId;
import java.lang.reflect.Type;

public class SentryIdDeserializerAdapter implements JsonDeserializer<SentryId> {
  @Override
  public SentryId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return json == null ? null : new SentryId(json.getAsString());
  }
}
