package fortyonepost.com.ag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;

public class twoVideo_screen extends FragmentActivity{

	public String URLString1 = null;
	public String URLString2 = null;
	//private TextView fromMainMsg;
	
	@SuppressLint({ "InlinedApi", "NewApi" }) @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.twovideo_screen);
		
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
		URLString1 = activityThatcalled.getStringExtra(AccessGyroscope.EXTRA_MESSAGE2);	
		URLString2 = activityThatcalled.getStringExtra(AccessGyroscope.EXTRA_MESSAGE3);
		//Debug what is received
		System.out.println("from main activity");
		System.out.println(URLString1);
		System.out.println(URLString2);
	}

	
}