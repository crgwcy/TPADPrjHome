package com.RTC.TpadRTC;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.voiceengine.WebRtcAudioManager;
/*
import org.webrtc.voiceengine.MyAudioManagerAndroid;
import org.webrtc.voiceengine.WebRtcAudioRecord;
*/
import com.mywebrtc.japan.util.OutDoorCfg;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.activitys.CallFragment;
//import com.tpad.activitys.LoginActivity;
import com.tpad.activitys.MainMenuActivity;
import com.tpad.pad.activitys.PadStandbyActivity;

import com.RTC.TpadRTC.WSRtcClient.RTCListener;

//import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyRTCActivity extends Activity implements WSRtcClient.RTCListener,CallFragment.OnCallEvents {
  private final static int VIDEO_CALL_SENT = 666;
  private static final String VIDEO_CODEC_VP8 = "VP8"; 
  private static final String VIDEO_CODEC_VP9 = "VP9";
  private static final String VIDEO_CODEC_H264 = "H264";//"VP9";
  
  private static final String AUDIO_CODEC_OPUS ="opus";
  private static final String AUDIO_CODEC_ISAC = "ISAC";//"opus";
  // Local preview screen position before call is connected.
  private static final int LOCAL_X_CONNECTING = 0;
  private static final int LOCAL_Y_CONNECTING = 0;
  private static final int LOCAL_WIDTH_CONNECTING = 100;
  private static final int LOCAL_HEIGHT_CONNECTING = 100;
  // Local preview screen position after call is connected.
  private static final int LOCAL_X_CONNECTED = 72;
  private static final int LOCAL_Y_CONNECTED = 72;
  private static final int LOCAL_WIDTH_CONNECTED = 25;
  private static final int LOCAL_HEIGHT_CONNECTED = 25;
  // Remote video screen position
  private static final int REMOTE_X = 0;
  private static final int REMOTE_Y = 0;
  private static final int REMOTE_WIDTH = 100;
  private static final int REMOTE_HEIGHT =100;
  
  private static final int REMOTE1_X = 3;
  private static final int REMOTE1_Y = 10;
  private static final int REMOTE1_WIDTH = 35;
  private static final int REMOTE1_HEIGHT =35;
  
  
  private static final int REMOTE2_X = 3;
  private static final int REMOTE2_Y = 50;
  private static final int REMOTE2_WIDTH = 35;
  private static final int REMOTE2_HEIGHT =35;
  
  private static final int THREE_PEERS_LOCAL_CAMERA_X = 43;
  private static final int THREE_PEERS_LOCAL_CAMERA_Y = 25;
  private static final int THREE_PEERS_LOCAL_CAMERA_WIDTH = 50;
  private static final int THREE_PEERS_LOCAL_CAMERA_HEIGHT =50;
 // private EglBase rootEglBase;
  
  
  private EglBase rootEglBase;   // bruce add new
  
  private SurfaceViewRenderer localRender_new;
  private SurfaceViewRenderer remoteRender_new;
  private PercentFrameLayout localRenderLayout;
  private PercentFrameLayout remoteRenderLayout;
  
 // static boolean test_opengl_render=true;
  
    
  private /*VideoRendererGui.*/ScalingType scalingType =/* VideoRendererGui.*/ScalingType.SCALE_ASPECT_FILL;
 
  
    //bruce delete it for new version
//  private GLSurfaceView vsv;
  
  
//  private CallFragment callFragment;
  
 // bruce add
