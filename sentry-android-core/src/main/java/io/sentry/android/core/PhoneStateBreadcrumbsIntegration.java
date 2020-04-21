package io.sentry.android.core;

import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import androidx.annotation.Nullable;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class PhoneStateBreadcrumbsIntegration implements Integration, Closeable {

  private final @NotNull Context context;
  private @Nullable SentryAndroidOptions options;
  private @Nullable PhoneStateChangeListener listener;
  private @Nullable TelephonyManager telephonyManager;

  public PhoneStateBreadcrumbsIntegration(final @NotNull Context context) {
    this.context = Objects.requireNonNull(context, "Context is required");
  }

  @Override
  public void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    Objects.requireNonNull(hub, "Hub is required");
    this.options =
        Objects.requireNonNull(
            (options instanceof SentryAndroidOptions) ? (SentryAndroidOptions) options : null,
            "SentryAndroidOptions is required");

    this.options
        .getLogger()
        .log(
            SentryLevel.DEBUG,
            "enableSystemEventsBreadcrumbs enabled: %s",
            this.options.isEnableSystemEventsBreadcrumbs());

    if (this.options.isEnableSystemEventsBreadcrumbs()) {
      telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (telephonyManager != null) {
        listener = new PhoneStateChangeListener(hub);
        telephonyManager.listen(listener, LISTEN_CALL_STATE);

        options.getLogger().log(SentryLevel.DEBUG, "PhoneStateBreadcrumbsIntegration installed.");
      } else {
        this.options.getLogger().log(SentryLevel.INFO, "TelephonyManager is not available");
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (telephonyManager != null && listener != null) {
      telephonyManager.listen(listener, LISTEN_CALL_STATE);
    }

    if (options != null) {
      options.getLogger().log(SentryLevel.DEBUG, "PhoneStateBreadcrumbsIntegration removed.");
    }
  }

  private static final class PhoneStateChangeListener extends PhoneStateListener {

    private final @NotNull IHub hub;

    PhoneStateChangeListener(final @NotNull IHub hub) {
      this.hub = hub;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      if (state == TelephonyManager.CALL_STATE_RINGING) {
        addBreadcrumb("CALL_STATE_RINGING");
      }
    }

    private void addBreadcrumb(final @NotNull String action) {
      final Breadcrumb breadcrumb = new Breadcrumb();
      breadcrumb.setType("info");
      breadcrumb.setCategory("app.broadcast");
      breadcrumb.setData("action", action);

      breadcrumb.setLevel(SentryLevel.DEBUG);
      hub.addBreadcrumb(breadcrumb);
    }
  }
}
