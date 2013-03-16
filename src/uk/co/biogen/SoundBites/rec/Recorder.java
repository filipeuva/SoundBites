package uk.co.biogen.SoundBites.rec;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class Recorder
{
  /**
   * INITIALIZING : recorder is initializing;
   * READY : recorder has been initialized, recorder not yet started
   * RECORDING : recording
   * ERROR : reconstruction needed
   * STOPPED: reset needed
   * 
   * Note that this recorder writes only the raw PCM data without any sort of header, let alone WAV.
   */
  public enum State {READY, RECORDING, ERROR, STOPPED};
  
  private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.DEFAULT;
  private static final int DEFAULT_SAMPLE_RATE_IN_HZ = 44100;
  private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
  private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  
  // The interval in which the recorded samples are output to the file
  // Used only in uncompressed mode
  private static final int TIMER_INTERVAL = 120;
  
  private AudioRecord    aRecorder = null;
  private boolean        recorderActive;
  
  // Stores current amplitude (only in uncompressed mode)
  private int        cAmplitude= 0;
  
  // Recorder state; see State
  private State      state;
  
  // File writer (only in uncompressed mode)
  private RandomAccessFile fWriter;
  
  // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat), channel configuration (1/2)
  private short      nChannels;
  private int        sRate;
  private short      bSamples;
  private int        bufferSize;
  private int        aSource;
  private int        aFormat;
  private int        cConfig;
  
  // Number of frames written to file on each output(only in uncompressed mode)
  private int        framePeriod;
    
  // Buffer for output(only in uncompressed mode)
  private byte[]       buffer;
  
  // Number of bytes written to file after header(only in uncompressed mode)
  @SuppressWarnings("unused")
  private int        payloadSize;
  
  /**
   * 
   * Returns the state of the recorder in a RehearsalAudioRecord.State typed object.
   * Useful, as no exceptions are thrown.
   * 
   * @return recorder state
   */
  public State getState()
  {
    return state;
  }
  
  /*
   * 
   * Method used for recording.
   * 
   */
  private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener()
  {
    public void onPeriodicNotification(AudioRecord recorder)
    {
      if(recorderActive)
      {
        aRecorder.read(buffer, 0, buffer.length); // Fill buffer
        try
        {
          fWriter.write(buffer); // Write buffer to file
          payloadSize += buffer.length;
          if (bSamples == 16)
          {
            for (int i=0; i<buffer.length/2; i++)
            { // 16bit sample size
              short curSample = getShort(buffer[i*2], buffer[i*2+1]);
              if (curSample > cAmplitude)
              { // Check amplitude
                cAmplitude = curSample;
              }
            }
          }
          else
          { // 8bit sample size
            for (int i=0; i<buffer.length; i++)
            {
              if (buffer[i] > cAmplitude)
              { // Check amplitude
                cAmplitude = buffer[i];
              }
            }
          }
        }
        catch (IOException e)
        {
          e.printStackTrace();
          stop();
        }
      }
    }
    
    public void onMarkerReached(AudioRecord recorder)
    {
      // NOT USED
    }
  };
  
  public Recorder()
  {
    initialise(
        DEFAULT_SOURCE,
        DEFAULT_SAMPLE_RATE_IN_HZ,
        DEFAULT_CHANNEL_CONFIG,
        DEFAULT_AUDIO_FORMAT);
  }
  
  public Recorder(int source)
  {
    initialise(
        source,
        DEFAULT_SAMPLE_RATE_IN_HZ,
        DEFAULT_CHANNEL_CONFIG,
        DEFAULT_AUDIO_FORMAT);
  }
  
  /**
   * 
   * Instantiates a new recorder, in case of compressed recording the parameters can be left as 0.
   * In case of errors, no exception is thrown, but the state is set to ERROR
   * 
   */ 
  public Recorder(
      int audioSource,
      int sampleRate,
      int channelConfig,
      int audioFormat)
  {
    initialise(
        audioSource,
        sampleRate,
        channelConfig,
        audioFormat);
  }
  
  private void initialise(
      int audioSource,
      int sampleRate,
      int channelConfig,
      int audioFormat)
  {
    if (audioFormat == AudioFormat.ENCODING_PCM_16BIT)
    {
      bSamples = 16;
    }
    else
    {
      bSamples = 8;
    }
    
    if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO)
    {
      nChannels = 1;
    }
    else
    {
      nChannels = 2;
    }
    
    cConfig = channelConfig;
    aSource = audioSource;
    sRate   = sampleRate;
    aFormat = audioFormat;
  }
  
  /**
   * Sets output file path, call directly after construction/reset.
   *  
   * @param output file path
   * 
   */
  public void setOutputFile(AudioFile af)
  {
    fWriter = af.getFile();
  }
  
  /**
   * 
   * Returns the largest amplitude sampled since the last call to this method.
   * 
   * @return returns the largest amplitude since the last call, or 0 when not in recording state. 
   * 
   */
  public int getMaxAmplitude()
  {
    if (state == State.RECORDING)
    {
      int result = cAmplitude;
      cAmplitude = 0;
      return result;
    } else
    {
      return 0;
    }
  }
  

  /**
   * 
  * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
  * the recorder is set to the ERROR state, which makes a reconstruction necessary.
  * In case uncompressed recording is toggled, the header of the wave file is written.
  * In case of an exception, the state is changed to ERROR
  *    
  */
  public void prepareForFile()
  {
    framePeriod = sRate * TIMER_INTERVAL / 1000;
    bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
    if (bufferSize < AudioRecord.getMinBufferSize(sRate, cConfig, aFormat))
    { // Check to make sure buffer size is not smaller than the smallest allowed one 
      bufferSize = AudioRecord.getMinBufferSize(sRate, cConfig, aFormat);
      // Set frame period and timer interval accordingly
      framePeriod = bufferSize / ( 2 * bSamples * nChannels / 8 );
    }
    
    aRecorder = new AudioRecord(aSource, sRate, cConfig, aFormat, bufferSize);
    aRecorder.setRecordPositionUpdateListener(updateListener);
      aRecorder.setPositionNotificationPeriod(framePeriod);

    cAmplitude = 0;
    
    try
    {
      if (aRecorder.getState() == AudioRecord.STATE_INITIALIZED)
      {
        // write file header
        
        // Ignoring header data for now.
        
//        fWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
//        fWriter.writeBytes("RIFF");
//        fWriter.writeInt(0); // Final file size not known yet, write 0 
//        fWriter.writeBytes("WAVE");
//        fWriter.writeBytes("fmt ");
//        fWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
//        fWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
//        fWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
//        fWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
//        fWriter.writeInt(Integer.reverseBytes(sRate*bSamples*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
//        fWriter.writeShort(Short.reverseBytes((short)(nChannels*bSamples/8))); // Block align, NumberOfChannels*BitsPerSample/8
//        fWriter.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
//        fWriter.writeBytes("data");
//        fWriter.writeInt(0); // Data chunk size not known yet, write 0
        
        buffer = new byte[framePeriod*bSamples/8*nChannels];
        state = State.READY;
      } else
      {
        System.err.println("prepareFile() method called on uninitialized recorder");
        state = State.ERROR;
      }
    } catch(Exception e)
    {
      e.printStackTrace();
      state = State.ERROR;
    }
  }
  
  /**
   * 
   * 
   *  Releases the resources associated with this class, and removes the unnecessary files, when necessary
   *  
   */
  public void release()
  {
    if (state == State.READY)
    {
      try
      {
        fWriter.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    
    if (aRecorder != null)
    {
      aRecorder.release();
    }
  }
  
  /**
   * 
   * 
   * Starts the recording, and sets the state to RECORDING.
   * Call after prepare().
   * 
   */
  public void start()
  {
    if (state == State.READY)
    {
      payloadSize = 0;
      aRecorder.startRecording();
      recorderActive = true;
      aRecorder.read(buffer, 0, buffer.length);
      
      state = State.RECORDING;
    } else
    {
      System.err.println("start() called on illegal state");
      state = State.ERROR;
    }
  }
  
  /**
   * 
   * 
   *  Stops the recording, and sets the state to STOPPED.
   * In case of further usage, a reset is needed.
   * Also finalizes the wave file in case of uncompressed recording.
   * 
   */
  public void stop()
  {
    if (state == State.RECORDING)
    {
      recorderActive = false;
      aRecorder.stop();
      
      try
      {
        // WARN
        
//        fWriter.seek(4); // Write size to RIFF header
//        fWriter.writeInt(Integer.reverseBytes(36+payloadSize));
//      
//        fWriter.seek(40); // Write size to Subchunk2Size field
//        fWriter.writeInt(Integer.reverseBytes(payloadSize));
      
        fWriter.close();
      }
      catch(IOException e)
      {
        e.printStackTrace();
        state = State.ERROR;
      }
      
      state = State.STOPPED;
    }
    else
    {
      System.err.println("stop() called on illegal state");
      state = State.ERROR;
    }
  }
  
  /* 
   * 
   * Converts a byte[2] to a short, in LITTLE_ENDIAN format
   * 
   */
  private short getShort(byte argB1, byte argB2)
  {
    return (short)(argB1 | (argB2 << 8));
  }
}
