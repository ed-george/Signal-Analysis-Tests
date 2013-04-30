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

	public JSONparser() {
		Log.i("JSON Parser", "Parser Object Created");
	}

	//Download JSON from web
	public JSONObject downloadJSON(String url) {

		try {
			
			DefaultHttpClient client = new DefaultHttpClient();
			//GET retrieves whatever information (in the form of an entity) is identified by the Request-URI
			HttpGet httpGet = new HttpGet(url);
			//Executes a request using the default HTTP client and processes the response using the given response handler HTTP GET
			HttpResponse response = client.execute(httpGet);
			//Return majority of HTTP request including some of header and full body
			HttpEntity entity = response.getEntity();
			//Retrieve Body
			is = entity.getContent();           

		} catch (Exception e){
			Log.e("JSON Parser", "HTTP Error");
			Log.e("JSON Parser", e.getMessage());
			e.printStackTrace();
		}

		try {
			//Read input stream from HTTP GET body
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			//Read in body line by line
			while ((line = reader.readLine()) != null) {
				//append to string builder
				sb.append(line + "\n");
			}
			//close stream
			is.close();
			//stringbuilder contains the JSON
			json = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
			Log.e("JSON Parser", "Error during conversion");
		}

		try {
			//Return string containing json as JSON object
			jObject = new JSONObject(json);
		} catch (JSONException e) { 
			e.printStackTrace();
			Log.e("JSON Parser", "Error parsing as JSON");
		}

		return jObject;

	}
}