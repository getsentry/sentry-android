package io.sentry.core;

public final class InvalidDsnException extends RuntimeException {
  private static final long serialVersionUID = 412945154259913013L;
  private final String dsn;

  public InvalidDsnException(String dsn) {
    this.dsn = dsn;
  }

  public InvalidDsnException(String dsn, String message) {
    super(message);
    this.dsn = dsn;
  }

  public InvalidDsnException(String dsn, String message, Throwable cause) {
    super(message, cause);
    this.dsn = dsn;
  }

  InvalidDsnException(String dsn, Throwable cause) {
    super(cause);
    this.dsn = dsn;
  }

  public String getDsn() {
    return dsn;
  }
}
