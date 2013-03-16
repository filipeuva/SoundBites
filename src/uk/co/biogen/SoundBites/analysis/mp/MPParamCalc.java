package uk.co.biogen.SoundBites.analysis.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import uk.co.biogen.SoundBites.util.FileManager;


public class MPParamCalc
{
  public static final double MP_SAMPLES_TO_COSDOMAIN =
      2 * Math.PI / uk.co.biogen.SoundBites.analysis.AnalysisInterface.SAMPLE_RATE;

  /*
   * Chu et al. used 2Hz.
   * 
   * Current value 1 Hz as determined by experiment to have equivalent effectiveness to other
   * values. For i Power = 2.6, i = 1 to 35, giving a top frequency of slightly over
   * 10341 Hz. 
   * 
   * used in MPParameters.getWaveform() 
   */
  public static final double LOWEST_FREQUENCY
    = 1;


  private static MPAtom[] mpCodebook;
  
  private static final int us = 4;
  private static final int omegas = 35; // actually the is used to calculate the omegas
 
  public static double omegaPower;
 
  private static final int ss = 12;
  private static final int thetas = 4;
  private static final String CODEBOOK_FILENAME =
      System.getProperty("user.dir") + File.separator +
      "mp_codebook" +
      "_" + us + "_" + ss + "_" + omegas + "_" + thetas + ".dat";
  
  /*
   * @author wab108
   * 
   @param   window - the window to be deconstructed
   @param   num    - number of constituents to deconstruct it into
   @return  an ArrayList containing the MP components
    
   before calling this method, make sure the object has a codebook, either by
   generating it or loading it from a file
   */
  public static ArrayList<MPAtom> mpDestruct(short[] window, int num) throws Exception
  {
    if (mpCodebook == null)
      throw new Exception("No codebook - generate or load one first");
    
    ArrayList<MPAtom> ret = new ArrayList<MPAtom>();
    
    int[] residualWindow = new int[window.length];
    // translate the input window into int-sample space to prevent clipping errors during the maths
    for(int i=0;i<window.length;i++)
    	residualWindow[i] = (int) window[i];
    
    for(int i=0;i<num;i++)
    {
      MPAtom mostDestructive = null; // the atom that removes the most energy from residualWindow
      long leastPostEnergy = Long.MAX_VALUE;
      long currentPostEnergy = 0;
      
      // iterates through the codebook to find the most destructive atom
      for(MPAtom mp : mpCodebook)
      {
        currentPostEnergy = energyOf(subtractWindow(mp.getWaveform(),residualWindow));
        if(currentPostEnergy < leastPostEnergy)
        {
          leastPostEnergy = currentPostEnergy;
          mostDestructive = mp;
        }
      }
      
      try
      {
    	  residualWindow = subtractWindow(mostDestructive.getWaveform(), residualWindow);
      } catch(IllegalArgumentException e)
      {
    	  e.printStackTrace();
      }
      ret.add(mostDestructive);
    }
    
    return ret;
  }
  
  /*
   *  subtracts the first window from the second - i.e., the MP atom should be
   *  given first, and the audio clip second. Both audio clips must be the same
   *  length.
   */
  private static int[] subtractWindow(int[] atom, int[] window) throws IllegalArgumentException
  {
	if(atom.length != window.length)  
		throw new IllegalArgumentException("Arguments must be the same length in samples");
	
    int[] retWindow = new int[window.length];
    
    for(int i=0;i<retWindow.length;i++)
    {
      retWindow[i] = window[i] - atom[i];
    }
    
    return retWindow;
  }
  
  // as specified by [1]
  // using a sequence number of 1
  private static long energyOf(int[] window)
  {
    long acc = 0;
    for(int s : window)
      acc += s * s;
    
    return acc/window.length;
  }

  //calculation per waveform is [in gnuplot]:
  //g(x) = (( 1 / sqrt(s) ) * exp(- pi * ((x-u)**2))) * cos((2 * pi * w * (x-u)) + h)  
  
  // Note that the frequencies of the atoms are not distributed logarithmically
  // as given in [2]; rather, they are distributed linearly. This is because I
  // am aiming to detect higher-frequency Gabor events, as I hypothesise that
  // lower-frequency events will be covered by the cepstral analysis, and a linear
  // spread will target this frequency range with better resolution.
  
  // codebook generated is currently as specified in [1] except with phase differences
  // use   - make the generated codebook the object's
  // write - write the codebook to default file
  public static void generateCodebook(boolean use, boolean write)
  {
    if(!use && !write)
      return;
    
    MPAtom[] workingCodebook = new MPAtom[us * omegas * ss * thetas];
    
    int codebookIndex = 0;
    for(int i=0;i<us;i++)
    {
      for(int j=0;j<omegas;j++)
      {
        for(int k=0;k<ss;k++)
        {
          for(int l=0;l<thetas;l++)
          {
            workingCodebook[codebookIndex++] =
                new MPAtom(
                    uk.co.biogen.SoundBites.analysis.AnalysisInterface.WINDOW_SIZE_SAMPLES,
                    k,
                    i * uk.co.biogen.SoundBites.analysis.AnalysisInterface.WINDOW_SIZE_SAMPLES / us,
                    j,
                    l * 2 * Math.PI / thetas);
          }
        }
      }
    }
    
    for(MPAtom m : workingCodebook)
    {
      m.getWaveform();
    }
    
    if(use)
    {
      mpCodebook = workingCodebook;
    }
    
    if(write)
    {
      File f = FileManager.getFile(CODEBOOK_FILENAME, true);
      writeCodebookToFile(mpCodebook, f);
    }
  }
  
  private static void writeCodebookToFile(MPAtom[] m, File file)
  {
    try
    {
      FileOutputStream fs = new FileOutputStream(file);
      ObjectOutputStream os = new ObjectOutputStream(fs);
      os.writeObject(m);
      os.close();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public static void loadCodebookFromFile(File f)
  {
    try
    {
      FileInputStream fs = new FileInputStream(f);
      ObjectInputStream os = new ObjectInputStream(fs);
      mpCodebook = (MPAtom[]) os.readObject();
      os.close();
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (StreamCorruptedException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    } catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
  }
  
  // References:
  // ---
  // [1]: Audio Content Analysis for Online Audiovisual Data Segmentation and Classification [Zhang, Jay Kuo]
  // [2]: ENVIRONMENTAL SOUND RECOGNITION USING MP-BASED FEATURES [Chu et al.]
}
