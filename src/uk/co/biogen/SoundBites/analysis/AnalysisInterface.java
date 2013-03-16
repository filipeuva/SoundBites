package uk.co.biogen.SoundBites.analysis;

import java.util.Arrays;

import uk.co.biogen.SoundBites.analysis.comirva.FFT;
import uk.co.biogen.SoundBites.analysis.comirva.MFCC;
import uk.co.biogen.SoundBites.analysis.jAudio.JAudioFFT;
import uk.co.biogen.SoundBites.analysis.jAudio.LPC;
import uk.co.biogen.SoundBites.analysis.jAudio.RMS;
import uk.co.biogen.SoundBites.analysis.jAudio.SpectralCentroid;
import uk.co.biogen.SoundBites.analysis.jAudio.SpectralFlux;
import uk.co.biogen.SoundBites.analysis.jAudio.SpectralRolloffPoint;
import de.dfki.lt.signalproc.analysis.LPCAnalyser;
import de.dfki.lt.signalproc.analysis.LPCAnalyser.LPCoeffs;
import de.dfki.lt.signalproc.util.DoubleDataSource;

public class AnalysisInterface
{
  // Methods for calculating the metrics required for this application:
  //   - Some less sophisticated metrics (my own, and subpackage jAudio)
  //   - MFCC (subpackage comirva)
  //   - MP (subpackage mp)
  //
  // The "less sophisticated metrics" are a selection of simple metrics as
  // given widely in the literature. No particular prescriptions are given for
  // each one's general suitability for the type of audio they would best
  // classify; as such, this is a slightly speculative list, and it will be
  // useful to the community to test their applicability to environmental
  // audio monitoring.
  
  // Chu et al. used the following features:
  // "MFCC (12), [Delta]MFCC (12), LPC (12), [Delta]LPC (12),
  // LPCC(12), band energy ratio, frequency roll-off set at 95%, spectral
  // centroid, spectral bandwidth, spectral asymmetry, spectral flatness,
  // zero-crossing, and energy."
  // All of these are accounted for here. Name differences:
  //   - Energy -> RMS()
  //   - Frequency roll-off -> spectralRolloff()
  //   - Spectral asymmetry -> spectralVariability()
  
  // It is important to note that, since all the recordings are being made
  // at the same volume (fixed microphone), no normalisation methods are needed.
  
  // Sourced from:
  // Content Analysis for Audio Classification and Segmentation [Lu et al.]
  // Adaptive Audio-Based Context Recognition [Dargie]
  // Audio-Based Context Recognition [Eronen et al.]

  public static final int WINDOW_SIZE_SAMPLES = 4096;
  public static final int SAMPLE_RATE = 44100;
  
  /*
   * jAudio
   * A standard measure for the volume of the audio signal.
   */
  public static double RMS(short[] window)
  {
    // instantiates a new RMS() and returns the first (and only) value in the
    // double[] generated by extractFeature()
    
    return (new RMS()).extractFeature(doublesFromShorts(window), 0.0, null)[0];
  }
  
  /*
   * @author wab108
   * 
   * jAudio does have this, but I did it myself before I realised it did.
   */
  public static int zeroCrossRate(short[] window)
  {
    int acc = 0;
    short lastShort = 0;
    
    for(short s : window)
    {
      if(lastShort == 0 && s != 0)
      {
        acc++;
      } else if(lastShort < 0 && s > 0)
      {
        acc++;
      } else if(lastShort > 0 && s < 0)
      {
        acc++;
      }
      
      lastShort = s;
    }
    
    return acc;
  }
  
  /*
   * jAudio
   * 
   * @throws Throws an exception if coefficients is <1.
   * 
   * @param window        The audio window.
   * @param coefficients  The number of LPC coefficients to calculate. The
   *   number used by Chu et al. was 12.
   * @param lambda        Effectively, the amount of Bark scaling to apply to
   *   the spectrum. 0 indicates no scaling.
   */
  public static double[] lpc(short[] window, int coefficients, int lambda) throws Exception
  {
    LPC lpc = new LPC();
    
    lpc.setNumDimensions(coefficients);
    lpc.setLambda(lambda);
    
    return lpc.extractFeature(doublesFromShorts(window), 0.0, null); // args 2 and 3 are ignored
  }
  
