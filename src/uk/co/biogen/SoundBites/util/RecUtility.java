package uk.co.biogen.SoundBites.util;

import java.io.IOException;
import java.io.RandomAccessFile;

import uk.co.biogen.SoundBites.analysis.AnalysisInterface;

public class RecUtility
{
  /*
   * Reads the entire file into a short[].
   */
  public static short[] fromAudioFile(RandomAccessFile f)
  {
    short[] samples = null;
    
    try
    {
      samples = new short[(int) (f.length() / 2)];
      f.seek(0);  //Seek to start point of file

      for (int i = 0; i < f.length() / 2; i++)
      {
        samples[i] = f.readShort();
      }
      f.close();
    } catch(IOException e)
    {
      e.printStackTrace();
    }
    
    return samples;
  }
  
  /*
   * As fromAudioFile(), but reads the audio data out into windows. Discards
   * any extra data.
   */
  public static short[][] windowsFromAudioFile(RandomAccessFile f)
  {
    short[][] ret = null;
    
    try
    {
      int numWindowsToRead = (int) Math.floor((f.length() / 2) / AnalysisInterface.WINDOW_SIZE_SAMPLES);
      ret = new short[numWindowsToRead][AnalysisInterface.WINDOW_SIZE_SAMPLES];

      f.seek(0);  //Seek to start point of file
      for(int i=0;i<numWindowsToRead;i++)
        for(int j=0;j<AnalysisInterface.WINDOW_SIZE_SAMPLES;j++)
          ret[i][j] = f.readShort();
      
      f.close();
    } catch(IOException e)
    {
      e.printStackTrace();
    }
    
    return ret;
  }
}
