package com.tpad.pad.activitys;

import java.io.File;

import org.json.JSONException;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRendererGui;
import org.webrtc.voiceengine.WebRtcAudioManager;

import com.hikvision.netsdk.HCNetSDK;
import com.mywebrtc.util.CepsaIPCameraUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.activitys.CameraListActivity;
import com.tpad.activitys.MainMenuActivity;
import com.tpad.hik.CompressDataBean;
import com.tpad.hik.DeviceBean;
import com.tpad.hik.HalfDuplexTalk;
import com.tpad.hik.HikView;
import com.tpad.hik.SetDVRConfig;

import com.RTC.TpadRTC.PeerConnectionParameters;
import com.RTC.TpadRTC.R;
import com.RTC.TpadRTC.WSRtcClient;
import com.RTC.TpadRTC.WSRtcClient.RTCListener;

import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import android.graphics.Point;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class HikCameraActivity extends Activity implements View.OnClickListener,OnTouchListener,OnGestureListener,WSRtcClient.RTCListener 
{
	  private static final String VIDEO_CODEC_H264 = "H264";//"VP9";
	  private static final String AUDIO_CODEC_OPUS ="opus";
	  private  ImageButton  speakerBtn;
//	  private Button start, set, stop,btUp,btDown,btLeft,btRight;
	  private ImageButton hangupImageBtn,
	  					 setImageBtn,
	  					 leftImageBtn,
	  					 rightImageBtn,
	  					 upImageBtn,
	  					 downImageBtn
	  					 ;
	  
	  private View layout;
	  private EditText ip;
	  private EditText port;
	  private EditText userName;
	  private EditText passWord;
	  private EditText channel;

	  private static boolean IpCameraActivity_created_flag=false;
	  public static boolean getIpCameraActivity_created_flag()
	  {
		  return IpCameraActivity_created_flag;
	  }
	  	  
	private Point screenSizePoint;
	public  int width,height;
	private static String TAG="HikCameraActivity";
	private  GestureDetector detector;
	private static HikCameraActivity hikCameraActivity=null;
	private boolean RUN_ON_PAD;
	private GLSurfaceView vsv;
	public SurfaceView  hikSurface;
	
	public boolean getRunOnPadFlag()
	{
		return RUN_ON_PAD;
	}
	public static HikCameraActivity getInstance()
	{
		return hikCameraActivity;
	}
	 
	public static void   setInstance(HikCameraActivity ins)
	{
		hikCameraActivity=ins;
	}
	public  Point getScreenSizePoint()
	{
		return screenSizePoint;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		MyLog.printf(TAG,"HikCameraActivity ...................on create");
		
		if (WSClientService.getInstance()==null)  
			MyLog.printf(TAG,"WSClientService.getInstance()==null");
		else  if (WSClientService.getInstance().client==null)
			MyLog.printf(TAG,"WSClientService.getInstance().client==null");
		WSClientService.getInstance().client.setDataChannelOnlyFlag(true);
	//	WSClientService.getInstance().client.setDataChannelOnlyFlag(false);

		RUN_ON_PAD=WSClientService.run_on_pad_flag;	
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		width=getWindowManager().getDefaultDisplay().getWidth();/*getSize(screenSizePoint);*/		
		height=getWindowManager().getDefaultDisplay().getHeight();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hik_camera);
		initButton();
	//	setContentView(new CameraShowSurface(this,null));
		hikSurface=(SurfaceView) findViewById(R.id.hik_camera_surface_id);
		vsv=(GLSurfaceView)findViewById(R.id.hik_camera_glview_id);
 	    vsv.setPreserveEGLContextOnPause(true);
 	    vsv.setKeepScreenOn(true);
 	    if (vsv!=null)
 	    	MyLog.printf(TAG,"onCreate222"); 	    
 	    VideoRendererGui.setView(vsv, new Runnable() {
 	      @Override
 	      public void run() {
 	        init();
 	      }
 	    });
 	   setInstance(this); 	  	 	
	}
	
	
	  private void createPeerConnectionFactoryInternal(Context context)  // bruce add new func
	  {
	 //     PeerConnectionFactory.initializeInternalTracer();

	    // Initialize field trials.
	    PeerConnectionFactory.initializeFieldTrials("WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/");

	    // Check preferred video codec.
/*	    preferredVideoCodec = VIDEO_CODEC_VP8;
	    if (videoCallEnabled && peerConnectionParameters.videoCodec != null) {
	      if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_VP9)) {
	        preferredVideoCodec = VIDEO_CODEC_VP9;
	      } else if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_H264)) {
	        preferredVideoCodec = VIDEO_CODEC_H264;
	      }
	    }*/
//	    Log.d(TAG, "Pereferred video codec: " + preferredVideoCodec);

	    // Check if ISAC is used by default.
/*	    preferIsac = false;
	    if (peerConnectionParameters.audioCodec != null
	        && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC)) {
	      preferIsac = true;
	    }
*/
	    // Enable/disable OpenSL ES playback.
	    /*
	    if (!peerConnectionParameters.useOpenSLES) {
	      Log.d(TAG, "Disable OpenSL ES audio even if device supports it");
	      WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true );
	    } else {
	      Log.d(TAG, "Allow OpenSL ES audio if device supports it");
	      WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
	    }
	    */
	    // Create peer connection factory.
	    if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true,
	        true)) 
	    {
	    	Log.e(TAG,"Failed to initializeAndroidGlobals!!!");
	    }
	    
