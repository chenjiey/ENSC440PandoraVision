package veg.mediaplayer.sdk.test;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import android.preference.PreferenceManager;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayer.VideoShot;
import veg.mediaplayer.sdk.MediaPlayerConfig;

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener 
{
	public static final float MIN_ZOOM = 0.7f;
	public static final float MAX_ZOOM = 1.0f;
	public float scaleFactor = 1.0f;
	public boolean zoom = false;
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) 
	{
		scaleFactor *= detector.getScaleFactor();
		scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
		Log.e("Player", "onScale " + scaleFactor);
		return true;
	}
	@Override
	 public boolean onScaleBegin(ScaleGestureDetector detector) 
	{
		Log.e("Player", "onScaleBegin");
		zoom = true;
		return true;
	 }

	 @Override
	 public void onScaleEnd(ScaleGestureDetector detector) 
	 {
		Log.e("Player", "onScaleEnd");
		zoom = false;
	 }
	
}

class ViewSizes
{
	public float dx = 0;
	public float dy = 0;
	
	public float orig_width = 0;
	public float orig_height = 0;
	
	public ScaleListener listnrr = null;
	
}

public class MainActivity extends Activity implements OnClickListener, MediaPlayer.MediaPlayerCallback, View.OnTouchListener
{
    private static final String TAG 	 = "MediaPlayerTest";
    
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;
	private Button						btnShot;

	private StatusProgressTask 			mProgressTask = null;
	
	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean 					playing = false;
    private MediaPlayer 				player = null;
    private MainActivity 				mthis = null;

    private RelativeLayout 				playerStatus = null;
    private TextView 					playerStatusText = null;
    private TextView 					playerHwStatus = null;
    
	public ScaleGestureDetector 		detectors = null;	
	public ViewSizes 					mSurfaceSizes 	= null;
    
    private MulticastLock multicastLock = null;
    
	private enum PlayerStates
	{
	  	Busy,
	  	ReadyForUse
	};

    private enum PlayerConnectType
	{
	  	Normal,
	  	Reconnecting
	};
    
	private Object waitOnMe = new Object();
	private PlayerStates player_state = PlayerStates.ReadyForUse; 
	private PlayerConnectType reconnect_type = PlayerConnectType.Normal;
	private int mOldMsg = 0;

	private Toast toastShot = null;
	
