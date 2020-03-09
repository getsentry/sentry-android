package io.sentry.core;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SessionAdapter extends TypeAdapter<Session> {
  @Override
  public void write(JsonWriter writer, Session value) throws IOException {
    if (value == null) {
      writer.nullValue(); // TODO: is it compatible with envelopes?
      return;
    }
    writer.beginObject();

    if (value.getSessionId() != null) {
      writer.name("sid");
      writer.value(value.getSessionId().toString());
    }

    if (value.getDeviceId() != null) {
      writer.name("did");
      writer.value(value.getDeviceId());
    }

    if (value.getInit() != null) {
      writer.name("init");
      writer.value(value.getInit());
    }

    if (value.getStarted() != null) {
      writer.name("started");
      writer.value(DateUtils.getTimestamp(value.getStarted()));
    }

    if (value.getStatus() != null) {
      writer.name("status");
      writer.value(value.getStatus().name().toLowerCase(Locale.ROOT));
    }

    int errorCount = value.errorCount();
    if (errorCount > 0) {
      writer.name("errors");
      writer.value(errorCount);
    }

    // TODO: attrs
    // TODO: seq, is timestamp started?, duration,

    writer.endObject();
  }

  @Override
  public Session read(JsonReader in) throws IOException {
    return null;
  }
}
