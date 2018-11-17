package biz.an_droid.riftevents.api;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 06:08
 */
public class VostigarActivesFilter extends BaseSingleZoneFilter
{
    @Override
    long getFilterZoneId()
    {
        return ZoneIDs.Vostigar;
    }
}
