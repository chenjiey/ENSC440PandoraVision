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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.widget.CompoundButton;

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
	        	  	//messsage = textField.getText().toString(); // get the text message on the text field
					//textField.setText(""); // Reset the text field to blank

		        	/* 
					This creates the class and executes the send message. 
		        	*/
					SendMessage sendMessageTask = new SendMessage();
					sendMessageTask.execute();

		        } else {
		         	// raise an error message here
		        }
            }
        });
    }
    
	private class SendMessage extends AsyncTask<Void, Void, Void> {
		
		
//		protected void onPreExecute(){
//			port = Integer.parseInt(serverPort.getText().toString());
//			SERVER_IP1 = serverIP.getText().toString();
//			
//			// connect the client to the server
//			try {
//				System.out.println("STARTING SERVER");
//				client = new Socket(SERVER_IP1, port);
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		/*
		(Jeremy Nov.12.2014) Pretty sure I figured out why it is only sending
		once. AsyncTask correctly creates a thread in the background which
		calls doInBackground. doInBackground only runs once and hence you only
		send data once. You basically need a while loop in there but you do
		need to define entrance and exit functions for the thread.

		The entrance function should initialize the connections.
		The exit function should disconnect the connection.

		You also need to properly exit the method there is a special function
		call that allows you to do that and it still completes the exit function
		afterwards.

		entrance function: onPreExecute()
		exit function: onPostExecute()

		This is just a theory as I haven't gotten it working correctly yet.
		*/


		@Override
		protected Void doInBackground(Void... params) {
			try {
				/* 
				(Jeremy Nov.11.2014) It looks like you are creating the
				connection more than once. If we create a connection to the 
				server twice even though a connection has previously been
				established this could cause the error. I think this should
				be moved into a contructor and stored 
				*/

				port = Integer.parseInt(serverPort.getText().toString());
				SERVER_IP1 = serverIP.getText().toString();
				
				// connect the client to the server
				client = new Socket(SERVER_IP1, port);

				/* 
				Once the socket is initialized you only need to run the
				code below upon orientation update. There are a couple ways to
				fix this I will try a few tomorrow. 

				One method I'm pretty sure will
				work but I don't like it because it removes the ability to run
				in another thread like was done currently.
				*/
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("ENTERING FUNCTION");
				printwriter = new PrintWriter(client.getOutputStream(), true);
				printwriter.write(messsage); // write the message to output stream
				printwriter.flush();
				printwriter.close();
				// Client.close should be called at the end, so when you toggle
				// the button off. This is causing your server thread to close
				// which is why you cannot reconnect and you cannot receive any
				// more data
				
				 client.close(); // close the connection
	
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
//		@Override
//		protected void onPostExecute(Void result) {
//			try {
//				System.out.println("CLOSING CONNECTION");
//				client.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} // close the connection
//		}
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
		 
		tv.setText("Orientation X (Roll) :"+ Float.toString(accMagOrientation[0]) +"\n"+
				   "Orientation Y (Pitch) :"+ Float.toString(accMagOrientation[1]) +"\n"+
				   "Orientation Z (Yaw) :"+ Float.toString(accMagOrientation[2]));

		messsage = ("Orientation Y (Roll) :" + Float.toString(accMagOrientation[0]) + "\n" +
				   "Orientation Y (Pitch) :" + Float.toString(accMagOrientation[1]) +"\n" +
				   "Orientation Z (Yaw) :" + Float.toString(accMagOrientation[2]));
		
		/*if (onOffToggle) {
			SendMessage sendMessageTask = new SendMessage();
			sendMessageTask.execute();
		}*/
	}
	
	// calculates orientation angles from accelerometer and magnetometer output
	public void calculateAccMagOrientation() {
		if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
		}
	}
}