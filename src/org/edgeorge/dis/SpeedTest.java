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
	private GraphViewSeries graphViewData; 
	private double last_x = 1.0d;

	//1MB = 1024 Bytes * 1024 Bytes = 2^20
	//1MB = 1024 KiloBytes = 1048576 Bytes = 8388608 bits 
	private final int EXPECTED_SIZE = 1048576;

	private TextView tv;
	private Button btn;
	private ProgressBar pb;
	private InputStream is = null;
	private final int UPDATE_OCCURED_MESSAGE = 0;
	private final int COMPLETE_MESSAGE = 1;
	private final int DL_TIME_THRESHOLD = 300;
	private long totalDownloadTime;
	private double maxSpeed = 0.0d;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
			if(is != null){
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private final Runnable downloadRun = new Runnable(){

		public void run() {
			InputStream stream = null;
			try {
				pb.setVisibility(View.VISIBLE);
				pb.setProgress(0);

				String dl_url = "http://www.jince.co.uk/dummy.txt";	
				URL url = new URL(dl_url);
				URLConnection con = url.openConnection();
				con.setUseCaches(false);
				stream = con.getInputStream();

				long start = System.currentTimeMillis();
				int currentByte = 0;
				long updateStart = System.currentTimeMillis();
				long updateTime = 0;
				int bytesInThreshold = 0;
				int bytesRecieved = 0;

				while((currentByte = stream.read()) != -1){	
					bytesRecieved++;
					bytesInThreshold++;
					if(updateTime >= DL_TIME_THRESHOLD){
						int progress=(int)((bytesRecieved/(double)EXPECTED_SIZE)*100);
						Message msg=Message.obtain(mHandler, UPDATE_OCCURED_MESSAGE, getSpeedInfo(updateTime, bytesInThreshold));
						msg.arg1 = progress;
						mHandler.sendMessage(msg);
						//Reset
						updateStart=System.currentTimeMillis();
						bytesInThreshold = 0;
					}
					updateTime = System.currentTimeMillis() - updateStart;
				}

				totalDownloadTime = (System.currentTimeMillis() - start);

				Message msg = Message.obtain(mHandler, COMPLETE_MESSAGE, getSpeedInfo(totalDownloadTime, bytesRecieved));
				msg.arg1 = bytesRecieved;
				mHandler.sendMessage(msg);
			} 
			catch (Exception e){
				Log.e("Speed Test", e.getMessage());
			}finally{
				try {
					if(stream!=null){
						stream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	};

	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(final Message msg) {
			switch(msg.what){
			
			case UPDATE_OCCURED_MESSAGE:
				final CurrentSpeedInfo current = (CurrentSpeedInfo) msg.obj;
				if(current.kb > maxSpeed){
					maxSpeed = current.kb;
				}
				last_x += 1d;
				//Append new latest Signal Data to graph (uses SpeedInfo method)
				graphViewData.appendData(new GraphViewData(last_x, current.kb), true);
				tv.setText("Downloading " + Integer.toString((int) msg.arg1) +"%");
				pb.setProgress(msg.arg1);
				break;		
			case COMPLETE_MESSAGE:
				last_x += 1d;
				//Append to graph to close it.
				graphViewData.appendData(new GraphViewData(last_x, 0), true);
				final CurrentSpeedInfo finalInfo = (CurrentSpeedInfo) msg.obj;
				String completeMsg = "Downloaded 1MB @  " + finalInfo.kb + " kbit/s";
				completeMsg += "\nDownload took: " + Double.toString(totalDownloadTime/1000.0) + " seconds\n";
				completeMsg += "Max Speed: " + Double.toString(maxSpeed) + " kbits/s";
				tv.setText(completeMsg);
				Toast.makeText(getBaseContext(), "Complete", Toast.LENGTH_SHORT).show();
				pb.setVisibility(View.INVISIBLE);
				break;	
			default:
				super.handleMessage(msg);		
			}
		}
	};

	private CurrentSpeedInfo getSpeedInfo(long dl_time, long bytes){
		CurrentSpeedInfo info = new CurrentSpeedInfo();
		//from mil to sec
		long bytes_per_sec = (bytes / dl_time) * 1000;
		double kilobits = info.toKBit(bytes_per_sec);
		double megabits = info.kBitToMbit(kilobits);
		info.download_speed = bytes_per_sec;
		info.kb= kilobits;
		info.mb= megabits;
		return info;
	}

	private static class CurrentSpeedInfo{
		//Byte to Kilobit
		private final double B_TO_KBIT = 0.0078125;
		//Kilobit to Megabit
		private final double KBIT_TO_MEGABIT = 0.0009765625;

		public double kb = 0;
		public double mb = 0;
		public double download_speed = 0;	
		
		public double toKBit(double bytes){
			return bytes * B_TO_KBIT; 
		}
		
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
