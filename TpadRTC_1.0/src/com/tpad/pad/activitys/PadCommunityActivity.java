package com.tpad.pad.activitys;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mywebrtc.util.HttUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;

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
import android.view.KeyEvent;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PadCommunityActivity extends Activity {
	
	private  final int  UPDATE_UI_FLAG=2;	
	@SuppressLint("HandlerLeak")
	private  Handler   myhandler=new Handler()
	{
		  public void handleMessage(Message msg) 
		  {   
              switch (msg.what) {   
                   case UPDATE_UI_FLAG:   
             //   	    MyLog.printf(TAG,".........msg.arg1=%d handle message str=%s",msg.arg1,msg.obj.toString());
            	    if (msg.arg1>0)
            	      communityGalleryViewShow(msg.arg1);               	    
                    break;   
              }   
              super.handleMessage(msg);   
         }   
	};
		
	public static PadCommunityActivity   padCommunityActivity=null;
	public static PadCommunityActivity getInstance()
	{
		return padCommunityActivity;
	}
	private  boolean RUN_ON_PAD;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
/*   //just delete it  at 20150506		
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
		}
*/	
/*		getWindow().setFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
*/		
		MyLog.printf(TAG,"..........onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pad_community);
//		registerReceiverHandle();					
		communityViewInit();
		communityGalleryViewShow(5);	
//		mySendBroadcast();	
	}
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.community, menu);
		return true;
	}*/
	/************************communityViewInit****************************************/
	   //  community view !!! 
    private static  String TAG="PadCommunityActivity";	
