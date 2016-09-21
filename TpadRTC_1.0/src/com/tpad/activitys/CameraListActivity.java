package com.tpad.activitys;

import java.io.ObjectOutputStream.PutField;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mywebrtc.util.CepsaIPCameraUtil;
import com.mywebrtc.util.HttUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.activitys.CommunityActivity.callIpCameraTestThread;
import com.tpad.pad.activitys.HikCameraActivity;
//import com.tpad.hik.MainActivity;
import com.tpad.pad.activitys.PadStandbyActivity;

import com.RTC.TpadRTC.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.KeyEvent;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CameraListActivity extends Activity {

	public static CameraListActivity  cameraListActivity=null;
	private  final int  UPDATE_UI_FLAG=2;
	private  static String IP_CAMERA_NAME="IP CAMERA";
	private  static String OUTDOOR_CAMERA_NAME="OUTDOOR CAMERA";
	
	
	public static int hik_camera_index=-1;
	public static int getHikCameraIndex()
	{
		return hik_camera_index;
	}
	
	public static void setHikCameraIndex(int index)
	{
		 hik_camera_index=index;
	}	
	
	
	public static CameraListActivity getInstance()
	{
		return cameraListActivity;
	}
	
	@SuppressLint("HandlerLeak")
	private  Handler   myhandler=new Handler()
	{
		  public void handleMessage(Message msg) {   
              switch (msg.what) {   
                   case UPDATE_UI_FLAG:   
                	    MyLog.printf(TAG,".........msg.arg1=%d handle message str=%s",msg.arg1,msg.obj.toString());
                	    if (msg.arg1>0)
                	    {
                	    	if(msg.arg2==1)  // ipcamera exit!!!!!!!!!
                	    		cameraListGalleryViewShow((msg.arg1+2)/*(msg.arg1+1)*/,true);
                	    	else
                    	    	cameraListGalleryViewShow(msg.arg1,false);
                	    }
                        break;   
              }   
              super.handleMessage(msg);   
         }   
	};
		
	private  boolean RUN_ON_PAD;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		
		RUN_ON_PAD=WSClientService.run_on_pad_flag;//this.getResources().getBoolean(R.bool.APP_RUN_ON_PAD);		
		if (RUN_ON_PAD)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);	  	 	     
		}
		else
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
		}			
		MyLog.printf(TAG,"..........onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_list);
//		registerReceiverHandle();
				
		cameraListViewInit();
		cameraListGalleryViewShow(5,false);
		cameraListActivity=this;		
				
//		mySendBroadcast();	
		
	}
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.community, menu);
		return true;
	}
*/
	/************************communityViewInit****************************************/
	   //  community view !!! 
    private static  String TAG="CameraListActivity";	
