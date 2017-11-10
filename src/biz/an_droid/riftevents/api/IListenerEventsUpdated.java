package biz.an_droid.riftevents.api;

import java.util.Map;
import java.util.Set;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 06:27
 */
public interface IListenerEventsUpdated
{
    void haveNewEvents(Map<String, Set<ServerEvent>> events);
}