	private Handler handler = new Handler() 
    {
		String strText = "Connecting";
		
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	        switch (status) 
	        {
	        	case CP_CONNECT_STARTING:
	        		if (reconnect_type == PlayerConnectType.Reconnecting)
	        			strText = "Reconnecting";
	        		else
	        			strText = "Connecting";
	        			
	        		startProgressTask(strText);
	        		
	        		player_state = PlayerStates.Busy;
	    			showStatusView();
	    			
	    			reconnect_type = PlayerConnectType.Normal;
	    			setHideControls();
	    			break;
	                
		    	case VRP_NEED_SURFACE:
		    		player_state = PlayerStates.Busy;
		    		showVideoView();
			        //synchronized (waitOnMe) { waitOnMe.notifyAll(); }
					break;
	
		    	case PLP_PLAY_SUCCESSFUL:
		    		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("");
	    			//playerHwStatus.setText("Hardware");
		     		player.setAlpha(1.0f);
		     		
		    		setTitle(R.string.app_name);
			        break;
	                
	        	case PLP_CLOSE_STARTING:
	        		player_state = PlayerStates.Busy;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setUIDisconnected();
	    			break;
	                
	        	case PLP_CLOSE_SUCCESSFUL:
	        		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			System.gc();
	    			setShowControls();
	    			setUIDisconnected();
	                break;
	                
	        	case PLP_CLOSE_FAILED:
	        		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	   			break;
	               
	        	case CP_CONNECT_FAILED:
	        		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_BUILD_FAILED:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_PLAY_FAILED:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_ERROR:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case CP_INTERRUPTED:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
	            case CP_STOPPED:
	            case VDP_STOPPED:
	            case VRP_STOPPED:
	            case ADP_STOPPED:
	            case ARP_STOPPED:
	            	if (player_state != PlayerStates.Busy)
	            	{
		        		stopProgressTask();
	            		player_state = PlayerStates.Busy;
						if (toastShot != null)
							toastShot.cancel();
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
		    			player_state = PlayerStates.ReadyForUse;
		    			setShowControls();
		    			setUIDisconnected();
	            	}
	                break;
	
	            case CP_ERROR_DISCONNECTED:
	            	if (player_state != PlayerStates.Busy)
	            	{
	            		player_state = PlayerStates.Busy;
						if (toastShot != null)
							toastShot.cancel();
	            		player.Close();

	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
		    			player_state = PlayerStates.ReadyForUse;
		    			setUIDisconnected();
	            		
						Toast.makeText(getApplicationContext(), "Demo Version!",
								   Toast.LENGTH_SHORT).show();
						
//		    			reconnect_type = PlayerConnectType.Reconnecting;
//
//		    			SharedSettings sett = SharedSettings.getInstance();
//	        			boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
//	        	    	int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
//	        			player.Open(MediaPlayer.ConnectionUrl, 
//	        					sett.decoderType, sett.rendererType, sett.synchroEnable, sett.synchroNeedDropVideoFrames, sett.rendererEnableColorVideo, 
//	        					aspect, MediaPlayer.DataReceiveTimeout, mthis);
	            	}
	                break;
	                
	            //case CONTENT_PROVIDER_FAILED_INIT:
	//            case VIDEO_DECODER_PROVIDER_FAILED_INIT:
	//            case VIDEO_RENDERER_PROVIDER_FAILED_INIT:
	//            case AUDIO_DECODER_PROVIDER_FAILED_INIT:
	//            case AUDIO_RENDERER_PROVIDER_FAILED_INIT:
	//        		player.Close();
	//        		state = PlayerStates.ReadyForUse;
	//    			playerStatusText.setText("Disconnected.");
	//              break;
	
	            default:
	            	player_state = PlayerStates.Busy;
	        }
	    }
	};

	// callback from Native Player 
	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
	{
		Log.e(TAG, "Form Native Player OnReceiveData: size: " + size + ", pts: " + pts);
		return 0;
	}
    
	@Override
	public int Status(int arg)
	{
		
		PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;
		
		Log.e(TAG, "Form Native Player status: " + arg);
	    switch (PlayerNotifyCodes.forValue(arg)) 
	    {
	    	// for synchronus process
			//case PLAY_SUCCESSFUL:
//	    	case VRP_NEED_SURFACE:
//	    		synchronized (waitOnMe) 
//	    		{
//					Message msg = new Message();
//					msg.obj = status;
//					handler.sendMessage(msg);
//	    		    try 
//	    		    {
//	    		        waitOnMe.wait();
//	    		    }
//	    		    catch (InterruptedException e) {}
//	    		}			
//				break;
	            
			// for asynchronus process
	        default:     
				Message msg = new Message();
				msg.obj = status;
				handler.removeMessages(mOldMsg);
				mOldMsg = msg.what;
				handler.sendMessage(msg);
	    }
	    
		return 0;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;

		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();
		
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		setContentView(R.layout.main);
		mthis = this;
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		SharedSettings.getInstance(this).loadPrefSettings();
		SharedSettings.getInstance().savePrefSettings();
		
		playerStatus 		= (RelativeLayout)findViewById(R.id.playerStatus);
		playerStatusText 	= (TextView)findViewById(R.id.playerStatusText);
		playerHwStatus 		= (TextView)findViewById(R.id.playerHwStatus);
		
		player = (MediaPlayer)findViewById(R.id.playerView);

//        ViewSizes sz = new ViewSizes();
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) player.getLayoutParams();
//        sz.orig_width = (float)lp.width;
//        sz.orig_height = (float)lp.height;
//        sz.listnrr = new ScaleListener();
//        mSurfaceSizes = sz;
//        
//        detectors = new ScaleGestureDetector(player.getContext(), sz.listnrr);
//        //FrameLayout playerViewLayout = (RelativeLayout)findViewById(R.id.playerViewLayout);
//        player.setOnTouchListener(this);  
//		
//        playerViewRelativeLayout = (RelativeLayout)findViewById(R.id.playerViewRelativeLayout);
//        ViewTreeObserver vto = playerViewRelativeLayout.getViewTreeObserver(); 
//        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() 
//        { 
//            @Override 
//            public void onGlobalLayout() 
//            { 
//            	mthis.playerViewRelativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
//            	mSurfaceSizes.orig_width  = playerViewRelativeLayout.getMeasuredWidth();
//            	mSurfaceSizes.orig_height = playerViewRelativeLayout.getMeasuredHeight(); 
//
//            } 
//        });  
        
		strUrl = settings.getString("connectionUrl", "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");		 
		
		player.getSurfaceView().setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		HashSet<String> tempHistory = new HashSet<String>();
		//tempHistory.add("http://sfe2.rt.bradburylab.tv/live/_definst_/smil:mcmtop_sd.smil/playlist.m3u8");
		//tempHistory.add("http://rm-edge-3.cdn2.streamago.tv/streamagoedge/34961/28964/chunklist.m3u8");
		tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
		tempHistory.add("http://hls.cn.ru/streaming/2x2tv/tvrec/playlist.m3u8");
		tempHistory.add("rtsp://rtmp.infomaniak.ch/livecast/latele");		
		
