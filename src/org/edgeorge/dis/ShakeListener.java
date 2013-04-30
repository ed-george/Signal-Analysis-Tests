package org.edgeorge.dis;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

//Adapted from Source: http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it/5117254#5117254
public class ShakeListener implements SensorEventListener {

	//Minimum require force to register
	private static final int MIN_REQ_FORCE = 10;
	//Minimum times in a shake gesture that the direction of movement needs to change.
	private static final int MIN_CHANGE = 3;
	//Maximum pause between movements.
	private static final int MAX_PAUSE = 200;
	//Maximum allowed time for shake gesture.
	private static final int MAX_SHAKE_DURATION = 400;
	//Time when the first and last gesture started and finished
	private long _firstGestureTime = 0;
	private long _lastGestureTime;
	//Amount of movements currently
	private int _directionCount = 0;
	//Last x, y and z positions
	private float _x = 0;
	private float _y = 0;
	private float _z = 0;
	//OnShakeListener called when shake is detected
	private OnShakeListener _sl;

	//Interface for shake listener.
	public interface OnShakeListener {
		void onShake();
	}

	public void setOnShakeListener(OnShakeListener listener) {
		_sl = listener;
	}

	public void onSensorChanged(SensorEvent se) {
		//Receive sensor data
		float x = se.values[0]; //x
		float y = se.values[1]; //y
		float z = se.values[2]; //z

		//Calculate the total movement of device
		float totalMovement = Math.abs(x + y + z - _x - _y - _z);

		//Is the movement greater than the minimum force? i.e. Shake Occurred? 
		if (totalMovement > MIN_REQ_FORCE) {

			//Get Current time
			long currentTime = System.currentTimeMillis();

			//Store time of the first movement
			if (_firstGestureTime == 0) {
				_firstGestureTime = currentTime;
				_lastGestureTime = currentTime;
			}

			//Check time since last movement
			long timeSinceLastMovement = currentTime - _lastGestureTime;
			if (timeSinceLastMovement < MAX_PAUSE) {

				//Update change count and last changed time
				_lastGestureTime = currentTime;
				_directionCount++;

				//Store last movement data
				_x = x;
				_y = y;
				_z = z;

				//Check current amount of movements detected
				if (_directionCount >= MIN_CHANGE) {

					//Check duration
					long total = currentTime - _firstGestureTime;
					if (total < MAX_SHAKE_DURATION) {
						//Call on Shake method
						_sl.onShake();
						//Reset params
						reset();
					}
				}

			} else {
				reset();
			}
		}
	}

	//Reset params
	private void reset() {
		_firstGestureTime = 0;
		_directionCount = 0;
		_lastGestureTime = 0;
		_x = 0;
		_y = 0;
		_z = 0;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}