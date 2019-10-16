package io.sentry.core.protocol;

import java.util.Date;

public class App {
  static final String TYPE = "app";

  private String identifier; // it was app_identifier
  private Date startTime; // it was app_start_time
  private String hash;
  private String buildType;
  private String name; // it was app_name
  private String version; // it was app_version
  private String build; // it was app_build

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getBuildType() {
    return buildType;
  }

  public void setBuildType(String buildType) {
    this.buildType = buildType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getBuild() {
    return build;
  }

  public void setBuild(String build) {
    this.build = build;
  }
}
