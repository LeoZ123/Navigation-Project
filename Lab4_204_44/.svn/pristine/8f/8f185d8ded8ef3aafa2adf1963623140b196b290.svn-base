package ca.uwaterloo.lab3_204_44;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class LightSensorEventListener implements SensorEventListener {
	
	TextView output;
	
	public LightSensorEventListener(TextView outputView){
	output = outputView;
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	
	public void onSensorChanged(SensorEvent se) {
	if (se.sensor.getType() == Sensor.TYPE_LIGHT) {
		
		//Format a string that prints out the sensor value
		String str = String.format("%.4f",se.values[0]);
		output.setText("Light Sensor: " + str + "\n" + "--- --- --- --- --- --- --- --- --- --- --- ---");
		
	}
	
	}
	}
