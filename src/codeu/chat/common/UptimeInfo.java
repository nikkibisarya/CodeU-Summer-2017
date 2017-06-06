package codeu.chat.common;

import codeu.chat.util.Time;

public final class UptimeInfo {
  public final Time startTime;
    public Uptime() {
      this.startTime = Time.now();            
    }

    public UptimeInfo(Time startTime) {
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
