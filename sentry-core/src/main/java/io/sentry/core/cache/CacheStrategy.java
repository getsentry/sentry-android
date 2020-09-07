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

      // TODO: double check the range
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
    final SentryEnvelope currentEnvelope = getEnvelope(currentFile);

    if (currentEnvelope == null) {
      return;
    }

    final Session session = getSession(currentEnvelope);

    if (session == null) {
      return;
    }

    if (!session.getStatus().equals(Session.State.Ok)) {
      return;
    }

    final Boolean init = session.getInit();
    if (init == null || !init) {
      return;
    }

    final UUID sessionId = session.getSessionId();

    if (sessionId == null) {
      return;
    }

    // we need to move the init flag
    for (final File notDeletedFile : notDeletedFiles) {
      final SentryEnvelope envelopeItem = getEnvelope(notDeletedFile);

      if (envelopeItem == null) {
        continue;
      }

      SentryEnvelopeItem newSessionItem = null;
      final Iterator<SentryEnvelopeItem> itemsIterator = envelopeItem.getItems().iterator();

      while (itemsIterator.hasNext()) {
        final SentryEnvelopeItem sentryEnvelopeItem = itemsIterator.next();

        if (!sentryEnvelopeItem.getHeader().getType().equals(SentryItemType.Session)) {
          continue;
        }

        try (final Reader reader =
            new BufferedReader(
                new InputStreamReader(
                    new ByteArrayInputStream(sentryEnvelopeItem.getData()), UTF_8))) {
          final Session sessionItem = serializer.deserializeSession(reader);

          if (sessionItem == null) {
            continue;
          }

          if (sessionId.equals(sessionItem.getSessionId())) {
            final Boolean initItem = sessionItem.getInit();
            if (initItem == null || !initItem) {
              continue;
            }

            sessionItem.setInitAsTrue();
            newSessionItem = SentryEnvelopeItem.fromSession(serializer, sessionItem);
            itemsIterator.remove();

            break;
          }
        } catch (Exception e) {
          // TODO: catch it
        }
      }

      if (newSessionItem != null) {
        final List<SentryEnvelopeItem> newEnvelopeItems = new ArrayList<>();

        for (final SentryEnvelopeItem newEnvelopeItem : envelopeItem.getItems()) {
          newEnvelopeItems.add(newEnvelopeItem);
        }
        newEnvelopeItems.add(newSessionItem);

        final SentryEnvelope newEnvelope =
            new SentryEnvelope(envelopeItem.getHeader(), newEnvelopeItems);

        notDeletedFile.delete();

        try (final OutputStream outputStream = new FileOutputStream(notDeletedFile);
            final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8))) {
          serializer.serialize(newEnvelope, writer);
        } catch (Exception e) {
          // TODO: log it
        }
      }
    }
  }

  private @Nullable SentryEnvelope getEnvelope(final @NotNull File file) {
    try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
      return serializer.deserializeEnvelope(inputStream);
    } catch (IOException e) {
      // TODO: catch it
    }

    return null;
  }

  private @Nullable Session getSession(final @NotNull SentryEnvelope envelope) {
    for (final SentryEnvelopeItem item : envelope.getItems()) {
      if (!item.getHeader().getType().equals(SentryItemType.Session)) {
        continue;
      }

      try (final Reader reader =
          new BufferedReader(
              new InputStreamReader(new ByteArrayInputStream(item.getData()), UTF_8))) {
        return serializer.deserializeSession(reader);
      } catch (Exception e) {
        // TODO: catch it
      }
    }
    return null;
  }
}
