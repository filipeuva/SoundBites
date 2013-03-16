package uk.co.biogen.SoundBites.rec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

//import uk.co.bio_gen.project.R;

public class AudioFile
{
  private File file;
  private String fullPath;
  
  public AudioFile(String filename) throws FileNotFoundException
  {
//    file = new RandomAccessFile(R.string.dir_path + filename, "rw");
    fullPath = 
        System.getProperty("user.dir") +
        "sdcard" + File.separator +
        "soundbites" + File.separator +
        "audio" + File.separator +
        filename;

    file = new File(fullPath);
    file.getParentFile().mkdirs();
    
    try
    {
      file.createNewFile();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  /*
   * @return A writable RandomAccessFile face to the stored file.
   * 
   * Due to the working of RecordingsManager, this file will always exist.
   */
  public RandomAccessFile getFile()
  {
    RandomAccessFile ra = null;
    
    try
    {
      ra = new RandomAccessFile(file, "rw");
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    
    return ra;
  }
  
  public String getFullPath()
  {
    return fullPath;
  }
}