/*	    if (options != null) 
	    {
	      Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask);
	    }
*/
//	    factory = new  (null/*options*/);
//	    factory.setVideoHwAccelerationOptions(VideoRendererGui.getEglBaseContext(), VideoRendererGui.getEglBaseContext());
	  }
	
	  private void init() {
			MyLog.printf(TAG,"onCreate3333333333333333333");
			Intent  intent=this.getIntent();

		    Point displaySize = new Point();
		    getWindowManager().getDefaultDisplay().getSize(displaySize);
		    PeerConnectionParameters params = new PeerConnectionParameters(
		            true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_H264, true, 1, AUDIO_CODEC_OPUS, true);
		    

/*  		if(MyRtcSip.ONLY_DATACHANNEL==true)
				PeerConnectionFactory.initializeAndroidGlobals(this,false,false,
		            params.videoCodecHwAccelerationfalse, nullVideoRendererGui.getEGLContext());
			else
*/
		    
		    if (MyRtcSip.NEW_VERSION_WEBRTC==false)
		    {	
		    	/*PeerConnectionFactory.initializeFieldTrials(null);   
		    	PeerConnectionFactory.initializeAndroidGlobals(this,true,true,
			            params.videoCodecHwAcceleration,VideoRendererGui.getEGLContext());	*/		
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
			MyLog.printf(TAG,"onCreate444444444444444444444444");

		    
		    
		    WSClientService.getInstance().client.setRTCListenerAndFactory((RTCListener)HikCameraActivity.this,params);

		    //  WSClientService.getInstance().client.setCamera();
		   String id = null,msg,callMsg=null,askMsg=null;
  		   MyLog.printf(TAG,"--------------------------------- intent000"); 
		   if (intent!=null)
		   {	
	  		//	MyLog.printf(TAG,"--------------------------------- intent111");		   
		   		id=intent.getStringExtra(MyRtcSip.IP_CAMERA_REMOTE_ID_EXTRA/*"rtc_id"*/);
		   		msg=intent.getStringExtra(MyRtcSip.ANSWER_EXTRA);
		   		callMsg=intent.getStringExtra(MyRtcSip.CALL_EXTRA);
		   		askMsg=intent.getStringExtra(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK_EXTRA);
		   		
	   			MyLog.printf(TAG,"---------------------------------id=%s askMsg=%s",id,askMsg);
	   			if (id==null)
					startPlay();
	   			
		   		try {
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
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  // 	WSClientService.getInstance().client.myCallTest(id);//myAddPeer(id,0,true);   //bruce add  		
		   }
		   MyLog.printf(TAG,"...........init end");	
		   IpCameraActivity_created_flag=true;
	  }
	 
		private void initButton(){
			
			hangupImageBtn=(ImageButton) findViewById(R.id.hik_camera_hangup_btn_id);
			hangupImageBtn.setOnClickListener((OnClickListener) this);

			setImageBtn=(ImageButton) findViewById(R.id.hik_camera_set_btn_id);
			setImageBtn.setOnClickListener((OnClickListener) this);
			setImageBtn.setOnTouchListener((OnTouchListener)this);
			
			leftImageBtn=(ImageButton) findViewById(R.id.hik_camera_left_btn_id);
			leftImageBtn.setOnClickListener((OnClickListener) this);
			leftImageBtn.setOnTouchListener((OnTouchListener) this);

			rightImageBtn=(ImageButton) findViewById(R.id.hik_camera_right_btn_id);
			rightImageBtn.setOnClickListener((OnClickListener) this);
			rightImageBtn.setOnTouchListener((OnTouchListener) this);
			
			upImageBtn=(ImageButton) findViewById(R.id.hik_camera_up_btn_id);
			upImageBtn.setOnClickListener((OnClickListener) this);
			upImageBtn.setOnTouchListener((OnTouchListener) this);
			
			downImageBtn=(ImageButton) findViewById(R.id.hik_camera_down_btn_id);
			downImageBtn.setOnClickListener((OnClickListener) this);
			downImageBtn.setOnTouchListener((OnTouchListener) this);
			
			speakerBtn=(ImageButton)findViewById(R.id.hik_camera_speaker_btn_id);
			speakerBtn.setOnClickListener((OnClickListener)this);
			speakerBtn.setOnTouchListener((OnTouchListener)this);
		}
	  public void hangupHandle()
	  {
     	  if (RUN_ON_PAD)
     	  {	  
     		//  	HikView.getInstance().logoutDevice();
				  String  remoteID=WSClientService.getInstance().client.getRemoteID();	
				  
		      	  if(CepsaIPCameraUtil.getInstance()!=null)
		      	  {  
		      		  if(CepsaIPCameraUtil.getInstance().getShouldGetJPGflag())
		      			  CepsaIPCameraUtil.getInstance().setShouldGetJPGflag(false);
		      	  } 		 		      	  
		      	  try 
		      	  {
						Thread.sleep(400);
		      	  } catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				  }
		      	  
 	 	   	   	WSClientService.getInstance().client.myClosePCResource(remoteID);  			 	
 	 	   		
 	 	   		try 
 	 	   		{
 	 	   			WSClientService.getInstance().client.sendMessage(remoteID,	MyRtcSip.HANGUP,null);
 	 	   		}
 	 	   		catch (JSONException e) 
 	 	   		{
 	 	   			// TODO Auto-generated catch block
 	 	   			e.printStackTrace();
 	 	   		}	
     	  }
     	  else
     	  {
				String  remoteID=WSClientService.getInstance().client.getRemoteID();
	 			if(CepsaIPCameraUtil.getInstance()!=null)
		      	  {  
		      		  if(CepsaIPCameraUtil.getInstance().getShouldGetJPGflag())
		      			  CepsaIPCameraUtil.getInstance().setShouldGetJPGflag(false);
		      	  } 

	     		 try 
	     		 {
	 	 	   		WSClientService.getInstance().client.sendMessage(remoteID,	MyRtcSip.HANGUP,null);
	 	 	   	 } 
	     		 catch (JSONException e) 
	     		 {
	 	 	   			// TODO Auto-generated catch block
	 	 	   		 e.printStackTrace();
	 	 	   	 }
     		 
		      	  try 
		      	  {
						Thread.sleep(400);
		      	  } catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				  }		 		      	  
		      	  WSClientService.getInstance().client.myClosePCResource(remoteID);  				 	 	   				 	        		  		      	  
     	  }	 	        	  	 	        	  
     
       	Intent intent=new Intent();
       	
		if(RUN_ON_PAD)
			intent.setClass(HikCameraActivity.this,PadStandbyActivity.class);
		else
			intent.setClass(HikCameraActivity.this,MainMenuActivity.class);    
    		
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);	
    
	   	finish();	   		 
       }
	  
	  private HalfDuplexTalk hdt=new HalfDuplexTalk();	 
	  
	  private SetDVRConfig SF;
	  private boolean IsOnTouch=false;
	  private boolean phoneVoicePlayFlag=true;
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			/*			
			  	case R.id.start:
				startPlay();
				break;
			 */		
			case R.id.hik_camera_hangup_btn_id:		
				if(RUN_ON_PAD==true)
					HikView.getInstance().stopPlay();
				hangupHandle();				
				break;
			case R.id.hik_camera_set_btn_id:
			//	setPlayer();	
				hikCameraSetDialog(this);
				