/*     
  private VideoRenderer.Callbacks localRender;
  private VideoRenderer.Callbacks remoteRender,remoteRender2;
*/ 
//  private int  remoteCount=0;
//  private WebRtcClient client;
  private String mSocketAddress;
  private String callerId;
  private static final String  TAG="MyRTCActivity";
  private boolean   RUN_ON_PAD;
  private boolean callControlFragmentVisible = true;
  public static boolean MyRTCActivity_created_flag=false;
  private  String remoteId=null;
  private  ImageButton  hangUpButton,cameraBtn,screenModeBtn,speakerBtn;
  private View videoMenuBar;
  private TextView timeTextView=null;
  private AudioManager audioManager=null;
  private  MediaPlayer mp=null;

  
  
  private static MyRTCActivity   instance;
  
  public static MyRTCActivity   getInstance()
  {
	  return instance;
  }
  
  public static void setInstance(MyRTCActivity ins)
  {
	  instance=ins;
  }

 // private static int test_times=0;
	  public static boolean getMyRTCActivity_created_flag()
	  {
		  return MyRTCActivity_created_flag;
	  }
	 
	  @SuppressLint("HandlerLeak")
	private Handler myHandler=new Handler(){    
	  	public void handleMessage(Message msg) {
	  	switch (msg.what) {
			case 1:	
				if (timeTextView!=null)
				{
		//			MyLog.printf(TAG,"handleMessage msg.what=1");
					timeTextView.setText(myGetTimeFormat(msg.arg1));
				}	
				break;
			default:
				break;
			}
	  }};  
	  
	private static String myGetTimeFormat(int counter)
    {
/*    	Calendar calendar=Calendar.getInstance();
    	int hour=calendar.get(Calendar.HOUR_OF_DAY);
    	int min=calendar.get(Calendar.MINUTE); 
    	int sec=calendar.get(Calendar.SECOND); 
*/
		int mySec=0,myMin=0;
		if (counter>0)
		{	
			mySec=counter%60;
			if (counter>=60)
				myMin=counter/60;
	    	return String.format("%02d:%02d",myMin,mySec);			
		}
        else
        	return "00:00";
    }
	
   private  boolean  video_capture_flag=false;
  @Override
  public void onCreate(Bundle savedInstanceState) {
	 // test_times=test_times+1;
	RUN_ON_PAD=WSClientService.run_on_pad_flag;//this.getResources().getBoolean(R.bool.APP_RUN_ON_PAD);			
/*
  	if (RUN_ON_PAD)
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	  	 	        
	}
	else
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
*/
	// Bruce modefied PAD and phone to LANDSCAPE?
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
			WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	  
	}
	MyLog.printf(TAG,"..............................................onCreate");	  
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(
            		LayoutParams.FLAG_FULLSCREEN
                    | LayoutParams.FLAG_KEEP_SCREEN_ON
                    | LayoutParams.FLAG_DISMISS_KEYGUARD
                    | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | LayoutParams.FLAG_TURN_SCREEN_ON);   
	MyLog.printf(TAG,"..............................................onCreate111");	  

    setContentView(R.layout.activity_call);
	MyLog.printf(TAG,"..............................................onCreate222");	  

    mSocketAddress = "http://" + getResources().getString(R.string.host);
    mSocketAddress += (":" + getResources().getString(R.string.port) + "/");

/*    vsv = (GLSurfaceView) findViewById(R.id.glview_call);
*/    
    
    
//  callFragment = new CallFragment();
    timeTextView=(TextView)findViewById(R.id.call_time_text_id);
    
/*    vsv.setPreserveEGLContextOnPause(true);
    vsv.setKeepScreenOn(true);*/
    
/*    VideoRendererGui.setView(vsv, new Runnable() {
      @Override
      public void run() {
        init();
      }
    });  */ 
    new Thread(  new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			init();
		}
	}).start();
    
    
    videoMenuBar = findViewById(R.id.menubar_fragment);
       
