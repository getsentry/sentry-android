package io.sentry.android.core;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.sentry.core.Breadcrumb;
import io.sentry.core.IHub;
import io.sentry.core.Integration;
import io.sentry.core.SentryLevel;
import io.sentry.core.SentryOptions;
import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class AppComponentsBreadcrumbsIntegration implements Integration, Closeable {

  private final @NotNull Context context;
  private @Nullable IHub hub;

  public AppComponentsBreadcrumbsIntegration(final @NotNull Context context) {
    this.context = context;
  }

  @Override
  public void register(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    this.hub = hub;
    context.registerComponentCallbacks(callbacks);
  }

  @Override
  public void close() throws IOException {
    context.unregisterComponentCallbacks(callbacks);
  }

  private final ComponentCallbacks callbacks =
      new ComponentCallbacks() {
        //  private final ComponentCallbacks2 callbacks2 = new ComponentCallbacks2() {
        //    @Override
        //    public void onTrimMemory(int level) {
        //
        //    }

        @SuppressWarnings("deprecation")
        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
          // TODO: reuse from DefaultAndroidEventProcessor
          String position;
          switch (context.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
              position = "land";
              break;
            case Configuration.ORIENTATION_PORTRAIT:
              position = "port";
              break;
            case Configuration.ORIENTATION_SQUARE:
            case Configuration.ORIENTATION_UNDEFINED:
            default:
              position = "undefined";
              break;
          }

          final Breadcrumb breadcrumb = new Breadcrumb();
          breadcrumb.setType("navigation");
          breadcrumb.setCategory("ui.orientation");
          breadcrumb.setData("position", position);
          breadcrumb.setLevel(SentryLevel.DEBUG);
          hub.addBreadcrumb(breadcrumb);
        }

        @Override
        public void onLowMemory() {
          final Breadcrumb breadcrumb = new Breadcrumb();
          breadcrumb.setType("info");
          breadcrumb.setCategory("app.memory");
          breadcrumb.setLevel(SentryLevel.WARNING);
          hub.addBreadcrumb(breadcrumb);
        }
      };
}
