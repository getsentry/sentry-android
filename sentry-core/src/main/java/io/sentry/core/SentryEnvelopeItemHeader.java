package io.sentry.core;

public final class SentryEnvelopeItemHeader {
  private final String contentType;
  private final String fileName;
  private final String type;
  private final long length;

  public String getType() {
    return type;
  }

  public long getLength() {
    return length;
  }

  public String getContentType() {
    return contentType;
  }

  public String getFileName() {
    return fileName;
  }

  public SentryEnvelopeItemHeader(String type, long length, String contentType, String fileName) {
    this.type = type;
    this.length = length;
    this.contentType = contentType;
    this.fileName = fileName;
  }
}