/*    vsv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	// toggleCallControlFragmentVisibility();
			// TODO Auto-generated method stub
			MyLog.printf(TAG,"vsv on click.............");
		    int visibility=videoMenuBar.getVisibility()==View.VISIBLE ? View.INVISIBLE:View.VISIBLE;
		    videoMenuBar.setVisibility(visibility);
		    boolean localShow=visibility==View.VISIBLE ? true:false;
		    videoMenuBar.bringToFront();
        }
      });*/
    
    hangUpButton=((ImageButton) findViewById(R.id.button_disconnect));
    screenModeBtn=(ImageButton) findViewById(R.id.button_screen_mode_id);
    speakerBtn=(ImageButton) findViewById(R.id.button_speaker_id);
    
    hangUpButton.setOnClickListener(
	        new View.OnClickListener() {
 	          @Override
 	          public void onClick(View view) {
		 	       if (remoteId!=null)
		 	   	   {   
			 	/* 
			 	   		WSClientService.getInstance().client.myClosePCResource(remoteId);
			 	   		try 
			 	   		{
			 	   			WSClientService.getInstance().client.sendMessage(remoteId,	MyRtcSip.HANGUP,null);
			 	   		} catch (JSONException e) {
			 	   			// TODO Auto-generated catch block
			 	   			e.printStackTrace();
			 	   		}
			 	*/
   		 			   WSClientService.getInstance().client.fromActivityHangupHandle();
   		 			   
		        		Intent intent=new Intent();
		        		if(RUN_ON_PAD)
		        			intent.setClass(MyRTCActivity.this,PadStandbyActivity.class);
		        		else
		        			intent.setClass(MyRTCActivity.this,MainMenuActivity.class);    
		        		
		        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		        		startActivity(intent);			        		
			 	   		finish();	
		 	   	   }
 	          }
 	        });   
    	screenModeBtn.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
	            Toast.makeText(getApplicationContext(), "video_capture_flag :"+video_capture_flag, Toast.LENGTH_SHORT).show();
				if (video_capture_flag==false)
				{
					video_capture_flag=true;
					WSClientService.getInstance().client.stopVideoSource();
					WSClientService.getInstance().client.stopVideoSource();
				}
				else
				{	
					video_capture_flag=false;
					WSClientService.getInstance().client.restartVideoSource();
				}

				