/*   private View communityView;
*/  private ImageButton  communityRetBtn;
    private Gallery communityGallery;
    private ViewGroup communityDotViewGrop;
    private TextView communityGalleryLabel;
	private  int community_online_total_num=0;
	private ImageView[] communityDots;	
	private  String[]   communityAccountNames=new String[20];//new String[]{};
	private  String[]   communityAccountIds= new  String[20];
	

	
	private Integer myCommunityImageIconID=R.drawable.human_online_icon;/*R.drawable.community_human_online_icon;*/

	private void  communityViewInit()
	{

    	communityRetBtn=(ImageButton)findViewById(R.id.community_ret_id);
    	communityGallery=(Gallery) findViewById(R.id.community_gallery_id);
    	
    	communityDotViewGrop=(ViewGroup)findViewById(R.id.domsViewGroup_id);
    	communityGalleryLabel=(TextView)findViewById(R.id.community_gallery_label_id);    	  	
    	communityRetBtn.setOnClickListener(btnListener);   	
   // 	communityHumanIcon.setOnClickListener(btnListener);
  //  	communityView.setVisibility(View.INVISIBLE);
    	
    	for(int i=0;i<community_online_total_num;i++)
    	{	
    		communityAccountIds[i]="test";   
    		communityAccountNames[i]="name";
    	}
	}
	
	public void  communityGalleryViewShow(int online_total_num)
	{	
		community_online_total_num=online_total_num;
		for (int i=0;i<online_total_num;i++)	
		{	
			communityAccountIds[i]="test";
			communityAccountNames[i]="name";
		}
		
		if (membersInfoRecive!=null&&membersInfoRecive.getMembersHashMap()!=null&&(!(membersInfoRecive.getMembersHashMap().isEmpty())))
		{			
			Set<String> keySet=membersInfoRecive.getMembersHashMap().keySet();
			Iterator<String> it=keySet.iterator();			
			while (it.hasNext())
			{
				String id=(String)it.next();
				for (int i=0;i<community_online_total_num;i++)
				{
					if (communityAccountIds[i].equals("test"))
					{
						communityAccountIds[i]=id;	
			      		communityAccountNames[i]=membersInfoRecive.getMembersHashMap().get(id);
			      		if (communityAccountNames[i].contains("_PAD"))
			      		{	
				    		MyLog.printf(TAG,"communityGalleryViewShow222");
			      			communityAccountNames[i]=communityAccountNames[i].substring(0, communityAccountNames[i].length()-4);
			      		}
						break;
					}	
				}	
			}
		}
	   	if (online_total_num>0)  	
			 communityDots = new ImageView[online_total_num/*communityImageIDs.length*/];
	   	else 
	   		return ;
	   	
	   	communityDotViewGrop.removeAllViews();//  bruce add 0128
	   	
		for(int i=0; i<online_total_num/*communityDots.length*/; i++)
		{
			ImageView imageView = new ImageView(this);
	    //	imageView.setLayoutParams(new LayoutParams(20,20));
		//	imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		//	imageView.setPadding(0, 0,50, 0);
	    	communityDots[i] = imageView;
	    	if(i == 0){
	    		communityDots[i].setBackgroundResource(R.drawable.page_indicator_focused2);
	    	}
	    	else
	    	{
	    		communityDots[i].setBackgroundResource(R.drawable.page_indicator_unfocused2);
	    	}	   
	    	communityDotViewGrop.addView(imageView);
		}   	   	  		    	
		
	//	communityGallery.setSpacing(40);		
		communityGallery.setAdapter(new ImageAdapter(this));
		
		communityGallery.setOnItemClickListener(new OnItemClickListener() {			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				MyLog.printf(TAG,"onItemClick............postion=%d",position);							
					if (MyRtcSip.onIceConnected==false)
					{	
						JSONObject myJsonobj=new JSONObject();
						if (communityAccountNames[position]!=null&&communityAccountIds[position]!=null)
						{	
							try {
							//	WSClientService.getUsrName().
							//	String str=new String(WSClientService.getUsrName().getBytes(),"utf-8");
							//	String str=new String(WSClientService.getUsrName().getBytes(),"utf-8");
								byte[] bytes=WSClientService.getUsrName().getBytes();
								myJsonobj.put(MyRtcSip.REMOTE_NAME_MSG,bytes);								
								MyLog.printf(TAG,"---------------send ring message---------------");								
								WSClientService.getInstance().client.sendMessage(communityAccountIds[position],MyRtcSip.SEND_RING_MSG,myJsonobj);					
							} catch (JSONException e) {
						// 		TODO Auto-generated catch block
								e.printStackTrace();
							}							
						}
						if (!communityAccountNames[position].equals(WSClientService.getUsrName()))	
						{	
							MyLog.printf(TAG,"..............USRNAME=%s",WSClientService.getUsrName());
				//			WSClientService.getInstance().startMyRtcActivity(communityAccountIds[position],null);
							WSClientService.getInstance().startCallActivity(communityAccountIds[position],communityAccountNames[position]);
				//			finish();
						}	
						
						
/*
 * 						try {
							WSClientService.getInstance().client.sendMessage(communityAccountIds[position],MyRtcSip.SEND_RING_MSG,null);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
*/							

	
/* 						WSClientService.getInstance().client.sendMessage(communityAccountIds[position],MyRtcSip.CALL,null);
						if (!communityAccountNames[position].equals(WSClientService.getUsrName()))						
							WSClientService.getInstance().startMyRtcActivity(communityAccountIds[position],null);
*/
					}	

/*	
 * 				{				
					if (client!=null)					
					{
						if(!communityAccountIds[position].equals("test"))
						{	
							setCommunityHostCallFlag(true);	
							vsv.setVisibility(View.VISIBLE);
							
							
							mainView.setVisibility(View.INVISIBLE);
							communityView.setVisibility(View.INVISIBLE);
							client.myCallTest(communityAccountIds[position]);											
						}	
					}	
				}
*/
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
			return community_online_total_num/*communityImageIDs.length*/;//0;
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
	//		MyLog.printf(TAG,"GALLERY   getItem.............postion=%d",position);
			return position;//null;
		}
		@Override
		public long getItemId(int position) {
		// 	TODO Auto-generated method stub
		//	MyLog.printf(TAG,"GALLERY  getItemId.............postion=%d",position);	
			for(int i=0; i<community_online_total_num/*communityDots.length*/; i++){
				if(i == position)
				{
					communityDots[i].setBackgroundResource(R.drawable.page_indicator_focused2);
				}
				else
				{
					communityDots[i].setBackgroundResource(R.drawable.page_indicator_unfocused2);
				}
			}	
			if (communityAccountIds[position]!=null)
			{
		//		MyLog.printf(TAG,"communityAccountNames[%d]:%s",position,communityAccountNames[position]);
		//		communityGalleryLabel.setText(("online human name:"+position));
				communityGalleryLabel.setText(communityAccountNames[position]);
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
				imageView.setImageResource(myCommunityImageIconID/*communityImageIDs[position]*/);
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
			if (v.getId()==R.id.community_ret_id)
			{
				   Intent  intent=new Intent();
				   intent.setClass(PadCommunityActivity.this,PadStandbyActivity.class);		   
		   		   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);		    
				   startActivity(intent);
		   		  finish();
			}			
		}
	};
	
	
	  
private static String  USER_INTENT_EXTRA="usrs_info_padCommunity";		  
private final static String RECI_COAST="com.tpad.act.PadCommunityActivityCoast";
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
	            Intent intent = new Intent(RECI_COAST);  
	            try {  
	                //获取服务器返回的信息  
	                String reslut = HttUtil  
	                        .getRequest(mGetDataAddress);  
	                intent.putExtra(USER_INTENT_EXTRA, reslut);  
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
	    				 }	 
	    			 }	 
	    			 if (membersHashMap.size()>0)
	    			 {
	    				 Message msg=new Message();
	    				 msg.what=UPDATE_UI_FLAG;
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
//  		WSClientService.getInstance().client.sendMessage(communityAccountIds[0],"data_test",null);
  		    WSClientService.getInstance().client.myCallTest(this.id);  		
  		}
  	}	
  
	
	
	@Override	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK )  
		{			
		   Intent  intent=new Intent();
		   intent.setClass(this,PadStandbyActivity.class);	
     	   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);		    
		   startActivity(intent);
		//   finish();		   
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
		padCommunityActivity=this;
		registerReceiverHandle();
		mySendBroadcast();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MyLog.printf(TAG,"..........onPause()");
		padCommunityActivity=null;

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
		super.onDestroy();
	}
}
