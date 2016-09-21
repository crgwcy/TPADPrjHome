package com.tpad.activitys;

import java.io.UnsupportedEncodingException;

import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.WSClientService;
import com.tpad.pad.activitys.PadStandbyActivity;

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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private static String TAG="LoginActivity";
	private  EditText  userNameText;
	Button loginBtn;
	private boolean RUN_ON_PAD;
//	private boolean padCheckedFlag=false;
	public static String loginName;

/*
 * 	private static String  MEMBERS_INFO_URL="http://121.40.34.48:3000/streams";
	private static String  USER_INTENT_EXTRA="usrs_info";	
	public final static String RECI_COAST="com.tpad.act.LoginActivity";
    public static HashMap<String,String>  membersHashMap=new HashMap<String, String>();
*/
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);		
		RUN_ON_PAD=this.getResources().getBoolean(R.bool.APP_RUN_ON_PAD);		
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
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
/*		
		// Remove notification bar
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);	
*/	
		
		setContentView(R.layout.login_act);
		loginBtn=(Button)findViewById(R.id.login_btn_id);
		userNameText=(EditText)findViewById(R.id.login_name_id);
		loginBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
		//		String name=userNameText.getText().toString().getBytes("UTF-8");
				byte[] bytes=null;
				try {			
  					if(WSClientService.run_on_pad_flag)						
						bytes = (userNameText.getText().toString()+"_PAD").toString().getBytes("UTF-8");
					else
						bytes = userNameText.getText().toString().getBytes("UTF-8");		
			//		bytes = userNameText.getText().toString().getBytes("UTF-8");	
					
					if (bytes==null)
					{	
						MyLog.printf(TAG,".....name is null");
						bytes="my_test".getBytes();
					}			
			//		loginName=new String(bytes);
					WSClientService.setUsrName(new String(bytes));
			//      MyLog.printf(TAG,"login activity..............usr_name=%s",WSClientService.getUsrName());
			        
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				

								
				intent.putExtra(Intent.EXTRA_TEXT,userNameText.getText().toString());
				if(WSClientService.run_on_pad_flag==false)	
					intent.setClass(LoginActivity.this,MainMenuActivity.class);//(this, Butterfly.class);//(this, Butterfly.class);	    
				else
					intent.setClass(LoginActivity.this,PadStandbyActivity.class);//(this, Butterfly.class);//(this, Butterfly.class);	    					
				//startService(intent);			    
				startActivity(intent);//(this,Butterfly.class);				
				finish();
			    Intent intentService=new Intent(getApplicationContext(),WSClientService.class);			
				startService(intentService);
			}
		});
		
		RadioGroup graGroup=(RadioGroup)findViewById(R.id.login_radio_group_id);		
		graGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				//获取变更后的选中项的ID
			   int id=group.getCheckedRadioButtonId();
			   //根据ID获取RadioButton的实例
			   //RadioButton rb = (RadioButton)findViewById(id);		   
			   if (id==R.id.login_radio_btn_pad_id)
			   {
			//	   padCheckedFlag=true;   
				   WSClientService.run_on_pad_flag=true;
			//	   boolean val=getResources().getBoolean(R.bool.APP_RUN_ON_PAD);					   
			   }   
			   else if (id==R.id.login_radio_btn_mphone_id)   
			   {
			//	   padCheckedFlag=false;
				   WSClientService.run_on_pad_flag=false;
			   }   			   
			}
		});	
				
/*	   //  获取屏幕的大小和dpi相关信息。	
		DisplayMetrics metric=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width=metric.widthPixels;
		int height=metric.heightPixels;
		int density=metric.densityDpi;
		Toast.makeText(getApplicationContext(),"w:"+width+",h:"+height+",density:"+density,
				Toast.LENGTH_LONG).show();
*/		
		
/*    	//注册广播
    	IntentFilter filter=new IntentFilter(RECI_COAST);  	
    	BroadcastReceiver myrecive=new MyRecive();
    	
    	 *Register a BroadcastReceiver to be run in the main activity thread. 
    	 *The receiver will be called with any broadcast Intent that matches filter, 
    	 *in the main application thread. 
    	 * 
    	 
    	registerReceiver(myrecive, filter);	
    	new Thread(){
    		@Override  
            public void run() {  
                Intent intent = new Intent(RECI_COAST);  
                try {  
                    //获取服务器返回的信息  
                    String reslut = HttUtil  
                            .getRequest(MEMBERS_INFO_URL);  
                    intent.putExtra(USER_INTENT_EXTRA, reslut);  
                    //发送广播  
                    sendBroadcast(intent);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                } catch (ExecutionException e) {  
                    e.printStackTrace();  
                }  
            }   		
    	}.start();*/
    	
	}	
/*
 *     private class MyRecive  extends BroadcastReceiver
    {
    	public void onReceive(Context context,Intent intent)
    	{
    		try
    		{			
    			JSONObject jsonobject = new JSONObject(  
                        intent.getStringExtra(USER_INTENT_EXTRA)); 
    			
    			 Iterator<?> it=jsonobject.keys();
    			
    			 String a1="",a2="";
    			 while(it.hasNext())
    			 {
    				 a1=(String)it.next().toString();			 
    				 MyLog.printf(TAG,"............keys  str=%s", a1);
    				 JSONObject  myjsobject=jsonobject.getJSONObject(a1);
    				 String  getjsonName=myjsobject.optString("name");
    				 membersHashMap.put(a1,getjsonName);    				 
    				 if(getjsonName!=null)
    				 {	 
    					 MyLog.printf(TAG,"............keys  str=%s", getjsonName);		   				 
    				 }	 
    			 }	     			
    		}catch(JSONException e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
*/
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"...onRestart");		
		super.onRestart();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"...onStart");
		super.onStart();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"...onResume");		
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"...onPause");				
		super.onPause();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"...onStop");						
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"...onDestory");								
		super.onDestroy();
	}
}
