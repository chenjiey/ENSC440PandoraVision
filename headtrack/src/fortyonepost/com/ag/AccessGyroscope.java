package fortyonepost.com.ag;//Created by: DimasTheDriver on Apr/27/2010. Available at: http://www.41post.com/?p=3745

import android.R.array;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.CircularRedirectException;

//import com.thousandthoughts.tutorials.SensorFusionActivity.calculateFusedOrientationTask;




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

@SuppressLint("NewApi") public class AccessGyroscope extends Activity implements SensorEventListener {
	
	//a TextView
	private TextView tv;
	//the Sensor Manager
	private SensorManager sManager;
	
	private Socket client;
	private PrintWriter printwriter;
	public static EditText serverIP, serverPort, oneVideo, twoVideo1, twoVideo2;
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
	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	public static final String EXTRA_MESSAGE2 = "EXTRA_MESSAGE2";
	public static final String EXTRA_MESSAGE3 = "EXTRA_MESSAGE3";
	private double[] runningAverage = new double[MAX_RUN_AVG];
	private int arrayCnt = 0;
	private double[] runAvg = new double[3];
	private double[] runAvgArr = new double[MAX_RUN_AVG];
	private String videoURL = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
	
	/*
	 * Below are the parameters for the sensor fusion filter
	 */
	/* **************************************************** */
    // angular speeds from gyro
    private float[] gyro = new float[3];
 
    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];
 
    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];
 
    // magnetic field vector
    private float[] magnet = new float[3];
 
    // accelerometer vector
    private float[] accel = new float[3];
 
    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];
 
    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];
 
    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];
    
    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private boolean initState = true;
    
	public static final int TIME_CONSTANT = 10;
	public static final float FILTER_COEFFICIENT = 0.98f;
	private Timer fuseTimer = new Timer();

	/* ************************************************************/
	private float THRESH_HOLD = (float) ((10.0 * Math.PI / 180.0));  // convert to radians
	private float THRESH_HOLD_FUSION = (float) ((2.0 * Math.PI / 180.0));  // convert to radians
	private float[] position_send;
	private float[] position_last = new float[3];
	
	/*
	 * Initalizes the gyroOrientation to 0 vector and gyroMaxtrix to 
	 * 	Identity matrix
	 */
	public void init_gyro(){
		
		position_last[0] = 0.0f;
        position_last[1] = 0.0f;
        position_last[2] = 0.0f;
		
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;
 
        // initialize gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        init_gyro();
        
        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();
        
        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                                      1000, TIME_CONSTANT);

        
        //get the TextView from the layout file
        tv = (TextView) findViewById(R.id.tv);
        
        oneVideo = (EditText) findViewById(R.id.oneVideoLink); // reference to the text field
        twoVideo1 = (EditText) findViewById(R.id.twoVideoLink1); // reference to the text field
        twoVideo2 = (EditText) findViewById(R.id.twoVideoLink2); // reference to the text field
        serverIP = (EditText) findViewById(R.id.editText2); // reference to the text field
        serverPort = (EditText) findViewById(R.id.editText3); // reference to the text field
        toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1); // reference to the send button
		twoVideo1.setText("rtsp://192.168.1.3:8554/pi_encode.h264");
		twoVideo2.setText("rtsp://192.168.1.7:8554/pi_encode.h264");

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
    	int _delay = SensorManager.SENSOR_DELAY_FASTEST;
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), _delay);
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), _delay);    	
    	sManager.registerListener(this,
    			sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), _delay);
    }
    
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) 
	{
		//Do nothing
	}

    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;
     
        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }
     
        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
        System.arraycopy(event.values, 0, gyro, 0, 3);
        getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }
     
        // measurement done, save current time for next interval
        timestamp = event.timestamp;
     
        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);
     
        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);
     
        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];
     
        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);
     
        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;
     
        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;
     
        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;
     
        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }
    
    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];
     
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];
     
        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];
     
        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];
     
        return result;
    }

	
	// This function is borrowed from the Android reference
	// at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	// It calculates a rotation vector from the gyroscope angular speed values.
    private void getRotationVectorFromGyro(float[] gyroValues,
    		float[] deltaRotationVector,
            float timeFactor)
	{
		float[] normValues = new float[3];
		
		// Calculate the angular speed of the sample
		float omegaMagnitude =
		(float)Math.sqrt(gyroValues[0] * gyroValues[0] +
		gyroValues[1] * gyroValues[1] +
		gyroValues[2] * gyroValues[2]);
		
		// Normalize the rotation vector if it's big enough to get the axis
		if(omegaMagnitude > EPSILON) {
		normValues[0] = gyroValues[0] / omegaMagnitude;
		normValues[1] = gyroValues[1] / omegaMagnitude;
		normValues[2] = gyroValues[2] / omegaMagnitude;
		}
		
		// Integrate around this axis with the angular speed by the timestep
		// in order to get a delta rotation from this sample over the timestep
		// We will convert this axis-angle representation of the delta rotation
		// into a quaternion before turning it into the rotation matrix.
		float thetaOverTwo = omegaMagnitude * timeFactor;
		float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
		float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
		deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
		deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
		deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
		deltaRotationVector[3] = cosThetaOverTwo;
	}

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            
            /*
             * Fix for 179° <--> -179° transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360° (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360° from the result
             * if it is greater than 180°. This stabilizes the output in positive-to-negative-transition cases.
             */
            
            // azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
            	fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
        		fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
            	fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
            	fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
            	fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }
            
            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
            	fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
        		fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
            	fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
            	fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
            	fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }
            
            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
            	fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
        		fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
            	fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
            	fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
            	fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }
     
            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
            
            
            // update sensor output in GUI
            
            // TODO(JEREMY): PASS THIS VALUE TO THREAD!!!!
