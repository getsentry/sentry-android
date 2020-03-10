package io.sentry.core;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class Session {

  public enum State {
    Ok,
    Exited,
    Crashed,
    Abnormal // ,
    //    Degraded TODO: do we need it?
  }

  private Date started; // TODO: maybe should be final and get it in the ctor
  // NOTE: Serializes as 'timestamp'
  private Date ended;
  private final AtomicInteger errorCount;
  // TODO: serializes as 'did'? Must be UUID?
  private String deviceId; // did, distinctId, optional
  // serializes as 'sid'?
  private UUID sessionId; // sid
  private Boolean init;
  private State status; // if none, it should be State.Ok
  private Integer sequence;
  private Double duration; // maybe float?

  // TODO: do we need user? if we have deviceId
  // its only to set did properly, id, email or username
  // but actually we'd like to go with a generated GUID

  // attrs
  private String ipAddress;
  private String userAgent;
  private String environment;
  private String release;

  // TODO: started as non final and expose start() ?
  public Session(final int errorCount) {
    // TODO: No millisecond precision?
    //    this.started = DateUtils.getCurrentDateTime();
    this.errorCount = new AtomicInteger(errorCount);
  }

  // TODO: started as non final and expose start() ?
  public Session() {
    this(0);
  }

  public void addError() {
    // TODO: Need to be synchronized anyway to mark Status as crashed or something?
    // Discard result
    errorCount.addAndGet(1);
  }

  public synchronized void end() {
    if (getEnded() != null) {
      setEnded(DateUtils.getCurrentDateTime());
    } else {
      // TODO: take ILogger and log out a warn?
    }
    if (status == null || status == State.Ok) {
      status = State.Exited;
    }
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(UUID sessionId) {
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

  public Boolean getInit() {
    return init;
  }

  public void setInit(Boolean init) {
    this.init = init;
  }

  public int errorCount() {
    return errorCount.get();
  }

  // TODO: maybe this should be only in the ctor
  public void setErrorCount(int errorCount) {
    this.errorCount.set(errorCount);
  }

  public State getStatus() {
    return status;
  }

  public void setStatus(State status) {
    this.status = status;
  }

  public Integer getSequence() {
    return sequence;
  }

  public void setSequence(Integer sequence) {
    this.sequence = sequence;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public Date getEnded() {
    return ended;
  }

  public void setEnded(Date ended) {
    this.ended = ended;
  }

  public synchronized void start() {
    if (sessionId == null) {
      sessionId = UUID.randomUUID();
    }
    if (started == null) {
      started = DateUtils.getCurrentDateTime();
    }
    if (status == null) {
      status = State.Ok;
    }
    if (deviceId == null) {
      // TODO: get as a param?
      deviceId = UUID.randomUUID().toString();
    }
    // ip address come from user
  }
}
