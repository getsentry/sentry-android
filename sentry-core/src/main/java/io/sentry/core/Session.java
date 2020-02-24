package io.sentry.core;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public final class Session {

  public enum State {
    // TODO: What about 'Started' 'Ended'? Ok is not very clear
    Ok,
    Exited,
    Crashed
  }

  private final Date started;
  // NOTE: Serializes as 'timestamp'
  private volatile Date ended;
  private final AtomicInteger errorCount = new AtomicInteger();
  // TODO: serializes as 'did'? Must be UUID?
  private String deviceId;
  // serializes as 'sid'?
  private String sessionId;
  private String ipAddress;
  private String userAgent;
  private String environment;
  private String release;

  // TODO: started as non final and expose start() ?
  public Session() {
    // TODO: No millisecond precision?
    this.started = DateUtils.getCurrentDateTime();
  }

  public void addError() {
    // TODO: Need to be synchronized anyway to mark Status as crashed or something?
    // Discard result
    errorCount.addAndGet(1);
  }

  public synchronized void end() {
    if (ended != null) {
      ended = DateUtils.getCurrentDateTime();
    } else {
      // TODO: take ILogger and log out a warn?
    }
  }

  public Date getStarted() {
    return started;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public String getRelease() {
    return release;
  }
}
