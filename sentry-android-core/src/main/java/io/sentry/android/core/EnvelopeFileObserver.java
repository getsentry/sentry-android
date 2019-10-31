package io.sentry.android.core;

import android.os.FileObserver;
import io.sentry.core.*;
import io.sentry.core.util.Objects;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

final class EnvelopeFileObserver extends FileObserver {

  private String path;
  private IHub hub;
  private final EnvelopeReader envelopeReader;
  private ISerializer serializer;
  private ILogger logger;
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  // The preferred overload (Taking File instead of String) is only available from API 29
  @SuppressWarnings("deprecation")
  EnvelopeFileObserver(String path, IHub hub, EnvelopeReader envelopeReader, ISerializer serializer, ILogger logger) {
    super(path);
    this.hub = Objects.requireNonNull(hub, "Hub is required.");
    this.path = Objects.requireNonNull(path, "File path is required.");
    this.envelopeReader = Objects.requireNonNull(envelopeReader, "Envelope reader is required.");
    this.serializer = Objects.requireNonNull(serializer, "Serializer is required.");
    this.logger = Objects.requireNonNull(logger, "Logger is required.");
  }

  @Override
  public void onEvent(int index, @android.support.annotation.Nullable String file) {
    logger.log(SentryLevel.DEBUG, "onEvent fired for EnvelopeFileObserver with index %d on path: %s for file %s.", index, this.path, file);

    InputStream stream = null;
    try {
      stream = new FileInputStream(file);
      SentryEnvelope envelope = envelopeReader.read(stream);
      logger.log(SentryLevel.DEBUG, "Envelope for event Id: %s", envelope.getHeader().getEventId());
      int items = 0;
      for (SentryEnvelopeItem item: envelope.getItems()) {
        items++;
        if (item.getHeader() != null) {
          logger.log(SentryLevel.ERROR, "Item %d has no header", items);
          continue;
        }

        if ("event".equals(item.getHeader().getType())) {
          Reader eventReader = null;
          try {
            eventReader = new InputStreamReader(new ByteArrayInputStream(item.getData()), UTF_8);
            SentryEvent event = serializer.deserializeEvent(eventReader);
            if (envelope.getHeader().getEventId() != event.getEventId()) {
              logger.log(SentryLevel.ERROR, "Item %d of has a different event id (%s) to the envelope header (s)", items, envelope.getHeader().getEventId(), event.getEventId());
              continue;
            }
            hub.captureEvent(event);
            logger.log(SentryLevel.DEBUG, "Item %d is being captured.", items);
          } finally {
            eventReader.close();
          }
        } else {
          logger.log(SentryLevel.WARNING, "Item %d of type: %s ignored.", items, item.getHeader().getType());
        }
      }
    } catch (IOException e) {
      logger.log(SentryLevel.ERROR, "Error reading envelope.", e);
    } finally {
      try {
        if (stream != null)
          stream.close();
      } catch (IOException ex) {
        logger.log(SentryLevel.ERROR, "Error closing envelope.", ex);
      }
      // TODO: get rid of the file the file
    }
  }
}
