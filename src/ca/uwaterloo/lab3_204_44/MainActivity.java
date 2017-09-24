package ca.uwaterloo.lab3_204_44;

import android.app.Activity;

import java.io.File;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.uwaterloo.lab3_204_44.R;
import mapper.*;	
import android.os.Build;

public class MainActivity extends Activity {
	
	public Mapper mv;
	PedometerMap map;
	PointF[] path;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

		
		map = new PedometerMap();
		mv = new Mapper(getApplicationContext(), 1227, 1070, 50, 50);
		registerForContextMenu(mv);
		
		MapLoader maploader = new MapLoader();
		
		//String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/ca.uwaterloo.lab3_204_44/files";
		String file_path = "/storage/emulated/0/Android/data/ca.uwaterloo.lab3_204_44/files";
		File file = new File(file_path);

		
		map = maploader.loadMap(file, "E2-3344-Lab-room-S15-tweaked.svg");
		mv.setMap (map);
		
		

		
	
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public  void  onCreateContextMenu(ContextMenu  menu , View v, ContextMenuInfo  menuInfo) {
		super.onCreateContextMenu(menu , v, menuInfo);
		mv.onCreateContextMenu(menu , v, menuInfo); 
	
	}
	
	@Override 
	public  boolean  onContextItemSelected(MenuItem  item) {
		return  super.onContextItemSelected(item) ||  mv.onContextItemSelected(item);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
			
		}
		
		
		
		/*
		 * Created a personal OnClickListener that takes in as paraeters the Acc. Event Listener
		 * This will call the listener's reset function upon being clicked!
		 */
		
		private static class ButtonListener implements OnClickListener 
		{
			AccelerometerEventLister ael;
			MagneticFieldEventListener mag;
			ButtonListener(AccelerometerEventLister a, MagneticFieldEventListener m) {
				ael = a;
				mag = m;

				
			}

			@Override
			public void onClick(View v) {
				ael.reset();								//Reset our listener's values
				ael.stepCount = 0;	
				mag.reset();

				for (int i = 0; i < 2; i++) {
					mag.displacements[i] = 0;
				}
			}
			
		}
		
		
		private static class CalibrateButtonListener implements OnClickListener 
		{
			//AccelerometerEventLister ael;
			MagneticFieldEventListener mag;
			PositionListener pos;
			Activity activity;
			
			CalibrateButtonListener(Activity act, PositionListener p, MagneticFieldEventListener m) {
				activity = act;
				pos = p;	
				mag = m;
			}

			@Override
			public void onClick(View v) {
				pos.calibrated_angle = (int) mag.axisAngles[0];
				Toast.makeText(activity.getApplicationContext(), "Calibrated at: " + pos.calibrated_angle + " Degrees!", Toast.LENGTH_SHORT).show();;
				System.out.println(pos.calibrated_angle);
			}
			
		}


		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
			
			//Initialize rootView
			
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			MainActivity main = (MainActivity)getActivity();


		
			//Define LinearLayout and set Orientation
			LinearLayout layout = (LinearLayout)rootView.findViewById(R.id.layout);	
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(main.mv);

			
			TextView directionTextView = new TextView(rootView.getContext());
			layout.addView(directionTextView);
			
			
			//Instantiate the LineGraphView, add it to the layout and set visible
			
			LineGraphView graph = new LineGraphView(main.getApplicationContext(), 100, Arrays.asList("x","y","z"));;
			//layout.addView(graph);
			graph.setVisibility(View.VISIBLE);
						
			
			TextView stepView = new TextView(rootView.getContext());
			layout.addView(stepView);
			
			//------------------- LAB 3 CHANGES
			
			TextView rotationTextView = new TextView(rootView.getContext());
			layout.addView(rotationTextView);
			
			
			
			
			
			
			//Add the button
			Button clearButton = new Button(rootView.getContext());
			clearButton.setText("CLEAR");
			layout.addView(clearButton);
			
			Button calibrationButton = new Button(rootView.getContext());
			calibrationButton.setText("CALIBRATE");
			layout.addView(calibrationButton);

			
			//Instantiate our sensorManager - This object manages ALL sensor inputs
			SensorManager sensorManager = (SensorManager)rootView.getContext().getSystemService(SENSOR_SERVICE);
			
			
			//Instantiate a sensor of type TYPE_ACCELEROMETER and as well as it's listener
			Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			AccelerometerEventLister a = new AccelerometerEventLister(stepView,
					graph
					);

			

			

			
			
			//Register Position Listener
			PositionListener p = new PositionListener(main.mv, directionTextView);
			main.mv.addListener(p);
			
			
			
			
			//------------------- LAB 3 CHANGES
			
			Sensor magSensor = sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD));
			MagneticFieldEventListener m = new MagneticFieldEventListener(rotationTextView, a, p, graph,
					directionTextView
				);
			
			//Register our Accelerometer Listener
			sensorManager.registerListener(a, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
			sensorManager.registerListener(m, magSensor, SensorManager.SENSOR_DELAY_FASTEST);
					
					
			//------------------- LAB 3 CHANGES
			
			
			
			
			
			//Set the OnClickListener for the Clear Button
			clearButton.setOnClickListener(new ButtonListener(a,m));
			calibrationButton.setOnClickListener(new CalibrateButtonListener(main, p,m));
			
			
			
			return rootView;	
		}
	}
}
