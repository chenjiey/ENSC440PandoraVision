package veg.mediaplayer.sdk.testmulti;

import java.nio.ByteBuffer;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import veg.mediaplayer.sdk.*;

public class artsSecondFrag extends Fragment implements MediaPlayer.MediaPlayerCallback {
	
	private String URL2;
	private MediaPlayer player;
	
//	private String URL1="default for testing purpose";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.artssecond_frag, container, false);	
		player = (MediaPlayer) view.findViewById(R.id.playerViewARTSfrag2);	
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		//Get URL from fragment activity
		twoVideoARTs mydata = (twoVideoARTs)getActivity();
		URL2 = mydata.URLString2;
		//Debug what is received
		System.out.println("second frag URL:");
		System.out.println(URL2);
		
//		 player = new MediaPlayer(this);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(250,250, Gravity.CENTER);
//        player.setLayoutParams(params);
//        FrameLayout lp = (FrameLayout)findViewById(R.id.playerViewARTS);
//        lp.addView(player);
////       player = (MediaPlayer)findViewById(R.id.playerViewARTSfrag2);
////        addView(player, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 300));
		
        player.Open(
        		URL2, //example”, // correct URL or full path for media file
        		-1, // RTSP over http tunneling 
        		5000, // 500 ms on probing
        		3000, // 500 ms buffer on start
        		1, // Decoder type : 0- S/W 1, - H/W
        		1, // Renderer Type : 0 - SDL, 1 - pure OpenGL
        		1, // A/V synchronization: 1- Enable , 0 - Disable
        		1, // Drop Video frame if it is late : 1- Enable , 0 – Disable
        		1, // Color / Grayscale video output : 0 - grayscale, 1 – color
        		0, // Aspect ratio / Full size : 1 – aspect rate
        		30000, // Reconnection timeout (milliseconds),
        		0, // Number Of Cpu Cores for decoding (1-6), 0-autodetect 
        		this);
	}

    @Override
	public void onDestroy() {
    	
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
	
}