package uk.co.biogen.SoundBites.guiController;

import java.io.File;
import java.io.RandomAccessFile;

import uk.co.biogen.SoundBites.R;
import uk.co.biogen.SoundBites.guiModel.SoundBitesStartModel;
import uk.co.biogen.SoundBites.util.RecUtility;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class SoundBitesStart extends Activity
{
  public static final int SB_SERVICE_DEFAULT_INTERVALTIME_MS = 10000;
  public static final int SB_SERVICE_MINIMUM_INTERVALTIME_MS = 5000;
  public static final int SB_SERVICE_DEFAULT_POLLTIME_MS = 500;
  public static final int SB_SERVICE_MINIMUM_POLLTIME_MS = 100;
  public static final int SB_SERVICE_DEFAULT_TRAINTIME_MS = 1000;
  public static final int SB_SERVICE_MINIMUM_TRAINTIME_MS = 100;
  
  private static boolean useMovementsMC = true;
  private static int pollIntervalTimems = SB_SERVICE_DEFAULT_INTERVALTIME_MS;
  private static int pollTimems = SB_SERVICE_DEFAULT_POLLTIME_MS;
  private static boolean isPollingHalted = false;
  private Intent sbServiceCommands;
  
//  private static int trainTimems = SB_SERVICE_DEFAULT_TRAINTIME_MS;
  
  /*
   * UP:   active
   * DOWN: inactive
   * WAIT: service is going from up to down or vice versa - makes no sense to
   *   attempt to change state.
   */
  private enum ServiceState {UP, DOWN, WAIT}
  
  /*
   * String[] sbCommands -> commands as defined by parsing in service
   * bool sbCommandsExecute -> whether commands present should be executed
   *   (should be false unless commands have been included)
   */
  private ServiceState sbServiceState;
  private boolean sbServiceInitialised;
  
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    File soundbitesDir = new File(Environment.getExternalStorageDirectory() + "/soundbites/");
    soundbitesDir.mkdirs();

    sbServiceState = ServiceState.DOWN;
    sbServiceInitialised = false;
    sbServiceCommands = new Intent(getApplicationContext(), SoundBitesService.class);
    
    setContentView(R.layout.main);
    
    prepareUIElements();
  }
  
  private void prepareUIElements()
  {
    final ViewFlipper viewChanger = (ViewFlipper) findViewById(R.id.flipper);
    viewChanger.showNext(); // to start on the main interface
    
    /*
     * Main screen UI elements.
     */
    
    final Button activateCNSBtn = (Button) findViewById(R.id.btnActivateCNS);
    final Button trainContextBtn = (Button) findViewById(R.id.btnTrainContext);
    final Button settingsBtn = (Button) findViewById(R.id.btnSettings);

    activateCNSBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        ifaceToggleSBService();
      }
    });

    trainContextBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        viewChanger.showPrevious();
      }
    });

    settingsBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        viewChanger.showNext();      
      }
    });
    
    /*
     * Settings screen UI elements.
     */
    
    final Button btnSettingsBack = (Button) findViewById(R.id.btnSettingsBack);
    final CheckBox cbUseMvmtData = (CheckBox) findViewById(R.id.cbUseMvmtData);
    final EditText edittextTimeMins = (EditText) findViewById(R.id.edittextTime);
    final EditText edittextPollTimeSecs = (EditText) findViewById(R.id.edittextPollTime);
    
    btnSettingsBack.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        // parse for 'use movements mc'
        useMovementsMC = cbUseMvmtData.isChecked();
        
        // parse for interval time
        String intervalTimeMinsText = edittextTimeMins.getText().toString();
        int inputIntervalTimems = Integer.parseInt(intervalTimeMinsText) * 60000;
        if(inputIntervalTimems > SB_SERVICE_MINIMUM_INTERVALTIME_MS)
          pollIntervalTimems = inputIntervalTimems;
        
        // parse for poll length time
        String pollTimeSecsText = edittextPollTimeSecs.getText().toString();
        int inputPollTimems = Integer.parseInt(pollTimeSecsText) * 1000;
        if(inputPollTimems > SB_SERVICE_MINIMUM_POLLTIME_MS)
          pollTimems = inputPollTimems;
        
        sendCommandsToService();

        viewChanger.showPrevious();
      }
    });
    cbUseMvmtData.setChecked(true);
