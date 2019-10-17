package io.sentry.core;

import io.sentry.core.util.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnvelopeReader {

  private ISerializer serializer;

  public EnvelopeReader(ISerializer serializer) {
    this.serializer = serializer;
  }

  public @Nullable SentryEnvelope read(InputStream stream) {
    try {
      byte[] buffer = new byte[1024];
      int currentLength = 0;
      int streamOffset = 0;
      int headerOffset = -1;
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      while ((currentLength = stream.read(buffer)) != -1) {
        for (int i = 0; headerOffset == -1 && i < currentLength; i++) {
          if (buffer[i] == '\n') {
            if (headerOffset == -1) {
              headerOffset = streamOffset + i;
              break;
            }
          }
        }
        outputStream.write(buffer, 0, currentLength);
        streamOffset += currentLength;
      }
      // TODO: Work on the stream instead reading to the whole thing and allocating this array
      byte[] envelopeBytes = outputStream.toByteArray();

      if (headerOffset == -1 || envelopeBytes.length <= headerOffset) {
        // Invalid envelope
        return null;
      }

      SentryEnvelopeHeader header =
          serializer.deserializeEnvelopeHeader(envelopeBytes, 0, headerOffset);

      int startHeaderOffset = headerOffset + 1;
      int headerEndOffset = -1;
      int envelopeHeaderLength = -1;
      int payloadEndOffset = -1;
      List<SentryEnvelopeItem> items = new ArrayList<>();
      do {
        // Look from startHeaderOffset until line break to find next header
        for (int i = startHeaderOffset; i < envelopeBytes.length; i++) {
          if (envelopeBytes[i] == '\n') {
            envelopeHeaderLength = i - startHeaderOffset;
            break;
          }
        }

        SentryEnvelopeItemHeader itemHeader =
            serializer.deserializeEnvelopeItemHeader(
                envelopeBytes, startHeaderOffset, envelopeHeaderLength);

        headerEndOffset = startHeaderOffset + envelopeHeaderLength + 1; // \n
        payloadEndOffset = headerEndOffset + itemHeader.getLength();
        byte[] envelopeItemBytes =
            Arrays.copyOfRange(envelopeBytes, headerEndOffset, payloadEndOffset);
        SentryEnvelopeItem item = new SentryEnvelopeItem(itemHeader, envelopeItemBytes);
        items.add(item);
        startHeaderOffset = payloadEndOffset + 1;
        // TODO: test
        if (startHeaderOffset < envelopeBytes.length || envelopeBytes[startHeaderOffset] != '\n') {
          break;
        }
      } while (true);

      return new SentryEnvelope(header, items);
    } catch (Exception e) {
      // TODO Log
    }
    return null;
  }
}
