package uk.co.biogen.SoundBites.guiModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

import uk.co.biogen.SoundBites.analysis.AnalysisInterface;
import uk.co.biogen.SoundBites.learning.KNNClassifier;
import uk.co.biogen.SoundBites.learning.KNNClassifier.ContextPointsPair;
import uk.co.biogen.SoundBites.learning.MarkovChain;
import uk.co.biogen.SoundBites.rec.Recorder;
import uk.co.biogen.SoundBites.rec.RecordingsManager;
import uk.co.biogen.SoundBites.util.FileManager;
import android.media.MediaRecorder;
import android.util.Log;

public class SoundBitesStartModel
{
  private static MarkovChain movements;
  private static KNNClassifier knnClassifier;
  public static int knnQueryK = 1;
  
  private static final String KNN_FILENAME = "knnClassifierData.dat";
  
  static
  {
    movements = new MarkovChain();
    
    if(FileManager.fileExists(KNN_FILENAME))
    {
      File f = FileManager.getFile(KNN_FILENAME, false);
      
      FileInputStream saveFile = null;
      try
      {
        saveFile = new FileInputStream(f);
      } catch (FileNotFoundException e)
      {
        e.printStackTrace();
      }
      try
      {
        ObjectInputStream restore = new ObjectInputStream(saveFile);
        knnClassifier = (KNNClassifier) restore.readObject();
        restore.close();
      } catch (IOException e)
      {
        e.printStackTrace();
      } catch (ClassNotFoundException e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      knnClassifier = new KNNClassifier();
    }
  }
  
  public static void trainContext(short[][] audioWindows, String contextName)
  {
    double[][] metricBundles = calculateMetrics(audioWindows);

    ContextPointsPair[] trainingData = new ContextPointsPair[] {new ContextPointsPair(contextName, metricBundles)};
    
    knnClassifier.train(trainingData, false);
  }
  
  /*
   * @param audioWindows    Audio windows to be analysed
   * @param useMovementsMC  Whether to use the Markov chain that tracks
   *   inter-context movement
   *   
   * Converts the input data into observation sequences compatible with the
   * HMMs, then selects the context with the highest probability and returns
   * its name.
   */
  public static String calculateContext(short[][] audioWindows, boolean useMovementsMC)
  {
    double[][] audioMetrics = calculateMetrics(audioWindows);
    HashMap<String,Integer> contextCounts = new HashMap<String, Integer>();
    
    for(double[] ms : audioMetrics)
    {
      String contextName = knnClassifier.query(ms, knnQueryK);
      
      if(contextCounts.containsKey(contextName))
      {
        int currentCount = contextCounts.get(contextName); 
        currentCount++;
        contextCounts.put(contextName, currentCount);
      }
      else
      {
        contextCounts.put(contextName, 1);
      }
    }
    
    // default value will be replaced
    String highestContext = "Learning_Error";
    int highestCount = 0;
    for(String c : contextCounts.keySet())
    {
      if(contextCounts.get(c) > highestCount)
      {
        highestCount = contextCounts.get(c);
        highestContext = c;
      }
    }
    
    return highestContext;
  }
  
  /*
   * Should be called whenever this application terminates,
   * or earlier, if you like - this method freezes everything to file ready
   * for picking up again when the application is started again.
   */
  public static void consolidateMemory()
  {
    File f = FileManager.getFile(KNN_FILENAME, false);
    
    FileOutputStream saveFile = null;
    try
    {
      saveFile = new FileOutputStream(f);
    } catch (FileNotFoundException e1)
    {
      e1.printStackTrace();
    }
    try
    {
      ObjectOutputStream save = new ObjectOutputStream(saveFile);
      save.writeObject(knnClassifier);
      save.close();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public static void nextStepForMarkovChain(String nextContext)
  {
    movements.nextContext(nextContext);
  }
  
  /*
   * @param recordTimems   The length of time to record for, in milliseconds.
   * @param isContextPoll  set to true if this is a context poll
   * @param filename       set if not a context poll; give it a name
   * 
   * @return  A handle on the created file.
   */
  public static RandomAccessFile recordFromMicrophone(int recordTimems, boolean isContextPoll, String filename)
  {
    Recorder r = new Recorder(MediaRecorder.AudioSource.DEFAULT);
    String nameOfRecordedFile = "";
    
    if(isContextPoll)
    {
      nameOfRecordedFile = "poll_record_file.raw";
    } else
    {
      nameOfRecordedFile = filename;
    }
    
    r.setOutputFile(RecordingsManager.getFile(nameOfRecordedFile));
    r.prepareForFile();
    r.start();
    try
    {  
      Thread.sleep(recordTimems);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    r.stop();
    
    return (RecordingsManager.getFile(nameOfRecordedFile)).getFile();
  }
  
  private static double[][] calculateMetrics(short[][] audioWindows)
  {
    double[][] metricBundles = new double[audioWindows.length][];
    
    // convert audio windows to metric bundles
    for(int i=0;i<audioWindows.length;i++)
    {
      metricBundles[i] = new double[8];
      
      metricBundles[i][0] = AnalysisInterface.RMS(audioWindows[i]);
      metricBundles[i][1] = AnalysisInterface.zeroCrossRate(audioWindows[i]);
      metricBundles[i][2] = AnalysisInterface.spectralFlatness(audioWindows[i]);
      metricBundles[i][3] = AnalysisInterface.bandEnergyRatio(audioWindows[i], 0.5);
      metricBundles[i][4] = AnalysisInterface.spectralCentroid(audioWindows[i]);
      metricBundles[i][5] = AnalysisInterface.spectralBandwidth(audioWindows[i]);
      metricBundles[i][6] = AnalysisInterface.spectralFlux(audioWindows[i]);
      metricBundles[i][7] = AnalysisInterface.spectralRolloff(audioWindows[i]);
    }
    
    return metricBundles;
  }
}
