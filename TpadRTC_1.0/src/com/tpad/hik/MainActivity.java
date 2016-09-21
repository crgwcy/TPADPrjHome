/*package com.tpad.hik;

import fr.pchab.AndroidRTC.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener,OnTouchListener{
//public class MainActivity extends Activity {

	private Button start, set, stop,btUp,btDown,btLeft,btRight;
	//btUpLeft,btUpRight,btDownLeft,btDownRight;
	private SurfaceView mySurfaceView;
	//private boolean backflag;
	private View layout;
	private EditText ip;
	private EditText port;
	private EditText userName;
	private EditText passWord;
	private EditText channel;

//	final int left = 1;
//	final int right = 2;
//	final int up = 3;
//	final int down = 4;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hik_camera);  
        mySurfaceView = (SurfaceView) findViewById(R.id.video_hik);
        //initView();
		initButton();
    }

//    @Override
//	protected void onResume() {
//    	super.onResume();
//    	
//    }
    
	@Override
	protected void onPause() {
		super.onPause();
		if (HikView.getInstance().getM_iPlayID()==0) {
			HikView.getInstance().stopPlay();   //停止实时预览
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (HikView.getInstance().getM_iPlayID()==0) {
			HikView.getInstance().stopPlay();   //停止实时预览
		}
		new Thread() {
			@Override
			public void run() {
				HikView.getInstance().logoutDevice();
				HikView.getInstance().freeSDK();
				System.exit(0);
			}
		}.start();
	}

	//private class myBtListener implements OnClickListener,OnTouchListener{
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.start:
				startPlay();
				break;
			case R.id.stop:
				HikView.getInstance().stopPlay();
				break;
			case R.id.set:
				setPlayer();
				break;
			case R.id .bt_down:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,22,1);
				break;
			case R.id.bt_left:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,23,1);			
				break;
			case R.id.bt_right:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,24,1);
				break;
			case R.id.bt_up:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,21,1);
				break;
			}
		}
	
		@SuppressLint("ClickableViewAccessibility") @Override
		public boolean onTouch(View view, MotionEvent event){
			switch (view.getId()) {
			case R.id .bt_down:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,22,0);
				break;
			case R.id.bt_left:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,23,0);			
				break;
			case R.id.bt_right:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,24,0);
				break;
			case R.id.bt_up:
				HikView.getInstance().getVideoCtr().
				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,21,0);
				break;
//			case R.id .bt_up_left:
//				HikView.getInstance().getVideoCtr().
//				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,25,0);
//				break;
//			case R.id.bt_up_right:
//				HikView.getInstance().getVideoCtr().
//				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,26,0);			
//				break;
//			case R.id.bt_down_left:
//				HikView.getInstance().getVideoCtr().
//				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,27,0);
//				break;
//			case R.id.bt_down_right:
//				HikView.getInstance().getVideoCtr().
//				NET_DVR_PTZControl_Other(HikView.getInstance().getM_iPlayID(),1,28,0);
//				break;
			}
			return false;
		}
	//}
	private void initButton(){
		
		start=(Button) findViewById(R.id.start);
		start.setOnClickListener((OnClickListener) this);
		
		stop = (Button) findViewById(R.id.stop);
		stop.setOnClickListener((OnClickListener) this);
		
		set = (Button) findViewById(R.id.set);
		set.setOnClickListener((OnClickListener) this);
		
		btUp = (Button) findViewById(R.id.bt_up);
		btUp.setOnTouchListener((OnTouchListener) this);
		btUp.setOnClickListener((OnClickListener) this);
		
		btDown = (Button) findViewById(R.id.bt_down);
		btDown.setOnTouchListener((OnTouchListener) this);
		btDown.setOnClickListener((OnClickListener) this);
		
		btLeft = (Button) findViewById(R.id.bt_left);
		btLeft.setOnClickListener((OnClickListener) this);
		btLeft.setOnTouchListener((OnTouchListener) this);
		
		btRight = (Button) findViewById(R.id.bt_right);
		btRight.setOnClickListener((OnClickListener) this);
		btRight.setOnTouchListener((OnTouchListener) this);
		
//		btUpLeft = (Button) findViewById(R.id.bt_up_left);
//		btRight.setOnClickListener((OnClickListener) this);
//		btRight.setOnTouchListener((OnTouchListener) this);
//		
//		btUpRight = (Button) findViewById(R.id.bt_up_right);
//		btRight.setOnClickListener((OnClickListener) this);
//		btRight.setOnTouchListener((OnTouchListener) this);
//		
//		btDownLeft = (Button) findViewById(R.id.bt_down_left);
//		btRight.setOnClickListener((OnClickListener) this);
//		btRight.setOnTouchListener((OnTouchListener) this);
//		
//		btDownRight = (Button) findViewById(R.id.bt_down_right);
//		btRight.setOnClickListener((OnClickListener) this);
//		btRight.setOnTouchListener((OnTouchListener) this);
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

	protected void startPlay() {
//		if (backflag) {
//			backflag = false;
//			new Thread(){
//			@Override
//			public void run(){
//				HikView.getInstance().setSurfaceHolder(mySurfaceView.getHolder());
//				HikView.getInstance().initSDK();
//				HikView.getInstance().setExceptionCallBack();
//				HikView.getInstance().realPlay();
//			}
//			}.start();
//		}
//		else
			new Thread() {
			@Override
			public void run() {
				HikView.getInstance().setDeviceBean(getDeviceBean());
				HikView.getInstance().setSurfaceHolder(mySurfaceView.getHolder());
				HikView.getInstance().initSDK();
				HikView.getInstance().setExceptionCallBack();
				HikView.getInstance().loginDevice();
				HikView.getInstance().realPlay();
			}
		}.start();
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

		bean.setIP(ip);
		bean.setPort(port);
		bean.setUserName(userName);
		bean.setPassWord(passWord);
		bean.setChannel(channel);
		
		return bean;
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new Thread() {
				@Override
				public void run() {
					HikView.getInstance().stopPlay();
				}
			}.start();
		}
		return super.onKeyDown(keyCode, event);
	}

//	public class ButtonListener implements OnTouchListener,OnClickListener{
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			switch (v.getId()){
//				case R.id.start:
//					startPlay();
//					break;
//				case R.id.stop:
//					HikView.getInstance().stopPlay();
//					break;
//				case R.id.set:
//					setPlayer();
//					break;
//				case R.id .bt_down:
//					break;
//				case R.id.bt_left:
//					break;
//				case R.id.bt_right:
//					break;
//				case R.id.bt_up:
//					break;
//			}
//		}
//
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			 TODO Auto-generated method stub
//			switch (v.getId()) {
//				case R.id .bt_down:
//					//myPTZControl(down);
//					break;
//				case R.id.bt_left:
//					//myPTZControl(left);
//					break;
//				case R.id.bt_right:
//					//myPTZControl(right);
//					break;
//				case R.id.bt_up:
//					//myPTZControl(up);
//					break;
////			}
//			return false;
//		}
//		
//	}
}
*/