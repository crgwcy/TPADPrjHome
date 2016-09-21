package com.tpad.pad.activitys;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import jp.co.odelic.smt.remote03.act.module.WelcomeAct;

import org.json.JSONException;
import org.json.JSONObject;

import com.mywebrtc.japan.util.OutDoorCfg;
import com.mywebrtc.util.HttUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.activitys.CameraListActivity;

import com.tpad.activitys.MainMenuActivity;

import com.RTC.TpadRTC.R;
import com.RTC.TpadRTC.SettingsActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.OnFinished;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;

@SuppressLint("DefaultLocale")
public class PadStandbyActivity extends Activity /*implements OnTouchListener,OnGestureListener*/{

	/*private boolean RUN_ON_PAD=false;*/
	private static String TAG="PadStandbyActivity";
    private MyTimer mytimer;
    private TextView dateTextView=null;
    private TextView timeTextView=null;
    private  static final String AT_HOME="AT HOME";
    private  static final String OUT_HOME=" IS OUT";
    
    private SharedPreferences sp;
    private static final String USR_SHARED_PREFERENCE_FILE="my_usr_info";
    private static final String USR_NAME_KEY="my_usr_name_key";
    private static final String USR_LOGIN_TYPE_KEY="login_type_key";
    private static final String USR_LOGIN_PASSWORD_KEY="login_password_key";
    
    private static final String PHONE_TYPE="PHONE";
    private static final String PAD_TYPE="PAD";
	EditText nameEditText;

    private static boolean atHomeStatus=true;
    private  LinearLayout  homeBgView=null;
    private TextView statusTextView=null;
    private ImageView  homeImageView=null;
    private static PadStandbyActivity instance;
    public static PadStandbyActivity getInstance()
    {
    	return instance;
    }
    
/*    boolean lightFlag=false;
*/    
    private Handler myHandler=new Handler(){    
    	public void handleMessage(Message msg) {
    	switch (msg.what) 
    	{
			case 1:	
				if (dateTextView!=null)
				{
		//			MyLog.printf(TAG,"handleMessage msg.what=1");
					dateTextView.setText(myGetDateFormat());
					timeTextView.setText(myGetTimeFormat());
/*
 * 					lightFlag=lightFlag==false ? true:false;
					if(lightFlag)
					{	
						setScreenBrightness(getInstance(),100);
				//		getScreenBrightness(getInstance());
						getCurrentActivityBrightness(getInstance());
					}	
					else
					{	
						setScreenBrightness(getInstance(),1);
				//		getScreenBrightness(getInstance());
						getCurrentActivityBrightness(getInstance());
					}
*/
				}	
				break;
			default:
				break;
		}
    }};
    
    @SuppressLint("DefaultLocale")
	private  static String myGetDateFormat()
    {
    	Calendar calendar=Calendar.getInstance();
    	int year=calendar.get(Calendar.YEAR);
    	int month=calendar.get(Calendar.MONTH)+1;
    	int day=calendar.get(Calendar.DAY_OF_MONTH);
  	
    	return String.format("%04d/%02d/%02d", year,month,day);
    }
    
    @SuppressLint("DefaultLocale")
	private static String myGetTimeFormat()
    {
    	Calendar calendar=Calendar.getInstance();
    	int hour=calendar.get(Calendar.HOUR_OF_DAY);
    	int min=calendar.get(Calendar.MINUTE); 
    	return String.format("%02d:%02d",hour,min);
    }
    
    
    private void saveUserInfo(String s)
    {
    	sp.edit().putString(USR_NAME_KEY, s).commit();
    }
    
    private String getUsrInfo()
    {
    	String  loginName=sp.getString(USR_NAME_KEY, "guest");
    	if (loginName.equals(""))
    		loginName="Anonymous";
    	return loginName;
    }
    private void saveLoginType(String type)
    {
    	sp.edit().putString(USR_LOGIN_TYPE_KEY,type).commit();
    }
    private String getLoginType()
    {
    	String type=sp.getString(USR_LOGIN_TYPE_KEY,PHONE_TYPE);
    	return type;
    }
    private void saveUsrPassword(String type)
    {
    	sp.edit().putString(USR_LOGIN_PASSWORD_KEY,type).commit();
    }
    private String getUsrPassword()
    {
    	String type=sp.getString(USR_LOGIN_PASSWORD_KEY,"000000");
    	return type;
    }
    
