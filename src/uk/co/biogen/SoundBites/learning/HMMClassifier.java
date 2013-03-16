package uk.co.biogen.SoundBites.learning;

import java.util.ArrayList;
import java.util.Arrays;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;

public class HMMClassifier
{
  /*
   * Builds a fresh 'full' HMM with even probability pis, bs and as.
   * 
   * @param numStates             The number of internal states of this HMM.
   * @param observationSpaceSize  The number of tokens that will be observed
   *   by this HMM. For binary discrimination of n metrics, this will be 2^n.
   */
  public static <OType extends Enum<OType>> Hmm<ObservationInteger>
  buildInitialHmm(
      int numStates,
      int observationSpaceSize)
  {
    // base init
    Hmm<ObservationInteger> hmm =
      new Hmm<ObservationInteger>(numStates,
        new OpdfIntegerFactory(observationSpaceSize));
    
    // init states
    for(int i=0;i<numStates;i++)
    {
      hmm.setPi(i, (double) 1 / (double) numStates);
    }
    
    // init state pdfs (Opdfs)
    for(int i=0;i<numStates;i++)
    {
      double[] pdfValues = new double[observationSpaceSize];
      Arrays.fill(pdfValues, (double) 1 / (double) observationSpaceSize);
      hmm.setOpdf(i, new OpdfInteger(pdfValues));
    }

    for(int i=0;i<numStates;i++)
      for(int j=0;j<numStates;j++)
        hmm.setAij(i, j, (double) 1 / (double) numStates);
    
    return hmm;
  }
  
  /*
   * @param os  A list (sequence) of lists of metrics.
   * 
   * Performs n iterations of Baum-Welch learning with HMM hmm using the
   * observation sequence os.
   */
  public static <OType extends Enum<OType>> void
    trainBW(
      Hmm<ObservationInteger> hmm,
      ArrayList<ArrayList<ObservationInteger>> os,
      int n)
  {
    BaumWelchLearner bwl = new BaumWelchLearner();
    bwl.setNbIterations(n);
    bwl.learn(hmm, os);
  }
  
  public static double query(Hmm<ObservationInteger> hmm, ArrayList<ObservationInteger> os)
  {
    return hmm.probability(os);
  }
  
  // ObservationInteger implementation.
  /*
   * @param metricsSequence  The series of metrics. The maximum number of
   *   elements of each metric group is 31 (log base 2 of the value range
   *   that jahmm generates internally, evidenced in OpdfInteger.java). 
   * @param splitPoint       The point at which to make the HIGH/LOW token
   *   split, for each metric.
   * 
   * Function name stands for "observationSequenceDiscretizedToIntegers".
   * 
   * Generates a series of binary observations into HIGH/LOW tokens, ready to
   * train an HMM with, from an array of arrays of metric values.
   * 
   * Conversion is done according to a notional binary scheme for HIGH = 1 and
   * LOW = 0, with the first value in the input double array corresponding to
   * the least significant bit; e.g.:
   * 
   *   the metric double[]:
   *     {1.5,  7.3,  24.2, 197.2, 682.011}
   *   That converts to (say):
   *     {HIGH, HIGH, LOW,  LOW,   HIGH   }
   *   Translates to the binary number:
   *     10011
   *   This produces the denary integer (the observation to be included in the
   *   output:
   *     19
   */
  public static ArrayList<ObservationInteger> observationSequenceD2I(double[][] metricsSequence, double[] splitPoint)
  {
    ArrayList<ObservationInteger> ret = new ArrayList<ObservationInteger>(metricsSequence.length);
    
    // per metric group in the sequence
    for(int i=0;i<metricsSequence.length;i++)
    {
      int observationValue = 0;
      
      // generate a corresponding observation integer group for each metric group
        // if current value HIGH, add denary
        // else, add nothing to the integer
      for(int j=0;j<metricsSequence[i].length;j++)
        if(metricsSequence[i][j] > splitPoint[j])
          observationValue += (int) Math.pow(2,j);
      
      ret.add(i, new ObservationInteger(observationValue));
    }
    
    return ret;
  }
}
