package org.edgeorge.dis;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

public class SignalP extends Activity
{
   /* This variables need to be global, so we can used them onResume and onPause method to
      stop the listener */
   TelephonyManager        Tel;
   MyPhoneStateListener    MyListener;

   /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal);

        /* Update the listener, and start it */
        MyListener   = new MyPhoneStateListener();
        Tel       = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
      Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /* Called when the application is minimized */
    @Override
   protected void onPause()
    {
      super.onPause();
      //Remove later
      Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
      
   }

    /* Called when the application resumes */
   @Override
   protected void onResume()
   {
      super.onResume();
      Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
   }

   /* ÑÑÑÑÑÑÑÑÑÐ */
    /* Start the PhoneState listener */
   /* ÑÑÑÑÑÑÑÑÑÐ */
    private class MyPhoneStateListener extends PhoneStateListener
    {
      /* Get the Signal strength from the provider, each time there is an update */
      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength)
      {
         super.onSignalStrengthsChanged(signalStrength);
         
         Context context = getApplicationContext();
         CharSequence text = "GSM Cinr = "
                 + String.valueOf(signalStrength.getGsmSignalStrength());
         int duration = Toast.LENGTH_SHORT;
         Toast toast = Toast.makeText(context, text, duration); //Make some Toast
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.show();
         
       
         
     
      }

    };/* End of private Class */

}/* GetGsmSignalStrength */
 
