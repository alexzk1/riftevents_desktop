package biz.an_droid.riftevents.api;

import biz.an_droid.lsnrsupp.IListenerSupport;
import biz.an_droid.lsnrsupp.ListenerSupportFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 06:32
 */
public class EventsReaderThreaded implements IListenerSupport<IListenerEventsUpdated>, AutoCloseable
{
    private final IListenerEventsUpdated m_listeners = ListenerSupportFactory.createListenerSupport(IListenerEventsUpdated.class);
    private final AtomicInteger periodSeconds = new AtomicInteger(30);
    private final AtomicReference<List<String>> servers  = new AtomicReference<>(null);
    private final Thread thread;
    
    public EventsReaderThreaded()
    {
       this(null);
    }

    public EventsReaderThreaded(IZoneFilter cf)
    {
        this(cf, true);
    }

    public EventsReaderThreaded(IZoneFilter cf, boolean start)
    {
        final IZoneFilter currentFilter = (cf == null) ? new AnyActiveEvent() : cf;
        
        Runnable threadFunc = () -> {
            Map<String, Set<ServerEvent>> last = new HashMap<>(10);
            List<String> toDel = new ArrayList<>(10);

            List<String> servers_old = null;
            while (!Thread.currentThread().isInterrupted())
            {
                Map<String, Set<ServerEvent>> curr = null;
                List<String> servers = this.servers.get();
                boolean had_changes     = servers_old != servers;
                boolean had_new_events  = false;

                servers_old = servers;
                
                if (servers != null && !servers.isEmpty())
                    curr = RequestEvents.fetchFiltered(servers, currentFilter);

                if (curr != null)
                {
                    toDel.clear();
                    for (String k : last.keySet())
                    {
                        if (!curr.containsKey(k) || curr.get(k).isEmpty())
                        {
                            toDel.add(k);
                            had_changes = true;
                        }
                    }

                    for (String k : toDel)
                        last.remove(k);

                    for (String k : curr.keySet())
                    {
                        Set<ServerEvent> currs = curr.get(k);
                        if (!last.containsKey(k))
                        {
                            last.put(k, currs);
                            had_changes    = true;
                            had_new_events = true;
                        } else
                        {
                            Set<ServerEvent> lasts = last.get(k);
                            had_changes = had_changes || lasts.retainAll(currs);

                            boolean added = lasts.addAll(currs);
                            had_changes = had_changes || added;
                            had_new_events = had_new_events || added;
                            
                            //this should never happen though
                            if (lasts.size() < 1)
                            {
                                last.remove(k);
                                had_changes = true;
                            }
                        }
                    }
                }

                //if (had_changes)
                    synchronized (m_listeners)
                    {
                        m_listeners.haveNewEvents(curr, had_new_events);
                    }

                try
                {
                    if (!Thread.currentThread().isInterrupted())
                        Thread.sleep(1000 * periodSeconds.get());
                } catch (InterruptedException e)
                {
                    break;
                }
            }
        };
        thread = new Thread(threadFunc);
        if (start)
            thread.start();
    }

    public void start()
    {
        if (!thread.isAlive())
            thread.start();
    }

    public int getPeriod()
    {
        return periodSeconds.get();
    }

    public void setServers(List<String> servs)
    {
        servers.set(servs);
    }

    public void setPeriodSeconds(int period)
    {
        periodSeconds.set(period);
    }

    @Override
    public void addListener(IListenerEventsUpdated iListenerEventsUpdated)
    {
        if (iListenerEventsUpdated != null)
            synchronized (m_listeners)
            {
                getListeners().addListener(iListenerEventsUpdated);
            }
    }

    @Override
    public void removeListener(final IListenerEventsUpdated iListenerEventsUpdated)
    {
        if (iListenerEventsUpdated != null)
            synchronized (m_listeners)
            {
                getListeners().removeListener(iListenerEventsUpdated);
            }
    }

    @Override
    public void clearListeners()
    {
        synchronized (m_listeners)
        {
            getListeners().clearListeners();
        }
    }

    @Override
    public void close() throws Exception
    {
        thread.interrupt();
        clearListeners();
        thread.join();
        System.out.println("Reader thread finished.");
    }

    private IListenerSupport<IListenerEventsUpdated> getListeners()
    {
        return ((IListenerSupport<IListenerEventsUpdated>) m_listeners);
    }

}