/*   private View communityView;
*/  private ImageButton  cameraListRetBtn;
    private Gallery cameraListGallery;
    private ViewGroup cameraListDotViewGrop;
    private TextView cameraListGalleryLabel;
	private  int cameraList_online_total_num=0;
	private ImageView[] cameraListDots;	
	private  String[]   cameraListAccountNames =new String[20];//new String[]{};
	private  String[]   cameraListAccountIds = new  String[20];	
	
	private  String   cameraListPadId="test";

	
	private Integer myCameraListImageIconID=R.drawable.online_camera_icon;
	private Integer hikCameraImageIconID=R.drawable.online_hik_camera_icon;

	private void  cameraListViewInit()
	{
		cameraListRetBtn=(ImageButton)findViewById(R.id.camera_list_ret_btn_id);
    	cameraListGallery=(Gallery) findViewById(R.id.camera_list_gallery_id);  	
    	cameraListDotViewGrop=(ViewGroup)findViewById(R.id.cameraListDomsViewGroup_id);
    	cameraListGalleryLabel=(TextView)findViewById(R.id.camera_list_gallery_label_id);    	  	
    	cameraListRetBtn.setOnClickListener(btnListener);   	
   // 	communityHumanIcon.setOnClickListener(btnListener);
  //  	communityView.setVisibility(View.INVISIBLE);
    	
    	for(int i=0;i<cameraList_online_total_num;i++)
    	{	
    		cameraListAccountIds[i]="test";   
    		cameraListAccountNames[i]="name";
    	}   	
    	
	}	
	@SuppressWarnings("deprecation")
	public void  cameraListGalleryViewShow(int online_total_num,boolean ip_camera)
	{
		
		String nameTmp;
		cameraList_online_total_num=online_total_num;
		for (int i=0;i<online_total_num;i++)	
		{	
			cameraListAccountIds[i]="test";
			cameraListAccountNames[i]="name";
		}
		if (cameraList_online_total_num==0) return ;
		if(ip_camera)
		{	
			cameraListAccountNames[(online_total_num-2)]=IP_CAMERA_NAME;
			cameraListAccountNames[(online_total_num-1)]=OUTDOOR_CAMERA_NAME;
		}	
		if (membersInfoRecive!=null&&membersInfoRecive.getMembersHashMap()!=null&&(!(membersInfoRecive.getMembersHashMap().isEmpty())))
		{			
			Set<String> keySet=membersInfoRecive.getMembersHashMap().keySet();
			Iterator<String> it=keySet.iterator();			
			while (it.hasNext())
			{
				String id=(String)it.next();
				for (int i=0;i<cameraList_online_total_num;i++)
				{
/*					if (cameraListAccountIds[i].equals("test"))
					{
						cameraListAccountIds[i]=id;	
			      		cameraListAccountNames[i]=membersInfoRecive.getMembersHashMap().get(id);
			      		if (cameraListAccountNames[i].contains("TPADPAD"))
						cameraListAccountIds[(cameraList_online_total_num-1)]=cameraListAccountNames[i];
		//				MyLog.printf(TAG,".........ViewShow  communityAccountIds[%d]=%s  communityAccountNames[%d]=%s",i,communityAccountIds[i],i,communityAccountNames[i]);						
						break;
					}	
*/								
					if (cameraListAccountIds[i].equals("test"))
					{
						cameraListAccountIds[i]=id;	
						cameraListAccountNames[i]=membersInfoRecive.getMembersHashMap().get(id);
		//				MyLog.printf(TAG,".........ViewShow  communityAccountIds[%d]=%s  communityAccountNames[%d]=%s",i,communityAccountIds[i],i,communityAccountNames[i]);						
			      		if (cameraListAccountNames[i].contains("_PAD"))
			      		{	
				    		MyLog.printf(TAG,"camera list GalleryViewShow222");
				    		cameraListAccountNames[i]=cameraListAccountNames[i].substring(0, cameraListAccountNames[i].length()-4);
				    		cameraListPadId=cameraListAccountIds[i];
			      		}		      		
						break;
					}
				}	
			}
		}
	   	if (online_total_num>0)  	
			 cameraListDots = new ImageView[online_total_num/*communityImageIDs.length*/];
	   	else 
	   		return ;	   	
	   	cameraListDotViewGrop.removeAllViews();//  bruce add 0128	   	
		for(int i=0; i<online_total_num/*communityDots.length*/; i++)
		{
			ImageView imageView = new ImageView(this);
	    //	imageView.setLayoutParams(new LayoutParams(20,20));
		//	imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		//	imageView.setPadding(0, 0,50, 0);
	    	cameraListDots[i] = imageView;
	    	if(i == 0){
	    		cameraListDots[i].setBackgroundResource(R.drawable.page_indicator_focused2);
	    	}else{
	    		cameraListDots[i].setBackgroundResource(R.drawable.page_indicator_unfocused2);
	    	}	   
	    	cameraListDotViewGrop.addView(imageView);
		}   	   	  		    			
	//	communityGallery.setSpacing(60);		
		cameraListGallery.setAdapter(new ImageAdapter(this));
		
		cameraListGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// TODO Auto-generated method stub
				MyLog.printf(TAG,"onItemClick............postion=%d",position);	
				  if(MyRtcSip.onIceConnected==true)
					Toast.makeText(getApplicationContext(),"Wait a moment",Toast.LENGTH_SHORT).show();
				
					if (MyRtcSip.onIceConnected==false)
					{		
						JSONObject myJsonobj=new JSONObject();						
						if (id!=-1)
						{
/*
	  						if (id!=-1)
							{
								Intent intent=new Intent();
						//		intent.setClass(CameraListActivity.this,LocalIpCameraActivity.class);
								intent.setClass(CameraListActivity.this,HikCameraActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
								return ;
							}
*/
							
							if(cameraListAccountNames[position].equals(IP_CAMERA_NAME))
							{
								if(RUN_ON_PAD==false)
								{	
									if (!cameraListPadId.equals("test"))
									{	
										try 
										{
										//	setHikCameraIndex(0);
											WSClientService.getInstance().client.sendMessage(cameraListPadId,MyRtcSip.DATA_CHANNEL_ONLY+"camera0",null);
										}
										catch (JSONException e) 
										{
											// TODO Auto-generated catch block
											e.printStackTrace();
										}										
										if (!cameraListAccountNames[position].equals(new String(WSClientService.getUsrName())/*WSClientService.getUsrName()*/))	
										{	
											MyLog.printf(TAG,"..............USRNAME=%s",WSClientService.getUsrName());					
											//			WSClientService.getInstance().startIPCameraActivity(cameraListPadId,null/*MyRtcSip.REMOTE_RTC_ACT_INIT_ASK*/);
											// use new Hik CAMERA
											WSClientService.getInstance().startHikCameraActivity(cameraListPadId,null/*MyRtcSip.REMOTE_RTC_ACT_INIT_ASK*/);
										}										
								//		new callIpCameraTestThread(cameraListPadId).start();
									}
								}	
								else
								{
									setHikCameraIndex(0);
									MyLog.printf(TAG,"CAMERA000000000000000000:INDEX=%d",getHikCameraIndex());
									Intent intent=new Intent();
							//		intent.setClass(CameraListActivity.this,LocalIpCameraActivity.class);
									intent.setClass(CameraListActivity.this,HikCameraActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							}
							else if (cameraListAccountNames[position].equals(OUTDOOR_CAMERA_NAME))
							{
								if(RUN_ON_PAD==false)
								{	
									if (!cameraListPadId.equals("test"))
									{	
										try 
										{
									//		setHikCameraIndex(1);
											WSClientService.getInstance().client.sendMessage(cameraListPadId,MyRtcSip.DATA_CHANNEL_ONLY+"camera1",null);
										}
										catch (JSONException e) 
										{
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										if (!cameraListAccountNames[position].equals(new String(WSClientService.getUsrName())/*WSClientService.getUsrName()*/))	
										{	
											MyLog.printf(TAG,"..............USRNAME=%s",WSClientService.getUsrName());					
											//WSClientService.getInstance().startIPCameraActivity(cameraListPadId,null/*MyRtcSip.REMOTE_RTC_ACT_INIT_ASK*/);
											// use new Hik CAMERA
											WSClientService.getInstance().startHikCameraActivity(cameraListPadId,null/*MyRtcSip.REMOTE_RTC_ACT_INIT_ASK*/);
										}										
								//		new callIpCameraTestThread(cameraListPadId).start();
									}
								}	
								else
								{
									setHikCameraIndex(1);
									MyLog.printf(TAG,"CAMERA1111111111:INDEX=%d",getHikCameraIndex());
									Intent intent=new Intent();
							//		intent.setClass(CameraListActivity.this,LocalIpCameraActivity.class);
									intent.setClass(CameraListActivity.this,HikCameraActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}								
							}	
							else
							{	
								try 
								{
									WSClientService.getInstance().client.sendMessage(cameraListAccountIds[position],MyRtcSip.RING_ANSWER_MSG,null);									
/*
 									for (int i=0;i<4;i++)
									{	
										if (cameraListAccountNames[i].equals("crg"))
											WSClientService.getInstance().client.sendMessage(cameraListAccountIds[i],MyRtcSip.RING_ANSWER_MSG,null);
									}
*/
								} 
								catch (JSONException e) 
								{
								// 	TODO Auto-generated catch block
									e.printStackTrace();
								}	
				   		   		//	if (WSClientService.getUsrName()!=null)	
								{	
									WSClientService.getInstance().startMyRtcActivity(cameraListAccountIds[position],null);
						//			WSClientService.getInstance().startCallActivity(communityAccountIds[position],communityAccountNames[position]);
								}
							}	
							finish();
							return ;
						}	
					}
			}
		});		
		return ;
	}	
	
	
	public class ImageAdapter extends BaseAdapter
	{
		Context context;
		int itemBackground;
		
		public ImageAdapter(Context c) {
			context =c;
			// setting the style
		}
		// return the num of images
		@Override
		public int getCount() {
			// TODO Auto-generated method stub			
			return cameraList_online_total_num/*communityImageIDs.length*/;//0;
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
	//		MyLog.printf(TAG,"GALLERY   getItem.............postion=%d",position);
			return position;//null;
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
	//		MyLog.printf(TAG,"GALLERY  getItemId.............postion=%d",position);	
			for(int i=0; i<cameraList_online_total_num/*communityDots.length*/; i++)
			{
				if(i == position)
				{
					cameraListDots[i].setBackgroundResource(R.drawable.page_indicator_focused2);
				}
				else
				{
					cameraListDots[i].setBackgroundResource(R.drawable.page_indicator_unfocused2);
				}
			}
			
			if (cameraListAccountIds[position]!=null)
			{
				MyLog.printf(TAG,"communityAccountNames[%d]:%s",position,cameraListAccountNames[position]);
		//		communityGalleryLabel.setText(("online human name:"+position));
				cameraListGalleryLabel.setText(cameraListAccountNames[position]);
			}
			else
			{
				MyLog.printf(TAG,"communityAccountNames[position]==null");
			}				
			return position;//0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView imageView;
			if (convertView==null)
			{
				imageView=new ImageView(context);	
				int hik_camera_position=cameraList_online_total_num-1;
				int shiwai_hik_camera_position=cameraList_online_total_num-2;
				if (position==hik_camera_position)
					imageView.setImageResource(hikCameraImageIconID);	
				else if (position==shiwai_hik_camera_position)
					imageView.setImageResource(hikCameraImageIconID);	
				else	
					imageView.setImageResource(myCameraListImageIconID/*communityImageIDs[position]*/);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		//		imageView.setLayoutParams(new Gallery.LayoutParams(450,360));
			}	
			else
			{
				imageView=(ImageView) convertView;
			}
			return imageView;
		}
		
	}	
	
	private  OnClickListener btnListener= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId()==R.id.camera_list_ret_btn_id)
			{
				   Intent  intent=new Intent();
				   if (RUN_ON_PAD)
					   intent.setClass(CameraListActivity.this,PadStandbyActivity.class);		   
				   else	   
					   intent.setClass(CameraListActivity.this,MainMenuActivity.class);
		   		   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);		   
				   startActivity(intent);
				   finish();					
			}	
			
		}
	};
	
	
	  