    private void loginHandle(final String name)
    {
    	
/*    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (OutDoorCfg.IS_OUTDOOR_FLAG)
				{	
					try {
						Thread.sleep(10*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				String myLoginName;
				if(WSClientService.run_on_pad_flag)	
				{	
			    	if (OutDoorCfg.IS_OUTDOOR_FLAG)	
			    		myLoginName=name+"_DOOR";
			    	else	
			    		myLoginName=name+"_PAD";
				}	
				else
					myLoginName=name;
				WSClientService.setUsrName(myLoginName);
			    Intent intentService=new Intent(getApplicationContext(),WSClientService.class);			
				startService(intentService);
			}
		}).start();*/
    	


		String myLoginName;
		if(WSClientService.run_on_pad_flag)	
		{	
	    	if (OutDoorCfg.IS_OUTDOOR_FLAG)	
	    		myLoginName=name+"_DOOR";
	    	else	
	    		myLoginName=name+"_PAD";
		}	
		else
			myLoginName=name;
//		bytes = userNameText.getText().toString().getBytes("UTF-8");	
//		loginName=new String(bytes);
		WSClientService.setUsrName(myLoginName);
//      MyLog.printf(TAG,"login activity..............usr_name=%s",WSClientService.getUsrName());	  
	    Intent intentService=new Intent(getApplicationContext(),WSClientService.class);			
		startService(intentService);	
    }
        
	@Override
	protected void onCreate(Bundle savedInstanceState) {
/*
 * 		RUN_ON_PAD=WSClientService.run_on_pad_flag;//this.getResources().getBoolean(R.bool.APP_RUN_ON_PAD);		
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
		
/*		
 * 		int pid = android.os.Process.myPid();
		long threadid=Thread.currentThread().getId();
		String strthid=String.valueOf(threadid);
		MyLog.printf(TAG,"..........onCreate() pid=%d thred=%s",pid,strthid);		
*/
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.activity_pad_standby);		
		sp=getSharedPreferences(USR_SHARED_PREFERENCE_FILE, MODE_PRIVATE);
		
