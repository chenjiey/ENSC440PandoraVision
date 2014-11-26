package fortyonepost.com.ag;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

public class firstFragment extends Fragment{
	
	private VideoView myVideoView;
	private String URL1;
	
//	private String URL1="default for testing purpose";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.firstfragment, container, false);	
		myVideoView = (VideoView) view.findViewById(R.id.firstfrag);		
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		//Get URL from fragment activity
		twoVideo_screen mydata = (twoVideo_screen)getActivity();
		URL1 = mydata.URLString1;
		//Debug what is received
		System.out.println("first frag URL:");
		System.out.println(URL1);
		
		//parse url and play video
		Uri video = Uri.parse(URL1);
		myVideoView.setVideoURI(video);
		System.out.println("first video started.");
		myVideoView.start();
	}
	
}
