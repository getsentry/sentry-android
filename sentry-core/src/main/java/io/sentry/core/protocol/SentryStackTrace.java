package io.sentry.core.protocol;

import io.sentry.core.IUnknownPropertiesConsumer;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

/** The Sentry stacktrace. */
public final class SentryStackTrace implements IUnknownPropertiesConsumer {
  private List<SentryStackFrame> frames;
  private Map<String, String> registers;

  @SuppressWarnings("unused")
  private Map<String, Object> unknown;

  public SentryStackTrace() {}

  public SentryStackTrace(List<SentryStackFrame> frames) {
    this.frames = frames;
  }
  /**
   * Gets the frames of this stacktrace.
   *
   * @return the frames.
   */
  public List<SentryStackFrame> getFrames() {
    return frames;
  }

  /**
   * Sets the frames of this stacktrace.
   *
   * @param frames the frames.
   */
  public void setFrames(List<SentryStackFrame> frames) {
    this.frames = frames;
  }

  @ApiStatus.Internal
  @Override
  public void acceptUnknownProperties(Map<String, Object> unknown) {
    this.unknown = unknown;
  }

  public Map<String, String> getRegisters() {
    return registers;
  }

  public void setRegisters(Map<String, String> registers) {
    this.registers = registers;
  }
}
