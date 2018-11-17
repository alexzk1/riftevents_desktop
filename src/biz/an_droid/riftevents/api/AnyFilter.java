package biz.an_droid.riftevents.api;

import java.util.*;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/17/18.
 * At 21:21
 */
public class AnyFilter implements IZoneFilter
{
    private List<IZoneFilter> filters;
    public AnyFilter(List<IZoneFilter> list)
    {
        filters = list;
    }

    public AnyFilter(IZoneFilter[] list)
    {
        filters = new LinkedList<>();
        Collections.addAll(filters, list);
    }

    @Override
    public Set<ServerEvent> filter(EventWrapper response)
    {
        Set<ServerEvent> res = new HashSet<>(15);
        for (IZoneFilter f : filters)
            res.addAll(f.filter(response));
        return res;
    }
}
