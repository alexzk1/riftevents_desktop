package biz.an_droid.riftevents.api;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 04:15
 */
public class RequestEvents
{
    //tables are from here https://github.com/chennin/yaret/blob/master/yaret-data.sql

    private static Map<String, Integer> shardsEU = new HashMap<String, Integer>()
    {
        {
            put("Bloodiron", 2702);
            put("Brutwacht", 2711);
            put("Brisesol", 2714);
            put("Gelidra", 2721);
            put("Zaviel", 2722);
            put("Typhiria", 2741);
        }
    };

    private static Map<String, Integer> shardsUS = new HashMap<String, Integer>()
    {
        {
            put("Seastone", 1701);
            put("Greybriar", 1702);
            put("Deepwood", 1704);
            put("Wolfsbane", 1706);
            put("Faeblight", 1707);
            put("Laethys", 1708);
            put("Hailol", 1721);
        }
    };

//    private static Map<String, Integer> zones = new HashMap<String, Integer>()
//    {
//        {
//            put("Mathosia", 1);
//            put("Brevane/Dusken", 2);
//            put("Plane of Water", 3);
//            put("Celestial Lands", 4);
//        }
//    };

    private static String[] euServers;
    private static String[] usServers;

    static
    {
        euServers = shardsEU.keySet().toArray(new String[shardsEU.size()]);
        usServers = shardsUS.keySet().toArray(new String[shardsUS.size()]);
        Arrays.sort(euServers);
        Arrays.sort(usServers);
    }

    private final static EventWrapper failed = new EventWrapper();
    private final static Gson gson = new Gson();

    public static EventWrapper fetch(final String server)
    {
        EventWrapper res = failed;
        String urls = null;
        if (shardsEU.containsKey(server))
        {
            urls = String.format("https://web-api-eu.riftgame.com/chatservice/zoneevent/list?shardId=%d", shardsEU.get(server));
        } else
        {
            if (shardsUS.containsKey(server))
            {
                urls = String.format("https://web-api-us.riftgame.com/chatservice/zoneevent/list?shardId=%d", shardsUS.get(server));
            }
        }

        if (urls != null && !urls.isEmpty())
        {
            try
            {
                String jsr = getBody(urls);
                res = gson.fromJson(jsr, EventWrapper.class);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return res;
    }

    public static Set<ServerEvent> fetchFiltered(final String server, final IZoneFilter filter)
    {
        return filter.filter(fetch(server));
    }

    public static Map<String, Set<ServerEvent>> fetchFiltered(final List<String> servers, final IZoneFilter filter)
    {
        Map<String, Set<ServerEvent>> res = new HashMap<>(10);
        for (String s : servers)
        {
            Set<ServerEvent> r = fetchFiltered(s, filter);
            if (!r.isEmpty())
                res.put(s, r);
        }
        return res;
    }

    public static boolean isEuServer(final  String server)
    {
        return shardsEU.containsKey(server);
    }

    public static String[] getEuServers()
    {
        return euServers;
    }

    public static String[] getUsServers()
    {
        return usServers;
    }

    public static String getBody(String url) throws IOException
    {
        URLConnection con = new URL(url).openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();  // ** WRONG: should use "con.getContentType()" instead but it returns something like "text/html; charset=UTF-8" so this value must be parsed to extract the actual encoding
        encoding = encoding == null ? "UTF-8" : encoding;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1)
            baos.write(buf, 0, len);
        
        return new String(baos.toByteArray(), encoding);
    }

//    public static void dumpVoiceTexts()
//    {
//        for (String s: euServers)
//            System.out.println("Vostigar event started on " + s);
//
//        for (String s: usServers)
//            System.out.println("Vostigar event started on " + s);
//    }
//    static
//    {
//        dumpVoiceTexts();
//    }
}
