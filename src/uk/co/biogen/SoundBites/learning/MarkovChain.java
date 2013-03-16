package uk.co.biogen.SoundBites.learning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MarkovChain implements Serializable
{
  // name -> node object
  private HashMap<String, MCNode> contexts;
  private String currentNode;
  
  public MarkovChain()
  {
    initChain();
  }

  /*
   * Call to take the chain to the current node and effect all side-effects.
   * Probability calculation and stepping around the chain are divorced in
   * this implementation; call transitionProbability(c) to get the probability
   * for some next context c from the current context node.
   */
  public void nextContext(String context)
  {
    if(!contexts.containsKey(context))
      contexts.put(context, new MCNode());
    
    if(currentNode != null)
      contexts.get(currentNode).incrementCountFor(context);
    
    currentNode = context;
  }
  
  public void train(ArrayList<String> contextSequence, boolean replace)
  {
    if(replace)
      initChain();
    
    for(int i=0;i<contextSequence.size();i++)
      nextContext(contextSequence.get(i));
  }
  
  private void initChain()
  {
    contexts = new HashMap<String, MCNode>();
    currentNode = null;
  }
  
  /*
   * Probability of transition from the current context to the query context.
   */
  public double transitionProbability(String context)
  {
    return contexts.get(currentNode).getTransitionProbability(context);
  }
  
  private static class MCNode
  {
    // total number of times this context has been navigated from
    // to another context
    private int totalOutwardCount;
    // map of context names to the number of times this context
    // has been navigated from to that context
    private HashMap<String, Integer> outwardCounts;
    
    public MCNode()
    {
      totalOutwardCount = 0;
      outwardCounts = new HashMap<String, Integer>();
    }
    
    public void incrementCountFor(String context)
    {
      totalOutwardCount++;
      
      if(outwardCounts.containsKey(context))
        outwardCounts.put(context, outwardCounts.get(context) + 1);
      else
        outwardCounts.put(context, 1);
    }
    
    public double getTransitionProbability(String context)
    {
      if(outwardCounts.containsKey(context))
        return (double) outwardCounts.get(context) / (double) totalOutwardCount;
      else
        return 0.0;
    }
  }
}