//            mHandler.post(updateOreintationDisplayTask);
//            Log.i("SENSOR_FUSION", "COMPLETED FIRST PASS OF SENSOR FUSION");
        }
    }
    
	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		int yaw = 0;
		int pitch = 0;

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
		case Sensor.TYPE_GYROSCOPE:
	        gyroFunction(event);
		}

		position_send = accMagOrientation;		
		
		yaw = getNewOrientation(0);
		pitch = getNewOrientation(2);

		System.arraycopy(fusedOrientation, 0, position_last , 0, 3);
		
//		/* (Jeremy) Nov 23: I was playing around with different ways of averaging
//		 * the output. I'm pretty convinced that we need to mix this with the 
//		 * Sensor Fusion algorithm*/
//		
//		switch (SENSOR) {
//		case RUN_AVG:
//			runAvg = calculateRunAvg(Math.toDegrees(accMagOrientation[0]), runAvg, 180);
//			runAvg = runAvg - runAvg % 3;  // restrict movement by groups of 3 degrees
//			break;
//		case DEFAULT:
//			runAvg = Math.toDegrees(accMagOrientation[0]);
//			runAvg = runAvg - runAvg % 3;
//			break;
//		default:
//			runAvg = calcRunAvgArr(Math.toDegrees(accMagOrientation[0]));
//			break;
//		}

		tv.setText(String.format(
				"Orientation X (Yaw) :%d\n" + 
//				"Orientation Y (Roll) :%d\n" +
				"Orientation Z (Pitch) :%d",
//				yaw, (int) Math.toDegrees(accMagOrientation[1]), 
//				yaw, (int) (Math.toDegrees(accMagOrientation[1]) + 180), pitch));
				yaw, pitch));

		messsage = String.format("%d,%d\n", yaw, pitch);
//		messsage = String.format("%d\n", yaw);
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

	private String format(String string, float f) {
		// TODO Auto-generated method stub
		return null;
	}

	private int getNewOrientation(int i) {
		boolean test;
		int result;
		
		test = (Math.abs(position_last[i] - fusedOrientation[i]) < THRESH_HOLD_FUSION );
		test = test && (Math.abs(position_last[i] - accMagOrientation[i]) < THRESH_HOLD );
		
//		if (test) {
//			Log.i("SENSOR_FUSION", "DETECED MINOR CHANGE, USING FUSION");
//			result = (int) Math.toDegrees(fusedOrientation[i]);
//		} else {
//			Log.i("SENSOR_FUSION", "DETECED LARG CHANGE, USING ACCELEROMETER");
//			runAvg[i] = calculateRunAvg(Math.toDegrees(accMagOrientation[i]), runAvg[i], 0);
//			runAvg[i] = runAvg[i] - runAvg[i] % 3;  // restrict movement by groups of 3 degrees
//			result = (int) runAvg[i];
////			result = (int) Math.toDegrees(accMagOrientation[i]);
//		}
		result = (int) Math.toDegrees(fusedOrientation[i]);
		return result;
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