//    edittextTimeMins.setText(Integer.toString(intervalTimeMins));
    edittextPollTimeSecs.setText(Integer.toString(pollTimems / 1000));
    
    /*
     * Training screen UI elements.
     */
    
    final Button btnTrainingBack = (Button) findViewById(R.id.btnTrainingBack);
    final EditText edittextTrainingContextname = (EditText) findViewById(R.id.edittextTrainingContextname);
    final Button btnTrainingRecord = (Button) findViewById(R.id.btnTrainingRecord);
    final Button btnRecordFile = (Button) findViewById(R.id.btnRecordFile);        
    
    btnTrainingBack.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        viewChanger.showNext();
      }
    });
    
    edittextTrainingContextname.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        edittextTrainingContextname.setText("");
      }
    });
    
    btnTrainingRecord.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        recordNewContext();
      }
    });
    
    btnRecordFile.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        recordNewFile();
      }
    });
  }
  
  /*
   * Interface call: request to start the SB service.
   */
  private void ifaceToggleSBService()
  {
    final ImageView titleImage = (ImageView) findViewById(R.id.imageTitle);
    final TextView activeTV = (TextView) findViewById(R.id.textActive);
    
    switch(sbServiceState)
    {
    case UP:
      // shut it down
      stopSBService();

      titleImage.setBackgroundColor(Color.BLACK);
      activeTV.setText("SoundBites CNS is not active");
      
      break;
      
    case DOWN:
      // start it up
      startSBService();
      
      titleImage.setBackgroundColor(Color.LTGRAY);
      activeTV.setText("SoundBites CNS is active");
      
      break;
      
    case WAIT:
      // do nothing - patience, novice
      break;
    }
  }

  /*
   * To be used in conjuction with stopSBService()
   */
  private void startSBService()
  {
    // while service starts
    sbServiceState = ServiceState.WAIT;
    
    setSBServicePollingHalted(false);
    
//    Log.d("sb",sbService.toString());
    
    sendCommandsToService();
    sbServiceInitialised = true;
    sbServiceState = ServiceState.UP;
  }
  
  /*
   * To be used in conjuction with startSBService()
   */
  private void stopSBService()
  {
    // while service stops
    sbServiceState = ServiceState.WAIT;
    
    setSBServicePollingHalted(true);
    
    if(!sbServiceInitialised)
    {
      Log.w("sb", "SB service stop requested before its initialization");
    } else
    {
      stopService(sbServiceCommands);
    }
    
    sbServiceState = ServiceState.DOWN;
  }
  
  private void recordNewContext()
  {
    // Start timer, then fire off async task that returns UI control and performs task
    
    final ProgressBar pbTimer = (ProgressBar) findViewById(R.id.pbTimer);
    pbTimer.setVisibility(ProgressBar.VISIBLE);

    new TrainContextTask().execute(new Void[0]);
    
    pbTimer.setVisibility(ProgressBar.INVISIBLE);
  }
  
  private void recordNewFile()
  {
    // Start timer, then fire off async task that returns UI control and performs task
    
    final ProgressBar pbTimer = (ProgressBar) findViewById(R.id.pbTimer);
    pbTimer.setVisibility(ProgressBar.VISIBLE);

    new RecordFileTask().execute(new Void[0]);
    
    pbTimer.setVisibility(ProgressBar.INVISIBLE);
  }
  
  private class TrainContextTask extends AsyncTask<Void, Void, Void>
  {
    protected Void doInBackground(Void... v)
    {
      final EditText edittextTrainingContextname = (EditText) findViewById(R.id.edittextTrainingContextname);
      
      setSBServicePollingHalted(true);
      
      // at some point, an exception must be thrown in case the microphone
      // cannot be secured for recording. The exception should be caught
      // here and converted into something like a 'try again' dialog for
      // the user
      RandomAccessFile trainingFile =
        SoundBitesStartModel.recordFromMicrophone(SB_SERVICE_DEFAULT_POLLTIME_MS, false, "training_file.raw");
      SoundBitesStartModel.trainContext(
        RecUtility.windowsFromAudioFile(trainingFile),
        edittextTrainingContextname.getText().toString()
        );
      
      setSBServicePollingHalted(false);
      
      return null;
    }
  }
  
  /*
   * The same as TrainContextTask, except simpler
   * 
   * Currently hardcoded to produce a 1-second file, filename taken from
   * the text box, and recorded into a file in /soundbites/audio
   */
  private class RecordFileTask extends AsyncTask<Void, Void, Void>
  {
    protected Void doInBackground(Void... v)
    {
      final EditText edittextTrainingContextname = (EditText) findViewById(R.id.edittextTrainingContextname);
      
      setSBServicePollingHalted(true);
      
      /*
       at some point, an exception must be thrown in case the microphone
       cannot be secured for recording. The exception should be caught
       here and converted into something like a 'try again' dialog for
       the user
       */
      Log.d("event", "Record start");
      SoundBitesStartModel.recordFromMicrophone(3000, false, 
          edittextTrainingContextname.getText().toString());
      Log.d("event", "Record end");
      
      setSBServicePollingHalted(false);
      
      return null;
    }
  }
  
  private void sendCommandsToService()
  {
    String[] commands = new String[4];
    
    if(useMovementsMC)
      commands[0] = "1";
    else
      commands[0] = "0";
    commands[1] = String.valueOf(pollIntervalTimems);
    commands[2] = String.valueOf(pollTimems);
    if(isPollingHalted)
      commands[3] = "1";
    else
      commands[3] = "0";
    
    sbServiceCommands.putExtra("sbCommands", commands);
    startService(sbServiceCommands);
  }
  
  private void setSBServicePollingHalted(boolean set)
  {
    isPollingHalted = set;
    sendCommandsToService();
  }
}
