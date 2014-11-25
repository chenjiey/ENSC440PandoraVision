package fortyonepost.com.ag;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

public class firstFragment extends Fragment{
	
	VideoView myVideoView;
	
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
		
		//MediaController myMediaController = new MediaController(this);
		Uri video = Uri.parse("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");
		myVideoView.setVideoURI(video);
		System.out.println("first video started.");
		myVideoView.start();
	}

	
}
