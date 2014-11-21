package fortyonepost.com.ag;//Created by: DimasTheDriver on Apr/27/2010. Available at: http://www.41post.com/?p=3745

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
 
import android.app.Activity;
import android.content.Intent;
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
	
	private static final int MAX_RUN_AVG = 100;
	public static final String EXTRA_MESSAGE = null;
	private double[] runningAverage = new double[MAX_RUN_AVG];
	private int arrayCnt = 0;
	
	
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
    	//HandlerThread mHandlerThread = new HandlerThread("sensorThread");
    	//mHandlerThread.start();
    	//Handler handler = new Handler(mHandlerThread.getLooper());
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL);
     
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL);
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
		/*
		System.out.println(String.format("Adding: %f to running average", accMagOrientation[0]));
		runningAverage[arrayCnt] = Math.toDegrees(accMagOrientation[0]); 
		if (arrayCnt >= MAX_RUN_AVG) {
			arrayCnt++;
		} else {
			arrayCnt = 0;
		}

		double sum = 0;
		for (double d : runningAverage) {
			sum += d;
		}
		double run_avg = sum / MAX_RUN_AVG;

		System.out.println(String.format("runningAverage.length: %f", runningAverage.length));
		System.out.println(String.format("run_avg: %f", run_avg));
		*/
		tv.setText(String.format(
				"Orientation X (Yaw) :%f\n" + 
				"Orientation Y (Roll) :%f\n" +
				"Orientation Z (Pitch) :%f",
				Math.toDegrees(accMagOrientation[0]), Math.toDegrees(accMagOrientation[1]), 
				Math.toDegrees(accMagOrientation[2])));

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
	
	/** Called when the user clicks the displayVideo button */
	public void displayVideo(View view) {
	    // Do something in response to button
		Intent myintent = new Intent(this, videoStream.class);
		final int result = 1;
		myintent.putExtra(EXTRA_MESSAGE, "Hello This is my string");
		startActivityForResult(myintent, result);
	}
	
}