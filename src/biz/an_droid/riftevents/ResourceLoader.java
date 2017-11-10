package biz.an_droid.riftevents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 01:27
 */
public class ResourceLoader
{

    public static URL getResource(String resource) throws MalformedURLException
    {
        final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        classLoaders.add(Thread.currentThread().getContextClassLoader());
        classLoaders.add(ResourceLoader.class.getClassLoader());

        for (ClassLoader classLoader : classLoaders)
        {
            final URL url = getResourceWith(classLoader, resource);
            if (url != null)
            {
                return url;
            }
        }

        final URL systemResource = ClassLoader.getSystemResource(resource);
        if (systemResource != null)
        {
            return systemResource;
        } else
        {
            return new File(resource).toURI().toURL();
        }
    }

    private static URL getResourceWith(ClassLoader classLoader, String resource)
    {
        if (classLoader != null)
        {
            return classLoader.getResource(resource);
        }
        return null;
    }

    public static InputStream getResourceAsStream(String url) throws IOException
    {
        return getResource(url).openConnection().getInputStream();
    }
}