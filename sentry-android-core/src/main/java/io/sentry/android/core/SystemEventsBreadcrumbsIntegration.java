package io.sentry.android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class SystemEventsBreadcrumbsIntegration implements Integration, Closeable {

  private final @NotNull Context context;

  private SystemEventsBroadcastReceiver receiver;

  private @Nullable SentryAndroidOptions options;

  public SystemEventsBreadcrumbsIntegration(final @NotNull Context context) {
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
            "SystemEventsBreadcrumbsIntegration enabled: %s",
            this.options.isEnableSystemEventsBreadcrumbs());

    if (this.options.isEnableSystemEventsBreadcrumbs()) {
      receiver = new SystemEventsBroadcastReceiver(hub);
      final IntentFilter filter = new IntentFilter();
      for (String item : getActions()) {
        filter.addAction(item);
      }
      context.registerReceiver(receiver, filter);
      options.getLogger().log(SentryLevel.DEBUG, "SystemEventsBreadcrumbsIntegration installed.");
    }
  }

  private List<String> getActions() {
    final List<String> actions = new ArrayList<>();
    actions.add("android.appwidget.action.APPWIDGET_DELETED");
    actions.add("android.appwidget.action.APPWIDGET_DISABLED");
    actions.add("android.appwidget.action.APPWIDGET_ENABLED");
    actions.add("android.appwidget.action.APPWIDGET_HOST_RESTORED");
    actions.add("android.appwidget.action.APPWIDGET_RESTORED");
    actions.add("android.appwidget.action.APPWIDGET_UPDATE");
    actions.add("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS");
    actions.add("android.intent.action.ACTION_POWER_CONNECTED");
    actions.add("android.intent.action.ACTION_POWER_DISCONNECTED");
    actions.add("android.intent.action.ACTION_SHUTDOWN");
    actions.add("android.intent.action.AIRPLANE_MODE");
    actions.add("android.intent.action.BATTERY_LOW");
    actions.add("android.intent.action.BATTERY_OKAY");
    actions.add("android.intent.action.BOOT_COMPLETED");
    actions.add("android.intent.action.CAMERA_BUTTON");
    actions.add("android.intent.action.CLOSE_SYSTEM_DIALOGS");
    actions.add("android.intent.action.CONFIGURATION_CHANGED");
    actions.add("android.intent.action.CONTENT_CHANGED");
    actions.add("android.intent.action.DATE_CHANGED");
    actions.add("android.intent.action.DEVICE_STORAGE_LOW");
    actions.add("android.intent.action.DEVICE_STORAGE_OK");
    actions.add("android.intent.action.DOCK_EVENT");
    actions.add("android.intent.action.DREAMING_STARTED");
    actions.add("android.intent.action.DREAMING_STOPPED");
    actions.add("android.intent.action.INPUT_METHOD_CHANGED");
    actions.add("android.intent.action.LOCALE_CHANGED");
    actions.add("android.intent.action.REBOOT");
    actions.add("android.intent.action.SCREEN_OFF");
    actions.add("android.intent.action.SCREEN_ON");
    actions.add("android.intent.action.TIMEZONE_CHANGED");
    actions.add("android.intent.action.TIME_SET");
    actions.add("android.os.action.DEVICE_IDLE_MODE_CHANGED");
    actions.add("android.os.action.POWER_SAVE_MODE_CHANGED");

    // might require android.permission.READ_PHONE_STATE/android.permission.PROCESS_OUTGOING_CALLS
    // permissions
    actions.add("android.intent.action.NEW_OUTGOING_CALL"); // also consider
    // android.intent.action.PHONE_STATE
    return actions;
  }

  @Override
  public void close() throws IOException {
    if (receiver != null) {
      context.unregisterReceiver(receiver);

      if (options != null) {
        options.getLogger().log(SentryLevel.DEBUG, "SystemEventsBreadcrumbsIntegration remove.");
      }
    }
  }

  private static final class SystemEventsBroadcastReceiver extends BroadcastReceiver {

    private final @NotNull IHub hub;

    SystemEventsBroadcastReceiver(final @NotNull IHub hub) {
      this.hub = hub;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      final Breadcrumb breadcrumb = new Breadcrumb();
      breadcrumb.setType("info");
      breadcrumb.setCategory("app.broadcast");
      if (intent.getAction() != null) {
        final int lastDotIndex = intent.getAction().lastIndexOf(".");
        breadcrumb.setData("action", intent.getAction().substring(lastDotIndex + 1));
      }

      //      TODO: should we log the params?
      final Bundle extras = intent.getExtras();
      final Map<String, String> newExtras = new HashMap<>();
      if (extras != null && !extras.isEmpty()) {
        for (String item : extras.keySet()) {
          try {
            newExtras.put(item, extras.get(item).toString());
          } catch (Exception ignored) {
          }
        }
      }
      breadcrumb.setData("extras", newExtras);

      breadcrumb.setLevel(SentryLevel.DEBUG);
      hub.addBreadcrumb(breadcrumb);
    }
  }
}
