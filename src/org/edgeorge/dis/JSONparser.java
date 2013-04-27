package org.edgeorge.dis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONparser {
	
	//Please note:
	//Adapted from Tutorial by Ravi Tamada
	//http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
	
	private InputStream is = null;
	private JSONObject jObject = null;
	private String json = "";

	// constructor
	public JSONparser() {
		Log.i("JSON Parser", "Parser Object Created");
	}

	public JSONObject downloadJSON(String url) {

		try {

			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();           

		} catch (Exception e){
			Log.e("JSON Parser", "HTTP Error");
			Log.e("JSON Parser", e.getMessage());
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();


		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		try {
			jObject = new JSONObject(json);
		} catch (JSONException e) { 
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		return jObject;

	}
}