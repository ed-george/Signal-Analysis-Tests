package org.edgeorge.dis;

import android.app.Activity;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;



public class SignalProgram extends Activity implements OnClickListener {

	final private boolean DEBUG = true;
	final private boolean SCHOOL_DEBUG = false; // Is emulator running in school?
	private boolean PROXY_SET = false;   // Is proxy set?

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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
		switch (v.getId()){

		case R.id.about_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Open About");
			}
			Intent i = new Intent(this, About.class);
			startActivity(i);
			break;

		case R.id.exit_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Exit");
			}
			finish();
			break;

		case R.id.track_button:
			if(openGPSSettings()){
				if(DEBUG){
					Log.i("SignalAnalysis", "Open Track");
				}
				Intent n = new Intent(this, Map.class);
				startActivity(n);
			}
			break;


		case R.id.db_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Open DB");
			}
			Intent n1 = new Intent(this, ReadDB.class);
			startActivity(n1);
			break;

		case R.id.st_button:
			if(DEBUG){
				Log.i("SignalAnalysis", "Speed Test");
			}
			Intent st = new Intent(this, SpeedTest.class);
			startActivity(st);
			break;
		}

	}//end onclick()

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private boolean openGPSSettings()
	{

		if(SCHOOL_DEBUG) {
			if (PROXY_SET == false) {			
				System.setProperty("http.proxyHost", "wwwcache.cs.nott.ac.uk");
				System.setProperty("http.proxyPort", "3128");
				Log.w("Proxy", "Proxy is set to CS School");
				PROXY_SET = true;
			}
		}

		LocationManager loc = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );
		if( loc.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER ) )
		{
			// Can Remove if Required   
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
			Toast.makeText( this, "Please turn on GPS", Toast.LENGTH_LONG).show();
			Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
			startActivity(myIntent);
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			break;
		}
		return true;
	}
}

