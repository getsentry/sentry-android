package io.sentry.core;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SessionAdapter extends TypeAdapter<Session> {
  @Override
  public void write(JsonWriter writer, Session value) throws IOException {
    if (value == null) {
      writer.nullValue();
      return;
    }
    writer.beginObject();

    if (value.getSessionId() != null) {
      writer.name("sid").value(value.getSessionId().toString());
    }

    if (value.getDeviceId() != null) {
      writer.name("did").value(value.getDeviceId());
    }

    if (value.getInit() != null) {
      writer.name("init").value(value.getInit());
    }

    if (value.getStarted() != null) {
      writer.name("started").value(DateUtils.getTimestamp(value.getStarted()));
    }

    if (value.getStatus() != null) {
      writer.name("status").value(value.getStatus().name().toLowerCase(Locale.ROOT));
    }

    if (value.getSequence() != null) {
      writer.name("seq").value(value.getSequence());
    }

    int errorCount = value.errorCount();
    if (errorCount > 0) {
      writer.name("errors").value(errorCount);
    }

    if (value.getDuration() != null) {
      writer.name("duration").value(value.getDuration());
    }

    if (value.getEnded() != null) {
      writer.name("timestamp").value(DateUtils.getTimestamp(value.getEnded()));
    }

    // TODO: attrs

    writer.endObject();
  }

  @Override
  public Session read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    Session session = new Session();

    reader.beginObject();

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "sid":
          session.setSessionId(UUID.fromString(reader.nextString()));
          break;
        case "did":
          session.setDeviceId(reader.nextString());
          break;
        case "init":
          session.setInit(reader.nextBoolean());
          break;
        case "started":
          session.setStarted(DateUtils.getDateTime(reader.nextString()));
          break;
        case "status":
          session.setStatus(Session.State.valueOf(capitalize(reader.nextString())));
          break;
        case "errors":
          session.setErrorCount(reader.nextInt());
          break;
        case "seq":
          session.setSequence(reader.nextInt());
          break;
        case "duration":
          session.setDuration(reader.nextDouble());
          break;
        case "timestamp":
          session.setEnded(DateUtils.getDateTime(reader.nextString()));
          break;
        default:
          reader.skipValue();
          break;
      }
    }
    reader.endObject();

    return session;
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1);
  }
}
