package biz.an_droid.riftevents.api;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/17/18.
 * At 21:17
 */
public class CrackingSkullFilter extends BaseSingleZoneNameFilter
{
    @Override
    long getFilterZoneId()
    {
        return ZoneIDs.XarMire;
    }

    @Override
    boolean checkNameIfInclude(String event_name)
    {
        if (event_name == null)
            return false;
        return event_name.contains("Cracking") && event_name.contains("Skull");
    }
}
