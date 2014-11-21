package fortyonepost.com.ag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class videoStream extends Activity{

	private TextView fromMainMsg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_screen);
		
		Intent activityThatcalled = getIntent();
		String fromMainActivity = activityThatcalled.getStringExtra(AccessGyroscope.EXTRA_MESSAGE);
		fromMainMsg = (TextView) findViewById(R.id.fromMain);
		System.out.println("Print recieved msg from main activity:" + fromMainActivity);
		fromMainMsg.setText(fromMainActivity);
		
	}
	

}
