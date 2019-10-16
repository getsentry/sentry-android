package io.sentry.android.core.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.sentry.core.protocol.Device;
import java.lang.reflect.Type;
import java.util.Locale;

public class OrientationDeserializerAdapter implements JsonDeserializer<Device.DeviceOrientation> {
  @Override
  public Device.DeviceOrientation deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return json == null
        ? null
        : Device.DeviceOrientation.valueOf(json.getAsString().toUpperCase(Locale.ROOT));
  }
}
