package uk.co.biogen.SoundBites.guiController;

import java.io.File;

import uk.co.biogen.SoundBites.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

public class OldSoundBitesStart extends Activity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);

      File soundbitesDir = new File(Environment.getExternalStorageDirectory() + "/soundbites/");
      soundbitesDir.mkdirs();
      
      setContentView(R.layout.main);
  }

  // InterfaceCall: Record. Called when a 'record' control is activated by the user.
  public void ifcallRecord(View v)
  {
    try
    {
      Thread.sleep(15000);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
//    findViewById(R.id.progressBar1).setVisibility(ProgressBar.INVISIBLE);
//    new RecordFileTask().execute(new Void[0]);
//    findViewById(R.id.progressBar1).setVisibility(ProgressBar.VISIBLE);
  }
  
//  private class RecordFileTask extends AsyncTask<Void, Void, Void>
//  {
//    protected void onPreExecute()
//    {
//    }
//    
//     protected Void doInBackground(Void... v)
//     {
//
////       final int[] devices = {
////           MediaRecorder.AudioSource.CAMCORDER,
////           MediaRecorder.AudioSource.DEFAULT,
////           MediaRecorder.AudioSource.MIC,
////           MediaRecorder.AudioSource.VOICE_CALL,
////           MediaRecorder.AudioSource.VOICE_DOWNLINK,
////           MediaRecorder.AudioSource.VOICE_RECOGNITION,
////           MediaRecorder.AudioSource.VOICE_UPLINK};
//       
////       for(int i : devices)
////       {
//       
////         Recorder r = new Recorder(MediaRecorder.AudioSource.DEFAULT);
////         r.setOutputFile(RecordingsManager.getFile(
////             ((TextView) (findViewById(R.id.filenameInputbox))).getText() + ".raw"));
////         r.prepareForFile();
////      
////         r.start();
////         try
////         {  
////           Thread.sleep(30000);
////         } catch (InterruptedException e)
////         {
////           e.printStackTrace();
////         }
////         
////         r.stop();
////       }
//       
//       return null;
//     }
//
//     protected void onPostExecute(Void v)
//     {
//     }
//   }
  
}