/*				MyLog.printf(TAG,"..............................hik_camera_set_btn_id on click");
				HikView.audio_play_flag=true;	
				hdt.StopHalfDuplexTalk();
				IsOnTouch = false;
*/

		//		HikView.audio_play_flag=  HikView.audio_play_flag==false?true:false;			  
				break;
			case R.id.hik_camera_down_btn_id:
				MyLog.printf(TAG,"..............................hik_camera button down id");
				/*				if (RUN_ON_PAD==true)			
				{
					if(SF==null)
						SF = new SetDVRConfig(HikView.getInstance().getM_iLogID());
					CompressDataBean cdb = new CompressDataBean();
					
					cdb.setBitRate("19");
					cdb.setFrameRate("11");
					cdb.setResolution("16");
					
					SF.setCompressDataBean(cdb);
					SF.SetDevice(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30);
				}
*/
			  	if (RUN_ON_PAD==true)			
					HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,22,1);
				else
					WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_DOWN_END);			
			  	break;
			case R.id.hik_camera_left_btn_id:
				if (RUN_ON_PAD==true)			
					HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,23,1);			
				else
					WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_LEFT_END);
				break;
			case R.id.hik_camera_right_btn_id:
				if (RUN_ON_PAD==true)			
					HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,24,1);
				else
					WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_RIGHT_END);
				break;
			case R.id.hik_camera_up_btn_id:
				if (RUN_ON_PAD==true)			
					HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,21,1);
				else
					WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_UP_END);
				break;
			case R.id.hik_camera_speaker_btn_id:
				if (RUN_ON_PAD==true)
				{
					if (hdt!=null)
						hdt.StopHalfDuplexTalk();
					IsOnTouch = false;
					if (HikView.audio_play_flag==false)
						HikView.audio_play_flag=true;
				}
				else
				{	
					if (hdt!=null)
						hdt.StopHalfDuplexTalk();
					IsOnTouch = false;
					
					phoneVoicePlayFlag=true;
					WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_VOICE_ON);
					Toast.makeText(this,"camera voice on",Toast.LENGTH_SHORT).show();
					
					/*
					 * if (phoneVoicePlayFlag==false)
					{	
						phoneVoicePlayFlag=true;
						WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_VOICE_ON);
						Toast.makeText(this,"camera voice on",Toast.LENGTH_SHORT).show();
					}
					else
					{
						phoneVoicePlayFlag=false;
						WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_VOICE_OFF);			
						Toast.makeText(this,"camera voice off",Toast.LENGTH_SHORT).show();

					}
					*/	
				}
				break;
				
			}
		}
		static int test_count=0;
	 	/*protected*/public void startPlay() {
			if(WSClientService.run_on_pad_flag==true)
			{
			  	new Thread() {
					@Override
					public void run() {
					//	HikView.getInstance().setDeviceBean(getDeviceBean());
						HikView.getInstance().setSurfaceHolder(hikSurface.getHolder());
						HikView.getInstance().initSDK();
						HikView.getInstance().setExceptionCallBack();
						//japan
						if (CameraListActivity.getHikCameraIndex()==0)
						{
							MyLog.printf(TAG,"192.168.101.200,0000");
					/*		Handler handler=new Handler(Looper.getMainLooper());
					        handler.post(new Runnable(){  
					            public void run(){  
					            	Toast.makeText(HikCameraActivity.this,"192.168.101.200", Toast.LENGTH_SHORT).show();    
					            }  
					        });
					 */
					/*japan shinei*/
					//		HikView.getInstance().myLoginDevice("192.168.101.200",8000,"admin","TPADHIK123");//loginDevice();
							HikView.getInstance().myLoginDevice("192.168.1.222",8000,"admin","tpadhik123");//loginDevice();
						}	
						else  /*if (CameraListActivity.getHikCameraIndex()==1)*/
						{	
							MyLog.printf(TAG,"192.168.101.233,1111");
					/*		Handler handler=new Handler(Looper.getMainLooper());
					        handler.post(new Runnable(){  
					            public void run(){  
									Toast.makeText(HikCameraActivity.this,"192.168.101.233", Toast.LENGTH_SHORT).show();
					            }  
					        });
					 */							
							HikView.getInstance().myLoginDevice("192.168.1.109",8000,"admin","tpadhik123");

							/*japan shiwai*/
					//		HikView.getInstance().myLoginDevice("192.168.101.233",8003,"admin","TPADHIK123");//loginDevice();
						}
						//guo nei
						//		HikView.getInstance().myLoginDevice("192.168.1.222",8001,"admin","tpadhik123");//loginDevice();			   
						// shiwai
					//	HikView.getInstance().myLoginDevice("192.168.1.233",8000,"admin","tpadhik123");//loginDevice();
					
						HikView.getInstance().realPlay();
						/*try {
							Thread.sleep(30*1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}*/
		/*				test_count=0;
						while(true)
						{
							test_count++;
							MyLog.printf(TAG,".........................TEST_COUNT=%d",test_count);
							try {
							//	if (test_count>15)
									Thread.sleep(100);
								else
									Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (WSRtcClient.currentCallNums==0) 	
								break;
								
							WSClientService.getInstance().client.sendHikCameraMsg("cs"String.format("hik_video_frame_end_%d", test_count));			    	
						}*/
					}
				}.start();
			}				
		}
		     
		private DeviceBean getDeviceBean() {
			SharedPreferences sharedPreferences = this.getSharedPreferences(
					"dbinfo", 0);
			String ip = sharedPreferences.getString("ip", "");
			String port = sharedPreferences.getString("port", "");
			String userName = sharedPreferences.getString("userName", "");
			String passWord = sharedPreferences.getString("passWord", "");
			String channel = sharedPreferences.getString("channel", "");
			DeviceBean bean = new DeviceBean();

			HikView.getInstance().myLoginDevice("192.168.101.200",8000,"admin","TPADHIK123");//loginDevice();
			if(ip==null)
			{
				ip=new String("192.168.101.200");
				port=new String("8000");
				userName=new String("admin");
				passWord=new String("TPADHIK123");
			}				
			bean.setIP(ip);
			bean.setPort(port);
			bean.setUserName(userName);
			bean.setPassWord(passWord);
			bean.setChannel(channel);			
			
			return bean;
		}
		
		public void setPlayer() {
			
			LayoutInflater inflater = getLayoutInflater();
			layout = inflater.inflate(R.layout.alert,(ViewGroup) findViewById(R.id.alert));
			ip = (EditText) layout.findViewById(R.id.ip);
			port = (EditText) layout.findViewById(R.id.port);
			userName = (EditText) layout.findViewById(R.id.userName);
			passWord = (EditText) layout.findViewById(R.id.passWord);
			channel = (EditText) layout.findViewById(R.id.channel);
			DeviceBean db = getDeviceBean();
			ip.setText(db.getIP());
			port.setText(db.getPort());
			userName.setText(db.getUserName());
			passWord.setText(db.getPassWord());
			channel.setText(db.getChannel());

			new AlertDialog.Builder(this).setTitle("设置").setView(layout)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setDBData(ip.getText().toString(), port.getText()
									.toString(), userName.getText().toString(),
									passWord.getText().toString(), channel
											.getText().toString());
						}
					}).setNegativeButton("取消", null).show();
		}		
		
		
		// 向系统中存入devicebean的相关数据
		public void setDBData(String ip, String port, String userName,
				String passWord, String channel) {
			SharedPreferences sharedPreferences = this.getSharedPreferences(
					"dbinfo", 0);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("ip", ip);
			editor.putString("port", port);
			editor.putString("userName", userName);
			editor.putString("passWord", passWord);
			editor.putString("channel", channel);
			editor.commit();
		}
		
	  @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub		  
		MyLog.printf(TAG,"onConfigurationChanged!!!");
		super.onConfigurationChanged(newConfig);
		

		width=getWindowManager().getDefaultDisplay().getWidth();/*getSize(screenSizePoint);*/				
		height=getWindowManager().getDefaultDisplay().getHeight();