  /*
   * jAudio
   */
  public static double[] lpcDelta(short[] window, int coefficients, int lambda) throws Exception
  {
    short[][] splitWindow = splitWindow(window);
    
    LPC lpc = new LPC();
    
    lpc.setNumDimensions(coefficients);
    lpc.setLambda(lambda);
    
    double[] lpcs1 = lpc.extractFeature(doublesFromShorts(splitWindow[0]), 0.0, null); // args 2 and 3 are ignored
    double[] lpcs2 = lpc.extractFeature(doublesFromShorts(splitWindow[1]), 0.0, null);
    
    double[] ret = new double[coefficients];
    for(int i=0;i<ret.length;i++)
    {
      ret[i] = lpcs2[i] - lpcs1[i];
    }
    
    return ret;
  }
  
  /*
   * DFKI LT
   * 
   * From:
   * http://elckerlyc.ewi.utwente.nl/browser/Elckerlyc/Shared/repository/MARYTTS/java/marytts/signalproc/window/Window.java
   * it appears that a Hann (Hanning [sic]) window is '3'. The doc is minimal
   * and does not specify, but I assume that the window is applied before the
   * coefficients are calculated, since a constructor exists to specify one and
   * no method exists to trigger the application of such.
   */
  public static double[] lpcc(final short[] window, int coefficients)
  {
    DoubleDataSource windowAsDDS = new DoubleDataSource()
    {
      int ptr = 0;
      
      @Override
      public boolean hasMoreData()
      {
        return ptr<=window.length;
      }
      
      @Override
      public long getDataLength()
      {
        return window.length;
      }
      
      @Override
      public int getData(double[] arg0, int arg1, int arg2)
      {
        arg0 = Arrays.copyOfRange(doublesFromShorts(window), arg1, arg2);
        return arg0.length;
      }
      
      @Override
      public int getData(double[] arg0)
      {
        ptr += arg0.length;
        arg0 = Arrays.copyOfRange(doublesFromShorts(window), ptr, ptr+arg0.length);
        return arg0.length;
      }
      
      @Override
      public double[] getData(int arg0)
      {
        ptr += arg0;
        return Arrays.copyOfRange(doublesFromShorts(window), ptr, ptr+arg0);
      }
      
      @Override
      public double[] getAllData()
      {
        return doublesFromShorts(window);
      }
      
      @Override
      public int available()
      {
        return window.length;
      }
    };
    
    LPCAnalyser lpca = new LPCAnalyser(windowAsDDS, window.length, 0, SAMPLE_RATE, coefficients, 3); // 3: Hann window used
    LPCoeffs lpcoeffs = (LPCoeffs) lpca.analyse(doublesFromShorts(window));
    
    /*
     * Internal documentation from:
     *   http://elckerlyc.ewi.utwente.nl/browser/Elckerlyc/Shared/repository/MARYTTS/java/marytts/signalproc/analysis/LpcAnalyser.java?rev=2
     *   
     * indicates that getA() returns the true LP coefficients. 
     */ 
    return lpcoeffs.getA();
    
//    LPCoeffs lpCoefficients = LPCAnalyser.calcLPC(doublesFromShorts(window), coefficients);
//   
//    return LPCCepstrum.lpc2lpcc(lpCoefficients.getA(),lpCoefficients.getGain(),lpCoefficients.getOrder());
  }
  
