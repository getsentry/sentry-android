package io.sentry.samples.console;

import io.sentry.core.Breadcrumb;
import io.sentry.core.EventProcessor;
import io.sentry.core.Sentry;
import io.sentry.core.SentryEvent;
import io.sentry.core.SentryLevel;
import io.sentry.core.protocol.Message;
import io.sentry.core.protocol.User;
import java.util.Collections;

public class Main {

  public static void main(String[] args) {
    Sentry.init(
        options -> {
          // NOTE: Replace the test DSN below with YOUR OWN DSN to see the events from this app in
          // your Sentry project/dashboard
          options.setDsn("https://f7f320d5c3a54709be7b28e0f2ca7081@sentry.io/1808954");

          // Modifications to event before it goes out. Could replace the event altogether
          options.setBeforeSend(
              (event, hint) -> {
                // Drop an event altogether:
                if (event.getTag("SomeTag") != null) {
                  return null;
                }
                return event;
              });

          // Allows inspecting and modifying, returning a new or simply rejecting (returning null)
          options.setBeforeBreadcrumb(
              (breadcrumb, hint) -> {
                // Don't add breadcrumbs with message containing:
                if (breadcrumb.getMessage() != null
                    && breadcrumb.getMessage().contains("bad breadcrumb")) {
                  return null;
                }
                return breadcrumb;
              });

          // Configure the background worker which sends events to sentry:
          // Wait up to 5 seconds before shutdown while there are events to send.
          options.setShutdownTimeout(5000);

          // Enable SDK logging with Debug level
          options.setDebug(true);
          // To change the verbosity, use:
          options.setDiagnosticLevel(SentryLevel.LOG);

          // Using a proxy:
          options.setProxy(null); // new Proxy(Type.HTTP, new InetSocketAddress(8090)));

          // Exclude classes from certain packages from stack traces sent to Sentry:
          options.addInAppExclude("org.jboss");
        });

    Sentry.addBreadcrumb(
        "A 'bad breadcrumb' that will be rejected because of 'BeforeBreadcrumb callback above.'");

    // Data added to the root scope (no PushScope called up to this point)
    // The modifications done here will affect all events sent and will propagate to child scopes.
    Sentry.configureScope(
        scope -> {
          scope.addEventProcessor(new SomeEventProcessor());

          scope.setExtra("SomeExtraInfo", "Some value for extra info");
        });

    // Configures a scope which is only valid within the callback
    Sentry.withScope(
        scope -> {
          scope.setLevel(SentryLevel.FATAL);
          scope.setTransaction("main");

          Sentry.captureMessage("Fatal message!");
        });

    Sentry.captureMessage("Some warning!", SentryLevel.WARNING);

    Exception exception = new RuntimeException("Attempting to send this multiple times");

    // Only the first capture will be sent to Sentry
    for (int i = 0; i < 3; i++) {
      // The SDK is able to detect duplicate events:
      // This is useful, for example, when multiple loggers log the same exception. Or exception is
      // re-thrown and recaptured.
      Sentry.captureException(exception);
    }

    int count = 10;
    for (int i = 0; i < count; i++) {
      String msg = "%d of %d items we'll wait to flush to Sentry!";
      Message message = new Message();
      message.setMessage(msg);
      message.setFormatted(String.format(msg, i, count));
      SentryEvent event = new SentryEvent();
      event.setMessage(message);
      Sentry.captureEvent(event, SentryLevel.DEBUG);
    }

    // Console output will show queue being flushed. Task completes then and timeout is never
    // reached (you don't need to wait a day :)
    Sentry.flush(10000);

    // An event with breadcrumb and user data
    SentryEvent evt = new SentryEvent();
    Message msg = new Message();
    msg.setMessage("Detailed event");
    evt.setMessage(msg);
    evt.addBreadcrumb(new Breadcrumb("Breadcrumb directly to the event"));
    User user = new User();
    user.setUsername("some@user");
    evt.setUser(user);
    // Group all events with the following fingerprint:
    evt.setFingerprints(Collections.singletonList("NewClientDebug"));
    evt.setLevel(SentryLevel.DEBUG);
    Sentry.captureEvent(evt);
  }

  private static class SomeEventProcessor implements EventProcessor {
    @Override
    public SentryEvent process(SentryEvent event, Object hint) {
      // Here you can modify the event as you need
      if (event.getLevel() != null && event.getLevel().ordinal() > SentryLevel.INFO.ordinal()) {
        event.addBreadcrumb(new Breadcrumb("Processed by " + SomeEventProcessor.class));
      }

      return event;
    }
  }
}
