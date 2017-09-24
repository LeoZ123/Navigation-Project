package ca.uwaterloo.lab3_204_44;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class RotationVectorEventListener implements SensorEventListener {
	
	TextView output;

	//like se.values, 0 corresponds to X, 1 corresponds to 2, Z corresponds to 3
	public float[] maxvalues = new float[3];
	
	public RotationVectorEventListener(TextView outputView){
	output = outputView;

	}
	
	public void onAccuracyChanged(Sensor s, int i) {}
	
	
	public void onSensorChanged(SensorEvent se) {
	if (se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
		
		//For loop that checks for a new high record
		for (int i = 0; i < 3; i++) {
			if (Math.abs(se.values[i]) > maxvalues[i]) {
				maxvalues[i] = Math.abs(se.values[i]);
			}
			
		}
		
		//Output the current values of the Rotation Vector
		String str = String.format("Rotation Vector X: " + "%.4f" + "\n" + "Rotation Vector Y: " + 
			     "%.4f" + "\n" + "Rotation Vector Z:" + "%.4f" + "\n" + "--- --- --- --- --- --- --- --- --- --- --- ---",
			     se.values[0], se.values[1], se.values[2]);

		
		output.setText(str);
	
	
	
	}
	}
	}
