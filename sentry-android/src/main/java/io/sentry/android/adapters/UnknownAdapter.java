package io.sentry.android.adapters;

import com.google.gson.*;
import io.sentry.SentryEvent;
import io.sentry.UnknownField;
import io.sentry.protocol.SentryId;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnknownAdapter<T extends UnknownField> implements JsonDeserializer<T> {

  private Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(SentryId.class, new SentryIdSerializerAdapter())
          .registerTypeAdapter(SentryId.class, new SentryIdDeserializerAdapter())
          .registerTypeAdapter(Date.class, new DateSerializerAdapter())
          .registerTypeAdapter(Date.class, new DateDeserializerAdapter())
          .create();

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    T object = gson.fromJson(json, typeOfT);

    if (object != null && json.isJsonObject()) {
      JsonObject jsonObject = json.getAsJsonObject();

      Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

      List<String> keys = getKeys(object);

      if (keys != null && !keys.isEmpty()) {
        for (Map.Entry<String, JsonElement> item : entries) {
          if (!keys.contains(item.getKey())) {
            object.getUnknown().put(item.getKey(), gson.fromJson(item.getValue(), Object.class));
          }
        }
      }
    }

    return object;
  }

  private List<String> getKeys(T clazz) {
    List<String> keys = null;

    if (clazz instanceof SentryEvent) {
      keys = SentryEvent.KEYS;
    } else { // if...
      // get the keys of all the classes which suppots unknown properties, or create our own naming
      // policy (like LOWER_CASE_WITH_UNDERSCORES) over reflection
    }

    return keys;
  }
}
