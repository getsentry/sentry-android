package io.sentry.android.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import io.sentry.core.HubAdapter;
import io.sentry.core.hints.DiskFlushNotification;
import io.sentry.core.hints.Flushable;
import io.sentry.core.hints.SessionEnd;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * This is a best effort to end a session during onTaskRemoved (user swiped up the App. aka killed
 * it. It needs to be public.
 */
@ApiStatus.Internal
public final class SentryService extends Service {

  @Override
  public @Nullable IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    // START_NOT_STICKY, we don't want to recreate the service nor the AppContext.
    return START_NOT_STICKY;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    // TODO: get logger and flush timeout from options thru onStartCommand.intent
    final TaskRemovedHint hint = new TaskRemovedHint(15000);
    HubAdapter.getInstance().endSession(hint);

    hint.waitFlush();

    super.onTaskRemoved(rootIntent);
  }

  @NonNull
  public static Intent getIntent(Context context) {
    return new Intent(context, SentryService.class);
  }

  private static final class TaskRemovedHint
      implements DiskFlushNotification, Flushable, SessionEnd {

    private final CountDownLatch latch;
    private final long flushTimeoutMillis;

    TaskRemovedHint(final long flushTimeoutMillis) {
      this.flushTimeoutMillis = flushTimeoutMillis;
      latch = new CountDownLatch(1);
    }

    @Override
    public boolean waitFlush() {
      try {
        return latch.await(flushTimeoutMillis, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return false;
    }

    @Override
    public void markFlushed() {
      latch.countDown();
    }
  }
}
