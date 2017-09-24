package ca.uwaterloo.lab3_204_44;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class AccelerometerEventLister implements SensorEventListener {
	
	TextView xyzOutput;
	TextView stepCountOutput;
	LineGraphView graph;
	
	
	WalkingState state;
	WalkingState stateCyclePos;
	
	
	boolean inStateCycle = false;
	int stepCount;
	public float[] smoothed;
	public float[] values;
	
	float previousAccY;
	float currentAccY;
	float deltaY;
	int accCounter;
	
	float previousAccZ;
	float currentAccZ;
	
	float previousAccX;
	float currentAccX;
	float deltaX;
	
	float deltaZ;

	float maxDeltaZ;
	
	
	//Lab 3 change: 
	public boolean inFirstStep;
	public boolean completedStep;
	public boolean FirePositionListener = false;
	

	
	public AccelerometerEventLister(TextView stepCountView, LineGraphView lineGraph){
	stepCountOutput = stepCountView;
	graph = lineGraph;

	
	//Start walking state in our cycle at a "static", i.e. NOT MOVING
	stateCyclePos = WalkingState.STATIC; 
	
	
	//Counts how many steps there have been, set to 0
	stepCount = 0;
	
	
	//Initialize smoothed values to 0
	smoothed = new float[3];
	for (int i = 0; i < 3; i++) {
		smoothed[i] = 0.0f;
	}
	values = new float[3];

	//Initialize delta, which contains the change in sampled values over two data points
	deltaX = 0;
	deltaY = 0;
	deltaZ = 0;
	
	//AccCounter counts how many values have been summed before averaging - Used to sample
	accCounter = 0;
	
	//Store previous and current X,Y,Z values; All initialized to 0 at first
	previousAccX = 0;
	currentAccX = 0 ;
	previousAccY = 0;
	currentAccY = 0;
	previousAccZ = 0;
	currentAccZ = 0;
	


	
	/*
	 * Initialize the dynamic state to STATIC state 
	 * This refers to the given state at a moment, not necessarily state in the directed cycle
	 */
	
	state = WalkingState.STATIC;
	
	
	
	/*
	 * Initialize the First Step to false; This is for the MgListener to track the direction 
	 * of the step BEFORE values get spiked by the step motion
	 */
	inFirstStep = false;
	completedStep = false;
	
	
	}
	
	
	
	
	
//-------------------------------------------------------------------------------------------------
	//ENUMERATION FOR Walking
	
	/*
	 * The following code is a public enum for our WalkingState. I.e., this essentially creates an object-like
	 * enumeration that allows us to associate states with a numerical value that is masked with a string.
	 * In this case, "STATIC", "POSITIVE RISING", "POSITING FALLING", "NEGATIVE RISING", "NEGATIVE FALLING"
	 */
	
	public enum WalkingState {
		STATIC(0),
		POSITIVE_RISING(1),							//Positive Increasing Slope
		POSITIVE_FALLING(2),						//Positive Decreasing Slope
		NEGATIVE_RISING(3),							//Negative Increasing Slope
		NEGATIVE_FALLING(4);						//Negative Decreasing Slope
		
		int numericalValue;
		WalkingState(int type) {
			this.numericalValue = type;
		}
		
		int getNumericalValue() {
			return numericalValue;
		}
	}
	
	
	
	
//-------------------------------------------------------------------------------------------------
	//RESET FUNCTION
	
	/*
	 * The reset function is called when a) the clear button is clicked or b) their is sudden rotation/shaking
	 * This code snipped just resets our states to static, and resets all relevant values = 0;
	 */
	public void reset() {
		
		state = WalkingState.STATIC;
		stateCyclePos = WalkingState.STATIC;
		inStateCycle = false;
		accCounter = 0;
		
		previousAccX = 0;
		currentAccX = 0;
		deltaX = 0;
		
		previousAccY = 0;
		currentAccY = 0;
		deltaY = 0;

		previousAccZ = 0;
		currentAccZ = 0;
		deltaZ = 0;
		
		inFirstStep = false;
		completedStep = false;
	}
	

//-------------------------------------------------------------------------------------------------

	
	public void onAccuracyChanged(Sensor s, int i) {}
	

//-------------------------------------------------------------------------------------------------
	//CRUX OF CODE: ONSENSORCHANGED 
	
	
	public void onSensorChanged(SensorEvent se) {
	if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

		values = se.values;
		
	//Initialize our smoothing constant to 14;
	
	final float c =14f;
	
	//Set our smoothed values equal to below equation, using our smoothing constant c
	
	for (int i = 0; i < 3; i++) {
		smoothed[i] += (se.values[i] - smoothed[i]) / c;
	}


	//Add smoothed value points onto the graph
	
	//graph.addPoint(smoothed);
	
	
	//Define a boolean for positive/negative; a boolean for a larger delta
	boolean isPositive = false;
	boolean largerDelta = false;
	
	
	
	accCounter++;													//Increment the accCounter by 1
	currentAccX += smoothed[0];
	currentAccY += smoothed[1];									//Add the value to our accumulated sum
	currentAccZ += smoothed[2];


	
//-------------------------------------------------------------------------------------------------
	//CODE WHEN SAMPLING 4 POINTS IS COMPLETE
	
	if (accCounter == 4) {											//Once we've added 4 points
		accCounter = 0;												//Reset the counter
		currentAccX = currentAccX / 4;
		currentAccY = currentAccY / 4;								//Average values by 4
		currentAccZ = currentAccZ / 4;
		
		/*
		 * ADDED RECENTLY
		 */
		

		
		
		
		
		
	
		if (currentAccY - previousAccY > 0) {						//If the new value is larger than the previous
			isPositive = true;										//Set isPositive to true
		} 
		
		if (Math.abs(currentAccY - previousAccY) > deltaY) {		//If the absolute change is bigger than delta
			largerDelta = true;										//set largerDelta to true
		}
		
		
																	//Set delta to the new delta
		deltaX = Math.abs(currentAccX - previousAccX);
		deltaY = Math.abs(currentAccY - previousAccY);	
		deltaZ = Math.abs(currentAccZ - previousAccZ); 
		
		
		/*
		 * Case by case situations to determine which state we are currently in based 
		 * on whether our change was positive (which allows to determine the sign of the derivative 
		 * and whether the delta increased (which allows to determine how the derivative is changing)
		 */
		
		
		if (isPositive == true && largerDelta == true) {			
			 state = WalkingState.POSITIVE_RISING;
		} else if (isPositive == true && largerDelta == false) {
			 state = WalkingState.POSITIVE_FALLING;
		} else if (isPositive == false && largerDelta == true) {
			 state = WalkingState.NEGATIVE_RISING;
		} else {
			 state = WalkingState.NEGATIVE_FALLING;
		}
	
		
		/*
		 * Threshold using delta values. If our Y delta is lower than a certain value, ignore it is a step.
		 * If deltaX or deltaZ is greater than 2, then reset the entire situation
		 * This ensures that we Do NOT account any sudden shifts, turns or shakes as steps
		 */
		
			if (deltaY < 0.065) {
				state = WalkingState.STATIC;
			}
		
			if (deltaZ > 2) {
				reset();
			}
		
			if (deltaX > 2) {
				reset();
			}
			
		/*
		 * Switch statement that will essentially ensure that our statement machine follows that
		 * when going from state to another, a state increase will only occur when the next
		 * expected state occurs. I.e. we want to follow a directed cycle where 1 -> 2 -> 3 -> 4
		 * in some form - this allows for some noise in between. As long as the sequence of 4 is seen
		 * We will consider it a step
		 */
		
		switch (state) {
		
		
		case POSITIVE_RISING:
		
			if (inStateCycle == false) {								//If not in a cycle yet
				stateCyclePos = state;									//Set state
				inStateCycle = true;									//Now in a cycle
				
				inFirstStep = true;										//Tell MgEventListener to recognize step
			}
		
			break;
			
		case POSITIVE_FALLING:
		
			if (stateCyclePos == WalkingState.POSITIVE_RISING) {		//Check if in proper state
				stateCyclePos = state;									//Update current state
			}
			
			break;
			
		case NEGATIVE_RISING:
			if (stateCyclePos == WalkingState.POSITIVE_FALLING) {		//Check if in proper state
				stateCyclePos = state;									//Update current state
			}
			
			break;
			
		case NEGATIVE_FALLING:
			if (stateCyclePos == WalkingState.NEGATIVE_RISING) {		//Check if in proper state
				stateCyclePos = state;									//Update state
				stepCount++;											//Increase step counter
				inStateCycle = false;									//Exit Cycle
				completedStep = true;
				FirePositionListener = true;
			}
			
			break;
		
		}
			
	
		/*
		 * Reset our previous,current X,Y and Z values. That is, previous is now equal to the current 
		 * and resent our current back to 0. This is because we're going to sum the next 4 steps into
		 * currentAccX/Y/Z and therefore we must return to 0 to ensure we're not overcounting. 
		 */
		
		previousAccX = currentAccX;
		currentAccX = 0;
		
		previousAccY = currentAccY;
		currentAccY = 0;
	
		previousAccZ = currentAccZ;
		currentAccZ = 0;
	}	//End bracket of when AccCounter == 4
	
	
	
	
		//Output the current values of the Accelerometer
		String str = String.format("Accelerometer X: " + "%.4f" + "\n" + "Accelerometer Y: " + 
	    "%.4f" + "\n" + "Accelerometer Z:" + "%.4f" + "\n" + "--- --- --- --- --- --- --- --- --- --- --- ---", 
	    smoothed[0], smoothed[1], smoothed[2]);
		
		
		//Format the string for the max values of the Accelerometer
		String StepString = String.format("StepCount: " + "%d", stepCount);
		
		
		stepCountOutput.setText(StepString);
		
		
		
	
		
	
		
		
		
		
		
	}
	}
	

	
	}
