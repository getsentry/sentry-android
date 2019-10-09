package io.sentry.android;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.sentry.ISerializer;
import io.sentry.SentryEnvelope;
import io.sentry.SentryEvent;
import io.sentry.android.adapters.*;
import io.sentry.protocol.SentryId;
import java.util.Date;

public class AndroidSerializer implements ISerializer {

  private Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(SentryId.class, new SentryIdSerializerAdapter())
          .registerTypeAdapter(SentryId.class, new SentryIdDeserializerAdapter())
          .registerTypeAdapter(Date.class, new DateSerializerAdapter())
          .registerTypeAdapter(Date.class, new DateDeserializerAdapter())
          .registerTypeAdapter(
              SentryEvent.class,
              new UnknownAdapter<
                  SentryEvent>()) // register the adapters for all the classes which supports
          // unknown properties
          .create();

  @Override
  public SentryEnvelope deserializeEnvelope(String envelope) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SentryEvent deserializeEvent(String envelope) {
    return gson.fromJson(envelope, SentryEvent.class);
  }

  @Override
  public String serialize(SentryEvent event) {
    return gson.toJson(event);
  }
}
