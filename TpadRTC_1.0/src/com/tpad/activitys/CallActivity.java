package com.tpad.activitys;

import org.json.JSONException;

import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.pad.activitys.PadCommunityActivity;

import com.RTC.TpadRTC.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class CallActivity extends Activity {

	private Intent intent;
	private String remoteName=null;
	private String remoteId=null;
	private static CallActivity createdCallActivityInstance=null;
	private boolean RUN_ON_PAD=false;
	public static CallActivity getcreatedCallActivityInstance()
	{
		return createdCallActivityInstance;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	//	super.onCreate(savedInstanceState);
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
		}
		else
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		}
		createdCallActivityInstance=this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_peer);
		intent=getIntent();
		if (intent!=null)
		{
			remoteName=intent.getStringExtra(MyRtcSip.RING_REMOTE_NAME_EXTRA);
			remoteId=intent.getStringExtra(MyRtcSip.RING_REMOTE_ID_EXTRA);
			if(remoteName!=null && remoteId!=null)
			{
				((TextView)findViewById(R.id.call_name_id)).setText(remoteName);		
			}
			else
			{
				return ;
			}	
			
		}		
		
		((ImageButton)findViewById(R.id.cancel_btn_id)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					WSClientService.getInstance().client.sendMessage(remoteId,MyRtcSip.CALL_CANCEL_MSG,null);					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent=new Intent();
				if (RUN_ON_PAD)
					intent.setClass(CallActivity.this,PadCommunityActivity.class);
				else
					intent.setClass(CallActivity.this,CommunityActivity.class);
				startActivity(intent);	
				finish();		
			}
		});
	}
   @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	   createdCallActivityInstance=null;
	
		super.onDestroy();
	}
}
