package io.sentry.core;

import com.jakewharton.nopen.annotation.Open;
import java.util.ArrayList;
import java.util.List;

/** Subset of {@link SentryOptions} properties used in 3rd party framework integrations. */
@Open
public class SentryCommonOptions {

  /** @see SentryOptions#dsn */
  private String dsn = "";

  /** @see SentryOptions#shutdownTimeoutMillis */
  private Long shutdownTimeoutMillis;

  /** @see SentryOptions#flushTimeoutMillis */
  private Long flushTimeoutMillis;

  /** @see SentryOptions#readTimeoutMillis */
  private Integer readTimeoutMillis;

  /** @see SentryOptions#bypassSecurity */
  private Boolean bypassSecurity;

  /** @see SentryOptions#debug */
  private Boolean debug;

  /** @see SentryOptions#diagnosticLevel */
  private SentryLevel diagnosticLevel = SentryLevel.DEBUG;

  /** @see SentryOptions#maxBreadcrumbs */
  private Integer maxBreadcrumbs;

  /** @see SentryOptions#release */
  private String release;

  /** @see SentryOptions#environment */
  private String environment;

  /** @see SentryOptions#sampleRate */
  private Double sampleRate;

  /** @see SentryOptions#inAppExcludes */
  private List<String> inAppExcludes = new ArrayList<>();

  /** @see SentryOptions#inAppIncludes */
  private List<String> inAppIncludes = new ArrayList<>();

  /** @see SentryOptions#dist */
  private String dist;

  /** @see SentryOptions#attachThreads */
  private Boolean attachThreads;

  /** @see SentryOptions#attachStacktrace */
  private Boolean attachStacktrace;

  /** @see SentryOptions#serverName */
  private String serverName;

  /**
   * Applies configuration from this instance to the {@link SentryOptions} instance.
   *
   * @param options the instance of {@link SentryOptions} to apply the configuration to
   */
  public void applyTo(SentryOptions options) {
    if (dsn != null) {
      options.setDsn(dsn);
    }
    if (maxBreadcrumbs != null) {
      options.setMaxBreadcrumbs(maxBreadcrumbs);
    }
    if (environment != null) {
      options.setEnvironment(environment);
    }
    if (shutdownTimeoutMillis != null) {
      options.setShutdownTimeout(shutdownTimeoutMillis);
    }
    if (flushTimeoutMillis != null) {
      options.setFlushTimeoutMillis(flushTimeoutMillis);
    }
    if (readTimeoutMillis != null) {
      options.setReadTimeoutMillis(readTimeoutMillis);
    }
    if (sampleRate != null) {
      options.setSampleRate(sampleRate);
    }
    if (bypassSecurity != null) {
      options.setBypassSecurity(bypassSecurity);
    }
    if (debug != null) {
      options.setDebug(debug);
    }
    if (attachThreads != null) {
      options.setAttachThreads(attachThreads);
    }
    if (attachStacktrace != null) {
      options.setAttachStacktrace(attachStacktrace);
    }
    if (diagnosticLevel != null) {
      options.setDiagnosticLevel(diagnosticLevel);
    }
    if (dist != null) {
      options.setDist(dist);
    }
    if (release != null) {
      options.setRelease(release);
    }
    if (sampleRate != null) {
      options.setSampleRate(sampleRate);
    }
    if (serverName != null) {
      options.setServerName(serverName);
    }
    if (inAppExcludes != null) {
      for (String inAppExclude : inAppExcludes) {
        options.addInAppExclude(inAppExclude);
      }
    }
    if (inAppIncludes != null) {
      for (String inAppInclude : inAppIncludes) {
        options.addInAppInclude(inAppInclude);
      }
    }
  }

  public String getDsn() {
    return dsn;
  }

  public void setDsn(String dsn) {
    this.dsn = dsn;
  }

  public Long getShutdownTimeoutMillis() {
    return shutdownTimeoutMillis;
  }

  public void setShutdownTimeoutMillis(long shutdownTimeoutMillis) {
    this.shutdownTimeoutMillis = shutdownTimeoutMillis;
  }

  public Boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public SentryLevel getDiagnosticLevel() {
    return diagnosticLevel;
  }

  public void setDiagnosticLevel(SentryLevel diagnosticLevel) {
    this.diagnosticLevel = diagnosticLevel;
  }

  public Integer getMaxBreadcrumbs() {
    return maxBreadcrumbs;
  }

  public void setMaxBreadcrumbs(int maxBreadcrumbs) {
    this.maxBreadcrumbs = maxBreadcrumbs;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public Double getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(Double sampleRate) {
    this.sampleRate = sampleRate;
  }

  public List<String> getInAppExcludes() {
    return inAppExcludes;
  }

  public void setInAppExcludes(List<String> inAppExcludes) {
    this.inAppExcludes = inAppExcludes;
  }

  public List<String> getInAppIncludes() {
    return inAppIncludes;
  }

  public void setInAppIncludes(List<String> inAppIncludes) {
    this.inAppIncludes = inAppIncludes;
  }

  public String getDist() {
    return dist;
  }

  public void setDist(String dist) {
    this.dist = dist;
  }

  public Boolean isAttachThreads() {
    return attachThreads;
  }

  public void setAttachThreads(boolean attachThreads) {
    this.attachThreads = attachThreads;
  }

  public Boolean isAttachStacktrace() {
    return attachStacktrace;
  }

  public void setAttachStacktrace(boolean attachStacktrace) {
    this.attachStacktrace = attachStacktrace;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public void setShutdownTimeoutMillis(Long shutdownTimeoutMillis) {
    this.shutdownTimeoutMillis = shutdownTimeoutMillis;
  }

  public Long getFlushTimeoutMillis() {
    return flushTimeoutMillis;
  }

  public void setFlushTimeoutMillis(Long flushTimeoutMillis) {
    this.flushTimeoutMillis = flushTimeoutMillis;
  }

  public Integer getReadTimeoutMillis() {
    return readTimeoutMillis;
  }

  public void setReadTimeoutMillis(Integer readTimeoutMillis) {
    this.readTimeoutMillis = readTimeoutMillis;
  }

  public Boolean getBypassSecurity() {
    return bypassSecurity;
  }

  public void setBypassSecurity(Boolean bypassSecurity) {
    this.bypassSecurity = bypassSecurity;
  }

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public void setMaxBreadcrumbs(Integer maxBreadcrumbs) {
    this.maxBreadcrumbs = maxBreadcrumbs;
  }

  public Boolean getAttachThreads() {
    return attachThreads;
  }

  public void setAttachThreads(Boolean attachThreads) {
    this.attachThreads = attachThreads;
  }

  public Boolean getAttachStacktrace() {
    return attachStacktrace;
  }

  public void setAttachStacktrace(Boolean attachStacktrace) {
    this.attachStacktrace = attachStacktrace;
  }
}