/*		String msg=newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE?"横屏":"竖屏";		
		Toast.makeText(this,msg,Toast.LENGTH_LONG).show();	
*/
		
	}
	  @Override
	protected void onResume() {
		// TODO Auto-generated method stub
	    vsv.onResume();
	    MyLog.printf(TAG,"....................onResume");
		super.onResume();
	 	setInstance(this);

	}
	  
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
	    MyLog.printf(TAG,"....................onPause");
		if (hdt!=null)
			hdt.StopHalfDuplexTalk();
		IsOnTouch = false;
		
	 	setInstance(null);
	    vsv.onPause();
	    IpCameraActivity_created_flag=false;
	//    setDataChannelOnlyFlag(boolean onlyFlag)
	    if (WSClientService.getInstance().client!=null)
	    {	
		    WSClientService.getInstance().client.stopVideoSource();
		    WSClientService.getInstance().client.setDataChannelOnlyFlag(false);//stopVideoSource();
		}
	    
		if (HikView.getInstance().getM_iPlayID()!=-1) {
			HikView.getInstance().stopPlay();   //停止实时预览
		}
	 	if (RUN_ON_PAD)
	 	{	  
	 		HikView.getInstance().logoutDevice();
	 	}	 
		super.onPause();
	 }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	    MyLog.printf(TAG,"....................onDestroy");		
	    //btuce delete it at 20150814
