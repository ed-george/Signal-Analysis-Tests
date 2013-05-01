package org.edgeorge.dis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


//Class for Main Menu Activity
public class SignalProgram extends Activity implements OnClickListener {

	//Should display DEBUG values?
	final private boolean DEBUG = true;
	//Use cs.nott.ac.uk proxy?
	final private boolean SCHOOL_DEBUG = false; // Is emulator running in school?
	private boolean PROXY_SET = false;   // Is proxy set?

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Set content view to Main MEnu
		setContentView(R.layout.main);

		//Set Click listeners to all buttons
		View exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(this);

		View trackButton = findViewById(R.id.track_button);
		trackButton.setOnClickListener(this);

		View spdButton = findViewById(R.id.st_button);
		spdButton.setOnClickListener(this);

		View dbButton = findViewById(R.id.db_button);
		dbButton.setOnClickListener(this);

		View aboutButton = findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);
	}


	public void onClick(View v) {
		//On click, switch by button ID
		switch (v.getId()){

		//About Dialog
		case R.id.about_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Open About");
			}
			Intent i = new Intent(this, About.class);
			startActivity(i);
			break;

		//Exit Button	
		case R.id.exit_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Exit");
			}
			finish();
			break;

		//Map View Button	
		case R.id.track_button:
			if(openGPSSettings()){
				if(DEBUG){
					Log.i("SignalAnalysis", "Open Track");
				}
				Intent n = new Intent(this, Map.class);
				startActivity(n);
			}
			break;

		//Mast DB Text View
		case R.id.db_button:
			if(openGPSSettings()){
				if(DEBUG){
					Log.i("SignalAnalysis", "Open DB");
				}
				Intent n1 = new Intent(this, ReadDB.class);
				startActivity(n1);
			}
			break;

		//Speed Test View	
		case R.id.st_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Speed Test");
			}
			Intent st = new Intent(this, SpeedTest.class);
			startActivity(st);
			break;
		}

	}//end onclick()


	//Check if GPS is enabled
	private boolean openGPSSettings()
	{
		//Check if proxy should be set
		if(SCHOOL_DEBUG) {
			if (PROXY_SET == false) {			
				System.setProperty("http.proxyHost", "wwwcache.cs.nott.ac.uk");
				System.setProperty("http.proxyPort", "3128");
				Log.w("Proxy", "Proxy is set to CS School");
				PROXY_SET = true;
			}
		}

		//Get Location Manager
		LocationManager loc = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );
		//Is GPS provider enabled?
		if( loc.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER ) )
		{
			//GPS is enabled, alert user
			if(DEBUG){   
				String string = "GPS is on";
				if(SCHOOL_DEBUG){
					string = "GPS is on - School Debug";
				}
				Toast.makeText( this, string, Toast.LENGTH_SHORT ).show();
			}
			return true;
		}
		else
		{
			//GPS is off
			Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_LONG).show();
			//Send users to Location settings menu to turn on GPS
			Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
			startActivity(myIntent);
			return false;
		}
	}

}

