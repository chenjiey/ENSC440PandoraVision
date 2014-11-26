package fortyonepost.com.ag;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

@SuppressLint("NewApi") public class secondFragment extends Fragment{
	
	VideoView myVideoView;
	private String URL2;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.secondfragment, container, false);
		
		myVideoView = (VideoView) view.findViewById(R.id.secondfrag);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		//MediaController myMediaController = new MediaController(this);
		
		//Get URL from fragment activity
		twoVideo_screen mydata = (twoVideo_screen)getActivity();
		URL2 = mydata.URLString2;
		//Debug what is received
		System.out.println("second frag URL:");
		System.out.println(URL2);
		
		//parse url and play video
		Uri video = Uri.parse(URL2);
		myVideoView.setVideoURI(video);
		System.out.println("second video started.");
		myVideoView.start();
	}
	
	

}