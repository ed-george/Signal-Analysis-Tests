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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ReadDB extends Activity {
	private static final int MIN_DIST = 20;
	private TextView textView;
	private String url = "http://houseready.co.uk/json.php";
	private static final String LOCATION = "Locations";
	private static final String OPERATOR = "Operator";
	private static final String LAT = "Latitude";
	private static final String LON = "Longitude";
	private static final String HEIGHT = "Height";
	private static final String TYPE = "Type";

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
		//Set layout to db layout
		setContentView(R.layout.db);
		//Get location manager
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		//Get access to layout's text view
		textView = (TextView) findViewById(R.id.dbText);

		Criteria criteria = new Criteria();
		//Find 'best provider' for last know location
		final String bestProvider = locationManager.getBestProvider(criteria, false);
		//Store last known location
		Location location = locationManager.getLastKnownLocation(bestProvider);
		if(_lat == -1000 || _lon == -1000){
			//Store last know location
			_lat = location.getLatitude();
			_lon = location.getLongitude();
			lat = location.getLatitude();
			lon = location.getLongitude();
		}

		//Listen to changes in location
		loc_listener = new LocationListener() {
			public void onLocationChanged(Location l) {
				//On location change
				Location old = new Location("");
				//re-create previous known location
				old.setLatitude(_lat);
				old.setLongitude(_lon);
				//check distance of old location to new location
				if(l.distanceTo(old) >= 50){
					//If far enough away, update
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

		//Request updates from GPS provider
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000 ,0, loc_listener);

	}

	@Override
	public void onPause(){
		super.onPause();
		
		if(locationManager.equals(null)){
			return;
		}
		//Remove listener updates
		locationManager.removeUpdates(loc_listener);
	}

	public class DownloadJSON extends AsyncTask<String, Void, String> {

		//Spinner for download task
		private ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			//Before download initiate and show progress spinner
			progress = new ProgressDialog(ReadDB.this);
			progress.setMessage("Reading Database...");
			progress.setIndeterminate(false);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setCancelable(false);
			progress.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			Log.i("JSON", "URL: " + urls[0]);
			//Hashmap for ListView
			ArrayList<TreeMap<String, String>> locationsList = new ArrayList<TreeMap<String, String>>();

			// Creating JSON Parser
			JSONparser jParser = new JSONparser();

			//Gewt JSON from download
			JSONObject json = jParser.downloadJSON(urls[0]);

			try {
				//Get list of locations
				locations = json.getJSONArray(LOCATION);

				//loop through list
				for(int i = 0; i < locations.length(); i++){
					JSONObject c = locations.getJSONObject(i);

					String operator = c.getString(OPERATOR);
					String lat = c.getString(LAT);
					String lon = c.getString(LON);
					String height = c.getString(HEIGHT);
					String type = c.getString(TYPE);

					//Create Treehmap
					TreeMap<String, String> map = new TreeMap<String, String>();

					//Add Keys and Values to TreeMap
					map.put(TYPE, type); 
					map.put(LAT, lat);
					map.put("Antenna " + OPERATOR, operator);
					map.put(LON, lon);
					map.put(HEIGHT, height);

					//Add the TreeList to the overall ArrayList
					locationsList.add(map);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//Return string of all itmes
			return iterateArrayList(locationsList);

		}

		@Override
		protected void onPostExecute(String result) {

			//Stop spinner as download complete
			progress.dismiss();
			//Show outputted results in TextView
			textView.setText(result);
			Toast.makeText(getApplicationContext(), "Data Loaded", Toast.LENGTH_SHORT).show();
		}
	}

	public static String iterateMap(TreeMap<String, String> treeMap)
	{
		//iterate through results and display each result and it's fields
		
		String s = "";
		for(Entry<String, String> entry : treeMap.entrySet())
		{
			//for each result, get all keys/values and order in list line by line
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

		s += Double.toString(makeDecimalPoint(lat,8)) + " " + Double.toString(makeDecimalPoint(lon,8));  		
		return s;
	}


	public double makeDecimalPoint(double d, int length){
		//Make 'length' decimal points for double d
		int i = 10;
		if(length <= 0){
			length = 6;
		}
		return (double) Math.round(d * Math.pow(i,length)) / Math.pow(i,length);

	}

	public void readWebpage(View view) {
		//Check if user is at least 20m from previous location or has been run before
		if(getDist() <= MIN_DIST && firstRun){
			if(_lat == -1000 || _lon == -1000){
				//Check user has been found by GPS before running
				Toast.makeText(getApplicationContext(), "Still trying to find you...", Toast.LENGTH_LONG).show();
			}else{
				//Too near from last location
				Toast.makeText(getApplicationContext(), "Too near previous location", Toast.LENGTH_LONG).show();
			}
		}else{
			//Run
			firstRun = true;
			DownloadJSON task = new DownloadJSON();
			String u = url + "?lat=" + Double.toString(makeDecimalPoint(lat,8)) +"&lon="+ Double.toString(makeDecimalPoint(lon,8));
			task.execute(new String[] { u });
		}
	}


	private float getDist(){
		//Get distance of current location vs previous location
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
