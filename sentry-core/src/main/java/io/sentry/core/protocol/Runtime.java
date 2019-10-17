package io.sentry.core.protocol;

public class Runtime {
  static final String TYPE = "runtime";

  private String name;
  private String version;
  private String rawDescription;
  private String build; // this is not in the documentation
  // https://docs.sentry.io/development/sdk-dev/event-payloads/contexts/#runtime-context

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

  public String getRawDescription() {
    return rawDescription;
  }

  public void setRawDescription(String rawDescription) {
    this.rawDescription = rawDescription;
  }

  public String getBuild() {
    return build;
  }

  public void setBuild(String build) {
    this.build = build;
  }
}
