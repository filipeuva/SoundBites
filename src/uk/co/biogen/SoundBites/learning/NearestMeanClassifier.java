package uk.co.biogen.SoundBites.learning;

import java.util.Arrays;
import java.util.HashMap;

/*
 * Linearly discriminates between classes by finding the class with the
 * nearest centroid.
 */
public class NearestMeanClassifier implements Cloneable
{
  private HashMap<String,MeanAndCount> contextsAndMeans;
  
  public NearestMeanClassifier()
  {
    contextsAndMeans = new HashMap<String, MeanAndCount>();
  }
  
  /*
   * Generates means for the classifier object, for the metrics of each
   * context.
   */
  public void train(ContextDataPair[] cdps, boolean replace)
  {
    if(replace)
      contextsAndMeans.clear();
    
    // for each input context
    for(ContextDataPair cdp : cdps)
    {
      MeanAndCount mac;
      
      // if context already exists
      if(contextsAndMeans.containsKey(cdp.contextName))
      {
        mac = contextsAndMeans.get(cdp.contextName);
      } else
      {
        mac = new MeanAndCount();
        mac.mean = new double[cdp.data[0].length];
        Arrays.fill(mac.mean, 0.0);
        
        contextsAndMeans.put(cdp.contextName, mac);
      }

      // use each value to iteratively update the relevant co-ordinate of the mean
      
      // for each metric bundle
      for(int i=0;i<cdp.data.length;i++)
      {
        mac.count++;
        
        // for each value
        for(int j=0;j<cdp.data[i].length;j++)
          // iterative mean according to http://www.heikohoffmann.de/htmlthesis/node134.html
          mac.mean[j] = mac.mean[j] + (((double) 1 / (double) (mac.count)) * (cdp.data[i][j] - mac.mean[j]));
      }
    }
  }
  
  public String query(double[] metricData)
  {
    double smallestEuclideanDistance = Double.MAX_VALUE;
    String sedContext = "";
    
    // look through recorded contexts and means
    for(String c : contextsAndMeans.keySet())
    {
      MeanAndCount mac = contextsAndMeans.get(c);
      
      // calculate the Euclidean distance between the value of each metric
      // and the mean, of the current context, for that metric
      double acc = 0.0;
      for(int j=0;j<metricData.length;j++)
      {
        double dist = metricData[j] - mac.mean[j];
        acc += dist * dist;
      }
      acc = Math.sqrt(acc);
      
      // if least, make the selected context the current one
      if(acc<smallestEuclideanDistance)
      {
        smallestEuclideanDistance = acc;
        sedContext = c;
      }
    }
    
    return sedContext;
  }
  
  /*
   * The field 'data' should be indexed first on metric, then on value;
   * i.e., data[0] will contain all of the zero cross rate values.
   */
  public class ContextDataPair
  {
    public String contextName;
    public double[][] data;

    public ContextDataPair(String contextName, double[][] data)
    {
      this.contextName = contextName;
      this.data = data;
    }
  }
  
  private static class MeanAndCount
  {
    // number of points that made this mean
    public int count;
    public double[] mean;
    
    public MeanAndCount()
    {
      count = 0;
      mean = null;
    }
    
//    public MeanAndCount(int count, double[] mean)
//    {
//      this.count = count;
//      this.mean = mean;
//    }
  }
  
  @Override
  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
}
