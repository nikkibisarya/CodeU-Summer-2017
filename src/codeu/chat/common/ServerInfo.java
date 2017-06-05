package codeu.chat.common;

import codeu.chat.util.Time;

public final class ServerInfo {
  public final Time startTime;
    public ServerInfo() {
      this.startTime = Time.now();            
    }

    public ServerInfo(Time startTime) {
      this.startTime = startTime;
    }

    public Time getTime() {
          return this.startTime;
    }

    @Override
    public String toString()
    {
      return getTime().toString();

    }
}
