package fortyonepost.com.ag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class videoStream extends Activity{

	private TextView fromMainMsg;
	
	@SuppressLint({ "InlinedApi", "NewApi" }) @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.video_screen);
		
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		
		
		Intent activityThatcalled = getIntent();
//		String fromMainActivity = activityThatcalled.getStringExtra(AccessGyroscope.EXTRA_MESSAGE);
//		fromMainMsg = (TextView) findViewById(R.id.fromMain);
//		System.out.println("Print recieved msg from main activity:" + fromMainActivity);
//		fromMainMsg.setText(fromMainActivity);
		
		VideoView myVideoView = (VideoView) findViewById(R.id.myVideo);
		//MediaController myMediaController = new MediaController(this);
		Uri video = Uri.parse("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");
		myVideoView.setVideoURI(video);
		myVideoView.start();
	}
	

}