/*				if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				{
					MyLog.printf(TAG,"SCREEN_ORIENTATION_LANDSCAPE");
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				else if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
				{
					MyLog.printf(TAG,"SCREEN_ORIENTATION_PORTRAIT");
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);				
				}	
				else
				{	
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);				
					MyLog.printf(TAG,"SCREEN_ORIENTATION_ELSE");
				}	*/
			}
		});
    audioManager=(AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
  //  mp=new MediaPlayer();
    speakerBtn.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (audioManager!=null)
		    {	
		    	if (audioManager.isSpeakerphoneOn())
		    	{	
		    		audioManager.setSpeakerphoneOn(false);
		//    		mp.setVolume(0.8f,0.8f);
		            Toast.makeText(getApplicationContext(), "set speaker false", Toast.LENGTH_SHORT).show();
		    	}
		    	else
		    	{	
		    		audioManager.setSpeakerphoneOn(true); 
		//    		mp.setVolume(0.8f,0.0f);
		            Toast.makeText(getApplicationContext(), "set speaker true", Toast.LENGTH_SHORT).show();
		    	}	
		    } 	
/*			   {
				   MyLog.printf(TAG,"onItemClick............,send data test..............");							
				   WSClientService.getInstance().client.mysendDataTest();
				   return ;
			   }
 */
			
		}
	});   
    cameraBtn=(ImageButton)findViewById(R.id.button_switch_camera);
    if (cameraBtn!=null)
    {
    	cameraBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
/*			    if (audioManager!=null)
			    {	
			    	if (audioManager.isSpeakerphoneOn())
			    	{	
			    		audioManager.setSpeakerphoneOn(false);
			            Toast.makeText(getApplicationContext(), "set speaker false", Toast.LENGTH_SHORT).show();
			    	}
			    	else
			    	{	
			    		audioManager.setSpeakerphoneOn(true); 
			            Toast.makeText(getApplicationContext(), "set speaker true", Toast.LENGTH_SHORT).show();
			    	}	
			    }  
*/			
	/*			if (VideoRendererGui.getLocalVideoShowScalFlag()==false)
				{
			
										
					VideoRendererGui.setLocalVideoShowScalFlag(true);					
					VideoRendererGui.update(localRender,
				            REMOTE_X, REMOTE_Y,
				            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType,false);	 
				   
					VideoRendererGui.update(remoteRender,
			            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
			            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
			            VideoRendererGui.ScalingType.SCALE_ASPECT_FIT,false);		
				}
				else
				{
					VideoRendererGui.setLocalVideoShowScalFlag(false);					

				    VideoRendererGui.update(remoteRender,
				            REMOTE_X, REMOTE_Y,
				            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType,false);
				    VideoRendererGui.update(localRender,
				            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
				            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
				            VideoRendererGui.ScalingType.SCALE_ASPECT_FIT,false);							
				}*/
				
/*				if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				{
					MyLog.printf(TAG,"SCREEN_ORIENTATION_LANDSCAPE");
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				else if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
				{
					MyLog.printf(TAG,"SCREEN_ORIENTATION_PORTRAIT");
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);				
				}	
				else
				{	
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);				
					MyLog.printf(TAG,"SCREEN_ORIENTATION_ELSE");
				}
*/
								       
			}
		});
    }
    
    
 // Create video renderers.
   /* rootEglBase = EglBase.create();
    
    remoteRender.init(rootEglBase.getEglBaseContext(), null);
    remoteRender.init(rootEglBase.getEglBaseContext(), null);
    localRender.setZOrderMediaOverlay(true);*/
    
    if (OutDoorCfg.IS_OUTDOOR_FLAG==false)	
    {	
		  MyLog.printf(TAG,"00000000000000000000");

        // Create UI controls.
        localRender_new = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        remoteRender_new = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
		  MyLog.printf(TAG,"1111111111111111111111");
  	      
        rootEglBase = EglBase.create();
        
        localRender_new.init(rootEglBase.getEglBaseContext(), null);
        remoteRender_new.init(rootEglBase.getEglBaseContext(), null);
        localRender_new.setZOrderMediaOverlay(true);
		  MyLog.printf(TAG,"22222222222222222222222");
   	
        
	    // local and remote render
       /* 
	    remoteRender = VideoRendererGui.create(
	            REMOTE_X, REMOTE_Y,
	            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);     
	    remoteRender2 = VideoRendererGui.create(
	            REMOTE2_X, REMOTE2_Y,
	            REMOTE2_WIDTH, REMOTE2_HEIGHT, scalingType, false);    
	    
	    localRender = VideoRendererGui.create(
	            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
	            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);  
	    */
    }
    else
    {
        localRender_new = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        rootEglBase = EglBase.create();
        localRender_new.init(rootEglBase.getEglBaseContext(), null);
        
	/*
	 *     localRender = VideoRendererGui.create(
	            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
	            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);
	*/  
    }	
    updateVideoView();   //bruce add new this function
    
    final Intent intent = getIntent();
    final String action = intent.getAction();

    if (Intent.ACTION_VIEW.equals(action)) {
      final List<String> segments = intent.getData().getPathSegments();
      callerId = segments.get(0);
    }    
//   callFragment.setArguments(intent.getExtras());
//    hudFragment.setArguments(intent.getExtras());
    // Activate call and HUD fragments and start the call.
//    FragmentTransaction ft = getFragmentManager().beginTransaction();
 //   ft.add(R.id.call_fragment_container, callFragment);   
    
  }
