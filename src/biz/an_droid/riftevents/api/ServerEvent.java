package biz.an_droid.riftevents.api;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 04:38
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
}
