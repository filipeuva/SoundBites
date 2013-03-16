package uk.co.biogen.SoundBites.rec;
//package uk.co.bio_gen.SoundBites.rec;
//
//import java.io.IOException;
//import java.io.RandomAccessFile;
//
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.util.Log;
//
//public class OldRecorder
//{
//  private static final int DEFAULT_SAMPLE_RATE_IN_HZ = 44100;
//  private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
//  private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//  
//  // components
//  private static AudioRecord audioRecord;
//  private static byte[] buffer;
//  private static AudioFile writeFile; // file to which audio buffer data will be written; set this before initiating pull process
//  private static int currentAmplitude;
//  
//  // primary variable values
//  private static int channels;
//  private static int sampleRate;
//  private static int bitsPerSample;
//  private static int bufferSize;
//  private static int audioSource;
//  
//
////  private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener()
////  {
////    public void onPeriodicNotification(AudioRecord recorder)
////    {
////      audioRecord.read(buffer, 0, buffer.length); // Fill buffer
////      try
////      {
////        writeFile.getFile().write(buffer); // Write buffer to file
////        payloadSize += buffer.length;
////        if (bitsPerSample == 16)
////        {
////          for (int i=0; i<buffer.length/2; i++)
////          { // 16bit sample size
////            short curSample = getShort(buffer[i*2], buffer[i*2+1]);
////            if (curSample > currentAmplitude)
////            { // Check amplitude
////              currentAmplitude = curSample;
////            }
////          }
////        } else
////        { // 8bit sample size
////          for (int i=0; i<buffer.length; i++)
////          {
////            if (buffer[i] > currentAmplitude)
////            { // Check amplitude
////              currentAmplitude = buffer[i];
////            }
////          }
////        }
////      }
////      catch (IOException e)
////      {
////        e.printStackTrace();
////        stop();
////      }
////    }
////    
////    public void onMarkerReached(AudioRecord recorder)
////    {
////      // NOT USED
////    }
////  };
////  
////  public Recorder(MicSelection whichMic)
////  {
////    switch(whichMic)
////    {
////      case FRONT:
////      {
////        audioRecord = new AudioRecord(
////            MediaRecorder.AudioSource.MIC,
////            DEFAULT_SAMPLE_RATE_IN_HZ,
////            DEFAULT_CHANNEL_CONFIG,
////            DEFAULT_AUDIO_FORMAT,
////            BUFFER_SIZE);
////        currentBufferSize = BUFFER_SIZE;
////      }
////      case BACK:
////      {
////        
////      }
////      case BOTH:
////      {
////        
////      }
////    }
////  }
////  
////  public Recorder(int MRMicSelection)
////  {
////    audioRecord = new AudioRecord(
////        MRMicSelection,
////        DEFAULT_SAMPLE_RATE_IN_HZ,
////        DEFAULT_CHANNEL_CONFIG,
////        DEFAULT_AUDIO_FORMAT,
////        BUFFER_SIZE);
////    currentBufferSize = BUFFER_SIZE;
////  }
////  
////  public Recorder(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes)
////  {
////    audioRecord = new AudioRecord(
////        audioSource,
////        sampleRateInHz,
////        channelConfig,
////        audioFormat,
////        bufferSizeInBytes);
////    currentBufferSize = bufferSizeInBytes;
////  }
////  
////  public void setMic(int MRMicSelection)
////  {
////    audioRecord = new AudioRecord(
////        MRMicSelection,
////        audioRecord.getSampleRate(),
////        audioRecord.getChannelConfiguration(),
////        audioRecord.getAudioFormat(),
////        currentBufferSize);
////  }
////  
////  public void setAudioRecord(int audioSource, int sampleRate)
////  {
////    int bitsPerSample = 16;
////    int channels = 1;
////    
////    if(DEFAULT_AUDIO_FORMAT == AudioFormat.ENCODING_PCM_8BIT)
////    {
////      bitsPerSample = 8;
////    }
////    if (DEFAULT_CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_STEREO)
////    {
////      channels = 2;
////    }
////    
////    framePeriod = sampleRate * DEFAULT_AUDIORECORD_BUFFER_FLUSH_PERIOD / 1000;
////    int bufferSize = framePeriod * 2 * bitsPerSample * channels / 8;
////    
////    if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT))
////    { // Check to make sure buffer size is not smaller than the smallest allowed one 
////      bufferSize = AudioRecord.getMinBufferSize(sampleRate, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
////      // Set frame period and timer interval accordingly
////      framePeriod = bufferSize / ( 2 * bitsPerSample * channels / 8 );
////    }
////    
////    audioRecord = new AudioRecord(audioSource, sampleRate, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, bufferSize);
////    audioRecord.setRecordPositionUpdateListener(updateListener);
////    audioRecord.setPositionNotificationPeriod(framePeriod);
////  }
////  
////  public AudioFile beginCaptureToFile(String basename)
////  {
////    writeFile = RecordingsManager.getFile(basename);
////
////    audioRecord.startRecording();
////    return writeFile;
////  }
////  
////  public void endCapture()
////  {
////    audioRecord.stop();
////    
////    try
////    {
////      writeFile.getFile().seek(4); // Write size to RIFF header
////      writeFile.getFile().writeInt(Integer.reverseBytes(36+writeFile.getPayloadSize()));
////    
////      writeFile.getFile().seek(40); // Write size to Subchunk2Size field
////      writeFile.getFile().writeInt(Integer.reverseBytes(writeFile.getPayloadSize()));
////    
////      writeFile.getFile().close();
////    }
////    catch(IOException e)
////    {
////      e.printStackTrace();
////    }
////  }
////  
////  /* 
////   * 
////   * Converts a byte[2] to a short, in LITTLE_ENDIAN format
////   * 
////   */
////  private short getShort(byte argB1, byte argB2)
////  {
////    return (short)(argB1 | (argB2 << 8));
////  }
//}
