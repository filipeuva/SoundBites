package uk.co.biogen.SoundBites.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileManager
{
  public static String ROOT_DIR =
      System.getProperty("user.dir") +
      "soundbites" + File.separator;
  {
    new File(ROOT_DIR).mkdirs();
  }
  
  private static HashMap<String,File> namesInUse = new HashMap<String,File>();
  
  /*
   * Creates the file if it does not already exist.
   * For audio files, use rec.RecordingsManager
   */
  public static File getFile(String name, boolean overwrite)
  {
    if(namesInUse.containsKey(name))
    {
      return namesInUse.get(name);
    } else
    {
      File af = new File(ROOT_DIR + name);
      if(overwrite || !af.exists())
      {
        try
        {
          af.createNewFile();
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
      
      namesInUse.put(name,af);
      return af;
    }
  }
  
  public static boolean fileExists(String name)
  {
    File af = new File(ROOT_DIR + name);
    
    return namesInUse.containsKey(name) || af.exists();
  }
  
  public static void forgetFile(String name)
  {
    namesInUse.remove(name);
  }
  
  public static void deleteFileFromDisk(String name)
  {
    if(namesInUse.containsKey(name))
    {
      // WARN: Not sure if getAbsolutePath will get the right thing
      // this is cloned from RecordingsManager and haphazardly replaced
      File file = new File(namesInUse.get(name).getAbsolutePath());
      file.delete();
    }
    forgetFile(name);
  }
}
