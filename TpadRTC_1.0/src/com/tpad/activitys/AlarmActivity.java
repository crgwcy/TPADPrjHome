package com.tpad.activitys;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mywebrtc.util.HttUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyMediaPlayer;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.pad.activitys.PadStandbyActivity.MembersInfoBroadcastRecive;

import com.RTC.TpadRTC.R;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AlarmActivity extends Activity {

	private Intent intent=null;
	private String remoteId=null;
	private String remoteName=null;
	private MyMediaPlayer myMP=null;
	private  boolean RUN_ON_PAD=false;
	private ImageView alarmIcon;
	private  static String TAG="AlarmActivity";
	private static AlarmActivity alarmInstance=null;
	public static AlarmActivity getInstance()
	{
		return alarmInstance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		RUN_ON_PAD=WSClientService.run_on_pad_flag;//this.getResources().getBoolean(R.bool.APP_RUN_ON_PAD);		
		if (RUN_ON_PAD)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN
							| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_FULLSCREEN
							| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
			getWindow().addFlags( LayoutParams.FLAG_TURN_SCREEN_ON|LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED);	         
		}
		else
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN
							| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_FULLSCREEN
							| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
			getWindow().addFlags( LayoutParams.FLAG_TURN_SCREEN_ON|LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED);	         
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		
    	alarmIcon=(ImageView)findViewById(R.id.alarm_flashing_icon_id);  			
		
		((Button)findViewById(R.id.alarm_ok_btn_id)).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mAlarmRingThread!=null)
				mAlarmRingThread.stopAlarmRing();
				alarmTimerCancel();
				finish();
		//		mySendBroadcast();
			}
		});

		alarmInstance=this;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"onResume");
		super.onResume();
//		registerReceiverHandle();
		alarmTimerDoSomethingRepeately();
		playAlarmRingThread();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"onPause");	
		alarmInstance=null;
		super.onPause();