/*
 * 		 VideoRendererGui.setLocalVideoShowScalFlag(false);
*/		super.onDestroy();
/*		if (HikView.getInstance().getM_iPlayID()!=-1) {
			HikView.getInstance().stopPlay();   //停止实时预览
		}*/

		/*
		new Thread() {
			@Override
			public void run() {
				HikView.getInstance().logoutDevice();
				HikView.getInstance().freeSDK();
				System.exit(0);
			}
		}.start();
		*/
	}
	
	@Override
	public void onCallReady(String callId) {
		// TODO Auto-generated method stub		
		if (callId != null) {
		 try {
			    MyLog.printf(TAG,"answer............");
		        answer(callId);
		      } catch (JSONException e) {
		        e.printStackTrace();
		      }
		}
		else {		     
		     MyLog.printf(TAG,"onCallReady start call");
		  //   Log.e(TAG,"WebRtcAudioRecord.BuiltInAECIsAvailable():"+WebRtcAudioRecord.BuiltInAECIsAvailable()); 
		   //   startCam();
		   //   myStartCam();
		   //   call(callId);
		   //   showCameraTest();
		}
	}
	public void answer(String callerId) throws JSONException {
//	    client.sendMessage(callerId, "init", null);
	 //   startCam();
		MyLog.printf(TAG,".......answer()");	 
		WSClientService.getInstance().client.sendMessage(callerId, "init", null);
	    WSClientService.getInstance().client.setCamera();
	}	
	@Override
	public void onStatusChanged(final String newStatus) {
		// TODO Auto-generated method stub		
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	          Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
	          if (newStatus!=null&&newStatus.equals("DISCONNECTED"))
	          {
	          	
	          }
	          else if (newStatus!=null&&newStatus.equals(MyRtcSip.HANGUP))
	          {
	        	  
/*	          	vsv.onPause();	          	
        		Intent intent=new Intent();
        		if(RUN_ON_PAD)
        			intent.setClass(IpCameraActivity.this,PadStandbyActivity.class);
        		else
        			intent.setClass(IpCameraActivity.this,MainMenuActivity.class);    
        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
*/	             	  
	          	finish();
	          }	
	        }
	      });		
	}
	
	@Override
	public void onLocalStream(MediaStream localStream) {
		// TODO Auto-generated method stub	
		if (localStream!=null&&localStream.audioTracks.get(0)!=null)
		{
			MyLog.printf(TAG,"..........................on Local Stream   audio setEnabled(false)");
		//	localStream.audioTracks.get(0).setEnabled(false);
		}
		if (WSClientService.getInstance().client.getDataChannelOnlyFlag())    return ;
	}
	
	@Override
	public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
		// TODO Auto-generated method stub	
		if (WSClientService.getInstance().client.getDataChannelOnlyFlag())   return ; 
	}
	
	@Override
	public void onRemoveRemoteStream(MediaStream remoteStream, int endPoint) {
		// TODO Auto-generated method stub		
		if (WSClientService.getInstance().client.getDataChannelOnlyFlag())    return ;
	}
		
	@Override	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	//	MyLog.printf(TAG,"..............onkeydown");
		if (keyCode ==KeyEvent.KEYCODE_BACK)  
		{
			if(RUN_ON_PAD==true)
				HikView.getInstance().stopPlay();
			hangupHandle();	
		}	
		return false/*true*/;
	}
	
