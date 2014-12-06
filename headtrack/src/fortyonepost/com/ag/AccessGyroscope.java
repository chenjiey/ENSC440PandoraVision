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
	private EditText serverIP, serverPort, oneVideo, twoVideo1, twoVideo2;
	private ToggleButton toggleButton1;
	private String messsage;
	int port = 0;
	private String SERVER_IP1;
	private boolean onOffToggle;
	
	private boolean thread_running = false;
	private Thread th;
	public SendMsg sendmsg = null;
	
	private static final int RUN_AVG = 0;
	private static final int DEFAULT = 1;
	private int SENSOR = RUN_AVG;
	
	private static final int MAX_RUN_AVG = 100;
	public static final String EXTRA_MESSAGE = null;
	public static final String EXTRA_MESSAGE2 = null;
	public static final String EXTRA_MESSAGE3 = null;
	private double[] runningAverage = new double[MAX_RUN_AVG];
	private int arrayCnt = 0;
	private double runAvg = 0;
	private double[] runAvgArr = new double[MAX_RUN_AVG];
	private String videoURL = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //get the TextView from the layout file
        tv = (TextView) findViewById(R.id.tv);
        
        oneVideo = (EditText) findViewById(R.id.oneVideoLink); // reference to the text field
        twoVideo1 = (EditText) findViewById(R.id.twoVideoLink1); // reference to the text field
        twoVideo2 = (EditText) findViewById(R.id.twoVideoLink2); // reference to the text field
        serverIP = (EditText) findViewById(R.id.editText2); // reference to the text field
        serverPort = (EditText) findViewById(R.id.editText3); // reference to the text field
        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1); // reference to the send button
        
        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();

     // Button press event listener
        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		startSendingMessages(buttonView, isChecked);
        		}
            }
        );
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
		
		/* (Jeremy) Nov 23: I was playing around with different ways of averaging
		 * the output. I'm pretty convinced that we need to mix this with the 
		 * Sensor Fusion algorithm*/
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

//		messsage = String.format("%d,%d", 
//				(int) Math.toDegrees(accMagOrientation[0]), 
//				(int) Math.toDegrees(accMagOrientation[2]));
		messsage = String.format("%d,%d", (int) runAvg, ((int) Math.toDegrees(accMagOrientation[2]) + 180));
//		messsage = (int) runAvg;
		
		if (sendmsg == null) {
			return; // if send message hasn't been initialized don't send
		}

		if (onOffToggle) {
//			System.out.println("Updatting queue");
			sendmsg.queue.offer(messsage);
//			sendmsg.queue.offer((int) runAvg);
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
		runAvgArr[arrayCnt] = cur_val;
		arrayCnt++;
		if (arrayCnt > runAvg) {
			arrayCnt = 0;
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
	/** Called when the user clicks the displayVideo button */
	public void displayVideo(View view) {
	    // Do something in response to button
		//Link to another activity
		Intent myintent = new Intent(this, videoStream.class);
		final int result = 1;
		String sendURL = null;
		// If user doesn't enter any URL, use the default URL
		if (oneVideo.getText().toString().matches("")) {
			sendURL = videoURL;	
		} else {
			sendURL = oneVideo.getText().toString();
		}

		myintent.putExtra(EXTRA_MESSAGE, sendURL);
		startActivityForResult(myintent, result);
	}
	
	/** Called when the user clicks the displayTwoVideo button */
	public void displayTwoVideo(View view) {
	    // Do something in response to button
		//Link to another activity
		Intent myintent = new Intent(this, twoVideo_screen.class);
		final int result = 1;
		String sendURL = null;
		String sendURL2 = null;
		// If user doesn't enter any URL, use the default URL
		if (twoVideo1.getText().toString().matches("")) {
			sendURL = videoURL;	
		} else {
			sendURL = twoVideo1.getText().toString();
		}
		
		if (twoVideo2.getText().toString().matches("")) {
			sendURL2 = videoURL;	
		} else {
			sendURL2 = twoVideo2.getText().toString();
		}
		
		myintent.putExtra(EXTRA_MESSAGE2, sendURL);
		myintent.putExtra(EXTRA_MESSAGE3, sendURL2);
		startActivityForResult(myintent, result);
	}
	
	/*	This creates the class and executes the send message.	*/
	public void startSendingMessages(CompoundButton buttonView, boolean isChecked) {
		onOffToggle = isChecked;
        if (isChecked) {
        	
        	/* look for any errors that can cause the app to crash */
        	if (serverPort.getText().toString().matches("")) {
        		System.out.printf("Found the server port to be empty\n");
        		return;
        	} else if (serverIP.getText().toString().matches("")) {
        		System.out.printf("ERROR: Found serverIP to be empty\n");
        		return;
        	} else if (this.sendmsg != null && this.sendmsg.state != this.sendmsg.SUCCESS) { 
        		System.out.printf(
        			"ERROR: If sendmsg has been run before expected state to be success\n");
        	} else if (this.thread_running == true) {
        		System.out.printf("THREAD-1: THREAD-2 already created");
        		return;
        	} else {
        		System.out.printf("THREAD-1: All checks are verified");
        	}
        	
        	String localserverIP = serverIP.getText().toString();
        	int localserverPort = Integer.parseInt(serverPort.getText().toString());

        	sendmsg = new SendMsg(localserverIP, localserverPort);

        	System.out.println("THREAD-1: CREATING NEW THREAD-2");
        	th = new Thread(sendmsg);
        	th.start();
        	System.out.println("THREAD-1: STARTING NEW THREAD-2");
        	this.thread_running = true;
        	
        } else {
        	if (sendmsg == null) {
        		System.out.printf("THREAD-1: Sendmsg not started\n");
        		return;
        	} else if (this.sendmsg.state != this.sendmsg.CONNECTED) {
        		System.out.printf("THREAD-1: Sendmsg not connected\n");
        		return;
        	}
        	
        	System.out.println("THREAD-1: Sending the shutdown command");
        	sendmsg.queue.clear();
        	sendmsg.queue.offer("stop");
//        	sendmsg.queue.offer(-1);

        	System.out.println("THREAD-1: Waiting for thread to stop");
        	try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	System.out.println("THREAD-1: Thread has Stopped Cleanly!");
        	this.thread_running = false;
        }
    }
}