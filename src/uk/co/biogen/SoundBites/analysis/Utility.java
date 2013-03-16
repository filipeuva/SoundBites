package uk.co.biogen.SoundBites.analysis;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utility
{
  public static short[] dataFromRaw(int len, int offset, RandomAccessFile r) throws IllegalArgumentException
  {
    System.out.println("Doing offset " + offset + ".");
    
    try
    {
      if(r.length() / 2 < len)
      {
        throw new IllegalArgumentException("Targeted file has fewer samples than requested");
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    
    short[] ret = new short[len];
    byte[] bytes = new byte[len*2];

    try
    {
      r.seek(offset * 2);
      r.read(bytes,0,len*2);
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    
    ret = new short[len];
    ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).asShortBuffer().get(ret);
    
    return ret;
  }
}
