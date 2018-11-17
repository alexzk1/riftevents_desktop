package biz.an_droid.riftevents.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/17/18.
 * At 21:15
 */
public abstract class BaseSingleZoneNameFilter implements IZoneFilter
{
    @Override
    public final Set<ServerEvent> filter(EventWrapper response)
    {
        Set<ServerEvent> res = new HashSet<>(15);
        if (response.isSuccess())
        {
            final long id = getFilterZoneId();
            for (ServerEvent e : response.getData())
            {
                if (e.isGoing() && e.getZoneId() == id && checkNameIfInclude(e.getName()))
                    res.add(e);
            }
        }
        return res;
    }

    abstract long getFilterZoneId();
    abstract boolean checkNameIfInclude(String event_name); //return true if this name is ok
}
