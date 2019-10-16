package io.sentry.android.core.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.sentry.core.protocol.Device;
import java.lang.reflect.Type;
import java.util.Locale;

public class OrientationSerializerAdapter implements JsonSerializer<Device.DeviceOrientation> {
  @Override
  public JsonElement serialize(
      Device.DeviceOrientation src, Type typeOfSrc, JsonSerializationContext context) {
    return src == null ? null : new JsonPrimitive(src.name().toLowerCase(Locale.ROOT));
  }
}
