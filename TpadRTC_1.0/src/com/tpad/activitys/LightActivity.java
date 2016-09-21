package com.tpad.activitys;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mywebrtc.util.HttUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;


import com.RTC.TpadRTC.R;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.app.Activity;
//import android.view.Menu;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class LightActivity extends Activity implements SeekBar.OnSeekBarChangeListener,View.OnClickListener{
	// “自定义SeekBar”
	private SeekBar mSeekBarSelf;
	private static final String  TAG="LightActivity";
	private  ImageButton light_on_off_status_btn;
	private  ImageButton open_btn;
	private  ImageButton connect_btn;
	private  boolean on_off_btn_status=false;	
	
//	private  ImageButton light_spot_on_off_status_btn;
	private  boolean spoton_off_btn_status=false;		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/*
 * 		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
*/
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_light);
		lightRetBtn=(ImageButton)findViewById(R.id.light_ret_id);
		lightRetBtn.setOnClickListener(lightRetListener);	
		// “自定义SeekBar”
		mSeekBarSelf = (SeekBar) findViewById(R.id.seekbar_self);
		mSeekBarSelf.setOnSeekBarChangeListener(this);
		open_btn=(ImageButton)findViewById(R.id.night_light_id);
		open_btn.setOnClickListener(this);
		connect_btn=(ImageButton)findViewById(R.id.memory_id);
		connect_btn.setOnClickListener(this);
		mSeekBarSelf.setOnSeekBarChangeListener(this);
		light_on_off_status_btn=(ImageButton)findViewById(R.id.on_off_id);
		light_on_off_status_btn.setOnClickListener(this);
/*		light_spot_on_off_status_btn=(ImageButton)findViewById(R.id.spot_id);
		light_spot_on_off_status_btn.setOnClickListener(this);	*/	
	}
	
/*
 * 	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
*/	

	private ImageButton lightRetBtn;
	//private  OnClickListener setRetListener=new  OnClickListener()
	
	private  OnClickListener lightRetListener=new OnClickListener()//OnClickListener()
	{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}

	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onResume()");	
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onPause()");
		super.onPause();
	}
	
    /*
     * SeekBar滚动时的回调函数
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
    	MyLog.printf(TAG, "seekid=%d  progess=%d",seekBar.getId(),progress);		
	}

    /*
     * SeekBar开始滚动的回调函数
     */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * SeekBar停止滚动的回调函数
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId()==R.id.on_off_id)
		{
			if (on_off_btn_status==false)
			{
				 WSClientService.setNotifyMsgStr(MyRtcSip.BLE_DATA_HEAD+"light_on");
				 WSClientService.getInstance().mySendBroadcast();
				on_off_btn_status=true;
				light_on_off_status_btn.setImageResource(R.drawable.light_on_btn_org);
			}
			else
			{
				 WSClientService.setNotifyMsgStr(MyRtcSip.BLE_DATA_HEAD+"light_off");
				 WSClientService.getInstance().mySendBroadcast();
				on_off_btn_status=false;
				light_on_off_status_btn.setImageResource(R.drawable.light_off_btn_org);			
			}	
		}
		else if(v.getId()==R.id.night_light_id)
		{
			 WSClientService.setNotifyMsgStr(MyRtcSip.BLE_DATA_HEAD+"light_open");
			 WSClientService.getInstance().mySendBroadcast();
		}
		else if(v.getId()==R.id.memory_id)
		{
			 WSClientService.setNotifyMsgStr(MyRtcSip.BLE_DATA_HEAD+"light_connect");
			 WSClientService.getInstance().mySendBroadcast();
		}	
			
/*		else if (v.getId()==R.id.spot_id)
		{
			if (spoton_off_btn_status==false)
			{
				spoton_off_btn_status=true;
				light_spot_on_off_status_btn.setImageResource(R.drawable.light_spot_on_btn_org);
			}
			else
			{
				spoton_off_btn_status=false;
				light_spot_on_off_status_btn.setImageResource(R.drawable.light_spot_off_btn_org);			
			}	
		}*/
		
	}	
	
	
	


}
