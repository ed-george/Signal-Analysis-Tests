package org.edgeorge.dis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class SpeedTest extends Activity {

	private GraphView graphView;
	private static GraphViewSeries graphViewData; 
	private static double last_x = 1.0d;

	//1MB = 1024 Bytes * 1024 Bytes = 2^20
	//1MB = 1024 KiloBytes = 1048576 Bytes = 8388608 bits 
	private final int EXPECTED_SIZE = 1048576;

	private static TextView tv;
	private Button btn;
	private static ProgressBar pb;
	private InputStream is = null;
	private final static int UPDATE_OCCURED_MESSAGE = 0;
	private final static int COMPLETE_MESSAGE = 1;
	private final int DL_TIME_THRESHOLD = 300;
	private static long totalDownloadTime;
	private static double maxSpeed = 0.0d;
	private static Activity activity;
	private static boolean shouldContinue = true;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Bugfix for Toast
		activity = this;
		//Set layout
		setContentView(R.layout.speed); 
		//Initialise graph data
		graphViewData = new GraphViewSeries(new GraphViewData[] {
				new GraphViewData(1, 0.0d) 

		});

		//Create new Line Graph titled "Current Signal Strength"
		graphView = new LineGraphView(this, "Current Signal Speed");

		//Draw blue background underneath line graph
		((LineGraphView) graphView).setDrawBackground(true);

		//Add the associated data series to the graph
		graphView.addSeries(graphViewData);

		//Locate the graph's location in layout from layout file
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph2);

		//Add graph specific params
		graphView.setViewPort(0,50);
		graphView.setHorizontalLabels(new String[] {""});
		graphView.setScrollable(true);

		// Add the Graph to the Layout
		layout.addView(graphView);

		//Set up references to layout items
		btn = (Button) findViewById(R.id.speed_btn);
		tv = (TextView) findViewById(R.id.status);
		pb = (ProgressBar) findViewById(R.id.progressBar);

		//Create 
		btn.setOnClickListener(new OnClickListener(){
			public void onClick(final View view) {
				setProgressBarVisibility(true);	
				btn.setEnabled(false);
				new Thread(downloadRun).start();				
			}
		});

	}

	@Override
	public void onPause(){
		super.onPause();
		try {
			Log.d("SpeedTest:", "shouldContinue = false");
			shouldContinue = false;
			if(is != null){
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		try {
			Log.d("SpeedTest:", "shouldContinue = false");
			shouldContinue = false;
			if(is != null){
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		shouldContinue = true;
	}


	private final Runnable downloadRun = new Runnable(){

		public void run() {
			InputStream stream = null;
			try {
				//Show and initalise progress bar
				pb.setVisibility(View.VISIBLE);
				pb.setProgress(0);

				//URL of dummy 1MB file
				String dl_url = "http://www.jince.co.uk/dummy.txt";	
				//Create new URL object
				URL url = new URL(dl_url);
				//Connect to URL
				URLConnection con = url.openConnection();
				//Turn caching off
				con.setUseCaches(false);
				//Input stream is URLConnection input stream...i.e. download from web
				stream = con.getInputStream();

				int currentByte = 0;
				long start = System.currentTimeMillis();
				long updateStart = System.currentTimeMillis();
				long updateTime = 0;
				int bytesCurrentThreshold = 0;
				int bytesRecieved = 0;

				//Read single byte at a time from download stream
				while((currentByte = stream.read()) != -1 && shouldContinue){	
					//Increase overall bytes received
					bytesRecieved++;
					//Number of bytes in current threshold
					bytesCurrentThreshold++;
					//If time to update lasted longer than minimum threshold
					if(updateTime >= DL_TIME_THRESHOLD){
						//work out current progress
						int progress= (int) ((bytesRecieved/(double) EXPECTED_SIZE) * 100);
						Log.e("Speed Test:", Integer.toString(progress) + "% - Current Byte: " + (char) currentByte);
						//Send message to handler telling UI to be updated as more bytes have been recieved - send current speed info
						Message msg = Message.obtain(threadHandler, UPDATE_OCCURED_MESSAGE, getSpeedInfo(updateTime, bytesCurrentThreshold));
						//Also send progress/bytes recieved
						msg.arg1 = progress;
						msg.arg2=bytesRecieved;
						//Send message to handler
						threadHandler.sendMessage(msg);
						//reset update start
						//reset bytes in threshold
						updateStart = System.currentTimeMillis();
						bytesCurrentThreshold = 0;
					}
					//Overall time taken to update
					updateTime = System.currentTimeMillis() - updateStart;

				}

				//Total time to download
				totalDownloadTime = (System.currentTimeMillis() - start);
				//Message handler telling of completion
				Message msg = Message.obtain(threadHandler, COMPLETE_MESSAGE, getSpeedInfo(totalDownloadTime, bytesRecieved));
				threadHandler.sendMessage(msg);
			} 
			catch (Exception e){
				Log.e("Speed Test", e.getMessage());
			}finally{
				try {
					//Close Stream if still open
					if(stream!=null){
						stream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	};

	//Handler for Background Runnable
	private static final Handler threadHandler = new Handler(){
		@Override
		public void handleMessage(final Message msg) {
			//Work out which message was sent via Message ID
			switch(msg.what){
			//Update UI about progress with download
			case UPDATE_OCCURED_MESSAGE:
				//get current speed info passed from runnable
				final CurrentSpeedInfo current = (CurrentSpeedInfo) msg.obj;
				//if the current speed is faster than the max overall speed, the new speed is the max 
				if(current.kb > maxSpeed){
					maxSpeed = current.kb;
				}
				//update graph x value
				last_x += 1d;
				//Append new latest Signal Data to graph using passed object
				graphViewData.appendData(new GraphViewData(last_x, current.kb), true);
				//Update text view with current % download
				tv.setText("Downloading " + Integer.toString((int) msg.arg1) +"%");
				//update progress bar
				pb.setProgress(msg.arg1);
				break;	
				//Update UI with stats from download as download has completed
			case COMPLETE_MESSAGE:
				//Was the Speed Test cut short?
				if(shouldContinue){
					//Speed test completed fully
					Log.i("Speed Test:", "Completed!");
					Toast.makeText(activity, "Complete", Toast.LENGTH_SHORT).show();
					last_x += 1d;
					//Append (x+1, 0) to graph to close it.
					graphViewData.appendData(new GraphViewData(last_x, 0), true);
					//Get information passed in about final download info
					final CurrentSpeedInfo finalInfo = (CurrentSpeedInfo) msg.obj;
					//Print information to TextView
					String completeMsg = "Downloaded 1MB @  " + finalInfo.kb + " kbit/s";
					completeMsg += "\nDownload took: " + Double.toString(totalDownloadTime/1000.0) + " seconds\n";
					completeMsg += "Max Speed: " + Double.toString(maxSpeed) + " kbits/s";
					//Show information
					tv.setText(completeMsg);
					//hide Progress bar
					pb.setVisibility(View.INVISIBLE);
				}else{
					//Speed test exited before completion
					Log.i("Speed Test:", "Exited before completion");
					//Alert user of this
					Toast.makeText(activity, "Speed Test not completed!", Toast.LENGTH_LONG).show();
				}
				break;	
			default:
				super.handleMessage(msg);		
			}
		}
	};

	private CurrentSpeedInfo getSpeedInfo(long dl_time, long bytes){
		//Get CurrentSpeedInfo from download time and bytes recieved
		CurrentSpeedInfo info = new CurrentSpeedInfo();
		//Work out bytes per second
		long bytes_per_sec = (bytes / dl_time) * 1000;
		//Convert to kilobits per sec
		double kilobits = info.toKBit(bytes_per_sec);
		//Convert to megabits per sec
		double megabits = info.kBitToMbit(kilobits);
		info.download_speed = bytes_per_sec;
		info.kb= kilobits;
		info.mb= megabits;
		return info;
	}

	private static class CurrentSpeedInfo{
		//Conversion from tool found at
		//http://www.matisse.net/bitcalc/
		//Byte to Kilobit
		private final double B_TO_KBIT = 0.0078125;
		//Kilobit to Megabit
		private final double KBIT_TO_MEGABIT = 0.0009765625;

		//Class variables
		public double kb = 0;
		public double mb = 0;
		public double download_speed = 0;	

		//Covert from bytes to Kilobits
		public double toKBit(double bytes){
			return bytes * B_TO_KBIT; 
		}

		//Convert from kilobits to megabits
		public double kBitToMbit(double kbit){
			return kbit * KBIT_TO_MEGABIT; 
		}

		public String toString(){
			//Simple toString() for debug
			return getClass().getName() + "[" +
			"kb=" + this.kb + ", " +
			"mb=" + this.mb + ", " +
			"download_speed=" + this.download_speed + "]";
		}
	}



}
