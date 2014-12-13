package veg.mediaplayer.sdk.testmulti;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;



import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;
import veg.mediaplayer.sdk.*;

public class twoVideoARTs extends FragmentActivity implements SensorEventListener{

	public String URLString1 = null;
	public String URLString2 = null;
	private SensorManager sManager;
	private String messsage;
	private static final int MAX_RUN_AVG = 100;
	int port = 0;
	private boolean onOffToggle;
	private boolean thread_running = false;
	private Thread th;
	public artsSendMsg sendmsg = null;
	private String serverIP, serverPort;
	private static final int RUN_AVG = 0;
	
	//private TextView fromMainMsg;
	
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
	
	
	@SuppressLint({ "InlinedApi", "NewApi" }) @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
	     init_gyro();
	     onOffToggle = true;
	     
	     setContentView(R.layout.twovideo_arts);
	     
	     sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	     initListeners();
	     
	     fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
               1000, TIME_CONSTANT);
	     
	     View decorView = getWindow().getDecorView();
			// Hide both the navigation bar and the status bar.
			// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
			// a general rule, you should design your app to hide the status bar whenever you
			// hide the navigation bar.
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			              | View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
			//Use intent get data from main activity
			Intent activityThatcalled = getIntent();
			serverIP = activityThatcalled.getStringExtra(ARTS.EXTRA_MESSAGE4);
			serverPort = activityThatcalled.getStringExtra(ARTS.EXTRA_MESSAGE5);
			startSendingMessages();
			URLString1 = activityThatcalled.getStringExtra(ARTS.EXTRA_MESSAGE2);	
			URLString2 = activityThatcalled.getStringExtra(ARTS.EXTRA_MESSAGE3);


			//Debug what is received
			System.out.println("from main activity");
			System.out.println(URLString1);
			System.out.println(URLString2);

	
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
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


    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            
            /*
             * Fix for 179� <--> -179� transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360� (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360� from the result
             * if it is greater than 180�. This stabilizes the output in positive-to-negative-transition cases.
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

//		tv.setText(String.format(
//				"Orientation X (Yaw) :%d\n" + 
//				"Orientation Z (Pitch) :%d",
//				yaw, pitch));

		messsage = String.format("%d,%d\n", yaw, pitch);
//		Log.i("SENSORS", messsage);		
		if (sendmsg == null) {
			return; // if send message hasn't been initialized don't send
		}

		if (onOffToggle) {
			sendmsg.queue.offer(messsage);
		}
	}
	
	/*	This creates the class and executes the send message.	*/
	public void startSendingMessages() {
//		onOffToggle = isChecked;
        if (true) {
//        	serverIP = "192.168.1.2";
//        	serverPort = "8000";
        	/* look for any errors that can cause the app to crash */
        	if (serverPort.toString().matches("")) {
        		System.out.printf("Found the server port to be empty\n");
        		return;
        	} else if (serverIP.toString().matches("")) {
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
        	
        	String localserverIP = serverIP.toString();
        	int localserverPort = Integer.parseInt(serverPort.toString());

        	sendmsg = new artsSendMsg(localserverIP, localserverPort);

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
