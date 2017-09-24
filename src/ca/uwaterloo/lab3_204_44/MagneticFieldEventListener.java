package ca.uwaterloo.lab3_204_44;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.TextView;

public class MagneticFieldEventListener implements SensorEventListener {
	


	
	public float maxazimuth;
	public float[] values;
	TextView rotationTextView;
	AccelerometerEventLister a;
	
	 float[] rotationmatrix;
	 float[] R;
	 float[] I;
	 float[] smoothedAzimuth;
	 float[] axisAngles;
	 public float[] displacements;
	 

	LineGraphView l;
	
	float stepUnit;
	float baseline;
	float averagingAcc;
	int sampleCount;
	boolean changedisp = true;
	
	PositionListener pos;
	TextView directionTextView;
	
	
	public MagneticFieldEventListener(TextView rotationTextView, AccelerometerEventLister a,
			PositionListener pos, LineGraphView lineGraph, TextView directionTextView){
		values = new float[3];
		this.directionTextView = directionTextView;
		this.rotationTextView = rotationTextView;
		this.a = a;
		this.pos = pos;
		I = new float[9];
		R = new float[9];
		smoothedAzimuth = new float[3];
		rotationmatrix =  new float[3];
		axisAngles = new float[2];				//North = 0; East = 1;
		displacements = new float[2];
		maxazimuth = 0;
		stepUnit = 0.85f;
		l = lineGraph;
		
		baseline = 0;
		sampleCount = -1; //Initialize on startup state
		averagingAcc = 0;
		displacements[0] = 0;
		displacements[1] = 0;
		

		
		
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	

//----------------------------------------------------------------------------------------------------------------------

	
//----------------------------------------------------------------------------------------------------------------------
//SECTION A: RESET FUNCTION
	
	
	/*
	 * Reset is called at specific points, I.e. button clear to reset our values
	 * This includes our accumulation, sampleCount
	 */
	
	public void reset() {
		maxazimuth = 0;
		baseline = 0;
		sampleCount = -1;
		averagingAcc = 0;
	
	}


//----------------------------------------------------------------------------------------------------------------------	
	
//----------------------------------------------------------------------------------------------------------------------
//SECTION B: RECEIVING ORIENTATION DATA

	/*
	 * OnSensorChanged function: This first section essentially retreives our orientation data
	 * and stores it into an accessible array. The following sections will do sampling operations 
	 * to ensure more accurate predictions
	 */
	
	public void onSensorChanged(SensorEvent se) {
		
	if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
	
		
		
		
		
		//For loop that checks for a new high record
		for (int i = 0; i < 3; i++) {
			values[i] = se.values[i];
			
		}
		 
	

	
		//Define boolean pass, true if we can retrieve a rotation matrix
		//Uses smoothed values from the accelerometer and the magnetic field values 
		
		boolean pass = SensorManager.getRotationMatrix(R, I, a.smoothed, this.values);
		
		if (pass) {
		
			
		//Call getOrientation to store the results in the variable rotationmatrix
			SensorManager.getOrientation(R, rotationmatrix);
			
			
//----------------------------------------------------------------------------------------------------------------------
		

//----------------------------------------------------------------------------------------------------------------------
//SECTION C: SAMPLING ALGORITHM; DISPLACEMENT CALCULATIONS
			
		/*
		 * Here lies the sampling algorithm. The samplecount will start as -1, indicating
		 * that there has been no starting point yet.
		 * The algorithm works by determining a "baseline" value of the azimuths.
		 * 
		 * Then, for X calls of this function, we will accumulate in the variable averagingAcc
		 * the difference between the current value and the baseline value. 
		 * 
		 * After X calls, averaging the accumulation and add the result (which is signed) to
		 * the baseline. This value becomes our new "baseline" and the process repeats cyclically.
		 */
			
			
			
			/*
			 * When no value has been initialized, assign our baseline (in degrees), initializing
			 * smoothed values as well
			 */
			
			if (sampleCount == -1) { 
				rotationmatrix[0] = roundten((float) Math.toDegrees(rotationmatrix[0]));
				baseline = rotationmatrix[0];
				
				sampleCount = 0;
			
			} else {						//If we are current counting
				
				rotationmatrix[0] = roundten((float) Math.toDegrees(rotationmatrix[0]));	
				
				//Add to accumulator that will be averaged
				
				averagingAcc += ( rotationmatrix[0] - baseline);	
				sampleCount++;
				
				
				 
				/*
				 * If we cross the -180/180 border, then reset our counting.
				 * This is a general purpose way of avoiding any mismatch in counting
				 */
				
				if ( rotationmatrix[0] <= -160 && baseline >= 160) {
					reset();
				} else if ( rotationmatrix[0] >= 160 && baseline <= 160) {
					reset();
				}
				
				
				/*
				 * If the phone's position is not held properly, i.e. Pitch is not in the right range,
				 * then reset. This is because the a tilt in the phone actually drastically changes azimuth values
				 * that is not correlated to it's actual rotation
				 */
			/*	
				if (rotationmatrix[1] < -1.55 || rotationmatrix[1] > -1.05) {
					changedisp = false;
					axisAngles[0] = 0;
					axisAngles[1] = 0;
					reset();
				} else {
					changedisp = true;
					
				}*/
				
				

			
//----------------------------------------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------------------------------------
//SECTION D: SAMPLING ALGORITH II - @ MAX COUNT
				
				
/*
* Reactive code after 25 events. It first averages our accumulation, and then
* adds that signed average distance to our baseline, redefining our baseline.
* 
* Furthermore, this snippet of code will address any rare cases that our baseline
* or axisAngles exceed our expected range of -180 to 180. Simply wrap the values
* back around the scale of -180-180.
*/
				
				
				if (sampleCount == 25) {

				averagingAcc = averagingAcc / 25;
				baseline += averagingAcc;
				baseline = roundten(baseline);
				
				if (a.completedStep && changedisp == true) {
					a.completedStep = false;
					displacements[0]  += nearesttenth(stepUnit*(float)Math.cos(Math.toRadians(roundtwofour(baseline))));
					displacements[1]  += nearesttenth(stepUnit*(float)Math.sin(Math.toRadians(roundtwofour(baseline))));
				
				
				//MOVE LOCATION
					pos.onStep(pos.map, baseline);
				
				
				}
			
				
				//Handle obscure case that perhaps baseline > 180 or baseline < -180
				if (baseline > 180) {
					baseline = baseline - 360;
				} else if (baseline < -180) {
					baseline = baseline + 360;
				}
				
				
				
				//Convert to North and East and Rounds to the nearest 10.
				//axisAngles[0] = Math.round((baseline/10.0f))*10;
				//axisAngles[1] = Math.round((baseline/10.0f))*10 - 90;
				//baseline = axisAngles[0];
				axisAngles[0] = roundten(baseline);
				axisAngles[1] = roundten(baseline) - 90;
				baseline = axisAngles[0];
				
				//Handle case when eastAngle is not within range
				if (axisAngles[1] > 180) {
					axisAngles[1] = axisAngles[1] - 360;
				} else if (axisAngles[1] < -180) {
					axisAngles[1] = axisAngles[1] + 360;
				}
				
				
				
				
				//Reset our sampleCount and our averaging accumulation
				sampleCount = 0;
				averagingAcc = 0;
				
				
				}
				
				
				
			}
	
			
			
//SECTION LAB 4

			
//
			
//----------------------------------------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------------------------------------
//SECTION E: DISPLAY AND OUTPUT

			
			String rotation_string = String.format("North: " + "%.0f" + "\nEast: " + "%.0f"
					  + "\nDisplacement N: " + "%.2f" + "\nDisplacement E: " + "%.2f", axisAngles[0], axisAngles[1], displacements[0], displacements[1]);
			rotationTextView.setText(rotation_string);
		} else {
			rotationTextView.setText("0");
		}
	
		pos.calculateApparentNextPath(pos.map, axisAngles[0]);	
	}
	}
	
	
	public float roundfive(float f) {
		return 5*(Math.round(f/5));
	}
	
	public float roundten(float f) {
		return 10*(Math.round(f/10));
	}
	
	public float roundtwofour(float f) {
		return 24*(Math.round(f/24));
	}
	
	public float nearesttenth(float f) {
		return (roundten(100.0f*f))/100.0f;
	}
	}
