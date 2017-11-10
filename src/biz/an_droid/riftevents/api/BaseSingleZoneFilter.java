package biz.an_droid.riftevents.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 06:02
 */
public abstract class BaseSingleZoneFilter implements IZoneFilter
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
                if (e.isGoing() && e.getZoneId() == id)
                    res.add(e);
            }
        }
        return res;
    }

    abstract long getFilterZoneId();
}
