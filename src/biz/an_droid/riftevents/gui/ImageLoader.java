package biz.an_droid.riftevents.gui;


import biz.an_droid.riftevents.ResourceLoader;
import com.sun.javafx.PlatformUtil;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOImage;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.net.URL;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

;


/**
 * Created by alex (alexzkhr@gmail.com) on 11/9/17.
 * At 23:03
 */
public class ImageLoader
{
    static final List<BufferedImage> def = new ArrayList<>(1);
    public static BufferedImage finalImage;
    
    static
    {
        try
        {
            def.add(ImageIO.read(ResourceLoader.getResourceAsStream("events-icon.png")));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static Image loadICOFromUrlForTray(String url)
    {
        List<BufferedImage> images = null;

        try {
            InputStream istr = new URL(url).openConnection().getInputStream();
            images = ICODecoder.read(istr);
        } catch (IOException ex)
        {
           ex.printStackTrace();
        }
        if (images == null)
            images = def;

        BufferedImage src = images.get(0);

        //finding best icon if ICO file has many
        for (int i = images.size() - 1; i > 0; --i)
            if (src.getHeight() < images.get(i).getHeight())
                src = images.get(i);

        finalImage = src;
//        if (PlatformUtil.isLinux())
//        {
//            BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
//            Graphics2D g2d = copy.createGraphics();
//            g2d.setColor(Color.WHITE); // Or what ever fill color you want...
//            g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
//            g2d.drawImage(src, 0, 0, null);
//            g2d.dispose();
//            return copy;
//        }
        return src;
    }


}