/*
 * get camera support pictureSizes
  Camera camera = Camera.open(); 
  Parametersparameters = camera.getParameters(); 
  List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes(); 
  List<Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
 */
  
  private void init() {
    Point displaySize = new Point();
    getWindowManager().getDefaultDisplay().getSize(displaySize);
    
/*
 *     PeerConnectionParameters params = new PeerConnectionParameters(
            true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_H264, true, 1, AUDIO_CODEC_OPUS, true); 
*/
    PeerConnectionParameters params;
	if (OutDoorCfg.IS_OUTDOOR_FLAG==false)
		params= new PeerConnectionParameters(
            true, false, 640/*352*//*displaySize.x*/,480/*288*//*displaySize.y*/,8,300, VIDEO_CODEC_VP8, true, 30, AUDIO_CODEC_ISAC, true);    
	else
		params= new PeerConnectionParameters(
	            true, false, 640/*352*//*displaySize.x*/,480/*288*//*displaySize.y*/,8,300, VIDEO_CODEC_VP8, true, 30, AUDIO_CODEC_ISAC, true);    	
    //  if (test_times%2!=0)  
	
	if (MyRtcSip.NEW_VERSION_WEBRTC==false)
	{	
		PeerConnectionFactory.initializeFieldTrials(null);   //bruce add 20150424
   	
		PeerConnectionFactory.initializeAndroidGlobals(this, true, true,
            params.videoCodecHwAcceleration/*,VideoRendererGui.getEGLContext()*/);
	}
	else
	{
	    PeerConnectionFactory.initializeFieldTrials("WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/");		
	    WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true);
	    if (!PeerConnectionFactory.initializeAndroidGlobals(this, true, true,
		        true)) 
		    {
		    	Log.e(TAG,"Failed to initializeAndroidGlobals!!!");
		    }
	}	
    WSClientService.getInstance().client.setRTCListenerAndFactory((RTCListener) MyRTCActivity.this,params);

    //  WSClientService.getInstance().client.setCamera();
   Intent  intent=this.getIntent();
   String id = null,msg,callMsg=null,askMsg=null;
   
   if (intent!=null)
   {	
   		id=intent.getStringExtra("rtc_id");
   		msg=intent.getStringExtra(MyRtcSip.ANSWER_EXTRA);
   		callMsg=intent.getStringExtra(MyRtcSip.CALL_EXTRA);
   		askMsg=intent.getStringExtra(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK_EXTRA);
   		remoteId=id;
   		try 
   		{
   			MyLog.printf(TAG,"...........init111");
   		   	if (msg!=null)
   		   	{
   	   		   MyLog.printf(TAG,"...........init222");
   		   	   WSClientService.getInstance().client.sendMessage(id,MyRtcSip.ANSWER,null);
   		   	}	
   		   	if (callMsg!=null)
   		   	{
   		   	   WSClientService.getInstance().client.sendMessage(id,MyRtcSip.CALL,null);	   		
   		   	}	
   		   	if (askMsg!=null)
   		   	{
   		   		WSClientService.getInstance().client.sendMessage(id,askMsg,null);
   		   	}	 		   		
		} 
   		catch (JSONException e) 
   		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  // 	WSClientService.getInstance().client.myCallTest(id);//myAddPeer(id,0,true);   //bruce add  		
   }
   MyLog.printf(TAG,"...........init end");
   MyRTCActivity_created_flag=true;   
  }

  
  @Override
	protected void onStart() {
		// TODO Auto-generated method stub
	    MyLog.printf(TAG,".................................................onStart");	  
		super.onStart();
	}

  private MyTimer mytimer=null;
  
  @Override
  public void onResume() {
    MyLog.printf(TAG,".......................................onResume");
	super.onResume();	
	setInstance(MyRTCActivity.this);
  // bruce delete it for new version
 //   vsv.onResume();
    
	mytimer=new MyTimer(myHandler);
	mytimer.timerDoSomethingRepeately();   
/*
 *     if(client != null) {
      client.restartVideoSource();
    }
*/
/* 
 *    if ( WSClientService.getInstance().client!=null) 
   	 WSClientService.getInstance().client.restartVideoSource();
*/  

  }

  @Override
  public void onPause() {
	
	setInstance(null);
	MyLog.printf(TAG,"......onPause");	 
    mytimer.timerCancel();
	
    super.onPause();
   // bruce delete  it for new version
 //   vsv.onPause();

    
    
/* 
 *    if(client != null) {
      client.stopVideoSource();
    }
*/
    if ( WSClientService.getInstance().client!=null) 
      	 WSClientService.getInstance().client.stopVideoSource();
  } 
  @Override
	protected void onStop() 
    {
		// TODO Auto-generated method stub
	    MyLog.printf(TAG,"...........onStop");	  
		super.onStop();
	}
  @Override
  public void onDestroy() {
	 MyLog.printf(TAG,"...........onDestroy");	  
	 myHandler=null;
	 if( WSClientService.getInstance().client!=null)
	 {
	//	 WSClientService.getInstance().client.disconnect();
	//	 WSClientService.getInstance().client.myClosePCResource();
	 }	 
/*    if(client != null) {
      client.disconnect();
    }
*/
	 MyRTCActivity_created_flag=false;
	 VideoRendererGui.setLocalVideoShowScalFlag(false);
    super.onDestroy();
  }
  // Helper functions.
  private void toggleCallControlFragmentVisibility() {
    // Show/hide call control fragment
    callControlFragmentVisible = !callControlFragmentVisible;
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (callControlFragmentVisible) {
 //     ft.show(callFragment);
//      ft.show(hudFragment);
    } else {
 //     ft.hide(callFragment);
 //    ft.hide(hudFragment);
    }
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
  }
  @Override
  public void onCallReady(String callId) {
    if (callerId != null) {
      try {
        answer(callerId);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } else {
     
     MyLog.printf(TAG,"onCallReady start call");
  //   Log.e(TAG,"WebRtcAudioRecord.BuiltInAECIsAvailable():"+WebRtcAudioRecord.BuiltInAECIsAvailable()); 
   //   startCam();
      myStartCam();
   //   call(callId);
   //   showCameraTest();
    }
  }

  public void answer(String callerId) throws JSONException {
//    client.sendMessage(callerId, "init", null);
 //   startCam();
	  MyLog.printf(TAG,".......answer()");
 
	WSClientService.getInstance().client.sendMessage(callerId, "init", null);
    WSClientService.getInstance().client.setCamera();
  }

  public void call(String callId) {
    Intent msg = new Intent(Intent.ACTION_SEND);
    msg.putExtra(Intent.EXTRA_TEXT, mSocketAddress + callId);
    msg.setType("text/plain");
    startActivityForResult(Intent.createChooser(msg, "Call someone :"), VIDEO_CALL_SENT);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == VIDEO_CALL_SENT) {
      startCam();
    }
  }

  public void startCam() {
    // Camera settings
//    client.setCamera();
 //   client.start("android_test");
 //   client.myCallTest("1i70ZcVwN2SIuA3kAAAP");
  }
  public void myStartCam() {
	    // Camera settings
	//    client.setCamera();
//	    client.start("android_test");
	 //   client.myCallTest("1i70ZcVwN2SIuA3kAAAP");
  }
  @Override
  public void onStatusChanged(final String newStatus) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();


        if (newStatus!=null&&newStatus.equals("DISCONNECTED"))
        {
        	
/*            try {
    			Thread.sleep(1000*3);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	Intent intent=new Intent();
			intent.setClass(MyRTCActivity.this,MainMenuActivity.class);//(this, Butterfly.class);//(this, Butterfly.class);
		    
			startActivity(intent);//(this,Butterfly.class);	
			
*/        	
/*        	vsv.onPause();
        	MyRTCActivity.this.finish();
 */
        }
        else if (newStatus!=null&&newStatus.equals(MyRtcSip.HANGUP))
        {
      //  	vsv.onPause();
        	if(MyRTCActivity_created_flag==true)
        	{
        		MyLog.printf(TAG,"MyRTCActivity  finished000");
        	//	MyRTCActivity.this.finish();     
        		
        		Intent intent=new Intent();
        		if(RUN_ON_PAD)
        			intent.setClass(MyRTCActivity.this,PadStandbyActivity.class);
        		else
        			intent.setClass(MyRTCActivity.this,MainMenuActivity.class);    
        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
        		finish();
        		MyLog.printf(TAG,"MyRTCActivity  finished111");
        	}	
        }	
      }
    }); 
  }


  
  @Override
  public void onLocalStream(MediaStream localStream) {
	MyLog.printf(TAG,".........................................onLocalStream");
	//bruce add local video show!!!!!
 //   if(OutDoorCfg.IS_OUTDOOR_FLAG)  return ;
    if(OutDoorCfg.IS_OUTDOOR_CALL_FLAG) return ;
/*
 * 	if (localStream!=null)
		return ;
	if (localStream==null)
		return ;
*/
	if (WSClientService.getInstance().client.getDataChannelOnlyFlag())  return ;	
	
//	if (test_times%2!=0)
	if (localStream.videoTracks!=null&&localStream.videoTracks.get(0)!=null)
	{	
	    localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender_new));
	    
	    
	    //bruce modified it 
