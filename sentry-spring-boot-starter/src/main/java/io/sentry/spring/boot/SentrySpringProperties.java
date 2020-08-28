package io.sentry.spring.boot;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.SentryCommonOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for Sentry integration. */
@ConfigurationProperties(prefix = "sentry")
@Open
public class SentrySpringProperties extends SentryCommonOptions {
  /** Whether Sentry integration should be enabled. */
  private boolean enabled = true;

  /** Weather to use Git commit id as a release. */
  private boolean useGitCommitIdAsRelease = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isUseGitCommitIdAsRelease() {
    return useGitCommitIdAsRelease;
  }

  public void setUseGitCommitIdAsRelease(boolean useGitCommitIdAsRelease) {
    this.useGitCommitIdAsRelease = useGitCommitIdAsRelease;
  }
}
