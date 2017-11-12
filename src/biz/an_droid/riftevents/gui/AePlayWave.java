package biz.an_droid.riftevents.gui;

import biz.an_droid.riftevents.ResourceLoader;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Set;

public class AePlayWave extends Thread
{

    private String filename;

    private Position curPosition;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    enum Position
    {
        LEFT, RIGHT, NORMAL
    }

    ;

    public AePlayWave(String wavfile)
    {
        filename = wavfile;
        curPosition = Position.NORMAL;
    }

    public AePlayWave(String wavfile, Position p)
    {
        filename = wavfile;
        curPosition = p;
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


        AudioInputStream audioInputStream = null;
        try
        {
            audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(ResourceLoader.getResourceAsStream(filename + ".mp3.wav")));
        } catch (UnsupportedAudioFileException e1)
        {
            e1.printStackTrace();
            return;
        } catch (IOException e1)
        {
            e1.printStackTrace();
            return;
        }

        AudioFormat format = audioInputStream.getFormat();
        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try
        {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e)
        {
            e.printStackTrace();
            return;
        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN))
        {
            FloatControl pan = (FloatControl) auline
                    .getControl(FloatControl.Type.PAN);
            if (curPosition == Position.RIGHT)
                pan.setValue(1.0f);
            else if (curPosition == Position.LEFT)
                pan.setValue(-1.0f);
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try
        {
            while (nBytesRead != -1)
            {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    auline.write(abData, 0, nBytesRead);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return;
        } finally
        {
            auline.drain();
            auline.close();
        }

    }
} 