/*	    VideoRendererGui.update(localRender,
	            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
	            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
	           ScalingType.SCALE_ASPECT_FILL,false);   
*/ 
	}
  }

  @Override
  public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
    MyLog.printf(TAG,"@@@@@@onAddRemoteStream"); 	  
    if (OutDoorCfg.IS_OUTDOOR_FLAG)  return ;

/*
  	if (remoteStream!=null)
		return ;
	if (remoteStream==null)
		return ;
*/
	if (WSClientService.getInstance().client.getDataChannelOnlyFlag())  return ;      
    {	
    	
    	
    	if(WSRtcClient.getCurrentCallNums()==1)
    	{
    		
    		remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender_new));
    		
    		// bruce delete it new 
/*      	    VideoRendererGui.update(remoteRender,
      	            REMOTE_X, REMOTE_Y,
      	            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType,false);	
      		VideoRendererGui.update(localRender,
    	            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
    	            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
    	            VideoRendererGui.ScalingType.SCALE_ASPECT_FIT,false); */
    	}
    	else if(WSRtcClient.getCurrentCallNums()==2)
    	{
 /*   		 remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender2));  
    		 
    
    		  VideoRendererGui.update(localRender,
    				  THREE_PEERS_LOCAL_CAMERA_X, THREE_PEERS_LOCAL_CAMERA_Y,
    				  THREE_PEERS_LOCAL_CAMERA_WIDTH, THREE_PEERS_LOCAL_CAMERA_HEIGHT,
    				  VideoRendererGui.ScalingType.SCALE_ASPECT_FILL,false);
    		  
    		 VideoRendererGui.update(remoteRender,
       	            REMOTE1_X, REMOTE1_Y,
       	            REMOTE1_WIDTH, REMOTE1_HEIGHT, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL,false);	 		 
     	    VideoRendererGui.update(remoteRender2,
     	            REMOTE2_X, REMOTE2_Y,
     	            REMOTE2_WIDTH, REMOTE2_HEIGHT, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL,false);     	  */
    	}
    	
    	
    	
    	
    	
