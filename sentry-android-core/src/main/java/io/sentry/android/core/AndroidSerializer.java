package io.sentry.android.core;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.sentry.core.*;
import io.sentry.core.protocol.SentryId;
import java.io.Writer;
import java.util.Date;

public class AndroidSerializer implements ISerializer {

  private Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(SentryId.class, new SentryIdSerializerAdapter())
          .registerTypeAdapter(SentryId.class, new SentryIdDeserializerAdapter())
          .registerTypeAdapter(Date.class, new DateSerializerAdapter())
          .registerTypeAdapter(Date.class, new DateDeserializerAdapter())
          .registerTypeAdapterFactory(UnknownPropertiesTypeAdapterFactory.get())
          .create();

  @Override
  public SentryEvent deserializeEvent(String envelope) {
    return gson.fromJson(envelope, SentryEvent.class);
  }

  @Override
  public void serialize(SentryEvent event, Writer writer) {
    gson.toJson(event, SentryEvent.class, writer);
  }
}