/*		if (membersInfoRecive!=null)
		{	
			unregisterReceiver(membersInfoRecive);
			membersInfoRecive=null;
		}*/
		if (mAlarmRingThread!=null)
		mAlarmRingThread.stopAlarmRing();
		alarmTimerCancel();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private  AlarmRingThread mAlarmRingThread;	
	
	private Timer alarm_timer;
	private  boolean  alarm_flashing_flag=false;
	public void  alarmTimerDoSomethingRepeately()
	{	
		alarm_timer=new Timer();
		alarm_timer.schedule(new TimerTask() {	
			@Override
			public void run() {
				// TODO Auto-generated method stub
		//		MyLog.printf(TAG,"timer......................%d",++counter);
				alarmIcon.post(new Runnable() {					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(alarm_flashing_flag==false)
						{
							alarm_flashing_flag=true;
							if (alarmIcon!=null)
								alarmIcon.setImageResource(R.drawable.alert_icon);
						}	
						else
						{	
							alarm_flashing_flag=false;
							if (alarmIcon!=null)							
								alarmIcon.setImageResource(R.drawable.alert_icon_flash);
						}	
					}
				});//setText(timeStr);
			}
		}, 0,200);// 1000ms
	}	
	public void  alarmTimerCancel()
	{
		if(alarm_timer!=null)
		{	
			alarm_timer.cancel();
			alarm_timer=null;
		}	
	}
	public void playAlarmRingThread()
	{
		mAlarmRingThread = new AlarmRingThread(this);
		mAlarmRingThread.start();	
		return ;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode==KeyEvent.KEYCODE_BACK)
		{
			if (mAlarmRingThread!=null)
			mAlarmRingThread.stopAlarmRing();
			alarmTimerCancel();
			finish();
		}	
		return true;
	}
	
	class AlarmRingThread extends Thread {
		MediaPlayer mMediaPlayer;
		Context mContext;
		public Vibrator vibrator;

		public AlarmRingThread(Context mContext) {
			mMediaPlayer = new MediaPlayer();
			this.mContext = mContext;
		}

		@Override
		public void run() {
	//		showNotification(true);
			MyLog.printf(TAG,"AlarmRingThread000");
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			try {
				mMediaPlayer.reset();
				mMediaPlayer.setDataSource(mContext, alert);
				final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				MyLog.printf(TAG,"AlarmRingThread111");

				if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
					MyLog.printf(TAG,"AlarmRingThread111222");
			//		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
					mMediaPlayer.setLooping(true);
					mMediaPlayer.prepare();
					mMediaPlayer.start();
				}
				MyLog.printf(TAG,"AlarmRingThread222");

			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				long[] pattern = { 1000, 1000, 1000, 1000, 1000, 1000, 1000,
						1000,1000,1000,1000,1000 }; // OFF/ON/OFF/ON...
				vibrator.vibrate(pattern, -1);
			} catch (Exception e) {
				e.printStackTrace();
			}		
			
		}
		public  void stopAlarmRing()
		{
			MyLog.printf(TAG,"stopAlarmRing000");		
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				MyLog.printf(TAG,"stopAlarmRing111");
				mMediaPlayer.stop();
			//	mMediaPlayer.release();
			}
			
			try {
				if (null != vibrator) {
					vibrator.cancel();
					vibrator = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
	}	
	
	
	
	
	
	
	
	
	
	private static String  USER_INTENT_EXTRA="usrs_info";		  
	public final static String RECI_COAST="com.tpad.act.GetDataUtil";
	private String mGetDataAddress;
	private  MembersInfoBroadcastRecive membersInfoRecive=null;
				
	public  void registerReceiverHandle()
	{
		IntentFilter filter=new IntentFilter(RECI_COAST);  	
		membersInfoRecive=null;
		membersInfoRecive=new MembersInfoBroadcastRecive();	
		registerReceiver(membersInfoRecive, filter);	
		MyLog.printf(TAG,"registerReceiverHandle111");
	}	
		
	//注册广播		
	/*
	*Register a BroadcastReceiver to be run in the main activity thread. 
	*The receiver will be called with any broadcast Intent that matches filter, 
	*in the main application thread. 
	* 
	*/
		
		public  void mySendBroadcast()
		{
			mGetDataAddress = "http://" + getResources().getString(R.string.host);
			mGetDataAddress += (":"+getResources().getString(R.string.port)+"/streams");	
			new Thread(){
				@Override  
		        public void run() {  
		            Intent intent = new Intent(RECI_COAST);  
		            try {  
		                //获取服务器返回的信息  
		                String reslut = HttUtil.getRequest(mGetDataAddress);  
		                intent.putExtra(USER_INTENT_EXTRA,reslut);  
		                MyLog.printf(TAG,"reslut=%s",reslut);
		                //发送广播  
		                sendBroadcast(intent);  
		            } catch (InterruptedException e) {  
		                e.printStackTrace();  
		            } catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
		        }   		
			}.start();
		}	

	  public  class MembersInfoBroadcastRecive  extends BroadcastReceiver
	  {

	  	private   HashMap<String,String>  membersHashMap/*=new HashMap<String, String>()*/;
	  	
	    
	  	public HashMap<String,String>  getMembersHashMap()
	  	{
	  		return membersHashMap;
	  	}
	  	public int getMembersTotal()
	  	{
	  		int total=membersHashMap.size();
	  		
	  	    return total;	
	  	}
	  	
	  	public MembersInfoBroadcastRecive() {
			// TODO Auto-generated constructor stub
	  	}	    	
	  	
	  	
	  	
	  	public void onReceive(Context context,Intent intent)
	  	{
	  		try
	  		{			
	  			JSONObject jsonobject = new JSONObject(  
	                      intent.getStringExtra(USER_INTENT_EXTRA)); 
	  			
	  			 Iterator<?> it=jsonobject.keys();	  			
	  			 String /*a1*/key="";
	  			 if (it.hasNext())
	  			 {	 
	  				 membersHashMap=null;
	  				 membersHashMap=new HashMap<String, String>();	  				 
		    			 while(it.hasNext())
		    			 {
		    				 /*a1*/key=(String)it.next().toString();			 
		    				 MyLog.printf(TAG,"............keys  str=%s", key/*a1*/);
		    				 JSONObject  myjsobject=jsonobject.getJSONObject(key/*a1*/);
		    				 String  getjsonName=myjsobject.optString("name");
		    				 membersHashMap.put(key/*a1*/,getjsonName); 		    				 
		    				 if(getjsonName!=null)
		    				 {	 
		    					 MyLog.printf(TAG,"............getjsonName  str=%s", getjsonName);		   				 
		    		//			 if (!getjsonName.equals(WSClientService.getUsrName()))
		    						WSClientService.getInstance().client.sendMessage(key,MyRtcSip.ALARM_MSG,null);								 		  							 
		    				 }	 
		    				 
		    				 
		    			 }	 
/*		    			 if (membersHashMap.size()>0)
		    			 {
		    				 Message msg=new Message();
		    				 msg.what=UPDATE_UI_FLAG;
		    				 msg.arg1=membersHashMap.size();
		    				 msg.obj=(Object)membersHashMap;
		    				 if (myhandler!=null)
		    					 myhandler.sendMessage(msg);
		    			 }	*/ 
	  			 }
	  			 else
	  			 {
	  				 membersHashMap=null;	 
	  			 }
	  			     //	jsonobject.keys();    			
	  			
	  		}catch(JSONException e)
	  		{
	  			e.printStackTrace();
	  		}
	  	}
	  } 
	
}