@Override
public boolean onDown(MotionEvent e) {
	// TODO Auto-generated method stub
	MyLog.printf(TAG,"onDown");
	return false;
}

private  int verticalMinDistance=100;
private   int minVelocity=0; 
@Override
public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		float velocityY) {
	// TODO Auto-generated method stub
	MyLog.printf(TAG,"onFling");
//	MyLog.printf(TAG,"e1:X=%f,y=%f ...e2=X=%f,y=%f",e1.getX(),e1.getY(),e2.getX(),e2.getY());
	String str="test";
	if ((e1.getX()-e2.getX()>verticalMinDistance)&&Math.abs(velocityX) > minVelocity)
	{
		//向左手势
		str="left";
	//	CepsaIPCameraUtil.getInstance().sendPtzDirectMsg(str);
		/*if(RUN_ON_PAD)
			CepsaIPCameraUtil.getInstance().new sendPtzMessageThread(str).start();
		else
			WSClientService.getInstance().client.sendCepsaCameraDirMsgfunc(str);*/
		
	}
	else if ((e2.getX()-e1.getX()>verticalMinDistance)&&Math.abs(velocityX) > minVelocity)
	{
		//向右手势
		str="right";
	//	CepsaIPCameraUtil.getInstance().sendPtzDirectMsg(str);
		/*if(RUN_ON_PAD)
			CepsaIPCameraUtil.getInstance().new sendPtzMessageThread(str).start();
		else
			WSClientService.getInstance().client.sendCepsaCameraDirMsgfunc(str);*/
	}
	else if ((e2.getY()-e1.getY()>verticalMinDistance)&&Math.abs(velocityY) > minVelocity)
	{
		//向上手势
		str="down";
	//	CepsaIPCameraUtil.getInstance().sendPtzDirectMsg(str);
	//	CepsaIPCameraUtil.getInstance().sendPtzThread("down");
		/*if(RUN_ON_PAD)
			CepsaIPCameraUtil.getInstance().new sendPtzMessageThread(str).start();
		else
			WSClientService.getInstance().client.sendCepsaCameraDirMsgfunc(str);*/
	}
	else if ((e1.getY()-e2.getY()>verticalMinDistance)&&Math.abs(velocityY) > minVelocity)
	{
		//向下手势
		str="up";		
	//	CepsaIPCameraUtil.getInstance().sendPtzDirectMsg(str);
		/*if(RUN_ON_PAD)
			CepsaIPCameraUtil.getInstance().new sendPtzMessageThread(str).start();
		else
			WSClientService.getInstance().client.sendCepsaCameraDirMsgfunc(str);*/
	
	}		
//	Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
	return false;
}
//重写OnTouchListener的onTouch方法 
//此方法在触摸屏被触摸，即发生触摸事件（接触和抚摸两个事件，挺形象）的时候被调用。