//		tempHistory.add("rtsp://russiatoday.fms.visionip.tv/rt/Russia_al_yaum_1000k_1/1000k_1");
//		tempHistory.add("rtsp://www.tvarm.ru:1935/live/myStream1.sdp");
//		tempHistory.add("rtsp://live240.impek.com/brtvhd");
//		tempHistory.add("http://tv.life.ru/lifetv/480p/index.m3u8");
//		tempHistory.add("http://hls.cn.ru/streaming/nickelodeon/tvrec/playlist.m3u8");

//		tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");

//		tempHistory.add("rtsp://10.20.16.158:554/video1");
//		tempHistory.add("rtp://224.0.0.10:20000/");
//		tempHistory.add("rtsp://stage-dev.meetviva.com:8081/axis-media/media.amp?videocodec=h264&streamprofile=viva&client=1.00408CE57096&vtoken=dkgkj49skdkfwlsfwjd&domain=stage-dev.meetviva.com");
//		tempHistory.add("http://tv.life.ru/lifetv/480p/index.m3u8");
//		tempHistory.add("rtsp://live240.impek.com/brtvhd");
//		tempHistory.add("rtmp://flash0.80137-live0.dna.qbrick.com/80137-live0/sevillafc");
//
//		tempHistory.add("http://tvrain-video.ngenix.net/mobile/TVRain_1m.stream/chunklist.m3u8");
//		tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
//		tempHistory.add("http://hls.cn.ru/streaming/nickelodeon/tvrec/playlist.m3u8");
	
		player.setOnTouchListener(new View.OnTouchListener() 
		{
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) 
            {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) 
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                    	if (player.getState() == PlayerState.Paused)
                    		player.Play();
                    	else
                        	if (player.getState() == PlayerState.Started)
                        		player.Pause();
                    }
                }
            		
	        	return true;
            }
        });
		
			
		edtIpAddressHistory = settings.getStringSet("connectionHistory", tempHistory);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
	
		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		edtIpAddress.setText(strUrl);

		edtIpAddress.setOnEditorActionListener(new OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) 
			{
				if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) 
				{
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
	
				}
				return false;
			}
		});

		btnHistory = (Button)findViewById(R.id.button_history);

		// Array of choices
		btnHistory.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(MainActivity.edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				if (edtIpAddressHistory.size() <= 0)
					return;

//				String urlHistory[] = {	"rtsp://russiatoday.fms.visionip.tv/rt/Russia_al_yaum_1000k_1/1000k_1",
//										"rtsp://www.tvarm.ru:1935/live/myStream1.sdp",
//										"rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov",
//										"rtsp://live240.impek.com/brtvhd",
//										"http://tv.life.ru/lifetv/480p/index.m3u8" };

				MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.history_item, new ArrayList<String>(edtIpAddressHistory));
				MainActivity.edtIpAddress.setAdapter(MainActivity.edtIpAddressAdapter);
				MainActivity.edtIpAddress.showDropDown();
			}   
		});

		btnShot = (Button)findViewById(R.id.button_shot);

		// Array of choices
		btnShot.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				if (player != null)
				{
					Log.e("SDL", "getVideoShot()");

	    	    	SharedSettings sett = SharedSettings.getInstance();
					if (sett.decoderType != 0)
					{
						Toast.makeText(getApplicationContext(), "For Software Decoder Only!",
							   Toast.LENGTH_SHORT).show();
						return;
					}
					
					//VideoShot frame = player.getVideoShot(200, 200);
					VideoShot frame = player.getVideoShot(-1, -1);
					if (frame == null)
						return;
					
					// get your custom_toast.xml ayout
					LayoutInflater inflater = getLayoutInflater();
	 
					View layout = inflater.inflate(R.layout.videoshot_view,
					  (ViewGroup) findViewById(R.id.videoshot_toast_layout_id));
	 
					// set a dummy image
//			        Bitmap bmp = Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
//			        bmp.copyPixelsFromBuffer(frame.getData());
//			        
//					ImageView image = (ImageView) layout.findViewById(R.id.videoshot_image);
//					image.setImageBitmap(bmp);
//			        bmp.recycle();
	 
					ImageView image = (ImageView) layout.findViewById(R.id.videoshot_image);
					image.setImageBitmap(getFrameAsBitmap(frame.getData(), frame.getWidth(), frame.getHeight()));
					
					// Toast...
					if (toastShot != null)
						toastShot.cancel();

					toastShot = new Toast(getApplicationContext());
					toastShot.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toastShot.setDuration(Toast.LENGTH_SHORT);
					toastShot.setView(layout);
					toastShot.show();
					
				}
			}   
		});

		btnConnect = (Button)findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);
        
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (getWindow() != null && getWindow().getCurrentFocus() != null && getWindow().getCurrentFocus().getWindowToken() != null)
					inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		});
        
		playerStatusText.setText("DEMO VERSION");
		setShowControls();
        
    }

    private int[] mColorSwapBuf = null;                        // used by saveFrame()
    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height)
    {
//    	if (mColorSwapBuf == null)
//            mColorSwapBuf = new int[width * height];
//
//        int pixelCount = width * height;
//        int[] colors = mColorSwapBuf;
//        frame.asIntBuffer().get(colors);
//        for (int i = 0; i < pixelCount; i++) 
//        {
//            int c = colors[i];
//            colors[i] = (c & 0xff00ff00) | ((c & 0x00ff0000) >> 16) | ((c & 0x000000ff) << 16);
//        }
//
//        Bitmap bmp = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    	//frame.order(ByteOrder.nativeOrder());
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }
    
    public void onClick(View v) 
	{
		SharedSettings.getInstance().loadPrefSettings();
		if (player != null)
		{
			if (!edtIpAddressHistory.contains(player.getConfig().getConnectionUrl()))
				edtIpAddressHistory.add(player.getConfig().getConnectionUrl());
			
			player.getConfig().setConnectionUrl(edtIpAddress.getText().toString());
			if (player.getConfig().getConnectionUrl().isEmpty())
				return;

			if (toastShot != null)
				toastShot.cancel();
			player.Close();
			if (playing)
			{
    			setUIDisconnected();
			}
			else
			{
    	    	SharedSettings sett = SharedSettings.getInstance();
    			boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    	    	int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
    	    	
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	
    	    	//sett.synchroEnable = 0;
    	    	//sett.synchroNeedDropVideoFrames = 0;
    	    	player.setVisibility(View.INVISIBLE);
    	    	
    	    	// set ssl key
    	    	conf.setSslKey("10563358ff0698b0461eca5b5cb71746");
    			
    			// try change start stream position
    	    	conf.setStartOffest(30000);
    			
    			// play first frame and pause
    	    	//conf.setStartPreroll(1);
    	    	conf.setConnectionUrl(player.getConfig().getConnectionUrl());
    			
    	    	conf.setConnectionNetworkProtocol(sett.connectionProtocol);
    	    	conf.setConnectionDetectionTime(sett.connectionDetectionTime);
    	    	conf.setConnectionBufferingTime(sett.connectionBufferingTime);
    	    	conf.setDecodingType(sett.decoderType);
    	    	conf.setRendererType(sett.rendererType);
    	    	conf.setSynchroEnable(sett.synchroEnable);
    	    	conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
    	    	conf.setEnableColorVideo(sett.rendererEnableColorVideo);
    	    	conf.setEnableAspectRatio(aspect);
    	    	conf.setDataReceiveTimeout(30000);
    	    	conf.setNumberOfCPUCores(0);
    	    	
        	    player.Open(conf, mthis);

//         	    player.Open(player.getConfig().getConnectionUrl(), sett.connectionProtocol, sett.connectionDetectionTime, sett.connectionBufferingTime, 
//         	    		sett.decoderType, sett.rendererType, sett.synchroEnable, 
//    	    			sett.synchroNeedDropVideoFrames, sett.rendererEnableColorVideo, aspect, player.getConfig().getDataReceiveTimeout(), 0, mthis);
         	    
//         	    player.Open(MediaPlayer.ConnectionUrl, MediaPlayer.DataReceiveTimeout, mthis);
//    	    	player.OpenAsPreview(MediaPlayer.ConnectionUrl, MediaPlayer.DataReceiveTimeout, mthis);
    			
				btnConnect.setText("Disconnect");
				playing = true;
			}
		}
    }
 
	protected void onPause()
	{
		Log.e("SDL", "onPause()");
		super.onPause();

		editor = settings.edit();
		editor.putString("connectionUrl", edtIpAddress.getText().toString());

		editor.putStringSet("connectionHistory", edtIpAddressHistory);
		editor.commit();
		
		if (player != null)
			player.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e("SDL", "onResume()");
		super.onResume();
		if (player != null)
			player.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e("SDL", "onStart()");
		super.onStart();
		if (player != null)
			player.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e("SDL", "onStop()");
		super.onStop();
		if (player != null)
			player.onStop();
		
		if (toastShot != null)
			toastShot.cancel();

	}

    @Override
    public void onBackPressed() 
    {
		if (toastShot != null)
			toastShot.cancel();
		
		player.Close();
		if (!playing)
		{
	  		super.onBackPressed();
	  		return;			
		}

		setUIDisconnected();
    }
  	
  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e("SDL", "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
		if (player != null)
			player.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e("SDL", "onLowMemory()");
  		super.onLowMemory();
		if (player != null)
			player.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e("SDL", "onDestroy()");
		if (toastShot != null)
			toastShot.cancel();
		
		if (player != null)
			player.onDestroy();
		
		stopProgressTask();
		System.gc();
		
		if (multicastLock != null) {
		    multicastLock.release();
		    multicastLock = null;
		}		
		super.onDestroy();
   	}	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item)  
	{
		switch (item.getItemId())  
		{
			case R.id.main_opt_settings:   
		
				SharedSettings.getInstance().loadPrefSettings();

				Intent intentSettings = new Intent(MainActivity.this, PreferencesActivity.class);     
				startActivity(intentSettings);

				break;
//			case R.id.main_opt_unlock:   
//
//				UnlockKey = "allkjkjKKl,.,KJhYW&*(ONnBJVHFDXvn,}?:uJ2";
//				break;
//			case R.id.main_opt_unlock2:   
//
//				UnlockKey = "allkjkjKKl,.,KJhYW&*(ONnBJVHFDXvn,}?:uJ22";
//				break;
			case R.id.main_opt_clearhistory:     
			
				new AlertDialog.Builder(this)
				.setTitle("Clear History")
				.setMessage("Do you really want to delete the history?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						HashSet<String> tempHistory = new HashSet<String>();
//						tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
//						tempHistory.add("rtsp://www.tvarm.ru:1935/live/myStream1.sdp");
//						tempHistory.add("rtsp://live240.impek.com/brtvhd");
//						tempHistory.add("http://tv.life.ru/lifetv/480p/index.m3u8");
//						tempHistory.add("http://hls.cn.ru/streaming/nickelodeon/tvrec/playlist.m3u8");
//						tempHistory.add("http://tvrain-video.ngenix.net/mobile/TVRain_1m.stream/chunklist.m3u8");

						tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
						tempHistory.add("http://hls.cn.ru/streaming/2x2tv/tvrec/playlist.m3u8");
						tempHistory.add("rtsp://rtmp.infomaniak.ch/livecast/latele");		
						
//						tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
//						tempHistory.add("rtsp://russiatoday.fms.visionip.tv/rt/Russia_al_yaum_1000k_1/1000k_1");
//						tempHistory.add("rtsp://10.20.16.158:554/video1");
//						tempHistory.add("rtp://224.0.0.10:20000/");
//						tempHistory.add("rtsp://stage-dev.meetviva.com:8081/axis-media/media.amp?videocodec=h264&streamprofile=viva&client=1.00408CE57096&vtoken=dkgkj49skdkfwlsfwjd&domain=stage-dev.meetviva.com");
//						tempHistory.add("http://tv.life.ru/lifetv/480p/index.m3u8");
//						tempHistory.add("rtsp://live240.impek.com/brtvhd");
//						tempHistory.add("rtmp://flash0.80137-live0.dna.qbrick.com/80137-live0/sevillafc");
				//
//						tempHistory.add("http://tvrain-video.ngenix.net/mobile/TVRain_1m.stream/chunklist.m3u8");
//						tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
//						tempHistory.add("rtsp://rtmp.infomaniak.ch/livecast/latele");
//						tempHistory.add("http://rm-edge-3.cdn2.streamago.tv/streamagoedge/34961/28964/chunklist.m3u8");
//						tempHistory.add("http://hls.cn.ru/streaming/2x2tv/tvrec/playlist.m3u8");
//						tempHistory.add("http://hls.cn.ru/streaming/nickelodeon/tvrec/playlist.m3u8");
//						tempHistory.add("http://sfe2.rt.bradburylab.tv/live/_definst_/smil:mcmtop_sd.smil/playlist.m3u8");	

						edtIpAddressHistory.clear();
						edtIpAddressHistory = tempHistory;  
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						// do nothing
					}
				}).show();
				break;
			case R.id.main_opt_about:     
				AboutDialog about = new AboutDialog(this);  
				about.show();
				break;
			case R.id.main_opt_exit:     
				finish();
				break;

		}
		return true;
	}

	protected void setUIDisconnected()
	{
		setTitle(R.string.app_name);
		btnConnect.setText("Connect");
		playing = false;
	}

	protected void setHideControls()
	{
		ActionBar actionBar = getActionBar();
		if (actionBar != null)
			actionBar.hide(); // slides out

		btnShot.setVisibility(View.VISIBLE);
		edtIpAddress.setVisibility(View.GONE);
		btnHistory.setVisibility(View.GONE);
		btnConnect.setVisibility(View.GONE);
	}

	protected void setShowControls()
	{
		ActionBar actionBar = getActionBar();
		if (actionBar != null)
			actionBar.show(); // slides out
		setTitle(R.string.app_name);
		
		btnShot.setVisibility(View.GONE);
		edtIpAddress.setVisibility(View.VISIBLE);
		btnHistory.setVisibility(View.VISIBLE);
		btnConnect.setVisibility(View.VISIBLE);
	}

	private void showStatusView() 
	{
		player.setVisibility(View.INVISIBLE);
		playerHwStatus.setVisibility(View.INVISIBLE);
		player.setAlpha(0.0f);
		playerStatus.setVisibility(View.VISIBLE);
		
	}
	
	private void showVideoView() 
	{
        playerStatus.setVisibility(View.INVISIBLE);
 		player.setVisibility(View.VISIBLE);
		playerHwStatus.setVisibility(View.VISIBLE);

 		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		setTitle("");
	}
    
	private void startProgressTask(String text)
	{
		stopProgressTask();
	    
	    mProgressTask = new StatusProgressTask(text);
	    //mProgressTask.execute(text);
	    executeAsyncTask(mProgressTask, text);
	}
	
	private void stopProgressTask()
	{
		playerStatusText.setText("");
		setTitle(R.string.app_name);
		
       	if (mProgressTask != null)
	    {
       		mProgressTask.stopTask();
	    	mProgressTask.cancel(true);
	    }
	}

	private class StatusProgressTask extends AsyncTask<String, Void, Boolean> 
    {
       	String strProgressTextSrc;
       	String strProgressText;
        Rect bounds = new Rect();
    	boolean stop = false;
      	
       	public StatusProgressTask(String text)
       	{
        	stop = false;
       		strProgressTextSrc = text;
       	}
       	
       	public void stopTask() { stop = true; }
       	
        @Override
        protected void onPreExecute() 
        {
        	super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) 
        {
            try 
            {
                if (stop) return true;

                String maxText = "Disconnected.....";//strProgressTextSrc + "....";
                int len = maxText.length();
            	playerStatusText.getPaint().getTextBounds(maxText, 0, len, bounds);

               	strProgressText = strProgressTextSrc + "...";
                
            	Runnable uiRunnable = null;
                uiRunnable = new Runnable()
                {
                    public void run()
                    {
                        if (stop) return;

    	                playerStatusText.setText(strProgressText);
    	            	
    	            	RelativeLayout.LayoutParams layoutParams = 
    	            		    (RelativeLayout.LayoutParams)playerStatusText.getLayoutParams();
    	           		
    	           		layoutParams.width = bounds.width();
    	           		playerStatusText.setLayoutParams(layoutParams);        	
    	            	playerStatusText.setGravity(Gravity.NO_GRAVITY);
    	            	
                        synchronized(this) { this.notify(); }
                    }
                };
                
               	int nCount = 4;
              	do
            	{
                    try
                    {
                    	Thread.sleep(300);
                    }
                    catch ( InterruptedException e ) { stop = true; }
                   
                    if (stop) break;
                    
                	if (nCount <= 3)
                	{
                		strProgressText = strProgressTextSrc;
                		for (int i = 0; i < nCount; i++)
                			strProgressText = strProgressText + ".";
                	}
                    
                    synchronized ( uiRunnable )
                    {
                    	runOnUiThread(uiRunnable);
                        try
                        {
                            uiRunnable.wait();
                        }
                        catch ( InterruptedException e ) { stop = true; }
                    }
                    
                    if (stop) break;
                    
                    nCount++;
                    if (nCount > 3)
                    {
                    	nCount = 1;
                    	strProgressText = strProgressTextSrc;
                    }
            	}
              	
            	while(!isCancelled());
            } 
            catch (Exception e) 
            {
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) 
        {
            super.onPostExecute(result);
            mProgressTask = null;
        }
        @Override
        protected void onCancelled() 
        {
            super.onCancelled();
        }
    }
	
    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) 
    {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
    	{
    		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    	}
    	else 
    	{
    		task.execute(params);
    	}
    }  
	
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{
		if (detectors != null)
			detectors.onTouchEvent(event);
		
	    switch (event.getAction()) 
	    {
	        case MotionEvent.ACTION_DOWN:
	        	mSurfaceSizes.dx =  event.getX();
	        	mSurfaceSizes.dy =  event.getY();
	            break;
	
	        case MotionEvent.ACTION_MOVE:
	            float x =  event.getX();
	            float y =  event.getY();
	            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
	            float left = lp.leftMargin + (x - mSurfaceSizes.dx); 
	            float top = lp.topMargin + (y - mSurfaceSizes.dy);
	            if (mSurfaceSizes.listnrr != null && mSurfaceSizes.listnrr.zoom)
	            {
	            	int srcw = lp.width;
	            	int srch = lp.height;
	            	
		    		//Log.e("Player", "ACTION_MOVE1 " + lp.width + "," + mSurfaceSizes.orig_width + "," + mSurfaceSizes.listnrr.scaleFactor);
		    		
		    		int left_offset = (int) (mSurfaceSizes.orig_width - (mSurfaceSizes.orig_width * mSurfaceSizes.listnrr.scaleFactor));
		    		int top_offset = (int) (mSurfaceSizes.orig_height - (mSurfaceSizes.orig_height * mSurfaceSizes.listnrr.scaleFactor));
		    		Log.e("Player", "ACTION_MOVE2 " + left_offset + "," + top_offset);
		    		//lp.setMargins(left_offset, top_offset, left_offset, top_offset);
		    		
	                lp.leftMargin = left_offset;
	                lp.topMargin  = top_offset;
	                lp.rightMargin = left_offset;
	                lp.bottomMargin  = top_offset;
		    		
		    		
	                //lp.width = (int) (mSurfaceSizes.orig_width * mSurfaceSizes.listnrr.scaleFactor);
		            //lp.height  = (int) (mSurfaceSizes.orig_height  * mSurfaceSizes.listnrr.scaleFactor);
	
	                //lp.leftMargin -= (lp.width - srcw) / 2;
	                //lp.topMargin  -= (lp.height - srch) / 2;
	            }
//	            else
//	            {
//	                lp.leftMargin = (int)left;
//	                lp.topMargin = (int)top;
//	            }
	            view.setLayoutParams(lp);
	            //view.requestLayout();
	            break;
	    }	    
	    //view.invalidate();
	    return true;
	}
	
}
