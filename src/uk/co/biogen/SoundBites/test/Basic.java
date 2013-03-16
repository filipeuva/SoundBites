package uk.co.biogen.SoundBites.test;

import uk.co.biogen.SoundBites.rec.Recorder;
import uk.co.biogen.SoundBites.rec.RecordingsManager;
import android.media.MediaRecorder;

/*
 * Test class used to make recordings of different Android audio sources.
 */
public class Basic
{
  static Recorder r;
  
  public static void testAllAvailable(final int secs)
  {
    final int[] devices = {
        MediaRecorder.AudioSource.CAMCORDER,
        MediaRecorder.AudioSource.DEFAULT,
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.VOICE_CALL,
        MediaRecorder.AudioSource.VOICE_DOWNLINK,
        MediaRecorder.AudioSource.VOICE_RECOGNITION,
        MediaRecorder.AudioSource.VOICE_UPLINK};
    
    (new Thread(new Runnable()
    {
      public void run()
      {
        for(final int d : devices)
        {
          RecordingsManager.deleteFileFromDisk("input" + d + ".wav");
          
          Thread recordingThread = new Thread(new Runnable()
          {
            public void run()
            {
              r = new Recorder(d);
              System.out.println("Recording source " + d);
              r.setOutputFile(RecordingsManager.getFile("input" + d + ".wav"));
              r.prepareForFile();
    
              r.start();
            }
          });
          
          recordingThread.start();
          try
          {
            Thread.sleep(secs * 1000);
          } catch (InterruptedException e)
          {
            e.printStackTrace();
          }
          r.stop();
        }
      }
    })).start();
  }
  
  public static void captureSomeSound(final int secs)
  {
    Thread recordingThread = new Thread(new Runnable()
    {
      public void run()
      {
        RecordingsManager.deleteFileFromDisk("DEFAULT.raw");
        RecordingsManager.deleteFileFromDisk("DEFAULT.wav");

        r = new Recorder(MediaRecorder.AudioSource.DEFAULT);
        System.out.println("Recording from 'DEFAULT'");
        r.setOutputFile(RecordingsManager.getFile("DEFAULT.raw"));
        r.prepareForFile();

        r.start();
      }
    });
    
    recordingThread.start();
    (new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          Thread.sleep(secs * 1000);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        r.stop();
      }
    })).start();
  }
  
  public static void fileMake()
  {
    System.out.println("Making file TEST.wav.");
  
    RecordingsManager.getFile("TEST.wav");
  }
}
