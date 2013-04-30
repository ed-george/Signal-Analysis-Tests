package org.edgeorge.dis;


import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;


public class Map extends MapActivity implements LocationListener

{
	private MapView mapView;
	private MapOverlay locationOverlay, basestationOverlay;
	private List<Overlay> mapOverlays;
	private LocationManager location_man;
	private int graphdata_points = 0;
	private TestListener tl; 
	private TelephonyManager tm;
	private double lastKnownLat, lastKnownLon;
	private final Handler mHandler = new Handler();
	private Runnable signalRefreshTimer;
	private GraphView graphView;
	private GraphViewSeries graphViewData; 
	private double last_x = 1.0d;
	private int duration = 500; //default
	private SensorManager mSensorManager;
	private ShakeListener mSensorListener;
	private final String[] empty = new String[] {""};
	private final String LOCATION = "Locations";
	private final String OPERATOR = "Operator";
	private final String LAT = "Latitude";
	private final String LON = "Longitude";
	private final String HEIGHT = "Height";
	private final String TYPE = "Type";
	private String url = "http://houseready.co.uk/json.php";
	private JSONArray locations = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Set interface layout
		setContentView(R.layout.maps); 

		//Initialize Google Map View
		mapView = (MapView) findViewById(R.id.map);      
		//Set Zoom Level
		mapView.getController().setZoom(20);
		//Set Satellite View as default
		mapView.setSatellite(true);
		//Give users Zoom Control
		mapView.setBuiltInZoomControls(true);
		//Get Overlays for map
		mapOverlays = mapView.getOverlays();


		//Set up overlays and graphics for Location Overlay
		locationOverlay = new MapOverlay(this.getResources().getDrawable(R.drawable.pin), this);
		//Set up overlays and graphics for BaseStation Overlay
		basestationOverlay = new MapOverlay(this.getResources().getDrawable(R.drawable.ant), this);

		//New Signal Listener
		tl = new TestListener();
		//Initialize phone to use Test Listener to listen to changes in signal strength 
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(tl,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		//Begin to listen to changes in location using GPS
		location_man = (LocationManager) getSystemService(LOCATION_SERVICE);
		location_man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);

		//Redraw Map for any Overlays
		mapView.invalidate();

		//Create new GraphData series with single data point (1,0)
		graphViewData = new GraphViewSeries(new GraphViewData[] {
				new GraphViewData(1, 0.0d) 

		});

		//Create new Line Graph titled "Current Signal Strength"
		graphView = new LineGraphView(this, "Current Signal Strength");

		//Draw blue background underneath line graph
		((LineGraphView) graphView).setDrawBackground(true);

		//Add the associated data series to the graph
		graphView.addSeries(graphViewData);

