package fortyonepost.com.ag;//Created by: DimasTheDriver on Apr/27/2010. Available at: http://www.41post.com/?p=3745

import android.R.integer;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.apache.http.client.CircularRedirectException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.os.Message;

public class AccessGyroscope extends Activity implements SensorEventListener {
	
	//a TextView
	private TextView tv;
	//the Sensor Manager
	private SensorManager sManager;
	
    // magnetic field vector
    private float[] magnet = new float[3];
 
    // accelerometer vector
    private float[] accel = new float[3];
 
    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];
 
    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];
    
	private Socket client;
	private PrintWriter printwriter;
	private EditText serverIP, serverPort;
	private ToggleButton toggleButton1;
	private String messsage;
	int port = 0;
	private String SERVER_IP1;
	private boolean onOffToggle;
	private Thread th;
	public SendMsg sendmsg = null;
	
	
	private static final int THRESH_HOLD = 10;
	
	private double runAvg = 0;
	
	private static final int DEFAULT = 0;
	private static final int RUN_AVG = 1;
	private static final int ARR = 2;
	private static final int SENSOR = RUN_AVG;
	
	private static final int MAX_ARR = 100000;
	private static int arrCnt = 0;
	private static double[] runAvgArr = new double[MAX_ARR];
	
	private ArrayList<Double> expAvg = new ArrayList<Double>(10);
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //get the TextView from the layout file
        tv = (TextView) findViewById(R.id.tv);
        
       // textField = (EditText) findViewById(R.id.editText1); // reference to the text field
        serverIP = (EditText) findViewById(R.id.editText2); // reference to the text field
        serverPort = (EditText) findViewById(R.id.editText3); // reference to the text field
        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1); // reference to the send button
        
        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();
        
     // Button press event listener
        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	onOffToggle = isChecked;
		        if (isChecked) {  
		        	/* 
					This creates the class and executes the send message. 
		        	*/
		        	
		        	System.out.println("STARTING A CONNECTION");
		        	
		        	String localserverIP = serverIP.getText().toString();
		        	System.out.println(localserverIP);
		        	
		        	int localserverPort = Integer.parseInt(serverPort.getText().toString());
		        	
		        	System.out.println("INIT CONNECTION");
		        	sendmsg = new SendMsg(localserverIP, localserverPort);
		        	
		        	System.out.println("CREATING THREAD");
		        	th = new Thread(sendmsg);
		        	System.out.println("STARTING THREAD");
		        	th.start();
		        	System.out.println("THREAD STARTED");
		        	
		        	
		        } else {
		        	System.out.println("Toggle stop");
		        	System.out.println("Cleraing the queue");
		        	sendmsg.queue.clear();
		        	System.out.println("Sending the shutdown command");
		        	sendmsg.queue.offer("stop");
//		        	sendmsg.stop = true;
		        	System.out.println("Waiting for thread to stop");
		        	try {
						th.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	System.out.println("Thread has stopped!");
		        }
            }
        });
    }
    //when this Activity starts
    @Override
	protected void onResume() 
	{
		super.onResume();
		/*register the sensor listener to listen to the gyroscope sensor, use the 
		 * callbacks defined in this class, and gather the sensor information as  
		 * quick as possible*/
		initListeners();
	}

    //When this Activity isn't visible anymore
	@Override
	protected void onStop() 
	{
		super.onStop();
		//unregister the sensor listener
		sManager.unregisterListener(this);
		
	}

    // This function registers sensor listeners for the accelerometer, magnetometer and gyroscope.
    public void initListeners(){
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST);
     
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST);
    }
    
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) 
	{
		//Do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		switch(event.sensor.getType()) {
	    case Sensor.TYPE_ACCELEROMETER:
	        // copy new accelerometer data into accel array and calculate orientation
	        System.arraycopy(event.values, 0, accel, 0, 3);
	        calculateAccMagOrientation();
	        break;
	 
	    case Sensor.TYPE_MAGNETIC_FIELD:
	        // copy new magnetometer data into magnet array
	        System.arraycopy(event.values, 0, magnet, 0, 3);
	        break;
	    }
		
		switch (SENSOR) {
		case RUN_AVG:
			runAvg = calculateRunAvg(Math.toDegrees(accMagOrientation[0]), runAvg, 180);
			runAvg = runAvg - runAvg % 3;  // restrict movement by groups of 3 degrees
			break;
		case DEFAULT:
			runAvg = Math.toDegrees(accMagOrientation[0]);
			runAvg = runAvg - runAvg % 3;
			break;
		default:
			runAvg = calcRunAvgArr(Math.toDegrees(accMagOrientation[0]));
			break;
		}

		tv.setText(String.format(
				"Orientation X (Yaw) :%d\n" + 
				"Orientation Y (Roll) :%d\n" +
				"Orientation Z (Pitch) :%d",
				(int) runAvg, (int) Math.toDegrees(accMagOrientation[1]), 
				(int) Math.toDegrees(accMagOrientation[2]) + 180));

		messsage = String.format("%.5f,%.5f", 
				Math.toDegrees(accMagOrientation[0]), 
				Math.toDegrees(accMagOrientation[2]));

		if (sendmsg == null) {
			return;
		}

		if (onOffToggle) {
			System.out.println("Updatting queue");
			sendmsg.queue.offer(messsage);
		}
	}
	
	// calculates orientation angles from accelerometer and magnetometer output
	public void calculateAccMagOrientation() {
		if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
		}
	}
	
	private double calculateRunAvg(double cur_value, double prev_val, int offset) {
	/*  Calculates an equally weighted running average 
	 * 
	 * */
		return (prev_val + (cur_value + offset)) * 0.5;
	}

	private double calcRunAvgArr(double cur_val) {
	/*  Calculates an equally weighted running average 
	 * 
	 * */
		int sum = 0;
		
		runAvgArr[arrCnt] = cur_val;
		arrCnt++;
		if (arrCnt > runAvg) {
			arrCnt = 0;
		}
		
		for (int i = 0; i < runAvgArr.length; i++) {
			sum += runAvgArr[i];
		}
		return sum / runAvgArr.length;
	}

	
	private double calcExpAvg(ArrayList<Double> circ_buf, double cur_val) {
		double sum = 0;	

		System.out.printf("circular buffer Size %d", circ_buf.size());
		circ_buf.add(cur_val);

		for (int i = 0; i < circ_buf.size(); i++) {
			sum += circ_buf.get(i) * Math.exp(-i);
		}
		sum = sum / circ_buf.size();
		java.util.Collections.rotate(circ_buf, 1);
		return sum;
	}
}