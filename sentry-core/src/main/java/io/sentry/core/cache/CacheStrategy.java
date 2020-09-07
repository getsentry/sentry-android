package io.sentry.core.cache;

import static io.sentry.core.SentryLevel.ERROR;

import io.sentry.core.ISerializer;
import io.sentry.core.SentryEnvelope;
import io.sentry.core.SentryEnvelopeItem;
import io.sentry.core.SentryItemType;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.Session;
import io.sentry.core.util.Objects;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class CacheStrategy {

  @SuppressWarnings("CharsetObjectCanBeUsed")
  protected static final Charset UTF_8 = Charset.forName("UTF-8");

  protected final @NotNull SentryOptions options;
  protected final @NotNull ISerializer serializer;
  protected final @NotNull File directory;
  private final int maxSize;

  CacheStrategy(
      final @NotNull SentryOptions options,
      final @NotNull String directoryPath,
      final int maxSize) {
    Objects.requireNonNull(directoryPath, "Directory is required.");
    this.options = Objects.requireNonNull(options, "SentryOptions is required.");

    this.serializer = options.getSerializer();
    this.directory = new File(directoryPath);

    this.maxSize = maxSize;
  }

  /**
   * Check if a dir. is valid and have write and read permission
   *
   * @return true if valid and has permissions or false otherwise
   */
  protected boolean isDirectoryValid() {
    if (!directory.isDirectory() || !directory.canWrite() || !directory.canRead()) {
      options
          .getLogger()
          .log(
              ERROR,
              "The directory for caching files is inaccessible.: %s",
              directory.getAbsolutePath());
      return false;
    }
    return true;
  }

  /**
   * Sort files from oldest to the newest using the lastModified method
   *
   * @param files the Files
   */
  private void sortFilesOldestToNewest(@NotNull File[] files) {
    // just sort it if more than 1 file
    if (files.length > 1) {
      Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
    }
  }

  /**
   * Rotates the caching folder if full, deleting the oldest files first
   *
   * @param files the Files
   */
  protected void rotateCacheIfNeeded(final @NotNull File[] files) {
    final int length = files.length;
    if (length >= maxSize) {
      options
          .getLogger()
          .log(SentryLevel.WARNING, "Cache folder if full (respecting maxSize). Rotating files");
      final int totalToBeDeleted = (length - maxSize) + 1;

      sortFilesOldestToNewest(files);

      final File[] notDeletedFiles = Arrays.copyOfRange(files, totalToBeDeleted, length);

      // delete files from the top of the Array as its sorted by the oldest to the newest
      for (int i = 0; i < totalToBeDeleted; i++) {
        final File file = files[i];

        // move init flag if necessary
        moveInitFlagIfNecessary(file, notDeletedFiles);

        if (!file.delete()) {
          options
              .getLogger()
              .log(SentryLevel.WARNING, "File can't be deleted: %s", file.getAbsolutePath());
        }
      }
    }
  }

  private void moveInitFlagIfNecessary(
      final @NotNull File currentFile, final @NotNull File[] notDeletedFiles) {
    final SentryEnvelope currentEnvelope = readEnvelope(currentFile);

    if (!isValidEnvelope(currentEnvelope)) {
      return;
    }

    final Session currentSession = getFirstSession(currentEnvelope);

    if (!isValidSession(currentSession)) {
      return;
    }

    // nothing to do if its not true
    final Boolean currentSessionInit = currentSession.getInit();
    if (currentSessionInit == null || !currentSessionInit) {
      return;
    }

    // we need to move the init flag
    for (final File notDeletedFile : notDeletedFiles) {
      final SentryEnvelope envelope = readEnvelope(notDeletedFile);

      if (!isValidEnvelope(envelope)) {
        continue;
      }

      SentryEnvelopeItem newSessionItem = null;
      final Iterator<SentryEnvelopeItem> itemsIterator = envelope.getItems().iterator();

      while (itemsIterator.hasNext()) {
        final SentryEnvelopeItem envelopeItem = itemsIterator.next();

        if (!isSessionType(envelopeItem)) {
          continue;
        }

        final Session session = readSession(envelopeItem);

        if (!isValidSession(session)) {
          continue;
        }

        // if its already true, nothing to do
        final Boolean init = session.getInit();
        if (init != null && init) {
          return;
        }

        if (currentSession.getSessionId().equals(session.getSessionId())) {
          session.setInitAsTrue();
          try {
            newSessionItem = SentryEnvelopeItem.fromSession(serializer, session);
            // remove item from envelope items so we can replace with the new one that has the
            // init flag true
            itemsIterator.remove();
          } catch (IOException e) {
            options
                .getLogger()
                .log(
                    ERROR,
                    e,
                    "Failed to create new envelope item for the currentSession %s",
                    currentSession.getSessionId());
          }

          break;
        }
      }

      if (newSessionItem != null) {
        final SentryEnvelope newEnvelope = buildNewEnvelope(envelope, newSessionItem);

        long notDeletedFileTimestamp = notDeletedFile.lastModified();
        if (!notDeletedFile.delete()) {
          options
              .getLogger()
              .log(
                  SentryLevel.WARNING,
                  "File can't be deleted: %s",
                  notDeletedFile.getAbsolutePath());
        }

        saveNewEnvelope(newEnvelope, notDeletedFile, notDeletedFileTimestamp);
        break;
      } else {
        // TODO: if theres no more files with that session id, should we still delete it?
        // that means the next session update will be init=false but the one
        // with init=true will be deleted.
        // either we don't delete it or we generate a new envelope only with that session.
      }
    }
  }

  private @Nullable SentryEnvelope readEnvelope(final @NotNull File file) {
    try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
      return serializer.deserializeEnvelope(inputStream);
    } catch (IOException e) {
      options.getLogger().log(ERROR, "Failed to deserialize the envelope.", e);
    }

    return null;
  }

  private @Nullable Session getFirstSession(final @NotNull SentryEnvelope envelope) {
    for (final SentryEnvelopeItem item : envelope.getItems()) {
      if (!isSessionType(item)) {
        continue;
      }

      // we are assuming that there's only 1 session per envelope for now
      return readSession(item);
    }
    return null;
  }

  private boolean isValidSession(final @Nullable Session session) {
    if (session == null) {
      return false;
    }

    if (!session.getStatus().equals(Session.State.Ok)) {
      return false;
    }

    final UUID sessionId = session.getSessionId();

    if (sessionId == null) {
      return false;
    }
    return true;
  }

  private boolean isSessionType(final @Nullable SentryEnvelopeItem item) {
    if (item == null) {
      return false;
    }

    return item.getHeader().getType().equals(SentryItemType.Session);
  }

  private @Nullable Session readSession(final @NotNull SentryEnvelopeItem item) {
    try (final Reader reader =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(item.getData()), UTF_8))) {
      return serializer.deserializeSession(reader);
    } catch (Exception e) {
      options.getLogger().log(ERROR, "Failed to deserialize the session.", e);
    }
    return null;
  }

  private void saveNewEnvelope(
      final @NotNull SentryEnvelope envelope, final @NotNull File file, final long timestamp) {
    try (final OutputStream outputStream = new FileOutputStream(file);
        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8))) {
      serializer.serialize(envelope, writer);
      // we need to set the same timestamp so the sorting from oldest to newest wont break.
      file.setLastModified(timestamp);
    } catch (Exception e) {
      options.getLogger().log(ERROR, "Failed to serialize the new envelope to the disk.", e);
    }
  }

  private @NotNull SentryEnvelope buildNewEnvelope(
      final @NotNull SentryEnvelope envelope, final @NotNull SentryEnvelopeItem sessionItem) {
    final List<SentryEnvelopeItem> newEnvelopeItems = new ArrayList<>();

    for (final SentryEnvelopeItem newEnvelopeItem : envelope.getItems()) {
      newEnvelopeItems.add(newEnvelopeItem);
    }
    newEnvelopeItems.add(sessionItem);

    return new SentryEnvelope(envelope.getHeader(), newEnvelopeItems);
  }

  private boolean isValidEnvelope(final @Nullable SentryEnvelope envelope) {
    if (envelope == null) {
      return false;
    }

    if (!envelope.getItems().iterator().hasNext()) {
      return false;
    }
    return true;
  }
}
