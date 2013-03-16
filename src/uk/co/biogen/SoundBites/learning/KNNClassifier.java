package uk.co.biogen.SoundBites.learning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class KNNClassifier implements Serializable
{
  private ArrayList<ContextPointsPair> contextsAndMeans;
  
  public KNNClassifier()
  {
    contextsAndMeans = new ArrayList<ContextPointsPair>();
  }
  
  public void train(ContextPointsPair[] cdps, boolean replace)
  {
    if(replace)
      contextsAndMeans.clear();
    
    outer:
      // for each cdp to be added
      for(ContextPointsPair cdp : cdps)
      {
        // check the ones already known
        for(ContextPointsPair cpp : contextsAndMeans)
        {
          if(cpp.contextName.equals(cdp.contextName))
          {
            double[][] combinedPoints = new double[cpp.points.length + cdp.points.length][];
            System.arraycopy(cpp.points, 0, combinedPoints, 0, cpp.points.length);
            System.arraycopy(cdp.points, 0, combinedPoints, cpp.points.length, cdp.points.length);
            
            cpp.points = combinedPoints;
            continue outer;
          }
        }
        
        contextsAndMeans.add(cdp);
      }
    
    for(ContextPointsPair c : contextsAndMeans)
    {
      Log.d("train", "Context " + c.contextName + ": " + c.points.length + " windows");
    }
  }
  
  /*
   * @param queryPoint  The query point.
   * @param k           The number of neighbours to consider. Chu et al. used 1.
   */
  public String query(double[] queryPoint, int k)
  {
    ContextDistancePair[] nearestContexts = new ContextDistancePair[k];
    for(int i=0;i<nearestContexts.length;i++)
      nearestContexts[i] = new ContextDistancePair("", Double.MAX_VALUE);
    
    // find the nearest k neighbours

    // iterate over each context's points
      // for each point recorded for that context,
        // see if there is a point in nearestContexts for which the distance
        // is greater, and if there is, replace it
    for(ContextPointsPair cpp : contextsAndMeans)
      for(double[] point : cpp.points)
        for(int i=0;i<nearestContexts.length;i++)
        {
          double pointDistance = euclideanDistance(point, queryPoint);
          
          if(pointDistance < nearestContexts[i].distance)
          {
            Log.d("attn", "Context " + cpp.contextName + " got distance " + pointDistance);
            nearestContexts[i] = new ContextDistancePair(cpp.contextName, pointDistance);
          }
        }
    
    // return the (or one of them, if there is more than one) modal context
    
    // construct occurrence map
    HashMap<String, Integer> contextCounts = new HashMap<String, Integer>();
    for(ContextDistancePair cdp : nearestContexts)
    {
      if(contextCounts.containsKey(cdp.contextName))
      {
        contextCounts.put(cdp.contextName, contextCounts.get(cdp.contextName) + 1);
      } else
      {
        contextCounts.put(cdp.contextName, 1);
      }
    }
    
    // find modal name
    int highest = 0;
    String highestOccuringContext = "";
    for(String c : contextCounts.keySet())
      if(contextCounts.get(c) > highest)
      {
        highest = contextCounts.get(c);
        highestOccuringContext = c;
      }
    
    return highestOccuringContext;
  }
  
  private double euclideanDistance(double[] a, double[] b)
  {
    double acc = 0.0;
    for(int i=0;i<a.length && i<b.length;i++)
      acc += (a[i]-b[i]) * (a[i]-b[i]);
    
    return Math.sqrt(acc);
  }
  
  /*
   * The field 'points' should be indexed first on metric, then on value;
   * i.e., points[0] will contain all of the zero cross rate values.
   */
  public static class ContextPointsPair
  {
    public String contextName;
    // an array of sets of co-ordinates
    public double[][] points;
    
    public ContextPointsPair() {}
    public ContextPointsPair(String contextName, double[][] points)
    {
      this.contextName = contextName;
      this.points = points;
    }
  }
  
  private static class ContextDistancePair
  {
    public String contextName;
    public double distance;
    
    public ContextDistancePair(String contextName, double distance)
    {
      this.contextName = contextName;
      this.distance = distance;
    }
  }
}