/*    	
    	VideoRendererGui.update(localRender,
	            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
	            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
	            VideoRendererGui.ScalingType.SCALE_ASPECT_FIT); 
    	remoteCount++;
    	if (remoteCount==1)
    	{
    	    remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
    	    VideoRendererGui.update(remoteRender,
    	            REMOTE_X, REMOTE_Y,
    	            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);	
    	}
    	else if (remoteCount==2)
    	{
    	    remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender2));
    	    VideoRendererGui.update(remoteRender2,
    	            REMOTE2_X, REMOTE2_Y,
    	            REMOTE2_WIDTH, REMOTE2_HEIGHT, scalingType);
    	}	
	    
	    VideoRendererGui.update(localRender,
	            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
	            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
	            VideoRendererGui.ScalingType.SCALE_ASPECT_FIT); 
*/
    }
  }  
  
  @Override
  public void onRemoveRemoteStream(MediaStream remoteStream, int endPoint) {
	 if (OutDoorCfg.IS_OUTDOOR_FLAG)  return ;

	 MyLog.printf(TAG,"onRemoveRemoteStream"); 
	// if (test_times%2!=0)
	 {	 
		 // bruce delete it new
/*	    VideoRendererGui.remove(remoteRender);
	    VideoRendererGui.update(localRender,
	            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
	            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
	            ScalingType.SCALE_ASPECT_FIT,false);*/
	 }  
  }

