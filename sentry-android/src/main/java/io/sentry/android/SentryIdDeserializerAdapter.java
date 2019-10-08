package io.sentry.android;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.sentry.protocol.SentryId;
import java.lang.reflect.Type;
import java.util.UUID;

class SentryIdDeserializerAdapter implements JsonDeserializer<SentryId> {
  @Override
  public SentryId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return json == null ? null : new SentryId(fromStringSentryId(json.getAsString()));
  }

  private UUID fromStringSentryId(String sentryIdString) {
    if (sentryIdString == null) {
      return null;
    }

    if (sentryIdString.length() == 32) {
      // expected format, SentryId is a UUID without dashes
      sentryIdString =
          new StringBuffer(sentryIdString)
              .insert(8, "-")
              .insert(13, "-")
              .insert(18, "-")
              .insert(23, "-")
              .toString();
    }
    if (sentryIdString.length() != 36) {
      throw new IllegalArgumentException(
          "String representation of SentryId has either 32 (UUID no dashes) "
              + "or 36 characters long (completed UUID). Received: "
              + sentryIdString);
    }

    return UUID.fromString(sentryIdString);
  }
}