@Override
public boolean onTouch(View v, MotionEvent event) {
	// TODO Auto-generated method stub
	MyLog.printf(TAG,"onTouch");
//	detector.onTouchEvent(event); 	
	switch (v.getId()) {	
	case R.id.hik_camera_set_btn_id:
/*
 * 		if (HikView.audio_play_flag==true)
			HikView.audio_play_flag=false;
		MyLog.printf(TAG,"...........IsOnTouch=%b",IsOnTouch);
		if(!IsOnTouch){
			IsOnTouch = !IsOnTouch;
			hdt.duplexSoundManger.stop_duplextalk();
			hdt.StartHalfDuplexTalk(HikView.getInstance().getM_iLogID(), 1, 0G722Encode);
		}	
*/
         break;
         
	case R.id.hik_camera_up_btn_id:
		if (RUN_ON_PAD==true)			
			HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,21,0);		
		else
			WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_UP_ING);
		break;	
	case R.id.hik_camera_down_btn_id:		
		if (RUN_ON_PAD==true)			
			HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,22,0);
		else
			WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_DOWN_ING);
		break;

/*		if(!IsOnTouch){
			IsOnTouch = !IsOnTouch;
			hdt.duplexSoundManger.stop_duplextalk();
			hdt.StartHalfDuplexTalk(HikView.getInstance().getM_iLogID(), 1, 0G722Encode);
		}
		break;
*/
	case R.id.hik_camera_left_btn_id:
		if (RUN_ON_PAD==true)			
			HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,23,0);			
		else
			WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_LEFT_ING);
		break;
	case R.id.hik_camera_right_btn_id:
		if (RUN_ON_PAD==true)	
		{
	//		MyLog.printf(TAG,"1111111111111111_ringht");
			HikView.getInstance().getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,24,0);
		}	
		else
			WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_RIGHT_ING);
		break;
	case R.id.hik_camera_speaker_btn_id:
		if (RUN_ON_PAD==true)
		{
			if (HikView.audio_play_flag==true)
				HikView.audio_play_flag=false;
			MyLog.printf(TAG,"...........IsOnTouch=%b",IsOnTouch);
			if(!IsOnTouch){
				IsOnTouch = !IsOnTouch;
				hdt.duplexSoundManger.stop_duplextalk();
				hdt.StartHalfDuplexTalk(HikView.getInstance().getM_iLogID(), 1, 0/*G722Encode*/);
			}
		}
		else
		{
			MyLog.printf(TAG,"...........IsOnTouch=%b",IsOnTouch);
			if(!IsOnTouch){
				
				phoneVoicePlayFlag=false;
				WSClientService.getInstance().client.sendHikCameraDirMsgfunc(MyRtcSip.HIK_CAMERA_VOICE_OFF);			
				Toast.makeText(this,"camera voice off",Toast.LENGTH_SHORT).show();
				
				IsOnTouch = !IsOnTouch;
				hdt.duplexSoundManger.stop_duplextalk();
				hdt.StartHalfDuplexTalk(HikView.getInstance().getM_iLogID(), 1, 0/*G722Encode*/);
			}
		}	
		
		break;
	}
	return false;
}



