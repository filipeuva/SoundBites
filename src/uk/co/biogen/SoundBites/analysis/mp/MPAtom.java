package uk.co.biogen.SoundBites.analysis.mp;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MPAtom implements Serializable
{
  // all parameters discrete; either defined as mantissas or sample offsets
  
// only applicable for one size of window
  private int windowSize;
//# [s] 'scale' - power of two
//#
//# Inverse. Normalises to 1 at s = 1
// what we actually have here is the power value of 2 that produces the required s  
  private int sPower;

//# [u] 'time'
//#
//# lateral shift (samples; must be <=(windowSize - 1))
  private int u;
  
//# [omega] 'frequency' i^2.6, 1 <= i <= 35
//#
//# What it says on the tin
// what we actually have here is the i used to calculate the omega
  private int omegaIValue;

//# [theta] 'phase'
//#
//# Phase, inverse from 0 at pi
  private double theta;
  
  private int[] waveform;


  public MPAtom(int windowSize, int s, int u, int omegaIValue, double theta)
  {
    this.windowSize = windowSize;
    this.sPower = s;
    this.u = u;
    this.omegaIValue = omegaIValue;
    this.theta = theta;
    waveform = null;
  }

  public int getS()
  {
    return sPower;
  }

  public void setS(int s)
  {
    this.sPower = s;
  }

  public int getU()
  {
    return u % windowSize;
  }

  public void setU(int u)
  {
    this.u = u;
  }

  public double getOmega()
  {
    return omegaIValue;
  }

  public void setOmega(int omega)
  {
    this.omegaIValue = omega;
  }

  public double getTheta()
  {
    return theta;
  }

  public void setTheta(double theta)
  {
    this.theta = theta;
  }
  
  // truncates at the end of waveform (as opposed to wrapping)
  // generation is assuming sample rate of 44.1 kHz and concordantly scaling
  // to make the highest frequency 22.050 kHz (Nyquist). We are assuming a
  // fixed span from 1-35 for omega i values; as such, the 'proportional'
  // frequency measure is i/35.
  
  // Essentially the method used in [1]. MP_SAMPLES_TO_SECS translates the 
  // sample index i from the sample range to the second range. MP_OMEGA_MODIFIER
  // upscales omega to make the lowest encoded frequency MPParamCalc.LOWEST_FREQUENCY.
  
  // Provides a value max 1; as such, we normalise with Short.MAX_VALUE; this
  // is because this waveform will be subtracted from sample-short space
  // microphone input data. Annoyingly, this produces a y-offset of 1 (most
  // negative short is -(Short.MAX_VALUE + 1)). 
  public int[] getWaveform()
  {
    if(waveform == null)
    {
      waveform = new int[windowSize];
      double offsetXCosDomain = 0;
      final double s = Math.pow(2,sPower);
      final double sqrtS = Math.sqrt(Math.pow(2,sPower));
      final double actualOmega = Math.pow(omegaIValue, MPParamCalc.omegaPower);
      
      for(int x=0;x<windowSize;x++)
      {
        offsetXCosDomain = (x - u) * MPParamCalc.MP_SAMPLES_TO_COSDOMAIN;
        waveform[x] =
            (int) (
                
              // short scaling *
              // Gaussian envelope *
              // cosine
              Short.MAX_VALUE *  // Short.MAX_VALUE is used since this calculation is being made for sample-short space
              (Math.exp(- Math.PI * Math.pow(offsetXCosDomain / s,2)) / sqrtS) *
              Math.cos(MPParamCalc.LOWEST_FREQUENCY * actualOmega * offsetXCosDomain + theta)
              
            );
      }
    }
    
    return waveform;
  }
  
  @Override
  public String toString()
  {
    return "windowSize: " + Integer.toString(windowSize) +
        ", sPower: " + sPower +
        ", u: " + u +
        ", omegaIValue: " + omegaIValue +
        ", theta: " + theta ;
  }
  
  // References:
  // -
  // [1]: ENVIRONMENTAL SOUND RECOGNITION USING MP-BASED FEATURES [Chu et al.]
}
