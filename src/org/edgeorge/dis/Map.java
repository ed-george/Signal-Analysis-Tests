package org.edgeorge.dis;


import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
	private MapOverlay itemiseOverlay, odg;
	private List<Overlay> mapOverlays;
	private LocationManager man;
	private int data_points = 0;
	private TestListener tl; 
	private TelephonyManager tm;
	private double lastKnownLat, lastKnownLon;
	private final Handler mHandler = new Handler();
	private Runnable mTimer1;
	private GraphView graphView;
	private GraphViewSeries exampleSeries1; 
	private double lastXValue = 1.0d;
	private int duration = 2000; //default
	private SensorManager mSensorManager;
	private ShakeListener mSensorListener;
	private final String[] empty = new String[] {""};
	private static final String TAG_CONTACTS = "Locations";
	private static final String TAG_OPERATOR = "Operator";
	private static final String TAG_LAT = "Latitude";
	private static final String TAG_LON = "Longitude";
	private static final String TAG_HEIGHT = "Height";
	private static final String TAG_TYPE = "Type";
	private String url = "http://houseready.co.uk/json.php";
	// contacts JSONArray
	JSONArray locations = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps); //Set layout

		mapView = (MapView) findViewById(R.id.map);       
		mapView.getController().setZoom(20);
		mapView.setSatellite(true);
		mapView.setBuiltInZoomControls(true);
		mapOverlays = mapView.getOverlays();


		Drawable drawable = this.getResources().getDrawable(R.drawable.pin);

		itemiseOverlay = new MapOverlay(drawable, this);
		odg = new MapOverlay(this.getResources().getDrawable(R.drawable.ant), this);
		tl = new TestListener();
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(tl,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		man = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);

		mapView.invalidate();

		//INSERTED HERE

		// init example series data
		exampleSeries1 = new GraphViewSeries(new GraphViewData[] {
				new GraphViewData(1, 0.0d) //start at 0,0 -  change to getSIG()??

		});

		// graph with dynamically genereated horizontal and vertical labels
		//		if (getIntent().getStringExtra("type").equals("bar")) {
		//			graphView = new BarGraphView(
		//					this // context
		//					, "Mobile Signal Analysis" // heading
		//					);
		//		} else {
		//			graphView = new LineGraphView(
		//					this // context
		//					, "Mobile Signal Analysis" // heading
		//					);
		//			((LineGraphView) graphView).setDrawBackground(true);
		//		}

		graphView = new LineGraphView(
				this // context
				, "Mobile Signal Analysis" // heading
				);
		((LineGraphView) graphView).setDrawBackground(true);
		graphView.addSeries(exampleSeries1); // data

		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
		//		graphView.setVerticalLabels(new String[] {"99", "0"});
		//		if (getIntent().getStringExtra("fixed").equals("true")){
		graphView.setManualYAxisBounds(32.0, 0.0);
		//		}

		//		if (getIntent().getStringExtra("speed").equals("fast")){
		duration = 500;
		//showToastMsg = false;
		//		}else{
		//			duration = 2000;
		//			//showToastMsg = true;
		//		}
		graphView.setHorizontalLabels(new String[] {"Time"});
		graphView.setViewPort(0,50);
		//graphView.setLegendAlign(LegendAlign.BOTTOM);  
		//graphView.setShowLegend(true);
		//graphView.setCameraDistance(distance)
		graphView.setHorizontalLabels(new String[] {""});
		graphView.setScrollable(true);
		layout.addView(graphView);


		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new ShakeListener();   

		mSensorListener.setOnShakeListener(new ShakeListener.OnShakeListener() {

			public void onShake() {

				Handler handler = new Handler(); 
				handler.postDelayed(new Runnable() { 
					public void run() { 
						exampleSeries1.resetData(new GraphViewData[] {
								new GraphViewData(1, getSIG())});

						Toast.makeText(Map.this, "Data Reset", Toast.LENGTH_LONG).show();
					} 
				}, 2000);


			}});




	}

	public void onLocationChanged(Location location) {
		if(data_points == 0){
			lastKnownLat = location.getLatitude();
			lastKnownLon = location.getLongitude();
		}

		if (shouldAddOverlay(location)){
			GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));

			int gsm = tl.getGSMSig();
			int cdma = tl.getCDMASig();
			Log.i("test-lat", Double.toString(location.getLatitude()));
			Log.i("test-lon", Double.toString(location.getLongitude()));


			mapView.getController().animateTo(point);

			if(tl.isGSMPhone()){
				itemiseOverlay.setIconOverlay(gsm);
			}else{
				itemiseOverlay.setIconOverlay(cdma);	
			}

			//		OverlayItem newOver = new OverlayItem(point,"Test Point: " + ++ data_points , "Lat: " + Double.toString(location.getLatitude()) + "\nLon: " + Double.toString(location.getLongitude()) + "\nGSM Signal: " + gsm );
			OverlayItem newOver = new OverlayItem(point,"Point: " + ++data_points , makeString(gsm, cdma, location) );

			itemiseOverlay.addOverlay(newOver);

			mapOverlays.add(itemiseOverlay);
		}

	}


	private boolean shouldAddOverlay(Location location) {
		if (data_points <= 0){
			return true;
		}

		if (getDist(location) >= 10.0f){

			return true;

		}

		return false;
	}

	public float getDist(Location location){

		Location dest = new Location(""); //DummyLocation
		dest.setLatitude(lastKnownLat);
		dest.setLongitude(lastKnownLon);
		Log.i("DISTANCE", Float.toString(location.distanceTo(dest)));
		lastKnownLat = location.getLatitude();
		lastKnownLon = location.getLongitude();
		return location.distanceTo(dest);


	}



	public String makeString(int gsm, int cdma, Location loc){

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

		int i = 10;
		if(length <= 0){
			length = 6; //default
		}

		return (double) Math.round(d * Math.pow(i,length)) / Math.pow(i,length);

	}


	@Override
	public void onPause(){
		super.onPause();
		Toast.makeText(Map.this, "OnPause()", Toast.LENGTH_SHORT).show();
		man.removeUpdates(this);
		tm.listen(tl,PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public void onResume(){
		super.onResume();
		Toast.makeText(Map.this, "OnResume()", Toast.LENGTH_SHORT).show();
		//FIXME onResume()
		//Soon my dog of war
		tm.listen(tl,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		mTimer1 = new Runnable() {
			public void run() {
				lastXValue += 1d;
				exampleSeries1.appendData(new GraphViewData(lastXValue, getSIG()), true);
				graphView.setHorizontalLabels(empty);
				//graphView.redrawAll();
				mHandler.postDelayed(this, duration);
			}
		};
		mHandler.postDelayed(mTimer1, duration);



	}

	@Override
	public void onStop(){
		super.onStop();
		Toast.makeText(Map.this, "OnStop()", Toast.LENGTH_SHORT).show();
		man.removeUpdates(this);
		tm.listen(tl,PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		man.removeUpdates(this);
		tm.listen(tl,PhoneStateListener.LISTEN_NONE);
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
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.mapmenu, menu);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{

		case R.id.mapbut1:
			Toast.makeText(Map.this, "Road View", Toast.LENGTH_SHORT).show();
			if(mapView.isSatellite()){
				mapView.setSatellite(false);
			}
			return true;

		case R.id.mapbut2:
						Toast.makeText(Map.this, "Satellite View", Toast.LENGTH_SHORT).show();
						if(!mapView.isSatellite()){
							mapView.setSatellite(true);
						}
			return true;
			
		case R.id.mapbut3:
			DownloadJSON task = new DownloadJSON();
			String u = url + "?lat=" + Double.toString(lastKnownLat) +"&lon="+ Double.toString(lastKnownLon); //+"&dif=0.0125"; 
			//			Toast.makeText(getApplicationContext(), u, Toast.LENGTH_LONG).show();
			task.execute(new String[] { u });
			return true;

		default:
			//shouldnt matter but #YOLO
			return super.onOptionsItemSelected(item);

		}
	}


	private double getSIG() {
		if(tl.isGSMPhone()){

			/*if(showToastMsg){
			Toast.makeText(RealtimeGraph.this, "Sig: " + Double.toString(s), Toast.LENGTH_SHORT).show();
			}*/

			//			TextView t = (TextView)findViewById(R.id.sig_text); 
			//			t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
			//			t.setText(Double.toString(s));
			//
			//			//TODO: 'Colour Algorithm'
			//			
			//			//------------------------

			return (double) tl.getGSMSig();

		}

		return (double) tl.getCDMASig();
	}



	public class DownloadJSON extends AsyncTask<String, Void, Integer> {

		private ProgressDialog progress;
		@Override
		protected void onPreExecute() {

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

					// creating new HashMap
					TreeMap<String, String> map = new TreeMap<String, String>();

					// adding each child node to HashMap key => value

					map.put(TAG_TYPE, type); 
					map.put(TAG_LAT, lat);
					map.put("Antenna " + TAG_OPERATOR, operator);
					map.put(TAG_LON, lon);
					map.put(TAG_HEIGHT, height);



					// adding HashList to ArrayList
					locationsList.add(map);
					//return iterateMap(map);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}



			for (TreeMap<String, String> entry : locationsList)
			{
				float _lat = 0;
				float _lon = 0;
				String op = null;
				String type = null;
				String height = null;
				for (Entry<String, String> finishEntry : entry.entrySet())
				{
					if(finishEntry.getKey().equals(TAG_LAT)){
						_lat = Float.parseFloat(finishEntry.getValue());

					}else if(finishEntry.getKey().equals(TAG_LON)){
						_lon = Float.parseFloat(finishEntry.getValue());
					}else if(finishEntry.getKey().equals("Antenna " + TAG_OPERATOR)){
						op = finishEntry.getValue();
					}else if(finishEntry.getKey().equals(TAG_HEIGHT)){
						height = finishEntry.getValue();
					}else if(finishEntry.getKey().equals(TAG_TYPE)){
						type = finishEntry.getValue();
					}
				}

				GeoPoint point = new GeoPoint((int) (_lat * 1E6), (int) (_lon * 1E6));
				OverlayItem newOver = new OverlayItem(point,"Basestation" , makeBaseStationInfo(type, op, height, _lat, _lon));
				odg.addOverlay(newOver);
				mapOverlays.add(odg);


				Log.i("Finding BS", "Added: " + Float.toString(_lat) + " " + Float.toString(_lon));
			}


			return locationsList.size();

		}

		@Override
		protected void onPostExecute(Integer result) {

			progress.dismiss();
			if(result == 0){
				Toast.makeText(getApplicationContext(), "Sorry! We couldn't find any...", Toast.LENGTH_SHORT).show();
			}{
				Toast.makeText(getApplicationContext(), "Found " + Integer.toString(result) + " Basestations", Toast.LENGTH_SHORT).show();
			}
		}
		
		
		private String makeBaseStationInfo(String type, String op, String height, float _lat, float _lon){
			String s = "";
			s += "Operator: " + op + "\n";
			s += "Type: " + type + "\n";
			s += "Height: " + height + "m\n";
			s += "Location: " + Float.toString(_lat) + "," + Float.toString(_lon);
			return s;
		}
		
	}




}

