package io.sentry.core.cache;

import static io.sentry.core.SentryLevel.DEBUG;
import static io.sentry.core.SentryLevel.ERROR;
import static io.sentry.core.SentryLevel.INFO;
import static io.sentry.core.SentryLevel.WARNING;
import static java.lang.String.format;

import io.sentry.core.ISerializer;
import io.sentry.core.SentryEnvelope;
import io.sentry.core.SentryEnvelopeItem;
import io.sentry.core.SentryEnvelopeItemType;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.Session;
import io.sentry.core.hints.SessionEnd;
import io.sentry.core.hints.SessionStart;
import io.sentry.core.hints.SessionUpdate;
import io.sentry.core.util.Objects;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class SessionCache implements IEnvelopeCache {

  /** File suffix added to all serialized envelopes files. */
  static final String SUFFIX_ENVELOPE_FILE = ".envelope";

  public static final String PREFIX_CURRENT_SESSION_FILE = "session";
  static final String SUFFIX_CURRENT_SESSION_FILE = ".json";

  @SuppressWarnings("CharsetObjectCanBeUsed")
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private final @NotNull File directory;
  private final int maxSize;
  private final @NotNull ISerializer serializer;
  private final @NotNull SentryOptions options;

  private final @NotNull Map<SentryEnvelope, String> fileNameMap = new WeakHashMap<>();

  public SessionCache(final @NotNull SentryOptions options) {
    Objects.requireNonNull(options.getSessionsPath(), "sessions dir. path is required.");
    this.directory = new File(options.getSessionsPath());
    this.maxSize = options.getSessionsDirSize();
    this.serializer = options.getSerializer();
    this.options = options;
  }

  @Override
  public void store(final @NotNull SentryEnvelope envelope, final @Nullable Object hint) {
    Objects.requireNonNull(envelope, "Envelope is required.");

    if (getNumberOfStoredEnvelopes() >= maxSize) {
      options
          .getLogger()
          .log(
              SentryLevel.WARNING,
              "Disk cache full (respecting maxSize). Not storing envelope {}",
              envelope);
      return;
    }

    final File currentEnvelopeFile = getCurrentSessionFile();

    if (hint instanceof SessionEnd) {
      if (!currentEnvelopeFile.delete()) {
        options.getLogger().log(WARNING, "Current envelope doesn't exist.");
      }
    }

    if (hint instanceof SessionStart) {
      if (currentEnvelopeFile.exists()) {
        options.getLogger().log(WARNING, "Current envelope is not ended, we'd need to end it.");

        try (final Reader reader =
            new BufferedReader(
                new InputStreamReader(new FileInputStream(currentEnvelopeFile), UTF_8))) {

          final Session session = serializer.deserializeSession(reader);

          if (session == null) {
            options
                .getLogger()
                .log(
                    SentryLevel.ERROR,
                    "Stream from path %s resulted in a null envelope.",
                    currentEnvelopeFile.getAbsolutePath());
          } else {
            options
                .getLogger()
                .log(
                    INFO,
                    "There's a left over session, it's gonna be ended and cached to be sent.");
            session.end();
            final SentryEnvelope fromSession = SentryEnvelope.fromSession(serializer, session);
            final File fileFromSession = getEnvelopeFile(fromSession);
            writeEnvelopeToDisk(fileFromSession, fromSession);
          }
        } catch (IOException e) {
          options.getLogger().log(SentryLevel.ERROR, "Error processing session.", e);
        }
      } else {
        final Iterable<SentryEnvelopeItem> items = envelope.getItems();

        // we know that an envelope with a SessionStart hint has a single item inside
        if (items.iterator().hasNext()) {
          final SentryEnvelopeItem item = items.iterator().next();

          if (SentryEnvelopeItemType.Session.getType().equals(item.getHeader().getType())) {
            try (final Reader reader =
                new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(item.getData()), UTF_8))) {
              final Session session = serializer.deserializeSession(reader);
              if (session == null) {
                options
                    .getLogger()
                    .log(
                        SentryLevel.ERROR,
                        "Item %d of type %s returned null by the parser.",
                        items,
                        item.getHeader().getType());
              } else {
                writeSessionToDisk(currentEnvelopeFile, session);
              }
            } catch (Exception e) {
              options.getLogger().log(ERROR, "Item failed to process.", e);
            }
          } else {
            options
                .getLogger()
                .log(
                    INFO,
                    "Current envelope has a different envelope type %s",
                    item.getHeader().getType());
          }
        } else {
          options
              .getLogger()
              .log(INFO, "Current envelope is empty %s", envelope.getHeader().getEventId());
        }
      }
    }

    if (hint instanceof SessionUpdate) {
      writeEnvelopeToDisk(currentEnvelopeFile, envelope);
      return;
    }

    final File envelopeFile = getEnvelopeFile(envelope);
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

    writeEnvelopeToDisk(envelopeFile, envelope);
  }

  private void writeEnvelopeToDisk(
      final @NotNull File file, final @NotNull SentryEnvelope envelope) {
    if (file.exists()) {
      options
          .getLogger()
          .log(
              DEBUG,
              "Overwriting envelope to offline storage: %s",
              envelope.getHeader().getEventId());
      file.delete();
    }

    try (final OutputStream outputStream = new FileOutputStream(file);
        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8))) {
      serializer.serialize(envelope, writer);
    } catch (Exception e) {
      options
          .getLogger()
          .log(
              ERROR,
              "Error writing Envelope to offline storage: %s",
              envelope.getHeader().getEventId());
    }
  }

  private void writeSessionToDisk(final @NotNull File file, final @NotNull Session session) {
    if (file.exists()) {
      options
          .getLogger()
          .log(DEBUG, "Overwriting session to offline storage: %s", session.getSessionId());
      file.delete();
    }

    try (final OutputStream outputStream = new FileOutputStream(file);
        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8))) {
      serializer.serialize(session, writer);
    } catch (Exception e) {
      options
          .getLogger()
          .log(ERROR, "Error writing Session to offline storage: %s", session.getSessionId());
    }
  }

  @Override
  public void discard(final @NotNull SentryEnvelope envelope) {
    Objects.requireNonNull(envelope, "Envelope is required.");

    final File envelopeFile = getEnvelopeFile(envelope);
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

  /**
   * Returns the envelope's file path. If the envelope has no eventId header, it generates a random
   * file name to it.
   *
   * @param envelope the SentryEnvelope object
   * @return the file
   */
  private synchronized @NotNull File getEnvelopeFile(final @NotNull SentryEnvelope envelope) {
    String filaName;
    if (fileNameMap.containsKey(envelope)) {
      filaName = fileNameMap.get(envelope);
    } else {
      if (envelope.getHeader().getEventId() != null) {
        filaName = envelope.getHeader().getEventId().toString();
      } else {
        filaName = UUID.randomUUID().toString();
      }
      filaName += SUFFIX_ENVELOPE_FILE;
      fileNameMap.put(envelope, filaName);
    }

    return new File(directory.getAbsolutePath(), filaName);
  }

  private @NotNull File getCurrentSessionFile() {
    return new File(
        directory.getAbsolutePath(), PREFIX_CURRENT_SESSION_FILE + SUFFIX_CURRENT_SESSION_FILE);
  }

  @Override
  public @NotNull Iterator<SentryEnvelope> iterator() {
    final File[] allCachedEnvelopes = allEnvelopeFiles();

    final List<SentryEnvelope> ret = new ArrayList<>(allCachedEnvelopes.length);

    for (final File file : allCachedEnvelopes) {
      try (final InputStream is = new BufferedInputStream(new FileInputStream(file))) {

        ret.add(serializer.deserializeEnvelope(is));
      } catch (FileNotFoundException e) {
        options
            .getLogger()
            .log(
                DEBUG,
                "Envelope file '%s' disappeared while converting all cached files to envelopes.",
                file.getAbsolutePath());
      } catch (IOException e) {
        options
            .getLogger()
            .log(
                ERROR,
                format("Error while reading cached envelope from file %s", file.getAbsolutePath()),
                e);
      }
    }

    return ret.iterator();
  }

  private @NotNull File[] allEnvelopeFiles() {
    if (isDirectoryValid()) {
      // lets filter the session.json here
      return directory.listFiles((__, fileName) -> fileName.endsWith(SUFFIX_ENVELOPE_FILE));
    }
    return new File[] {};
  }
}
