package io.sentry.core;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.TestOnly;

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

    if (value.getDistinctId() != null) {
      writer.name("did").value(value.getDistinctId());
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

    if (value.getTimestamp() != null) {
      writer.name("timestamp").value(DateUtils.getTimestamp(value.getTimestamp()));
    }

    boolean hasInitAttrs = false;
    if (value.getRelease() != null) {
      hasInitAttrs = initAttrs(writer, hasInitAttrs);

      writer.name("release").value(value.getRelease());
    }

    if (value.getEnvironment() != null) {
      hasInitAttrs = initAttrs(writer, hasInitAttrs);

      writer.name("environment").value(value.getEnvironment());
    }

    if (value.getIpAddress() != null) {
      hasInitAttrs = initAttrs(writer, hasInitAttrs);

      writer.name("ip_address").value(value.getIpAddress());
    }

    if (value.getUserAgent() != null) {
      hasInitAttrs = initAttrs(writer, hasInitAttrs);

      writer.name("user_agent").value(value.getUserAgent());
    }

    if (hasInitAttrs) {
      writer.endObject();
    }

    writer.endObject();
  }

  private boolean initAttrs(JsonWriter writer, boolean hasInitAtts) throws IOException {
    if (!hasInitAtts) {
      writer.name("attrs").beginObject();
    }
    return true;
  }

  @Override
  public Session read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    UUID sid = null;
    String did = null;
    Boolean init = null;
    Date started = null;
    Session.State status = null;
    int errors = 0;
    Long seq = null;
    Double duration = null;
    Date timestamp = null;
    String release = null;
    String environment = null;
    String ipAddress = null;
    String userAgent = null;

    reader.beginObject();

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "sid":
          sid = UUID.fromString(reader.nextString());
          break;
        case "did":
          did = reader.nextString();
          break;
        case "init":
          init = reader.nextBoolean();
          break;
        case "started":
          started = DateUtils.getDateTime(reader.nextString());
          break;
        case "status":
          status = Session.State.valueOf(capitalize(reader.nextString()));
          break;
        case "errors":
          errors = reader.nextInt();
          break;
        case "seq":
          seq = reader.nextLong();
          break;
        case "duration":
          duration = reader.nextDouble();
          break;
        case "timestamp":
          timestamp = DateUtils.getDateTime(reader.nextString());
          break;
        case "attrs":
          {
            reader.beginObject();

            while (reader.hasNext()) {
              switch (reader.nextName()) {
                case "release":
                  release = reader.nextString();
                  break;
                case "environment":
                  environment = reader.nextString();
                  break;
                case "ip_address":
                  ipAddress = reader.nextString();
                  break;
                case "user_agent":
                  userAgent = reader.nextString();
                  break;
                default:
                  reader.skipValue();
                  break;
              }
            }
            reader.endObject();
            break;
          }
        default:
          reader.skipValue();
          break;
      }
    }
    reader.endObject();

    return new Session(
        status,
        started,
        timestamp,
        errors,
        did,
        sid,
        init,
        seq,
        duration,
        ipAddress,
        userAgent,
        environment,
        release);
  }

  @TestOnly
  String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1);
  }
}
