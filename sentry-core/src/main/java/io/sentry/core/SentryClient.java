package io.sentry.core;

import io.sentry.core.cache.DiskCache;
import io.sentry.core.cache.IEventCache;
import io.sentry.core.cache.ISessionCache;
import io.sentry.core.cache.SessionCache;
import io.sentry.core.hints.Cached;
import io.sentry.core.protocol.SentryId;
import io.sentry.core.transport.Connection;
import io.sentry.core.transport.ITransport;
import io.sentry.core.transport.ITransportGate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SentryClient implements ISentryClient {
  static final String SENTRY_PROTOCOL_VERSION = "7";

  private boolean enabled;

  private final SentryOptions options;
  private final Connection connection;
  private final Random random;

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  SentryClient(SentryOptions options) {
    this(options, null);
  }

  public SentryClient(SentryOptions options, @Nullable Connection connection) {
    this.options = options;
    this.enabled = true;

    ITransport transport = options.getTransport();
    if (transport == null) {
      transport = HttpTransportFactory.create(options);
      options.setTransport(transport);
    }

    ITransportGate transportGate = options.getTransportGate();
    if (transportGate == null) {
      transportGate = () -> true;
      options.setTransportGate(transportGate);
    }

    if (connection == null) {
      // TODO this is obviously provisional and should be constructed based on the config in options
      IEventCache cache = new DiskCache(options);
      ISessionCache sessionCache = new SessionCache(options, new EnvelopeReader());

      connection = AsyncConnectionFactory.create(options, cache, sessionCache);
    }
    this.connection = connection;
    random = options.getSampleRate() == null ? null : new Random();
  }

  @Override
  public SentryId captureEvent(SentryEvent event, @Nullable Scope scope, @Nullable Object hint) {
    if (!sample()) {
      options
          .getLogger()
          .log(
              SentryLevel.DEBUG,
              "Event %s was dropped due to sampling decision.",
              event.getEventId());
      return SentryId.EMPTY_ID;
    }

    options.getLogger().log(SentryLevel.DEBUG, "Capturing event: %s", event.getEventId());

    if (!(hint instanceof Cached)) {
      // Event has already passed through here before it was cached
      // Going through again could be reading data that is no longer relevant
      // i.e proguard id, app version, threads
      event = applyScope(event, scope, hint);

      if (event == null) {
        return SentryId.EMPTY_ID;
      }
    } else {
      options
          .getLogger()
          .log(SentryLevel.DEBUG, "Event was cached so not applying scope: %s", event.getEventId());
    }

    for (EventProcessor processor : options.getEventProcessors()) {
      event = processor.process(event, hint);

      if (event == null) {
        options
            .getLogger()
            .log(
                SentryLevel.DEBUG,
                "Event was dropped by processor: %s",
                processor.getClass().getName());
        break;
      }
    }

    if (event == null) {
      return SentryId.EMPTY_ID;
    }

    event = executeBeforeSend(event, hint);

    if (event == null) {
      options.getLogger().log(SentryLevel.DEBUG, "Event was dropped by beforeSend");
      return SentryId.EMPTY_ID;
    }

    // TODO: there's already this check above (if its cached), but it's before event processors and
    // we'd need to refactor
    // that as well, let's keep like this for now
    if (!(hint instanceof Cached)) {
      // safe guard
      if (options.isEnableSessionTracking()) {
        final SentryEvent finalEvent = event;
        if (scope != null) {
          scope.withSession(
              session -> {
                if (session != null) {
                  Session.State status = null;
                  if (finalEvent.isCrashed()) {
                    status = Session.State.Crashed;
                  }

                  boolean crashedOrErrored = false;
                  if (Session.State.Crashed == session.getStatus() || finalEvent.isErrored()) {
                    crashedOrErrored = true;
                  }

                  String userAgent = null;
                  if (finalEvent.getRequest() != null
                      && finalEvent.getRequest().getHeaders() != null) {
                    if (finalEvent.getRequest().getHeaders().containsKey("user-agent")) {
                      userAgent = finalEvent.getRequest().getHeaders().get("user-agent");
                    }
                  }

                  session.update(status, userAgent, crashedOrErrored);
                } else {
                  options.getLogger().log(SentryLevel.INFO, "Session is null on scope.withSession");
                }
              });
        } else {
          options.getLogger().log(SentryLevel.INFO, "Scope is null on client.captureEvent");
        }
      }
    }

    try {
      connection.send(event, hint);
    } catch (IOException e) {
      options
          .getLogger()
          .log(SentryLevel.WARNING, "Capturing event " + event.getEventId() + " failed.", e);
    }

    return event.getEventId();
  }

  @Override
  public void captureSession(@NotNull Session session, @Nullable Object hint) {
    if (session.getRelease() == null) {
      options
          .getLogger()
          .log(SentryLevel.WARNING, "Sessions can't be captured without setting a release.");
      return;
    }

    SentryEnvelope envelope;
    try {
      envelope = SentryEnvelope.fromSession(options.getSerializer(), session);
    } catch (IOException e) {
      options.getLogger().log(SentryLevel.ERROR, "Failed to capture session.", e);
      return;
    }

    captureEnvelope(envelope, hint);
  }

  @Override
  public SentryId captureEnvelope(SentryEnvelope envelope, @Nullable Object hint) {
    try {
      connection.send(envelope, hint);
    } catch (IOException e) {
      options.getLogger().log(SentryLevel.ERROR, "Failed to capture envelope.", e);
      return SentryId.EMPTY_ID;
    }
    return envelope.getHeader().getEventId();
  }

  private SentryEvent applyScope(SentryEvent event, @Nullable Scope scope, @Nullable Object hint) {
    if (scope != null) {
      if (event.getTransaction() == null) {
        event.setTransaction(scope.getTransaction());
      }
      if (event.getUser() == null) {
        event.setUser(scope.getUser());
      }
      if (event.getFingerprints() == null) {
        event.setFingerprints(scope.getFingerprint());
      }
      if (event.getBreadcrumbs() == null) {
        event.setBreadcrumbs(new ArrayList<>(scope.getBreadcrumbs()));
      } else {
        event.getBreadcrumbs().addAll(scope.getBreadcrumbs());
      }
      if (event.getTags() == null) {
        event.setTags(new HashMap<>(scope.getTags()));
      } else {
        for (Map.Entry<String, String> item : scope.getTags().entrySet()) {
          if (!event.getTags().containsKey(item.getKey())) {
            event.getTags().put(item.getKey(), item.getValue());
          }
        }
      }
      if (event.getExtras() == null) {
        event.setExtras(new HashMap<>(scope.getExtras()));
      } else {
        for (Map.Entry<String, Object> item : scope.getExtras().entrySet()) {
          if (!event.getExtras().containsKey(item.getKey())) {
            event.getExtras().put(item.getKey(), item.getValue());
          }
        }
      }
      // Level from scope exceptionally take precedence over the event
      if (scope.getLevel() != null) {
        event.setLevel(scope.getLevel());
      }

      for (EventProcessor processor : scope.getEventProcessors()) {
        event = processor.process(event, hint);

        if (event == null) {
          options
              .getLogger()
              .log(
                  SentryLevel.DEBUG,
                  "Event was dropped by scope processor: %s",
                  processor.getClass().getName());
          break;
        }
      }
    }
    return event;
  }

  private SentryEvent executeBeforeSend(SentryEvent event, @Nullable Object hint) {
    SentryOptions.BeforeSendCallback beforeSend = options.getBeforeSend();
    if (beforeSend != null) {
      try {
        event = beforeSend.execute(event, hint);
      } catch (Exception e) {
        options
            .getLogger()
            .log(
                SentryLevel.ERROR,
                "The BeforeSend callback threw an exception. It will be added as breadcrumb and continue.",
                e);

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage("BeforeSend callback failed.");
        breadcrumb.setCategory("SentryClient");
        breadcrumb.setLevel(SentryLevel.ERROR);
        breadcrumb.setData("sentry:message", e.getMessage());
        event.addBreadcrumb(breadcrumb);
      }
    }
    return event;
  }

  @Override
  public void close() {
    options.getLogger().log(SentryLevel.INFO, "Closing SentryClient.");

    try {
      flush(options.getShutdownTimeout());
      connection.close();
    } catch (IOException e) {
      options
          .getLogger()
          .log(SentryLevel.WARNING, "Failed to close the connection to the Sentry Server.", e);
    }
    enabled = false;
  }

  @Override
  public void flush(long timeoutMills) {
    // TODO: Flush transport
  }

  private boolean sample() {
    // https://docs.sentry.io/development/sdk-dev/features/#event-sampling
    if (options.getSampleRate() != null && random != null) {
      double sampling = options.getSampleRate();
      return !(sampling < random.nextDouble()); // bad luck
    }
    return true;
  }
}