private static String  USER_INTENT_EXTRA="usrs_info";	
private static String  USER_IP_CAMERA_EXTRA="usrs_ip_camera_info";		  

public final static String RECI_COAST="com.tpad.act.GetDataUtil";
private String mGetDataAddress;
private  MembersInfoBroadcastRecive membersInfoRecive=null;
			
	public  void registerReceiverHandle()
	{
		IntentFilter filter=new IntentFilter(RECI_COAST);  	
		membersInfoRecive=null;
		membersInfoRecive=new MembersInfoBroadcastRecive();
	
		registerReceiver(membersInfoRecive, filter);	
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
/*				boolean ip_camera_online=false;
				if (RUN_ON_PAD)				
					ip_camera_online= new CepsaIPCameraUtil(true).testIpCameraOnline();
*/

	            Intent intent = new Intent(RECI_COAST);  
	            try {  
	                //获取服务器返回的信息  
	                String reslut = HttUtil.getRequest(mGetDataAddress);  
	                intent.putExtra(USER_INTENT_EXTRA, reslut);  
	                intent.putExtra(USER_IP_CAMERA_EXTRA, true/*ip_camera_online*/);
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
  		boolean ipCameraFlag=false;
  		try
  		{			
  			JSONObject jsonobject = new JSONObject(  
                      intent.getStringExtra(USER_INTENT_EXTRA)); 
  			
  			ipCameraFlag=intent.getBooleanExtra(USER_IP_CAMERA_EXTRA, false);
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
	    				 }	 
	    			 }	 
	    			 if (membersHashMap.size()>0)
	    			 {
	    				 Message msg=new Message();
	    				 msg.what=UPDATE_UI_FLAG;
	    				 if(ipCameraFlag==true)
	    					 msg.arg2=1;    				 
	    				 msg.arg1=membersHashMap.size();
	    				 msg.obj=(Object)membersHashMap;
	    				 if (myhandler!=null)
	    					 myhandler.sendMessage(msg);
	    			 }	 
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
	
	 class callIpCameraTestThread  extends  Thread
	{
		 private String id;
		 public callIpCameraTestThread(String id) {
			// TODO Auto-generated constructor stub
			 this.id=id;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
		// TODO Auto-generated method stub
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					}
//							WSClientService.getInstance().client.sendMessage(communityAccountIds[0],"data_test",null);
					WSClientService.getInstance().client.myCallTest(this.id);  		
		}
	}		
	
	@Override	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK )  
		{
			
		   Intent  intent=new Intent();
		   if (RUN_ON_PAD)
			   intent.setClass(this,PadStandbyActivity.class);		   
		   else	   
			   intent.setClass(this,MainMenuActivity.class);
   			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);		   
		   startActivity(intent);
		   finish();		   
		}	
		return true;
	};
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
		registerReceiverHandle();
		mySendBroadcast();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onPause()");
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
		myhandler=null;
		cameraListActivity=null;		
		super.onDestroy();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onConfigurationChanged()");		
		super.onConfigurationChanged(newConfig);
	}
}
