package io.sentry.core.cache;

import static io.sentry.core.SentryLevel.DEBUG;
import static io.sentry.core.SentryLevel.ERROR;
import static io.sentry.core.SentryLevel.WARNING;
import static java.lang.String.format;

import io.sentry.core.ISerializer;
import io.sentry.core.SentryEnvelope;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class EnvelopeCache implements IEnvelopeCache {

  /** File suffix added to all serialized envelopes files. */
  public static final String FILE_SUFFIX = ".envelope";

  @SuppressWarnings("CharsetObjectCanBeUsed")
  private static final Charset UTF8 = Charset.forName("UTF-8");

  private final File directory;
  private final int maxSize;
  private final ISerializer serializer;
  private final SentryOptions options;

  public EnvelopeCache(SentryOptions options) {
    Objects.requireNonNull(options.getSessionsPath(), "sessions dir. path is required.");
    this.directory = new File(options.getSessionsPath());
    this.maxSize = options.getSessionsDirSize();
    this.serializer = options.getSerializer();
    this.options = options;
  }

  @Override
  public void store(SentryEnvelope envelope) {
    if (getNumberOfStoredEnvelopes() >= maxSize) {
      options
          .getLogger()
          .log(
              SentryLevel.WARNING,
              "Disk cache full (respecting maxSize). Not storing envelope {}",
              envelope);
      return;
    }

    File envelopeFile = getEnvelopeFile(envelope);
    if (envelopeFile.exists()) {
      options
          .getLogger()
          .log(
              WARNING,
              "Not adding Envelope to offline storage because it already exists: %s",
              envelopeFile.getAbsolutePath());
      return;
    } else {
      options
          .getLogger()
          .log(DEBUG, "Adding Envelope to offline storage: %s", envelopeFile.getAbsolutePath());
    }

    try (OutputStream fileOutputStream = new FileOutputStream(envelopeFile);
        Writer wrt = new OutputStreamWriter(fileOutputStream, UTF8)) {
      serializer.serialize(envelope, wrt);
    } catch (Exception e) {
      options
          .getLogger()
          .log(
              ERROR,
              "Error writing Envelope to offline storage: %s",
              envelope.getHeader().getEventId());
    }
  }

  @Override
  public void discard(SentryEnvelope envelope) {
    File envelopeFile = getEnvelopeFile(envelope);
    if (envelopeFile.exists()) {
      options
          .getLogger()
          .log(DEBUG, "Discarding envelope from cache: %s", envelopeFile.getAbsolutePath());

      if (!envelopeFile.delete()) {
        options
            .getLogger()
            .log(ERROR, "Failed to delete envelope: %s", envelopeFile.getAbsolutePath());
      }
    } else {
      options.getLogger().log(DEBUG, "Envelope was not cached: %s", envelopeFile.getAbsolutePath());
    }
  }

  private int getNumberOfStoredEnvelopes() {
    return allEnvelopeFiles().length;
  }

  private boolean isDirectoryValid() {
    if (!directory.isDirectory() || !directory.canWrite() || !directory.canRead()) {
      options
          .getLogger()
          .log(
              ERROR,
              "The directory for caching Sentry envelopes is inaccessible.: %s",
              directory.getAbsolutePath());
      return false;
    }
    return true;
  }

  private File getEnvelopeFile(SentryEnvelope envelope) {
    return new File(
        directory.getAbsolutePath(), envelope.getHeader().getEventId().toString() + FILE_SUFFIX);
  }

  @NotNull
  @Override
  public Iterator<SentryEnvelope> iterator() {
    File[] allCachedEnvelopes = allEnvelopeFiles();

    List<SentryEnvelope> ret = new ArrayList<>(allCachedEnvelopes.length);

    for (File f : allCachedEnvelopes) {
      try (final InputStream is = new BufferedInputStream(new FileInputStream(f))) {

        ret.add(serializer.deserializeEnvelope(is));
      } catch (FileNotFoundException e) {
        options
            .getLogger()
            .log(
                DEBUG,
                "Envelope file '%s' disappeared while converting all cached files to envelopes.",
                f.getAbsolutePath());
      } catch (IOException e) {
        options
            .getLogger()
            .log(
                ERROR,
                format("Error while reading cached envelope from file %s", f.getAbsolutePath()),
                e);
      }
    }

    return ret.iterator();
  }

  private File[] allEnvelopeFiles() {
    if (isDirectoryValid()) {
      return directory.listFiles((__, fileName) -> fileName.endsWith(FILE_SUFFIX));
    }
    return new File[] {};
  }
}