		//Locate the graph's location in layout from layout file
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);

		//Add graph specific params
		graphView.setManualYAxisBounds(32.0, 0.0);
		graphView.setViewPort(0,50);
		graphView.setHorizontalLabels(new String[] {""});
		graphView.setScrollable(true);

		// Add the Graph to the Layout
		layout.addView(graphView);

		//Set up the manager of listener to sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//Create new listener for 'Shakes'
		mSensorListener = new ShakeListener();   

		//Handle Shake
		mSensorListener.setOnShakeListener(new ShakeListener.OnShakeListener() {

			//On shake, reset graph data
			public void onShake() {
				Handler handler = new Handler(); 
				handler.postDelayed(new Runnable() { 
					public void run() { 
						//Reset Data Series
						graphViewData.resetData(new GraphViewData[] {
								new GraphViewData(1, getSIG())});
						//Post message to screen telling user of Data Reset
						Toast.makeText(Map.this, "Data Reset", Toast.LENGTH_LONG).show();
					} 
				}, 2000);
			}});

	}

	public void onLocationChanged(Location location) {
		//Called when current GPS location has changed
		if (shouldAddOverlay(location)){

			//create new location
			GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
			//get phone signal data
			int gsm = tl.getGSMSig();
			int cdma = tl.getCDMASig();
			Log.i("test-lat", Double.toString(location.getLatitude()));
			Log.i("test-lon", Double.toString(location.getLongitude()));

			//move map to current location
			mapView.getController().animateTo(point);

			//check phone type
			if(tl.isGSMPhone()){
				//change icon depending on signal strength
				locationOverlay.setIconOverlay(gsm);
			}else{
				locationOverlay.setIconOverlay(cdma);	
			}

			//Create new overlay and information it displays in it's dialog
			OverlayItem newOver = new OverlayItem(point,"Point: " + ++graphdata_points , makeString(gsm, cdma, location) );
			
			//add this overlay into overlay list
			locationOverlay.addOverlay(newOver);
			//add the overlays to map
			mapOverlays.add(locationOverlay);
		}

		//update last current location known
		lastKnownLat = location.getLatitude();
		lastKnownLon = location.getLongitude();
	}


	private boolean shouldAddOverlay(Location location) {
		//Should a location Overlay be added?
		if (graphdata_points <= 0){
			//If none have been placed so far
			return true;
		}
		if (getDist(location, lastKnownLat, lastKnownLon) >= 10.0f){
			//if last known location was greater than 10m away from current location 
			return true;
		}
		//otherwise
		return false;
	}

	public float getDist(Location location, double lat, double lon){
		//find distance between two points
		Location dest = new Location(""); //DummyLocation
		dest.setLatitude(lat);
		dest.setLongitude(lon);
		Log.i("DISTANCE", Float.toString(location.distanceTo(dest)));
		return location.distanceTo(dest);


	}



	public String makeString(int gsm, int cdma, Location loc){
		//Create location overlay dialog text
		String a = "Lat: " + Double.toString(makeDecimalPoint(loc.getLatitude(), 8));
		String b = "\nLon: " + Double.toString(makeDecimalPoint(loc.getLongitude(), 8));
		if(tl.isGSMPhone()){
			int gsmDbm = gsm * 2 - 113;
			String c = "\nGSM: " + gsm + " = " + gsmDbm + "dBm" ;

			return a.concat(b.concat(c));
		}else{
			String c = "\nCDMA: " + cdma + "dBm"; 	
			return a.concat(b.concat(c));
		}

	}

	public double makeDecimalPoint(double d, int length){
		//Places 'length' decimal points after double d
		int i = 10;
		if(length <= 0){
			length = 6; //default
		}

		return (double) Math.round(d * Math.pow(i,length)) / Math.pow(i,length);

	}


	@Override
	public void onPause(){
		super.onPause();
		//Stop updates and listeners
		location_man.removeUpdates(this);
		tm.listen(tl,PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public void onResume(){
		super.onResume();
		//Restart listeners
		tm.listen(tl,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		location_man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		//Use handler to handle a runnable and 
		signalRefreshTimer = new Runnable() {
			public void run() {
				//increase the x value (time)
				last_x += 1d;
				//Append new latest Signal Data to graph (uses getSIG() method)
				graphViewData.appendData(new GraphViewData(last_x, getSIG()), true);
				graphView.setHorizontalLabels(empty);
				//Update
				mHandler.postDelayed(this, duration);
			}
		};
		mHandler.postDelayed(signalRefreshTimer, duration);
	}

	@Override
	public void onStop(){
		super.onStop();
		//Stop updates and listeners
		location_man.removeUpdates(this);
		tm.listen(tl,PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		//Stop updates and listeners
		location_man.removeUpdates(this);
		tm.listen(tl,PhoneStateListener.LISTEN_NONE);
	}

	private double getSIG() {
		//Uses TestListener tl
		if(tl.isGSMPhone()){
			//Return GSM
			return (double) tl.getGSMSig();
		}
		//Return CDMA
		return (double) tl.getCDMASig();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{	//Create Options Menu
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.mapmenu, menu);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item)
	{
		//Check which option button has been pressed
		switch (item.getItemId())
		{

		case R.id.mapbut1:
			//Turn Satellite View Off
			Toast.makeText(Map.this, "Road View", Toast.LENGTH_SHORT).show();
			if(mapView.isSatellite()){
				mapView.setSatellite(false);
			}
			return true;

		case R.id.mapbut2:
			//Turn Satellite View On
			Toast.makeText(Map.this, "Satellite View", Toast.LENGTH_SHORT).show();
			if(!mapView.isSatellite()){
				mapView.setSatellite(true);
			}
			return true;

		case R.id.mapbut3:
			//Plot local Basestations to map
			//Create new AsyncTask to download JSON from URL
			DownloadJSON task = new DownloadJSON();
			//Form URL
			String u = url + "?lat=" + Double.toString(lastKnownLat) +"&lon="+ Double.toString(lastKnownLon); 
			//Execute DownloadJSON task with URL
			task.execute(new String[] { u });
			return true;

		default:
			//shouldnt be used
			return super.onOptionsItemSelected(item);
		}
	}

	public class DownloadJSON extends AsyncTask<String, Void, Integer> {

		//Spinner for download task
		private ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			//Before download initiate and show progress spinner
			progress = new ProgressDialog(Map.this);
			progress.setMessage("Reading Database...");
			progress.setIndeterminate(false);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setCancelable(false);
			progress.show();
		}

		@Override
		protected Integer doInBackground(String... urls) {
			Log.i("JSON", "URL: " + urls[0]);
			//Hashmap for ListView
			ArrayList<TreeMap<String, String>> locationsList = new ArrayList<TreeMap<String, String>>();

			// Creating JSON Parser
			JSONparser jParser = new JSONparser();

			//Gewt JSON download from website - see JSONparser.java
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


			//iterate through TreeMap
			for (TreeMap<String, String> entry : locationsList)
			{
				//Placeholder variables for results
				float _lat = 0;
				float _lon = 0;
				String op = null;
				String type = null;
				String height = null;
				//Iterate through entries
				for (Entry<String, String> finishEntry : entry.entrySet())
				{
					//Get all entries and assign variables
					if(finishEntry.getKey().equals(LAT)){
						_lat = Float.parseFloat(finishEntry.getValue());
					}else if(finishEntry.getKey().equals(LON)){
						_lon = Float.parseFloat(finishEntry.getValue());
					}else if(finishEntry.getKey().equals("Antenna " + OPERATOR)){
						op = finishEntry.getValue();
					}else if(finishEntry.getKey().equals(HEIGHT)){
						height = finishEntry.getValue();
					}else if(finishEntry.getKey().equals(TYPE)){
						type = finishEntry.getValue();
					}
				}

				//Create new overlay!
				GeoPoint point = new GeoPoint((int) (_lat * 1E6), (int) (_lon * 1E6));
				OverlayItem newOver = new OverlayItem(point,"Basestation" , makeBaseStationInfo(type, op, height, _lat, _lon));
				//Add to basestation overlay list
				basestationOverlay.addOverlay(newOver);
				//Add to map
				mapOverlays.add(basestationOverlay);
				Log.i("Finding BS", "Added: " + Float.toString(_lat) + " " + Float.toString(_lon));
			}

			//Return number of base stations founds
			return locationsList.size();

		}

		@Override
		protected void onPostExecute(Integer result) {
			//Stop spinner as download complete
			progress.dismiss();
			if(result == 0){
				//Couldnt find any
				Toast.makeText(getApplicationContext(), "Sorry! We couldn't find any...", Toast.LENGTH_SHORT).show();
			}{
				//Alert user of how many results found
				Toast.makeText(getApplicationContext(), "Found " + Integer.toString(result) + " Basestations", Toast.LENGTH_SHORT).show();
			}
		}


		private String makeBaseStationInfo(String type, String op, String height, float _lat, float _lon){
			//Make text for basestation overlay dialog
			String s = "";
			s += "Operator: " + op + "\n";
			s += "Type: " + type + "\n";
			s += "Height: " + height + "m\n";
			s += "Location: " + Float.toString(_lat) + "," + Float.toString(_lon);
			Location l = new Location("");
			l.setLatitude(_lat);
			l.setLongitude(_lon);
			s += "\nDistance: " + Integer.toString(Math.round(getDist(l, lastKnownLat, lastKnownLon))) +"m (Approx)";  
			return s;
		}

	}




}

