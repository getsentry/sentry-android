package io.sentry.android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class SystemEventsIntegration implements Integration, Closeable {

  private final @NotNull Context context;

  private SystemEventsBroadcastReceiver receiver;

  public SystemEventsIntegration(final @NotNull Context context) {
    this.context = context;
  }

  @Override
  public void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    receiver = new SystemEventsBroadcastReceiver(hub);
    final IntentFilter filter = new IntentFilter();
    for (String item : getActions()) {
      filter.addAction(item);
    }

    context.registerReceiver(receiver, filter);
  }

  private List<String> getActions() {
    final List<String> actions = new ArrayList<>();
    // TODO: need to filter it or it will be flooded
    actions.add("android.accounts.LOGIN_ACCOUNTS_CHANGED");
    actions.add("android.accounts.action.ACCOUNT_REMOVED");
    actions.add("android.app.action.ACTION_PASSWORD_CHANGED");
    actions.add("android.app.action.ACTION_PASSWORD_EXPIRING");
    actions.add("android.app.action.ACTION_PASSWORD_FAILED");
    actions.add("android.app.action.ACTION_PASSWORD_SUCCEEDED");
    actions.add("android.app.action.APPLICATION_DELEGATION_SCOPES_CHANGED");
    actions.add("android.app.action.APP_BLOCK_STATE_CHANGED");
    actions.add("android.app.action.DEVICE_ADMIN_DISABLED");
    actions.add("android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED");
    actions.add("android.app.action.DEVICE_ADMIN_ENABLED");
    actions.add("android.app.action.DEVICE_OWNER_CHANGED");
    //    actions.add("android.app.action.INTERRUPTION_FILTER_CHANGED");
    actions.add("android.app.action.LOCK_TASK_ENTERING");
    actions.add("android.app.action.LOCK_TASK_EXITING");
    actions.add("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
    actions.add("android.app.action.NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED");
    actions.add("android.app.action.NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED");
    actions.add("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED");
    actions.add("android.app.action.NOTIFICATION_POLICY_CHANGED");
    actions.add("android.app.action.PROFILE_OWNER_CHANGED");
    actions.add("android.app.action.PROFILE_PROVISIONING_COMPLETE");
    actions.add("android.app.action.SYSTEM_UPDATE_POLICY_CHANGED");
    actions.add("android.appwidget.action.APPWIDGET_DELETED");
    actions.add("android.appwidget.action.APPWIDGET_DISABLED");
    actions.add("android.appwidget.action.APPWIDGET_ENABLED");
    actions.add("android.appwidget.action.APPWIDGET_HOST_RESTORED");
    actions.add("android.appwidget.action.APPWIDGET_RESTORED");
    actions.add("android.appwidget.action.APPWIDGET_UPDATE");
    actions.add("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS");
    actions.add("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED");
    actions.add("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
    actions.add("android.bluetooth.adapter.action.DISCOVERY_STARTED");
    actions.add("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
    actions.add("android.bluetooth.adapter.action.SCAN_MODE_CHANGED");
    actions.add("android.bluetooth.adapter.action.STATE_CHANGED");
    actions.add("android.bluetooth.device.action.ACL_CONNECTED");
    actions.add("android.bluetooth.device.action.ACL_DISCONNECTED");
    actions.add("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
    actions.add("android.bluetooth.device.action.BOND_STATE_CHANGED");
    actions.add("android.bluetooth.device.action.CLASS_CHANGED");
    actions.add("android.bluetooth.device.action.FOUND");
    actions.add("android.bluetooth.device.action.NAME_CHANGED");
    actions.add("android.bluetooth.device.action.PAIRING_REQUEST");
    actions.add("android.bluetooth.device.action.UUID");
    actions.add("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
    actions.add("android.bluetooth.devicepicker.action.LAUNCH");
    actions.add("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT");
    actions.add("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
    actions.add("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.hearingaid.profile.action.ACTIVE_DEVICE_CHANGED");
    actions.add("android.bluetooth.hearingaid.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.hearingaid.profile.action.PLAYING_STATE_CHANGED");
    actions.add("android.bluetooth.hiddevice.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED");
    actions.add("android.content.pm.action.SESSION_COMMITTED");
    actions.add("android.hardware.action.NEW_PICTURE");
    actions.add("android.hardware.action.NEW_VIDEO");
    actions.add("android.hardware.hdmi.action.OSD_MESSAGE");
    actions.add("android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS");
    actions.add("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
    actions.add("android.hardware.usb.action.USB_ACCESSORY_DETACHED");
    actions.add("android.hardware.usb.action.USB_DEVICE_ATTACHED");
    actions.add("android.hardware.usb.action.USB_DEVICE_DETACHED");
    actions.add("android.intent.action.ACTION_POWER_CONNECTED");
    actions.add("android.intent.action.ACTION_POWER_DISCONNECTED");
    actions.add("android.intent.action.ACTION_SHUTDOWN");
    actions.add("android.intent.action.AIRPLANE_MODE");
    actions.add("android.intent.action.APPLICATION_RESTRICTIONS_CHANGED");
    actions.add(
        "android.intent.action.BATTERY_CHANGED"); // useful but it comes quite a lot, maybe only
    // BATTERY_LOW would be ok
    actions.add("android.intent.action.BATTERY_LOW");
    actions.add("android.intent.action.BATTERY_OKAY");
    actions.add("android.intent.action.BOOT_COMPLETED");
    actions.add("android.intent.action.CAMERA_BUTTON");
    //    actions.add("android.intent.action.CLOSE_SYSTEM_DIALOGS");
    //    actions.add("android.intent.action.CONFIGURATION_CHANGED"); we can do this on
    // AppComponentsBreadcrumbsIntegration
    actions.add("android.intent.action.CONTENT_CHANGED");
    actions.add("android.intent.action.DATA_SMS_RECEIVED");
    actions.add("android.intent.action.DATE_CHANGED");
    actions.add("android.intent.action.DEVICE_STORAGE_LOW");
    actions.add("android.intent.action.DEVICE_STORAGE_OK");
    actions.add("android.intent.action.DOCK_EVENT");
    actions.add("android.intent.action.DOWNLOAD_COMPLETE");
    actions.add("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
    actions.add("android.intent.action.DREAMING_STARTED");
    actions.add("android.intent.action.DREAMING_STOPPED");
    actions.add("android.intent.action.DROPBOX_ENTRY_ADDED");
    actions.add("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
    actions.add("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
    actions.add("android.intent.action.FACTORY_RESET");
    actions.add("android.intent.action.FETCH_VOICEMAIL");
    actions.add("android.intent.action.GTALK_CONNECTED");
    actions.add("android.intent.action.GTALK_DISCONNECTED");
    actions.add("android.intent.action.HEADSET_PLUG");
    actions.add("android.intent.action.HEADSET_PLUG");
    actions.add("android.intent.action.INPUT_METHOD_CHANGED");
    actions.add("android.intent.action.INTENT_FILTER_NEEDS_VERIFICATION");
    actions.add("android.intent.action.LOCALE_CHANGED");
    actions.add("android.intent.action.LOCKED_BOOT_COMPLETED");
    actions.add("android.intent.action.MANAGE_PACKAGE_STORAGE");
    actions.add("android.intent.action.MASTER_CLEAR_NOTIFICATION");
    actions.add("android.intent.action.MEDIA_BAD_REMOVAL");
    actions.add("android.intent.action.MEDIA_BUTTON");
    actions.add("android.intent.action.MEDIA_CHECKING");
    actions.add("android.intent.action.MEDIA_EJECT");
    actions.add("android.intent.action.MEDIA_MOUNTED");
    actions.add("android.intent.action.MEDIA_NOFS");
    actions.add("android.intent.action.MEDIA_REMOVED");
    actions.add("android.intent.action.MEDIA_SCANNER_FINISHED");
    actions.add("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
    actions.add("android.intent.action.MEDIA_SCANNER_STARTED");
    actions.add("android.intent.action.MEDIA_SHARED");
    actions.add("android.intent.action.MEDIA_UNMOUNTABLE");
    actions.add("android.intent.action.MEDIA_UNMOUNTED");
    actions.add("android.intent.action.MY_PACKAGE_REPLACED");
    actions.add("android.intent.action.MY_PACKAGE_SUSPENDED");
    actions.add("android.intent.action.MY_PACKAGE_UNSUSPENDED");
    actions.add("android.intent.action.NEW_OUTGOING_CALL");
    actions.add("android.intent.action.NEW_VOICEMAIL");
    actions.add("android.intent.action.PACKAGES_SUSPENDED");
    actions.add("android.intent.action.PACKAGES_UNSUSPENDED");
    actions.add("android.intent.action.PACKAGE_ADDED");
    actions.add("android.intent.action.PACKAGE_CHANGED");
    actions.add("android.intent.action.PACKAGE_DATA_CLEARED");
    actions.add("android.intent.action.PACKAGE_FIRST_LAUNCH");
    actions.add("android.intent.action.PACKAGE_FULLY_REMOVED");
    actions.add("android.intent.action.PACKAGE_INSTALL");
    actions.add("android.intent.action.PACKAGE_NEEDS_VERIFICATION");
    actions.add("android.intent.action.PACKAGE_REMOVED");
    actions.add("android.intent.action.PACKAGE_REPLACED");
    actions.add("android.intent.action.PACKAGE_RESTARTED");
    actions.add("android.intent.action.PACKAGE_VERIFIED");
    actions.add("android.intent.action.PHONE_STATE");
    actions.add("android.intent.action.PROVIDER_CHANGED");
    actions.add("android.intent.action.PROXY_CHANGE");
    actions.add("android.intent.action.QUERY_PACKAGE_RESTART");
    actions.add("android.intent.action.REBOOT");
    actions.add("android.intent.action.SCREEN_OFF");
    actions.add("android.intent.action.SCREEN_ON");
    //    actions.add("android.intent.action.SIM_STATE_CHANGED");
    actions.add("android.intent.action.SPLIT_CONFIGURATION_CHANGED");
    actions.add("android.intent.action.TIMEZONE_CHANGED");
    actions.add("android.intent.action.TIME_SET");
    //    actions.add("android.intent.action.TIME_TICK");
    actions.add("android.intent.action.UID_REMOVED");
    actions.add("android.intent.action.UMS_CONNECTED");
    actions.add("android.intent.action.UMS_DISCONNECTED");
    actions.add("android.intent.action.USER_PRESENT");
    actions.add("android.intent.action.USER_UNLOCKED");
    actions.add("android.intent.action.WALLPAPER_CHANGED");
    //    actions.add("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
    actions.add("android.media.AUDIO_BECOMING_NOISY");
    //    actions.add("android.media.RINGER_MODE_CHANGED");
    //    actions.add("android.media.SCO_AUDIO_STATE_CHANGED");
    actions.add("android.media.VIBRATE_SETTING_CHANGED");
    actions.add("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
    actions.add("android.media.action.HDMI_AUDIO_PLUG");
    actions.add("android.media.action.MICROPHONE_MUTE_CHANGED");
    actions.add("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
    actions.add("android.media.tv.action.CHANNEL_BROWSABLE_REQUESTED");
    actions.add("android.media.tv.action.INITIALIZE_PROGRAMS");
    actions.add("android.media.tv.action.PREVIEW_PROGRAM_ADDED_TO_WATCH_NEXT");
    actions.add("android.media.tv.action.PREVIEW_PROGRAM_BROWSABLE_DISABLED");
    actions.add("android.media.tv.action.WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED");
    actions.add("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED");
    actions.add("android.net.conn.CONNECTIVITY_CHANGE");
    actions.add("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
    //    actions.add("android.net.nsd.STATE_CHANGED");
    actions.add("android.net.scoring.SCORER_CHANGED");
    actions.add("android.net.scoring.SCORE_NETWORKS");
    //    actions.add("android.net.wifi.NETWORK_IDS_CHANGED");
    //    actions.add("android.net.wifi.RSSI_CHANGED");
    //    actions.add("android.net.wifi.SCAN_RESULTS");
    //    actions.add("android.net.wifi.STATE_CHANGE");
    //    actions.add("android.net.wifi.WIFI_STATE_CHANGED");
    //    actions.add("android.net.wifi.aware.action.WIFI_AWARE_STATE_CHANGED");
    //    actions.add("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
    //    actions.add("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
    //    actions.add("android.net.wifi.p2p.PEERS_CHANGED");
    //    actions.add("android.net.wifi.p2p.STATE_CHANGED");
    //    actions.add("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
    //    actions.add("android.net.wifi.rtt.action.WIFI_RTT_STATE_CHANGED");
    //    actions.add("android.net.wifi.supplicant.CONNECTION_CHANGE");
    //    actions.add("android.net.wifi.supplicant.STATE_CHANGE");
    actions.add("android.nfc.action.ADAPTER_STATE_CHANGED");
    actions.add("android.nfc.action.TRANSACTION_DETECTED");
    actions.add("android.os.action.DEVICE_IDLE_MODE_CHANGED");
    actions.add("android.os.action.POWER_SAVE_MODE_CHANGED");
    actions.add("android.provider.Telephony.SECRET_CODE");
    actions.add("android.provider.Telephony.SIM_FULL");
    actions.add("android.provider.Telephony.SMS_CB_RECEIVED");
    actions.add("android.provider.Telephony.SMS_DELIVER");
    actions.add("android.provider.Telephony.SMS_RECEIVED");
    actions.add("android.provider.Telephony.SMS_REJECTED");
    actions.add("android.provider.Telephony.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED");
    actions.add("android.provider.Telephony.WAP_PUSH_DELIVER");
    actions.add("android.provider.Telephony.WAP_PUSH_RECEIVED");
    actions.add("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED");
    actions.add("android.provider.action.EXTERNAL_PROVIDER_CHANGE");
    actions.add("android.provider.action.SYNC_VOICEMAIL");
    actions.add("android.security.STORAGE_CHANGED");
    actions.add("android.security.action.KEYCHAIN_CHANGED");
    actions.add("android.security.action.KEY_ACCESS_CHANGED");
    actions.add("android.security.action.TRUST_STORE_CHANGED");
    actions.add("android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED");
    actions.add("android.speech.tts.engine.TTS_DATA_INSTALLED");
    //    actions.add("android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED");
    //    actions.add("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED");
    actions.add("android.telephony.action.REFRESH_SUBSCRIPTION_PLANS");
    actions.add("android.telephony.action.SIM_APPLICATION_STATE_CHANGED");
    actions.add("android.telephony.action.SIM_CARD_STATE_CHANGED");
    actions.add("android.telephony.action.SIM_SLOT_STATUS_CHANGED");
    actions.add("android.telephony.action.SUBSCRIPTION_CARRIER_IDENTITY_CHANGED");
    actions.add("android.telephony.euicc.action.NOTIFY_CARRIER_SETUP_INCOMPLETE");
    actions.add("android.telephony.euicc.action.OTA_STATUS_CHANGED");
    return actions;
  }

  @Override
  public void close() throws IOException {
    if (receiver != null) {
      context.unregisterReceiver(receiver);
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
        breadcrumb.setData("action", intent.getAction()); // we can short it
      }
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