@Override
public void onLongPress(MotionEvent e) {
	// TODO Auto-generated method stub
//	MyLog.printf(TAG,"onLongPress");	
}

@Override
public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
		float distanceY) {
	// TODO Auto-generated method stub
	//	MyLog.printf(TAG,"onScroll");
	return false;
}

@Override
public void onShowPress(MotionEvent e) {
	// TODO Auto-generated method stub
	//MyLog.printf(TAG,"onShowPress");	
}

@Override
public boolean onSingleTapUp(MotionEvent e) {
	// TODO Auto-generated method stub
//	MyLog.printf(TAG,"onSingleTapUp");
	return false;
}


public static  class MenuBarFragment extends Fragment {
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	      return inflater.inflate(R.layout.fragment_menubar, container, false);
	    }
  }



static String  resolutionValue="6",bitrateValue="11"; 


 private void hikCameraSetDialog (Context context) {

	 	AlertDialog hikSetDialog;
	 	RadioGroup  resolutionGroup,bitrate_radio_group;              
	     //动态加载布局生成View对象
	     LayoutInflater layoutInflater = LayoutInflater.from(context);
	     View hikSetDialogView = layoutInflater.inflate(R.layout.hik_camera_set_dialog, null);      
	     //获取布局中的控件
	     resolutionGroup=(RadioGroup)hikSetDialogView.findViewById(R.id.hik_camera_resolution_radio_group);    
	     if (resolutionValue.equals("16"))
	    	 resolutionGroup.check(R.id.resolution_640_480_id);
	     else if(resolutionValue.equals("6"))
		     resolutionGroup.check(R.id.resolution_320_240_id);
	      
    	  resolutionGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				int id=group.getCheckedRadioButtonId();
				if (id ==R.id.resolution_320_240_id)
				{
					MyLog.printf(TAG,"........................resolutionGroup ID=0");
					resolutionValue="6";
				}
				else if (id ==R.id.resolution_640_480_id)
				{
					MyLog.printf(TAG,"........................resolutionGroup ID=1");
					resolutionValue="16";
				}
			}		
		});
	     bitrate_radio_group=(RadioGroup)hikSetDialogView.findViewById(R.id.hik_camera_bitrate_radio_group);    
	     if (bitrateValue.equals("11"))
	    	 bitrate_radio_group.check(R.id.bitrate_0_id);
	     else if(bitrateValue.equals("15"))
	    	  bitrate_radio_group.check(R.id.bitrate_1_id);
	     else if(bitrateValue.equals("19"))
	    	  bitrate_radio_group.check(R.id.bitrate_2_id);
	     bitrate_radio_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				int id=group.getCheckedRadioButtonId();
				if (id ==R.id.bitrate_0_id)
				{
					MyLog.printf(TAG,"........................bitrateValue ID=0");
					bitrateValue="11";
				}
				else if (id ==R.id.bitrate_1_id)
				{
					MyLog.printf(TAG,"........................bitrateValue ID=1");
					bitrateValue="15";
				}
				else if (id ==R.id.bitrate_2_id)
				{
					MyLog.printf(TAG,"........................bitrateValue ID=1");
					bitrateValue="19";					
				}	
			}		
		});     
	     //创建一个AlertDialog对话框
	     hikSetDialog = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_DARK)
	         .setTitle("HikCamera Set Dialog")
	         .setView(hikSetDialogView)                //加载自定义的对话框式样
	         .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {			
	 			@Override
	 			public void onClick(DialogInterface dialog, int which) {
	 				// TODO Auto-generated method stub	
					if (RUN_ON_PAD==true)			
					{
						if(SF==null)
							SF = new SetDVRConfig(HikView.getInstance().getM_iLogID());
						CompressDataBean cdb = new CompressDataBean();
						
						cdb.setBitRate(bitrateValue);
						cdb.setFrameRate("8");
						cdb.setResolution(resolutionValue);
						
						SF.setCompressDataBean(cdb);
						SF.SetDevice(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30);
					}
	 				}
	 			})
	         .setNeutralButton("CANCEL", null)
	         .create();  
	         
	     hikSetDialog.show();
 	}
 }



