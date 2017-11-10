package biz.an_droid.riftevents.api;

import java.util.Set;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 05:22
 */
public interface IZoneFilter
{
    Set<ServerEvent> filter(EventWrapper response);
}
