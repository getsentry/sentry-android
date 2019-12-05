package io.sentry.core.protocol;

import com.google.gson.annotations.SerializedName;
import io.sentry.core.IUnknownPropertiesConsumer;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

/** The Sentry stack frame. */
public final class SentryStackFrame implements IUnknownPropertiesConsumer {
  private List<String> preContext;
  private List<String> postContext;
  private Map<String, String> vars;
  private List<Integer> framesOmitted;
  private String filename;
  private String function;
  private String module;
  private Integer lineno;
  private Integer colno;
  private String absPath;
  private String contextLine;
  private Boolean inApp;

  @SerializedName(value = "package")
  private String _package;

  @SerializedName(value = "native")
  private Boolean _native;

  private String platform;
  private String imageAddr;
  private String symbolAddr;
  private String instructionAddr;

  @SuppressWarnings("unused")
  private Map<String, Object> unknown;

  private String rawFunction;

  public List<String> getPreContext() {
    return preContext;
  }

  public void setPreContext(List<String> preContext) {
    this.preContext = preContext;
  }

  public List<String> getPostContext() {
    return postContext;
  }

  public void setPostContext(List<String> postContext) {
    this.postContext = postContext;
  }

  public Map<String, String> getVars() {
    return vars;
  }

  public void setVars(Map<String, String> vars) {
    this.vars = vars;
  }

  public List<Integer> getFramesOmitted() {
    return framesOmitted;
  }

  public void setFramesOmitted(List<Integer> framesOmitted) {
    this.framesOmitted = framesOmitted;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public Integer getLineno() {
    return lineno;
  }

  public void setLineno(Integer lineno) {
    this.lineno = lineno;
  }

  public Integer getColno() {
    return colno;
  }

  public void setColno(Integer colno) {
    this.colno = colno;
  }

  public String getAbsPath() {
    return absPath;
  }

  public void setAbsPath(String absPath) {
    this.absPath = absPath;
  }

  public String getContextLine() {
    return contextLine;
  }

  public void setContextLine(String contextLine) {
    this.contextLine = contextLine;
  }

  public Boolean isInApp() {
    return inApp;
  }

  public void setInApp(Boolean inApp) {
    this.inApp = inApp;
  }

  public String getPackage() {
    return _package;
  }

  public void setPackage(String _package) {
    this._package = _package;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getImageAddr() {
    return imageAddr;
  }

  public void setImageAddr(String imageAddr) {
    this.imageAddr = imageAddr;
  }

  public String getSymbolAddr() {
    return symbolAddr;
  }

  public void setSymbolAddr(String symbolAddr) {
    this.symbolAddr = symbolAddr;
  }

  public String getInstructionAddr() {
    return instructionAddr;
  }

  public void setInstructionAddr(String instructionAddr) {
    this.instructionAddr = instructionAddr;
  }

  public Boolean isNative() {
    return _native;
  }

  public void setNative(Boolean _native) {
    this._native = _native;
  }

  @ApiStatus.Internal
  @Override
  public void acceptUnknownProperties(Map<String, Object> unknown) {
    this.unknown = unknown;
  }

  public String getRawFunction() {
    return rawFunction;
  }

  public void setRawFunction(String rawFunction) {
    this.rawFunction = rawFunction;
  }
}