		if (sp!=null)
		{
			if (getLoginType().equals(PAD_TYPE))
			{	
				 WSClientService.run_on_pad_flag=true;
				 loginHandle(getUsrInfo());
			}
			else 
			{	
				 WSClientService.run_on_pad_flag=false;
				 loginHandle(getUsrInfo());
				 Intent intent=new Intent();
				 intent.setClass(this,MainMenuActivity.class);	    
				 startActivity(intent);
				 finish();
			//	 return ;
			}	 
		}	
	//	nameEditText=new EditText(this)
		
	
/*
 * 		mytimer=new MyTimer(myHandler);
		mytimer.timerDoSomethingRepeately();
*/		
/*		((Button)findViewById(R.id.alarm_test_id)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mySendBroadcast();
			}
		});
		*/
		 ((ImageButton)findViewById(R.id.usr_set_btn_id)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		//		usrSetDialog(PadStandbyActivity.this);
				usrLoginDialog(PadStandbyActivity.this);
			}
		});
		dateTextView=(TextView)findViewById(R.id.date_text_id);
		timeTextView=(TextView)findViewById(R.id.time_text_id);
		((LinearLayout)findViewById(R.id.community_btn_id)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//dateTextView=null;
				Intent intent=new Intent();
				intent.setClass(PadStandbyActivity.this,PadCommunityActivity.class);
				startActivity(intent);		
				//finish();	
			}
		});
		((LinearLayout)findViewById(R.id.camera_btn_id)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//dateTextView=null;
				Intent intent=new Intent();
				intent.setClass(PadStandbyActivity.this,CameraListActivity.class);
				startActivity(intent);		
				//finish();	
			}
		});
	((LinearLayout)findViewById(R.id.log_btn_id)).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
		//			 WSClientService.setNotifyMsgStr(MyRtcSip.SEND_RING_MSG);						
		//			 WSClientService.getInstance().mySendBroadcast();	
					Intent intent=new Intent();
					intent.setClass(PadStandbyActivity.this,SettingsActivity.class);
					startActivity(intent);
				}
			});
		
		
		
		((LinearLayout)findViewById(R.id.pad_light_btn_id)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
		// 		TODO Auto-generated method stub	
		//		mySendBroadcast();
					
				Intent intent=new Intent();
	//			intent.setClass(PadStandbyActivity.this,PadLightActivity.class);
				
		//		if(WelcomeAct.welcomeActInstanceFlag==false)
					intent.setClass(PadStandbyActivity.this,WelcomeAct.class);
/*				else
					intent.setClass(PadStandbyActivity.this,MainController.class);*/
		//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

	//	mySendBroadcast();	
		statusTextView=(TextView)findViewById(R.id.home_stus_text_id);
		homeBgView=(LinearLayout)findViewById(R.id.home_stus_view_bg_id);
		homeImageView=(ImageView)findViewById(R.id.home_image_id);
	//	homeBgView.setOnTouchListener(this);
		homeBgView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		//		mySendBroadcast();				
				boolean status=atHomeStatus==true?false:true;
				if (status)
				{
					atHomeStatus=true;		
					homeBgView.setBackgroundColor(getResources().getColor(R.color.main_menu_bg_color));				
					homeImageView.setImageResource(R.drawable.at_home_status_icon);
					statusTextView.setText(AT_HOME);
					statusTextView.setTextColor(Color.WHITE);
				}else
				{
					homeBgView.setBackgroundColor(getResources().getColor(R.color.out_home_color));
					atHomeStatus=false;	
					homeImageView.setImageResource(R.drawable.isout);
					statusTextView.setText(OUT_HOME);	
					statusTextView.setTextColor(Color.RED);
				}	
			}
		});
		if (atHomeStatus)
		{
			homeBgView.setBackgroundColor(getResources().getColor(R.color.main_menu_bg_color));	
			statusTextView.setText(AT_HOME);
			statusTextView.setTextColor(Color.WHITE);
			homeImageView.setImageResource(R.drawable.at_home_status_icon);
		}
		else
		{
			homeBgView.setBackgroundColor(getResources().getColor(R.color.out_home_color));	
			statusTextView.setText(OUT_HOME);
			statusTextView.setTextColor(Color.RED);
			homeImageView.setImageResource(R.drawable.isout);
		}
	}
	
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onRestart()");		
		super.onRestart();
	}	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onStart()");						
		super.onStart();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onResume()");	
		instance=this;

		mytimer=new MyTimer(myHandler);
		
		mytimer.timerDoSomethingRepeately();	
		registerReceiverHandle();

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onPause()");
		instance=null;
		mytimer.timerCancel();		
		if (membersInfoRecive!=null)
		{	
			unregisterReceiver(membersInfoRecive);
			membersInfoRecive=null;
		}
		super.onPause();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onStop()");								
		super.onStop();
	}	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onDestroy()");		
		super.onDestroy();
	}
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pad_standby, menu);
		return true;
	}*/
	@Override	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		MyLog.printf(TAG,"@@@@@@@@@@@@@@@@@@@@@@@onKeyDown=%d  ,event.getRepeatCount()=%d",keyCode,event.getRepeatCount());
		if (OutDoorCfg.IS_OUTDOOR_FLAG)
		{
			if (keyCode==OutDoorCfg.EMG_KEYCODE&&event.getRepeatCount()==0)
			{
				 WSClientService.setNotifyMsgStr("offline:"+/*"-visitor-"*/WSClientService.getUsrName());
				 WSClientService.getInstance().mySendBroadcast();
						   
			      WSClientService.run_on_pad_flag=false;
				   finish();			
				   WSClientService.getInstance().systemExit();
				   return false;
			}
			else if (keyCode==OutDoorCfg.TALK_KEYCODE&&event.getRepeatCount()==0)
			{
				 WSClientService.setNotifyMsgStr(MyRtcSip.SEND_RING_MSG);
				
				 WSClientService.getInstance().mySendBroadcast();								
			}	
			
			
		}	
		
		if (keyCode == KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)  
		{
/*		   Intent  intent=new Intent();
		   intent.setClass(this,MainMenuActivity.class);
		   startActivity(intent);
*/
//		   finish();
//		   System.runFinalizersOnExit(true);
		   MyLog.printf(TAG,"...................%s","onKeyDown");		   		   		   		   
		   WSClientService.setNotifyMsgStr("offline:"+/*"-visitor-"*/WSClientService.getUsrName());
	//	   MyLog.printf(TAG,"...................%s","onKeyDown000");
		   WSClientService.getInstance().mySendBroadcast();
	//	   MyLog.printf(TAG,"...................%s","onKeyDown111");
		   
		   WSClientService.run_on_pad_flag=false;


	//	   MyLog.printf(TAG,"...................%s","onKeyDown222");
		   finish();
		/*	  try 
			  {
				Thread.sleep(100);
			  } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }*/
/*		   try {
				Thread.sleep(200);
			  } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  } 
*/
	/*		 new Thread(new Runnable() {			
					@Override
					public void run() {
						// TODO Auto-generated method stub
			//			SystemClock.sleep(1000);
						String msg="offline:"+ " new user"WSClientService.usrName;
					    WSClientService.setNotifyMsgStr(msg);					    
					    WSClientService.getInstance().mySendBroadcast();
					}
				}).start();*/
  	//	   WSClientService.getInstance().client.disconnect();
	
		 WSClientService.getInstance().systemExit();
	//   System.exit(0);		
		}	
		return false/*true*/;
	};
	
	/*setScreenLight  int light bewteen 0-100 **/
