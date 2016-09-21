package com.tpad.activitys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mywebrtc.util.HttUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.WSClientService;
import com.tpad.pad.activitys.PadStandbyActivity;
//import com.tpad.activitys.MenuActivity.CustomListViewAdapter.ViewHolder;

import com.RTC.TpadRTC.R;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainMenuActivity extends Activity {
	
	  private static final String TAG="MainMenuActivity";
	  private boolean RUN_ON_PAD;
//	  private View  mainView=null;
//	  private TextView  timerTextView;
	  private ListView  menuList;
	  private List<MenuListItem> menuListItems;
	  private SharedPreferences sp;
	  private static final String USR_SHARED_PREFERENCE_FILE="my_usr_info";
	  private static final String USR_NAME_KEY="my_usr_name_key";
	  private static final String USR_LOGIN_TYPE_KEY="login_type_key";
	  private static final String USR_LOGIN_PASSWORD_KEY="login_password_key";

	  private static final String PHONE_TYPE="PHONE";
	  private static final String PAD_TYPE="PAD";

	  private static final String[] titles =
			{ "", "", "", "","","" }; 
      private static final Integer[]  images={ 
			  R.drawable.light_icon,
			  R.drawable.human_icon,
			  R.drawable.camera_icon,
			  R.drawable.log_icon,
			  R.drawable.door_icon,
			  R.drawable.air_icon};
      private static final String[] descriptions =
     { "LIGHT", "COMMUNITY","CAMERA", "LOG","HA","AIR-CONDITION" }; 
      
      private String userName;
      private  static MainMenuActivity instance;
      public static MainMenuActivity getInstance()
      {
    	  return instance;
      }
      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
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
		else
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}	
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		standbyItemsViewShow();
		
		sp=getSharedPreferences(USR_SHARED_PREFERENCE_FILE, MODE_PRIVATE);	
		ImageView setImageView=null;
		setImageView=(ImageView)findViewById(R.id.phone_set_btn_id);
		setImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				usrLoginDialog(MainMenuActivity.this);			
			}
		});
/*		Intent  intent=this.getIntent();
	    if (intent!=null)
	    {	
	    	userName=intent.getStringExtra(Intent.EXTRA_TEXT);
	    	if (userName!=null)
	    	{	
	    		MyLog.printf(TAG,"..........user name=%s",userName);
	    	}	
	    	else
	    	{	
	    		MyLog.printf(TAG,"..........user name  is null");
	    	//	showToast("usr name is null");
	    	}	
	    }*/
	    MyLog.printf(TAG,"............onCreate");
	}

	  
		@Override
		protected void onRestart() {
			// TODO Auto-generated method stub
			 MyLog.printf(TAG,"............onRestart");		  
			super.onRestart();
		}
	  @Override
	  public void onResume() {
		 MyLog.printf(TAG,"............onResume");		  
	    super.onResume();
	    instance=this;
	  } 
	  @Override
	  public void onPause() {
		 MyLog.printf(TAG,"............onPause");		  
	    super.onPause();
	    instance=null;
	  }
	  @Override  
	  public void onStop(){
		  MyLog.printf(TAG,"............onStop");
		  super.onStop();
		//  System.exit(0);
	  }	  
	  @Override  
	  public void onDestroy(){
		  MyLog.printf(TAG,"............onDestroy");
		  super.onDestroy();
		//  System.exit(0);
	  }	  
	  
	  
	  /*****************************standby view******************************************/  
		public void  standbyItemsViewShow()
		{
			MyLog.printf(TAG,".....................start standbyItemsView");	
/*
 * 			mainView=LayoutInflater.from(this).inflate(R.layout.activity_main_menu,null);
			this.addContentView(mainView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
*/
	//		timerTextView=(TextView) findViewById(R.id.time_textview_id);
			menuList=(ListView)findViewById(R.id.menu_list_view);
//			menuData=new ArrayList<HashMap<String,Object>>();
			menuListItems=new ArrayList<MenuListItem>();
			for (int i = 0; i < titles.length; i++) {
				MenuListItem item = new MenuListItem(images[i], titles[i], descriptions[i]);
				menuListItems.add(item);
			}		
					
			CustomListViewAdapter  adapter=new CustomListViewAdapter(this, R.layout.main_menu_list_item, menuListItems);
			menuList.setAdapter(adapter);
			menuList.setOnItemClickListener(new OnItemClickListener() 
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					if(id ==0)
					{	 
						Intent intent=new Intent();
						intent.setClass(MainMenuActivity.this,LightActivity.class);
						startActivity(intent);	
			//			finish();
					}
					else if (id==1)
					{
						Intent intent=new Intent();
						intent.setClass(MainMenuActivity.this,CommunityActivity.class);
						startActivity(intent);		
				//		finish();	
					}
					else if (id==2)
					{
						Intent intent=new Intent();
						intent.setClass(MainMenuActivity.this,CameraListActivity.class);
						startActivity(intent);	
					}	
					
				}
			});
		}
		
		class MenuListItem{
			private  int imageId;
			private  String tittle;
			private  String  desc;
			public  MenuListItem(int imageId,String tittle,String desc)
			{
				this.imageId=imageId;
				this.tittle=tittle;
				this.desc=desc;
			}
			public int getImageId() {
				return imageId;
			}
			public String  getTittle(){
				return tittle;
			}
			public String  getDesc(){
				return desc;
			}
		}
		class CustomListViewAdapter extends ArrayAdapter<MenuListItem> {

			Context context;

			public CustomListViewAdapter(Context context, int resourceId,
					List<MenuListItem> items) {
				super(context, resourceId, items);
				this.context = context;
			}
			
			/*private view holder class*/
			private class ViewHolder {
				ImageView imageView;
				TextView txtTitle;
				TextView txtDesc;
			}
			
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				MenuListItem menuListItem = getItem(position);
				
				LayoutInflater mInflater = (LayoutInflater) context
						.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.main_menu_list_item, null);
					holder = new ViewHolder();
					holder.txtDesc = (TextView) convertView.findViewById(R.id.menu_list_item_title1);
					holder.txtTitle = (TextView) convertView.findViewById(R.id.menu_list_item_title);
					holder.imageView = (ImageView) convertView.findViewById(R.id.menu_icon);
					convertView.setTag(holder);
				} else 
					holder = (ViewHolder) convertView.getTag();
						
				holder.txtDesc.setText(menuListItem.getDesc());
				holder.txtTitle.setText(menuListItem.getTittle());
				holder.imageView.setImageResource(menuListItem.getImageId());		
				return convertView;		
			}
		}	
	
	private  void  showToast(final String str)
	{
		Toast toast=Toast.makeText(this,str,Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0,0);
		toast.show();
	}
	
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
*/
	@Override	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK )  
		{
//		   finish();
		   MyLog.printf(TAG,"...................%s","onKeyDown");
	       WSClientService.setNotifyMsgStr("offline:"+/*" -visitor-"*/WSClientService.getUsrName());
				//	   MyLog.printf(TAG,"...................%s","onKeyDown000");
		   WSClientService.getInstance().mySendBroadcast();
		   WSClientService.run_on_pad_flag=false;
		   finish();
//		   System.exit(0);
		  WSClientService.getInstance().systemExit();

		}	
		return true;
	};
	
	
	
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
	   					if (loginTypeFlag==0)
	   						saveLoginType(PAD_TYPE);
	   					else
	   						saveLoginType(PHONE_TYPE);
	   				}
	   			})
	           .setNeutralButton("CANCEL", null)
	           .create();  
	           
	       longinDialog.show();
	   }
}
