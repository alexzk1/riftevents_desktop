package biz.an_droid.riftevents.gui;

import biz.an_droid.riftevents.ResourceLoader;
import com.sun.javafx.PlatformUtil;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class AePlayWave extends Thread
{
    private String filename;
    private static final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    public AePlayWave(String wavfile)
    {
        filename = wavfile;

    }

    public void playLocked()
    {
        System.out.println("Playing...");
        start();
        try
        {
            join();
        } catch (InterruptedException e)
        {
        }
        System.out.println("End playing...");
    }

    private static Thread player = null;

    public static void playList(final Set<String> list)
    {
        if (player != null)
        {
            player.interrupt();
            try
            {
                player.join();
            } catch (InterruptedException e)
            {
            }
        }
        if (list != null)
        {
            player = new Thread(() ->
            {
                for (String s : list)
                {
                    if (Thread.currentThread().isInterrupted())
                        break;
                    new AePlayWave(s).playLocked();
                }
            });
            player.start();
        }
    }

    public void run()
    {
        boolean has_default = false;

        if (PlatformUtil.isLinux())
        {
            Mixer.Info[] inf = AudioSystem.getMixerInfo();
            for (Mixer.Info i : inf)
            {
                if (i.getName().contains("[default]"))
                {
                    has_default = true;
                    break;
                }
            }
        }

        AudioInputStream audioInputStream;
        BufferedInputStream sound;
        try
        {
            sound =  new BufferedInputStream(ResourceLoader.getResourceAsStream(filename + ".mp3.wav"));

            if (!has_default && PlatformUtil.isLinux())
            {
                linuxPlayExtern(sound);
                return;
            }
            audioInputStream = AudioSystem.getAudioInputStream(sound);
        } catch (InterruptedException | UnsupportedAudioFileException | IOException e1)
        {
            e1.printStackTrace();
            return;
        }

        SourceDataLine auline = null;
        try
        {
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            auline = (SourceDataLine) AudioSystem.getLine(info);
            if (auline == null)
                return;

            auline.open(format);
        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        auline.start();
        try
        {
            pipeStream(audioInputStream, auline);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            auline.drain();
            auline.close();
        }

    }

    private final static byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
    private static <T> void pipeStream(InputStream is, T os) throws IOException
    {
        int nBytesRead = 0;
        while (nBytesRead != -1)
        {
            nBytesRead = is.read(abData, 0, abData.length);
            if (nBytesRead >= 0)
            {
                //oh this dumb java .. C++ is much better >:
                if (os instanceof OutputStream)
                    ((OutputStream)os).write(abData, 0, nBytesRead);

                if (os instanceof SourceDataLine)
                    ((SourceDataLine)os).write(abData, 0, nBytesRead);
            }
        }
    }

    private static void linuxPlayExtern(BufferedInputStream is) throws IOException, InterruptedException
    {
        System.out.println("Using aplay");
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("aplay", "-D", "pulse");
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        try
        {
            pipeStream(is, p.getOutputStream());
        }
        finally
        {
            p.waitFor();
        }
    }
} 