/*	void setScreenBrightness(Activity activity,int light)
	{
		WindowManager.LayoutParams layoutParams=activity.getWindow().getAttributes();
		layoutParams.screenBrightness=light/100.0f;
		this.getWindow().setAttributes(layoutParams);
	}
	
    private float getCurrentActivityBrightness(Activity act)
    {
    	float  screenBrightNess=act.getWindow().getAttributes().screenBrightness;
    	Log.e(TAG,"GET CURRENT ACTIVITY BRIGHT NESS ="+screenBrightNess);
    	return screenBrightNess;
    }
	
	int getScreenBrightness(Activity activity)
	{
		 int nowBrightnessValue = 0;  
		 ContentResolver resolver = activity.getContentResolver();
		 try {
			nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 MyLog.printf(TAG,"getScreenBrightness-----------------=%d",nowBrightnessValue);
		 return nowBrightnessValue;
	}*/
	
	public static long counter=0;
	public static Timer timer=null;		
	
	
	
	class MyTimer 
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
		//			MyLog.printf(TAG,"timer1......................%d",counter);
					if (timeHandler!=null)
					{
						Message msg=new Message();
						msg.what=1;
						timeHandler.sendMessage(msg);
					}	
				}
			}, 0,1000*5);// 1000ms*60
		}
		public void timerCancel()
		{
			timer.cancel();
			timer=null;
			counter=0;
		}
	}	
	
	
	
	private static String  USER_INTENT_EXTRA="usrs_info";		  
	private final static String RECI_COAST="com.tpad.act.GetDataUtil";
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
	            try {  
	                //获取服务器返回的信息  
	                String reslut = HttUtil.getRequest(mGetDataAddress);  
	                if (reslut==null) return ;
		            Intent intent = new Intent(RECI_COAST);  
	                intent.putExtra(USER_INTENT_EXTRA,reslut);  
	                MyLog.printf(TAG,"reslut=%s",reslut);
	                //发送广播  
	                sendBroadcast(intent);  
	            } 
	            catch (InterruptedException e) 
	            {  
	                e.printStackTrace();  
	            }
	            catch (ExecutionException e) 
	            {
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
		    			 }	
*/ 
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
 
  /*
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"onFling...........");
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"onTouch...........");
		return false;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		super.onTouchEvent(event);
		MyLog.printf(TAG,"onTouchEvent...........");
		return false;
	}*/
  
  
  /*
   * Function    :    构造函数
   * Param       :    context上下文对象
   * Author      :    博客园-依旧淡然
   */
  EditText mUserName,mPassword;
  AlertDialog longinDialog;
  RadioGroup  radioGroup;
  int loginTypeFlag=1;

   public void usrLoginDialog (Context context) {
                      
       //动态加载布局生成View对象
       LayoutInflater layoutInflater = LayoutInflater.from(context);
       View longinDialogView = layoutInflater.inflate(R.layout.login_dialog, null);
           
       //获取布局中的控件
       mUserName = (EditText)longinDialogView.findViewById(R.id.edit_username);
       mPassword = (EditText)longinDialogView.findViewById(R.id.edit_password);
       radioGroup=(RadioGroup)longinDialogView.findViewById(R.id.my_login_radio_group);    
       radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			int id=group.getCheckedRadioButtonId();
			
			if(R.id.login_pad_type_id==id)
			{
				loginTypeFlag=0;
			}
			else if (R.id.login_phone_type_id==id)
			{
				loginTypeFlag=1;
			}	
		}		
	});
       if(getLoginType().equals(PAD_TYPE))
       {   
			loginTypeFlag=0;
    	   radioGroup.check(R.id.login_pad_type_id);    	   
       }   
       else
       {   
			loginTypeFlag=1;
    	   radioGroup.check(R.id.login_phone_type_id);
       }   
       
       mUserName.setText(getUsrInfo());
       mPassword.setText(getUsrPassword());
       //创建一个AlertDialog对话框
       longinDialog = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_DARK)
           .setTitle("Login Info")
           .setView(longinDialogView)                //加载自定义的对话框式样
           .setPositiveButton("O K", new DialogInterface.OnClickListener() {			
   			@Override
   			public void onClick(DialogInterface dialog, int which) {
   				// TODO Auto-generated method stub
   					saveUserInfo(mUserName.getText().toString());		
   					saveUsrPassword(mPassword.getText().toString());
   					if(loginTypeFlag==0)
   						saveLoginType(PAD_TYPE);
   					else
   						saveLoginType(PHONE_TYPE);
   				}
   			})
           .setNeutralButton("CANCEL", null)
           .create();  
           
       longinDialog.show();
   }
  

/*  	private void usrSetDialog(Context c)
  	{
  		nameEditText.setText(getUsrInfo());
  		
  		AlertDialog.Builder builder=  new AlertDialog.Builder(c,AlertDialog.THEME_HOLO_DARK)
		.setMessage("USR NAME")
		.setPositiveButton("O K",new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
					saveUserInfo(nameEditText.getText().toString());					
				}
			}
		).setNegativeButton("CANCEL",null)
		.setView(nameEditText);
  		builder.show();
  	}
 */
}