  /*
   * @author wab108
   * 
   * @param window           The audio window.
   * @param separationPoint  The point at which the ratio for above and below
   *                           is calculated; e.g. separationPoint = 0.5 
   *                           calculates the ratio of energy above and below
   *                           the halfway point on the spectrum.
   * 
   * From Spectral and Textural Feature-Based System for Automatic Detection
   * of Fricatives and Affricates [Ruinsky et al.]. Gives the proportion of
   * energy in high bands versus low bands (this is used in speech recognition
   * to distinguish fricatives and affricates from lower-voiced phonemes).
   */
  public static double bandEnergyRatio(short[] window, double separationPoint)
  {
    double[] powSpectrum = null;
    try
    {
      powSpectrum = (new JAudioFFT(doublesFromShorts(window), null, false, true)).getPowerSpectrum();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    int binLimit = (int) (powSpectrum.length * separationPoint);
    double belowSum = 0.0, aboveSum = 0.0;
    
    for(int i=0;i<binLimit;i++)
      belowSum += powSpectrum[i];
    
    for(int i=binLimit;i<powSpectrum.length;i++)
      aboveSum += powSpectrum[i];
    
    return aboveSum / belowSum;
  }
  
  /*
   * @author wab108
   * 
   * Uses jAudio's power spectrum. The spectral flatness measure is:
   * 
   * geometricMean(powerSpectrum)/arithmeticMean(powerSpectrum)
   * 
   * As given by Transform coding of audio signals using perceptual noise
   * criteria [Johnston].
   */
  public static double spectralFlatness(short[] window)
  {
    double[] ps = null;
    
    try
    {
      ps = (new JAudioFFT(doublesFromShorts(window), null, false, true)).getPowerSpectrum();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    
    double geometricMean = 1.0, arithmeticMean = 0.0;
    for(int i=0;i<ps.length;i++)
    {
      geometricMean *= ps[i];
      arithmeticMean += ps[i];
    }
    geometricMean = Math.pow(geometricMean, 1 / WINDOW_SIZE_SAMPLES);
    arithmeticMean /= WINDOW_SIZE_SAMPLES;
    
    return geometricMean/arithmeticMean;
  }
  
  /*
   * jAudio
   */
  public static double spectralRolloff(short[] window)
  {
    double[][] ps = new double[1][WINDOW_SIZE_SAMPLES];
    
    try
    {
      ps[0] = (new JAudioFFT(doublesFromShorts(window), null, false, true)).getPowerSpectrum();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    
    return (new SpectralRolloffPoint()).extractFeature(null, 0.0, ps)[0];
  }
  
  /*
   * Comirva
   * 
   * @return The distance between the highest and the lowest bands with signal
   * in them; i.e. the index of the highest above-threshold analysis band
   * minus the index of the lowest above-threshold analysis band. For a signal
   * with none or only one of these, the value is 0; for two adjacent ones, it
   * is 1.
   * 
   * The magnitude transform is used, as opposed to the normalised power
   * transform for the MFCC calculations. The magnitude transform is the
   * 'standard' FFT - note that the power transform provides the same
   * coefficients, but squared.
   * 
   * Once again, the "Hanning" window is actually the Hann window.
   */
  private static double SB_NOISE_THRESHOLD = 10.0;
  
  public static int spectralBandwidth(short[] window)
  {
    double[] doublesFromShorts = doublesFromShorts(window);
    
    FFT magnitudeFFT = new FFT(FFT.FFT_MAGNITUDE, WINDOW_SIZE_SAMPLES, FFT.WND_HANNING);
    magnitudeFFT.transform(doublesFromShorts, null);
    
    int lowest = 0, highest = 0;
    boolean lowWasSet = false;
    
    for(int i=0;i<doublesFromShorts.length;i++)
    {
      if(doublesFromShorts[i]>SB_NOISE_THRESHOLD)
      {
        if(!lowWasSet)
        {
          lowest = i;
          lowWasSet = true;
        }
        highest = i;
      }
    }
    
    return highest - lowest;
  }
  
  /*
   * jAudio
   * 
   * Splits the window in two and calculates the flux from the first to the
   * second.
   */
  public static double spectralFlux(short[] window)
  {
    double[][] mses;
    mses = splitWindow(doublesFromShorts(window));
    
    return (new SpectralFlux()).extractFeature(null, 0.0, mses)[0];
  }
  
  public static double spectralCentroid(short[] window)
  {
    double[][] ps = new double[1][WINDOW_SIZE_SAMPLES];
    
    try
    {
      ps[0] = (new JAudioFFT(doublesFromShorts(window), null, false, true)).getPowerSpectrum();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    
    return (new SpectralCentroid()).extractFeature(null, 0.0, ps)[0];
  }
  
//  public static double mean(short[] window)
//  {
//    return 0.0;
//  }
  
  /*
   * MFCC calculator
   */
  
  private static final float MFCC_SAMPLERATE = SAMPLE_RATE; // unlikely to change
  private static final int MFCC_WINDOWSIZE = WINDOW_SIZE_SAMPLES; // should probably correspond with what's being used for MP
  private static final int MFCC_NUMBERCOEFFICIENTS = 13; // liable to change. Discarded first coefficient = 12 total.
  private static final boolean MFCC_USEFIRSTCOEFFICIENT = false; // it's generally a very large value; it measures a global offset (DC) effect
  private static final double MFCC_MINFREQ = 20; //  
  private static final double MFCC_MAXFREQ = 2000; // 20-2000 is probably a sensible range here in light of the fact I'm trying to profile the lower band
  private static final int MFCC_NUMBERFILTERS = 40; // Explanation of the significance at http://dsp.stackexchange.com/a/362
  
  // uses Hann window, by default
  private static MFCC mfcc = new MFCC(
    MFCC_SAMPLERATE,
    MFCC_WINDOWSIZE,
    MFCC_NUMBERCOEFFICIENTS,
    MFCC_USEFIRSTCOEFFICIENT,
    MFCC_MINFREQ,
    MFCC_MAXFREQ,
    MFCC_NUMBERFILTERS
    );
  
  private static MFCC mfccHalfWindowSize = new MFCC(
    MFCC_SAMPLERATE,
    MFCC_WINDOWSIZE / 2,
    MFCC_NUMBERCOEFFICIENTS,
    MFCC_USEFIRSTCOEFFICIENT,
    MFCC_MINFREQ,
    MFCC_MAXFREQ,
    MFCC_NUMBERFILTERS
    );

  /*
   * Comirva
   */
  public static double[] mfccs(short[] window)
  {
    double[] ret = null;
    
    try
    {
      ret = mfcc.processWindow(doublesFromShorts(window),0);
    } catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    
    return ret;
  }
  
  /*
   * Comirva
   * 
   * Splits the window in two and calculates the differences in MFCCs from the
   * first half to the second. Note that this measures a similar audio aspect
   * to spectral flux - the differences are:
   *   - values are not collected - MFCCs are kept separate 
   *   - a shift to the mel spectrum is made for MFCC deltas, magnifying the
   *       frequency spectrum approximately according to the response of the
   *       human ear
   */
  public static double[] mfccsDeltas(short[] window)
  {
    short[][] splitWindow = splitWindow(window);
    
    double[] mfccs1 = null;
    double[] mfccs2 = null;
    double[] ret = null;
    if(MFCC_USEFIRSTCOEFFICIENT)
    {
      ret = new double[MFCC_NUMBERCOEFFICIENTS];
    } else
    {
      ret = new double[MFCC_NUMBERCOEFFICIENTS - 1];
    }
    
    try
    {
      mfccs1 = mfccHalfWindowSize.processWindow(doublesFromShorts(splitWindow[0]),0);
      mfccs2 = mfccHalfWindowSize.processWindow(doublesFromShorts(splitWindow[1]),0);
    } catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }

    for(int i=0;i<mfccs1.length;i++)
    {
      ret[i] = mfccs2[i] - mfccs1[i];
    }
    
    return ret;
  }

  // short[]s are used for for inter-process transfer of audio
  // data in this project
  public static double[] doublesFromShorts(short[] in)
  {
    double[] ret = new double[in.length];
    
    for(int i=0;i<in.length;i++)
    {
      ret[i] = (double) in[i];
    }
    
    return ret;
  }
  
  /*
   * @author wab108
   * 
   * @throws IllegalArgumentException, if the window does not contain an even
   *   number of samples and is hence not cleanly splittable.
   *   
   * Splits the window down the middle. Returns the first half of the samples
   *   in short[0] and the second half in short[1]. The array does not contain
   *   anything else.
   */
  private static short[][] splitWindow(short[] window) throws IllegalArgumentException
  {
    if(window.length%2 != 0)
    {
      throw new IllegalArgumentException("Number of samples in window is not even.");
    }
    
    short[][] ret = new short[2][window.length/2];
    
    ret[0] = Arrays.copyOfRange(window, 0, window.length/2);
    ret[1] = Arrays.copyOfRange(window, window.length/2, window.length);
    
    return ret;
  }
  
  /*
   * Java does not allow arrays of generics:
   */
  private static double[][] splitWindow(double[] window) throws IllegalArgumentException
  {
    if(window.length%2 != 0)
    {
      throw new IllegalArgumentException("Number of samples in window is not even.");
    }
    
    double[][] ret = new double[2][window.length/2];
    
    ret[0] = Arrays.copyOfRange(window, 0, window.length/2);
    ret[1] = Arrays.copyOfRange(window, window.length/2, window.length);
    
    return ret;
  }
}