@Override
public void onCallHangUp() {
	// TODO Auto-generated method stub	
}

@Override
public void onCameraSwitch() {
	// TODO Auto-generated method stub
	
}

@Override
public void onVideoScalingSwitch(ScalingType scalingType) {
	// TODO Auto-generated method stub
	
}

public boolean onKeyDown(int keyCode, KeyEvent event)
{
	if (keyCode == KeyEvent.KEYCODE_BACK )  
	{
	   MyLog.printf(TAG,"onKeyDown...KEYCODE_BACK");
/*
  	   Intent  intent=new Intent();
	   intent.setClass(this,MainMenuActivity.class);
	   startActivity(intent);
*/
//	   finish();
//	   System.exit(0);
	   if (remoteId!=null)
	   {   
		   		   
	/*		WSClientService.getInstance().client.myClosePCResource(remoteId);			
			try 
			{
				WSClientService.getInstance().client.sendMessage(remoteId,	MyRtcSip.HANGUP,null);
			}
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	*/
		   WSClientService.getInstance().client.fromActivityHangupHandle();
			
			
			Intent intent=new Intent();
			if(RUN_ON_PAD)
				intent.setClass(MyRTCActivity.this,PadStandbyActivity.class);
			else
				intent.setClass(MyRTCActivity.this,MainMenuActivity.class);   
			
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(intent);		
			finish();
			
//			MyRtcSip.onIceConnected=false;
	   }
	}
	else if (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN)
	{
		if (audioManager!=null)
		{	
			MyLog.printf(TAG,"onKeyDown...KEYCODE_VOLUME_DOWN");
			audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_DOWN);
			Toast.makeText(getApplicationContext(),"audio_lower"+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),Toast.LENGTH_SHORT).show();
		}	
	}
	else if (keyCode==KeyEvent.KEYCODE_VOLUME_UP)
	{
		if (audioManager!=null)
		{	
			MyLog.printf(TAG,"onKeyDown...KEYCODE_VOLUME_UP");	
			audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
			Toast.makeText(getApplicationContext(),"audio_raise:"+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),Toast.LENGTH_SHORT).show();
		}	
	}		
	return true;
};
	public static  class MenuBarFragment extends Fragment {
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	      return inflater.inflate(R.layout.fragment_menubar, container, false);
	    }
	}
	private static int counter=0;
	private static Timer timer=null;			
	
	private class MyTimer 
	{
		private Handler timeHandler;
		public MyTimer(Handler handler)
		{
			timer=new Timer();
			timeHandler=handler;
		}
		public void  timerDoSomethingRepeately()
		{
			
			timer.schedule(new TimerTask() {	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					counter=counter+1;
	//				MyLog.printf(TAG,"timerMyRTC......................%d",counter);
					if (timeHandler!=null)
					{
						Message msg=new Message();
						msg.what=1;
						msg.arg1=counter;
						timeHandler.sendMessage(msg);
					}	
				}
			}, 0,1000);// 1000ms*60
		}
		public void timerCancel()
		{
			timer.cancel();
			timer=null;
			counter=0;
		}
	}
	  private void updateVideoView() {
		  MyLog.printf(TAG,"updateVideoView000");
		    remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
		    remoteRender_new.setScalingType(scalingType);
		    remoteRender_new.setMirror(false);
			  MyLog.printf(TAG,"updateVideoView222");

		      localRenderLayout.setPosition(
		          LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
		      localRender_new.setScalingType(ScalingType.SCALE_ASPECT_FIT);
			  MyLog.printf(TAG,"updateVideoView333");

		    localRender_new.setMirror(true);
			  MyLog.printf(TAG,"updateVideoView444");

		    localRender_new.requestLayout();
		    remoteRender_new.requestLayout();
		  }
}