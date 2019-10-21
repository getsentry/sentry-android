package io.sentry.core.protocol;

import io.sentry.core.IUnknownPropertiesConsumer;
import java.util.Date;
import java.util.Map;

public class App implements IUnknownPropertiesConsumer {
  static final String TYPE = "app";

  private String identifier;
  private Date startTime;
  private String hash;
  private String buildType;
  private String name;
  private String version;
  private String build;
  private Map<String, Object> unknown;

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

  @Override
  public void acceptUnknownProperties(Map<String, Object> unknown) {
    this.unknown = unknown;
  }
}
