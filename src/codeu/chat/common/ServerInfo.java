package codeu.chat.common;

import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import java.io.IOException;

public final class ServerInfo {

  private final static String SERVER_VERSION = "1.0.0";

  public Uuid version;
  public Time startTime;

  public ServerInfo() {
    try {
      this.version = Uuid.parse(SERVER_VERSION);
    } catch (IOException e) {
      this.version = null;
    }
    this.startTime = Time.now();
  }

  public ServerInfo(Uuid version, Time startTime) {
    this.version = version;
    this.startTime = startTime;
  }

  public Uuid getVersion() {
    return (this.version == null) ? Uuid.NULL : this.version;
  }

  public Time getTime() {
    return this.startTime;
  }

  public String getVersionStr() {
    return this.version == null ? null : this.version.toString();
  }

  public String getTimeStr() {
    return this.startTime.toString();
  }

}
