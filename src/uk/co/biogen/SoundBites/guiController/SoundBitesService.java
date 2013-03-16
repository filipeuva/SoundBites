package uk.co.biogen.SoundBites.guiController;

import uk.co.biogen.SoundBites.R;
import uk.co.biogen.SoundBites.guiModel.SoundBitesStartModel;
import uk.co.biogen.SoundBites.util.RecUtility;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/*
 * Activity class to boot SoundBites as a background context recognition
 * service. Performs regular polling of the microphone to identify the current
 * context. Manages back-end objects and a 'movements' Markov chain for this
 * purpose.
 * 
 * 
   * "There are two reasons that a service can be run by the system. If someone
   * calls Context.startService() then the system will retrieve the service
   * (creating it and calling its onCreate() method if needed) and then call 
   * its onStartCommand(Intent, int, int) method with the arguments supplied
   * by the client. The service will at this point continue running until
   * Context.stopService() or stopSelf() is called. Note that multiple calls
   * to Context.startService() do not nest (though they do result in multiple
   * corresponding calls to onStartCommand()), so no matter how many times it
   * is started a service will be stopped once Context.stopService() or
   * stopSelf() is called; however, services can use their stopSelf(int)
   * method to ensure the service is not stopped until started intents have
   * been processed."
   * 
 * Also, it turns out that stopService() doesn't actually reliably stop the
 * service in any way - rather, it signals that the service should be stopped
 * when convenient, whatever that means. As such, actual stopping of what the
 * service is doing has to be done in an ad-hoc fashion (here viz.
 * haltService()).
 */
public class SoundBitesService extends Service
{
  private NotificationManager nm;
  
  /*
   * Fields corresponding to settings go here
   */
  private boolean useMovementsMC = true;
  private int pollIntervalms = SoundBitesStart.SB_SERVICE_DEFAULT_INTERVALTIME_MS;
  private int pollTimems = SoundBitesStart.SB_SERVICE_DEFAULT_POLLTIME_MS;
//  private ContextNameService cnsCP;
  
  private String currentContext = "";
  
  private boolean isPollingHalted;
  private boolean isInitialised;
  
  /*
   * (non-Javadoc)
   * @see android.app.Service#onBind(android.content.Intent)
   * 
   * Unused.
   */
  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    // call executeCommands() first - important fields are initialised by it
    executeCommands(intent.getExtras());

    if(!isInitialised)
    {
      nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      // start the poll loop
      new PollMicrophoneTask().execute(new Void[0]);
    }
    
    isInitialised = true;
    
    return Service.START_STICKY;
  }
  
  @Override
  public void onDestroy()
  {
    SoundBitesStartModel.consolidateMemory();
    currentContext = "";
    nm.cancel(1);
  }
  
  /*
   * Executes the commands from the activity.
   */
  private void executeCommands(Bundle receivedData)
  {
    String[] commands = receivedData.getStringArray("sbCommands");
    if(commands[0].equals("1"))
    {
      useMovementsMC = true;
    } else
    {
      useMovementsMC = false;
    }
    pollIntervalms = Integer.parseInt(commands[1]);
    pollTimems = Integer.parseInt(commands[2]);
    if(commands[3].equals("1"))
    {
      isPollingHalted = true;
    } else
    {
      isPollingHalted = false;
    }
  }
  
//  private class PollSettingsFileTask extends AsyncTask<Void, Void, Void>
//  {
//    @Override
//    protected Void doInBackground(Void... arg0)
//    {
//      return null;
//    }
//  }
  
//  private class TestTask extends AsyncTask<Void, Void, Void>
//  {
//    @Override
//    protected Void doInBackground(Void... params)
//    {
//      Log.d("sb", "The service works, and background task executes as normal.");
//      
//      return null;
//    }
//  }
  
  /*
   * Performs polling of microphone for audio data, and also performs context
   * recognition in calling calculateContext();
   */
  private class PollMicrophoneTask extends AsyncTask<Void, Void, Void>
  {
    protected Void doInBackground(Void... v)
    {
      while(true)
      {
        if(!isPollingHalted)
        {
            // publish the context
              // calculated from
                // windows generated from
                  // the audio that was just recorded
            
          final long startTime = System.nanoTime();
          final long endTime;
          try
          {
            publishContext(
              calculateContext(
                RecUtility.windowsFromAudioFile(
                  SoundBitesStartModel.recordFromMicrophone(pollTimems, true, "")
                )
              )
            );
          } finally {
            endTime = System.nanoTime();
          }
          final long duration = endTime - startTime;
          Log.d("time", "query time was " + ((double) duration / (double) 1000000000) + " seconds");
        }
        
        
        // wait interval
        try
        {
          Thread.sleep(pollIntervalms);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
      
//      // go again
//      new PollMicrophoneTask().execute(new Void[0]);
//      
//      return null;
    }
  }

  private String calculateContext(short[][] audioWindows)
  {
    return SoundBitesStartModel.calculateContext(audioWindows, useMovementsMC);
  }
  
  /*
   * Forgets the old context name and publishes the new one.
   */
  private void publishContext(String contextName)
  {
    // broadcast the context name to rest of phone
    ContextNameService.publishContextName(this, contextName);

    if(!currentContext.equals(contextName))
    {
      // prepare status bar notification on context changed
      int icon = R.drawable.ear;
      CharSequence tickerText = "SoundBites";
      long when = System.currentTimeMillis();
      CharSequence contentTitle = "Context changed";
      CharSequence contentText = "You're now in " + contextName;
  
      Intent notificationIntent = new Intent(this, SoundBitesService.class);
      PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
  
      Notification notification = new Notification(icon, tickerText, when);
      notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
      
      notification.flags = Notification.FLAG_ONGOING_EVENT; 
      
      nm.notify(1, notification);
    }

    currentContext = contextName;
  }
}
