package biz.an_droid.riftevents.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 08:29
 */
public final class AnyActiveEvent implements IZoneFilter
{
    @Override
    public Set<ServerEvent> filter(EventWrapper response)
    {
        Set<ServerEvent> res = new HashSet<>(15);
        if (response.isSuccess())
        {
            for (ServerEvent e : response.getData())
            {
                if (e.isGoing())
                    res.add(e);
            }
        }
        return res;
    }
}
