package org.edgeorge.dis;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ReadDB extends Activity {
	private TextView textView;
	private String url = "http://houseready.co.uk/json.php";

	// JSON Node names
	//private static final String TAG_ID = "Id"; 
	//Unused 
	private static final String TAG_CONTACTS = "Locations";
	private static final String TAG_OPERATOR = "Operator";
	private static final String TAG_LAT = "Latitude";
	private static final String TAG_LON = "Longitude";
	private static final String TAG_HEIGHT = "Height";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_BAND = "Band";
	private static final String TAG_DIST = "Approx Distance";
	// contacts JSONArray
	JSONArray locations = null;

	private double lat;
	private double lon;
	private double _lat = -1000; //previous
	private double _lon = -1000; //previous
	private boolean firstRun = false;

	private LocationListener loc_listener;
	private LocationManager locationManager;
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		setContentView(R.layout.db);
		textView = (TextView) findViewById(R.id.dbText);

		loc_listener = new LocationListener() {
			public void onLocationChanged(Location l) {
				Location old = new Location("");
				old.setLatitude(_lat);
				old.setLongitude(_lon);
				if(l.distanceTo(old) >= 50){
					_lat = lat;
					_lon = lon;
					lat = l.getLatitude();
					lon = l.getLongitude();
				}
			}

			public void onProviderEnabled(String p) {
			}

			public void onProviderDisabled(String p) {
			}

			public void onStatusChanged(String p, int status, Bundle extras) {
			}      
		};


		Criteria criteria = new Criteria();
		final String bestProvider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		if(_lat == -1000 || _lon == -1000){

			lat = location.getLatitude();
			lon = location.getLongitude();
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000 ,0, loc_listener);

	}

	@Override
	public void onPause(){
		super.onPause();
		if(locationManager.equals(null)){
			return;
		}
		locationManager.removeUpdates(loc_listener);
	}

	public class DownloadJSON extends AsyncTask<String, Void, String> {

		private ProgressDialog progress;
		@Override
		protected void onPreExecute() {

			progress = new ProgressDialog(ReadDB.this);
			progress.setMessage("Reading Database...");
			progress.setIndeterminate(false);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setCancelable(false);
			progress.show();
		}

		@Override
		protected String doInBackground(String... urls) {

			// Hashmap for ListView
			ArrayList<TreeMap<String, String>> locationsList = new ArrayList<TreeMap<String, String>>();

			// Creating JSON Parser instance
			JSONparser jParser = new JSONparser();

			// getting JSON string from URL
			JSONObject json = jParser.getJSONFromUrl(urls[0]);

			try {
				// Getting Array of Contacts
				locations = json.getJSONArray(TAG_CONTACTS);

				// looping through All Contacts
				for(int i = 0; i < locations.length(); i++){
					JSONObject c = locations.getJSONObject(i);

					// Storing each json item in variable

					String operator = c.getString(TAG_OPERATOR);
					String lat = c.getString(TAG_LAT);
					String lon = c.getString(TAG_LON);
					String height = c.getString(TAG_HEIGHT);
					String type = c.getString(TAG_TYPE);
					String band = c.getString(TAG_BAND);

					// creating new HashMap
					TreeMap<String, String> map = new TreeMap<String, String>();

					// adding each child node to HashMap key => value

					map.put(TAG_TYPE, type); 
					map.put(TAG_BAND, band); 
					map.put(TAG_LAT, lat);
					map.put("Antenna " + TAG_OPERATOR, operator);
					map.put(TAG_LON, lon);
					map.put(TAG_HEIGHT, height); 

					float x = Float.parseFloat(lat);
					float y = Float.parseFloat(lon);

					map.put(TAG_DIST, Integer.toString((int) Math.round(getDist(x,y))) + "m");

					// adding HashList to ArrayList
					locationsList.add(map);
					//return iterateMap(map);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return iterateArrayList(locationsList);

		}

		@Override
		protected void onPostExecute(String result) {

			progress.dismiss();

			textView.setText(result);
			Toast.makeText(getApplicationContext(), "Data Loaded", Toast.LENGTH_SHORT).show();
		}
	}

	public static String iterateMap(TreeMap<String, String> treeMap)
	{
		String s = "";

		for(Entry<String, String> entry : treeMap.entrySet())

		{
			s += entry.getKey() +" : "+ entry.getValue()+"\n";
		}

		s += "\n";

		return s;
	}

	public String iterateArrayList(ArrayList<TreeMap<String, String>> locationsList)
	{
		String s = "";

		for(int i = 0; i < locationsList.size(); i++)

		{
			s += iterateMap(locationsList.get(i));
		}

		s += Double.toString(lat) + " " + Double.toString(lon);  		
		return s;
	}


	public void readWebpage(View view) {
		if(getDist() <= 20 && firstRun){
			if(_lat == -1000 || _lon == -1000){
				Toast.makeText(getApplicationContext(), "Still trying to find you...", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(getApplicationContext(), "Too near previous location", Toast.LENGTH_LONG).show();
			}
		}else{
			firstRun = true;
			DownloadJSON task = new DownloadJSON();
			String u = url + "?lat=" + Double.toString(lat) +"&lon="+ Double.toString(lon) +"&dif=0.0125";
			//Toast.makeText(getApplicationContext(), u, Toast.LENGTH_LONG).show();
			task.execute(new String[] { u });
		}
	}


	private float getDist(float lat, float lon){
		Location l1 = new Location("");
		l1.setLatitude(lat);
		l1.setLatitude(lon);
		Location l2 = new Location("");
		l2.setLatitude(getLat());
		l2.setLatitude(getLon());
		return l1.distanceTo(l2);
	}

	private float getDist(){
		if(_lat == -1000 || _lon == -1000){
			return 0.0f;
		}
		Location l1 = new Location("");
		l1.setLatitude(_lat);
		l1.setLatitude(_lon);
		Location l2 = new Location("");
		l2.setLatitude(getLat());
		l2.setLatitude(getLon());
		return l1.distanceTo(l2);
	}

	private double getLat() {
		return lat;
	}

	private double getLon() {
		return lon;
	}


} 
