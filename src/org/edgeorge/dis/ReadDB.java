package org.edgeorge.dis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ReadDB extends Activity {
	private TextView textView;
	private static String url = "http://houseready.co.uk/testdb.php";

	// JSON Node names
	private static final String TAG_CONTACTS = "Locations";
	private static final String TAG_ID = "TBL_ID";
	private static final String TAG_LAT = "TBL_LAT";
	private static final String TAG_LON = "TBL_LON";
	private static final String TAG_SIG = "TBL_SIG";

	// contacts JSONArray
	JSONArray locations = null;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.db);
		textView = (TextView) findViewById(R.id.dbText);
	}

	private class DownloadWebPage extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {

			// Hashmap for ListView
			ArrayList<HashMap<String, String>> locationsList = new ArrayList<HashMap<String, String>>();

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
					String id = c.getString(TAG_ID);
					String lat = c.getString(TAG_LAT);
					String lon = c.getString(TAG_LON);
					String sig = c.getString(TAG_SIG);


					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();

					// adding each child node to HashMap key => value

					map.put(TAG_LAT, lat);
					map.put(TAG_ID, id);
					map.put(TAG_LON, lon);
					map.put(TAG_SIG, sig); 

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
			textView.setText(result);

		}
	}

	public static String iterateMap(HashMap<String, String> sourceMap)
	{
		String s = "";

		for(Entry<String, String> entry : sourceMap.entrySet())

		{
			s += "Key: "+entry.getKey()+" and Value: "+entry.getValue()+"\n";
		}

		s += "\n";

		return s;
	}

	public static String iterateArrayList(ArrayList<HashMap<String, String>> sourceMap)
	{
		String s = "";

		for(int i = 0; i < sourceMap.size(); i++)

		{
			s += iterateMap(sourceMap.get(i));
		}

		return s;
	}


	public void readWebpage(View view) {
		DownloadWebPage task = new DownloadWebPage();
		task.execute(new String[] { url });

	}
} 
