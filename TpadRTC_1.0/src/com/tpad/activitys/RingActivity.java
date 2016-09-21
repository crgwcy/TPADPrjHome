package com.tpad.activitys;

import org.json.JSONException;

import com.mywebrtc.japan.util.OutDoorCfg;
import com.mywebrtc.util.MyMediaPlayer;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;

import com.RTC.TpadRTC.R;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

public class RingActivity extends Activity {

	private Intent intent=null;
	private String remoteId=null;
	private String remoteName=null;
	private MyMediaPlayer myMP=null;
	private  static RingActivity ringActivityInstance=null;
	private  boolean RUN_ON_PAD=false;
	public static RingActivity getRingActivityInstance()
	{
		return ringActivityInstance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ringActivityInstance=this;

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
/*		    getWindow().addFlags(
		            LayoutParams.FLAG_FULLSCREEN
		            | LayoutParams.FLAG_KEEP_SCREEN_ON
		            | LayoutParams.FLAG_DISMISS_KEYGUARD
		            | LayoutParams.FLAG_SHOW_WHEN_LOCKED
		            | LayoutParams.FLAG_TURN_SCREEN_ON);*/
	         
		}
		else
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ring);
		intent=getIntent();
		if (intent!=null)
		{
			remoteName=intent.getStringExtra(MyRtcSip.RING_REMOTE_NAME_EXTRA);
			remoteId=intent.getStringExtra(MyRtcSip.RING_REMOTE_ID_EXTRA);
			if(remoteName!=null && remoteId!=null)
			{
				((TextView)findViewById(R.id.remote_name_tx_id)).setText(remoteName);	
			}
			else
			{
				return ;
			}				
		}		
			
		((ImageButton) findViewById(R.id.ring_answer_btn)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
					if (remoteName.contains(OutDoorCfg.OUT_DOOR_LAST_NAME))
						OutDoorCfg.IS_OUTDOOR_CALL_FLAG=true;
   		   		try {
					WSClientService.getInstance().client.sendMessage(remoteId,MyRtcSip.RING_ANSWER_MSG,null);
				} catch (JSONException e) {
				// 	TODO Auto-generated catch block
					e.printStackTrace();
				}	
   		   		//	if (WSClientService.getUsrName()!=null)	
				{	
					WSClientService.getInstance().startMyRtcActivity(remoteId,null);
		//			WSClientService.getInstance().startCallActivity(communityAccountIds[position],communityAccountNames[position]);
				}
				finish();

			}
		});
		((ImageButton) findViewById(R.id.ring_decline_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
   		   		try {
					WSClientService.getInstance().client.sendMessage(remoteId,MyRtcSip.RING_DECLINE_MSG,null);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				finish();				
				
			}
		});		
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		myMP=new MyMediaPlayer(this);
		myMP.startPlayThread();	
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(myMP!=null)
			myMP.release();	
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		ringActivityInstance=null;				
		super.onDestroy();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ring, menu);
		return true;
	}
	

}
