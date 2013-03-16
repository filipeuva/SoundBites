package uk.co.biogen.SoundBites.rec;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class RecordingsManager
{
  public static String ROOT_DIR =
      System.getProperty("user.dir") + File.separator +
      "soundbites" + File.separator +
      "audio" + File.separator;
  {
    new File(ROOT_DIR).mkdirs();
  }
  
  private static HashMap<String,AudioFile> namesInUse = new HashMap<String,AudioFile>();
  
  /*
   * Creates the file if it does not already exist.
   */
  public static AudioFile getFile(String name)
  {
    if(namesInUse.containsKey(name))
    {
      return namesInUse.get(name);
    } else
    {
      AudioFile af = null;
      try
      {
        af = new AudioFile(name);
      } catch (FileNotFoundException e)
      {
        e.printStackTrace();
      }
      namesInUse.put(name,af);
      return af;
    }
  }
  
  public static void forgetFile(String name)
  {
    namesInUse.remove(name);
  }
  
  public static void deleteFileFromDisk(String name)
  {
    if(namesInUse.containsKey(name))
    {
      File file = new File(namesInUse.get(name).getFullPath());
      file.delete();
    }
    forgetFile(name);
  }
}
