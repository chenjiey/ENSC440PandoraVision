package veg.mediaplayer.sdk.testmulti;


import java.nio.ByteBuffer;

import veg.mediaplayer.sdk.MediaPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ToggleButton;
import veg.mediaplayer.sdk.*;

public class ARTS extends Activity implements MediaPlayer.MediaPlayerCallback  {
	
	private String videoURL = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
	private String rpi1 = "rtsp://192.168.1.4:8554/test";
	private MediaPlayer player;
	private EditText serverIP, serverPort, oneVideo, twoVideo1, twoVideo2;
	private ToggleButton toggleButton1;
	public static final String EXTRA_MESSAGE2 = "EXTRA_MESSAGE2";
	public static final String EXTRA_MESSAGE3 = "EXTRA_MESSAGE3";
	public static final String EXTRA_MESSAGE4 = "IP";
	public static final String EXTRA_MESSAGE5 = "Port";
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arts);
		
		Intent activityThatcalled = getIntent();
		
		
		twoVideo1 = (EditText) findViewById(R.id.artsVideo1); // reference to the text field
        twoVideo2 = (EditText) findViewById(R.id.artsVideo2); // reference to the text field
        serverIP = (EditText) findViewById(R.id.IPaddress);
        serverPort = (EditText) findViewById(R.id.port);
		twoVideo1.setText("rtsp://192.168.1.4:8554/test");
		twoVideo2.setText("rtsp://192.168.1.4:8554/test");
	}

    @Override
    protected void onDestroy() {
    	
    	if (player != null)
    	{
    		// Close connection to server
    		player.Close();
    		// Destroy player
    		player.onDestroy();
    	}
    		super.onDestroy();
	}
    
	@Override
	public int OnReceiveData(ByteBuffer arg0, int arg1, long arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int Status(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/** Called when the user clicks the displayTwoVideo button */
	public void displayTwoVideo(View view) {
	    // Do something in response to button
		//Link to another activity
		Intent myintent = new Intent(this, twoVideoARTs.class);
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
		myintent.putExtra(EXTRA_MESSAGE4, serverIP.getText().toString());
		myintent.putExtra(EXTRA_MESSAGE5, serverPort.getText().toString());

		startActivityForResult(myintent, result);
	}

}
