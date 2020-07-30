package io.sentry.core.protocol;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public final class Contexts extends ConcurrentHashMap<String, Object> implements Cloneable {
  private static final long serialVersionUID = 252445813254943011L;

  public Contexts(Map<String, Object> initialValues) {
    super(initialValues);
  }

  public Contexts() {
    this(Collections.emptyMap());
  }

  private <T> T toContextType(String key, Class<T> clazz) {
    Object item = get(key);
    return clazz.isInstance(item) ? clazz.cast(item) : null;
  }

  public App getApp() {
    return toContextType(App.TYPE, App.class);
  }

  public void setApp(App app) {
    this.put(App.TYPE, app);
  }

  public Browser getBrowser() {
    return toContextType(Browser.TYPE, Browser.class);
  }

  public void setBrowser(Browser browser) {
    this.put(Browser.TYPE, browser);
  }

  public Device getDevice() {
    return toContextType(Device.TYPE, Device.class);
  }

  public void setDevice(Device device) {
    this.put(Device.TYPE, device);
  }

  public OperatingSystem getOperatingSystem() {
    return toContextType(OperatingSystem.TYPE, OperatingSystem.class);
  }

  public void setOperatingSystem(OperatingSystem operatingSystem) {
    this.put(OperatingSystem.TYPE, operatingSystem);
  }

  public SentryRuntime getRuntime() {
    return toContextType(SentryRuntime.TYPE, SentryRuntime.class);
  }

  public void setRuntime(SentryRuntime runtime) {
    this.put(SentryRuntime.TYPE, runtime);
  }

  public Gpu getGpu() {
    return toContextType(Gpu.TYPE, Gpu.class);
  }

  public void setGpu(Gpu gpu) {
    this.put(Gpu.TYPE, gpu);
  }

  @Override
  public @NotNull Contexts clone() throws CloneNotSupportedException {
    Contexts clone = new Contexts();

    for (Map.Entry<String, Object> entry : entrySet()) {
      if (App.TYPE.equals(entry.getKey()) && entry.getValue() instanceof App) {
        clone.setApp(((App) entry.getValue()).clone());
      } else if (Browser.TYPE.equals(entry.getKey()) && entry.getValue() instanceof Browser) {
        clone.setBrowser(((Browser) entry.getValue()).clone());
      } else {
        clone.put(entry.getKey(), entry.getValue());
      }
    }

    return clone;
  }
}
