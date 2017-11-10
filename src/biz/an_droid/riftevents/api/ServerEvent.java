package biz.an_droid.riftevents.api;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 04:38
 * Corresponds to rift api response json, do not rename fields!
 * It is array element.
 */
public class ServerEvent
{
    private String zone ="";
    private long   zoneId;
    private String name = "";
    private long   started;

    public String getZone()
    {
        return zone;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    public long getZoneId()
    {
        return zoneId;
    }

    public void setZoneId(long zoneId)
    {
        this.zoneId = zoneId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getStarted()
    {
        return started;
    }

    public void setStarted(long started)
    {
        this.started = started;
    }

    public boolean isGoing()
    {
        return name != null && !name.isEmpty();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerEvent that = (ServerEvent) o;

        if (getZoneId() != that.getZoneId()) return false;
        return getStarted() == that.getStarted();
    }

    @Override
    public int hashCode()
    {
        int result = (int) (getZoneId() ^ (getZoneId() >>> 32));
        result = 31 * result + (int) (getStarted() ^ (getStarted() >>> 32));
        return result;
    }

    public long getElapsedSeconds(boolean is_eu)
    {
        ZoneId zid = ZoneId.of((is_eu) ? "GMT" : "America/Los_Angeles");
        ZonedDateTime base = ZonedDateTime.ofInstant(Instant.ofEpochSecond(getStarted()), zid);
        return Duration.between(base, ZonedDateTime.now(zid)).getSeconds();
    }

    public String getElapsed(boolean is_eu)
    {
        return String.format("%d minutes",getElapsedSeconds(is_eu) / 60);
    }
}
