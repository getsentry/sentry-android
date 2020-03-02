package io.sentry.okhttp3;

import io.sentry.core.Sentry;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/** Sentry Interceptor for OkHttp v3 */
public final class SentryOkHttpInterceptor implements Interceptor {

  @SuppressWarnings("DefaultLocale")
  @Override
  public Response intercept(@NotNull Chain chain) throws IOException {
    final Request request = chain.request();

    final long t1 = System.nanoTime();
    Sentry.addBreadcrumb(
        String.format(
            "Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));

    Response response = chain.proceed(request);

    final long t2 = System.nanoTime();
    Sentry.addBreadcrumb(
        String.format(
            "Received response for %s in %.1fms%n%s",
            response.request().url(), (t2 - t1) / 1e6d, response.headers()));

    return response;
  }
}
