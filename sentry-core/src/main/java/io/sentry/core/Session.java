package io.sentry.core;

import io.sentry.core.protocol.User;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class Session {

  public enum State {
    Ok,
    Exited,
    Crashed,
    Abnormal
  }

  private Date started; // TODO: maybe should be final and get it in the ctor
  private Date ended;
  private AtomicInteger errorCount;
  private String deviceId; // did, distinctId
  private UUID sessionId; // sid
  private Boolean init;
  private State status; // if none, it should be State.Ok?
  private Integer sequence;
  private Double duration;
  private User user;

  // attrs
  private String ipAddress;
  private String userAgent;
  private String environment;
  private String release;

  // TODO: we might need a sessionLock like Scope.

  public synchronized void end() {
    if (ended == null) {
      ended = DateUtils.getCurrentDateTime();
    } else {
      // TODO: take ILogger and log out a warn?
    }

    if (status == null && errorCount.get() > 0) {
      status = State.Abnormal;
    } else if (status == null || status == State.Ok) {
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

  public void setErrorCount(int errorCount) {
    if (this.errorCount == null) {
      this.errorCount = new AtomicInteger(errorCount);
    } else {
      this.errorCount.set(errorCount);
    }
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

  public void setUser(User user) {
    this.user = user;
  }

  public synchronized void start() {
    init = true;
    sequence = 0;
    errorCount = new AtomicInteger(0);

    if (sessionId == null) {
      sessionId = UUID.randomUUID();
    }
    //    TODO: No millisecond precision?
    if (started == null) {
      started = DateUtils.getCurrentDateTime();
    }
    // this doesnt sound right
    //    if (status == null) {
    //      status = State.Ok;
    //    }
  }

  public synchronized void update(
      State status, User user, String userAgent, boolean addErrorsCount) {
    if (State.Crashed == status) {
      this.status = status;
    } // else {
    //      this.status = null;
    //    }

    if (user != null) {
      this.user = user;

      deviceId = this.user.getId(); // TODO: replace to generated GUID

      if (this.user.getIpAddress() != null) {
        ipAddress = this.user.getIpAddress();
      }
    }
    if (userAgent != null) {
      this.userAgent = userAgent;
    }
    if (addErrorsCount) {
      errorCount.addAndGet(1);
    }

    // sequence and duration?
  }
}
