package com.mywebrtc.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import jp.co.odelic.smt.remote03.act.module.WelcomeAct;

import org.apache.http.conn.util.InetAddressUtils;
import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.tpad.activitys.AlarmActivity;
import com.tpad.activitys.CallActivity;
import com.tpad.activitys.LoginActivity;
import com.tpad.activitys.RingActivity;
import com.tpad.pad.activitys.HikCameraActivity;
import com.tpad.pad.activitys.PadStandbyActivity.MembersInfoBroadcastRecive;


import com.RTC.TpadRTC.MyRTCActivity;
import com.RTC.TpadRTC.R;
import com.RTC.TpadRTC.WSRtcClient;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

@SuppressWarnings("unused")
public class WSClientService extends Service
{
	private static final String TAG="MyService";	
//	private static WSClient wsClient;
	public  static  String usrName="crg_test";
	public static WSClientService  myService;
	private static String mSocketAddress;
	public static boolean run_on_pad_flag=false;
	/*private*/public /*static*/ WSRtcClient client;
	
	public static WSClientService getInstance()
	{
		return myService;
	}	
	
	public static synchronized  void setUsrName(String name)
	{
         usrName=name;	
         return ;
	}
	public static synchronized   String getUsrName()
	{
         return usrName;
	}	
//	public  static boolean myServiceRunFlag=false;		
	@Override
	public void onCreate()
	{	
		myService=this;
		MyLog.printf(TAG,"--------service on create---------------usrName:%s",usrName);
/*
 * 		int pid = android.os.Process.myPid();
		long threadid=Thread.currentThread().getId();
		String strthid=String.valueOf(threadid);	
		MyLog.printf(TAG,"hahahaha...........MyService-->onCreate pid=%d strthid=%s",pid,strthid);
*/
		super.onCreate();
/*		new Thread(new TcpThread()).start();	
		new Thread(new UdpTestThread()).start();
		Butterfly.butterfly.startUniosThread();
*/
//		new Thread(new UniosViewServiceThread()).start();		
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		MyLog.printf(TAG,"...........MyService-->onStartCommand");
//		new Thread(new TcpThread()).start();	
		//	new Thread(new UdpTestThread()).start();
	    mSocketAddress = "http://" + getResources().getString(R.string.host);
	    mSocketAddress += (":" + getResources().getString(R.string.port) + "/");
	//	wsClient = new WSClient(mSocketAddress);
		new Thread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		//	   wsClient = new WSClient(mSocketAddress);
			/*
			 *   try {
					Thread.sleep(30*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			*/
				client= new WSRtcClient(mSocketAddress);
				registerReceiverHandle();
			}
		}).start();
		return START_NOT_STICKY/*START_STICKY*/;
	}	
	
/*	@Override
	public void onStart(Intent intent, int startId)
	{
		MyLog.printf(TAG,"...........MyService-->onStart");
		
		// TODO Auto-generated method stub
		super.onStart(intent, startId);	
		
	}*/


	@Override
	public void onDestroy()
	{
		Log.e(TAG, "ExampleService-->onDestroy");
		super.onDestroy();
		if (membersInfoRecive!=null)
		{	
			unregisterReceiver(membersInfoRecive);
			membersInfoRecive=null;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	
	public void  startRingActivity(String fromID,String name)
	{
		Intent intent=new Intent(WSClientService.this,RingActivity.class);
		intent.putExtra(MyRtcSip.RING_REMOTE_ID_EXTRA,fromID);
		intent.putExtra(MyRtcSip.RING_REMOTE_NAME_EXTRA,name);
		
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);	
	}
	
	public void  startHikCameraActivity(String fromId,String recvMsg)
	{
		Intent intent=new Intent(WSClientService.this,HikCameraActivity.class);	

		if (recvMsg!=null&&recvMsg.equals(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK))
		{			
		   intent.putExtra(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK_EXTRA/*RING_REMOTE_NAME_EXTRA*/,recvMsg);		
		}	

		intent.putExtra(MyRtcSip.IP_CAMERA_REMOTE_ID_EXTRA,fromId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);		
	}
	
	public void  startBleWelcomeActivity()
	{
		Intent intent=new Intent(WSClientService.this,WelcomeAct.class);	
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);		
	}
	
	public void  startAlarmActivity()
	{
		if  (AlarmActivity.getInstance()==null)
		{	
			Intent intent=new Intent(WSClientService.this,AlarmActivity.class);	
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}	
	
	
	public  void startCallActivity(String fromId,String recvMsg)
	{
		MyLog.printf(TAG,"startCallActivity000");
		MyLog.printf(TAG,"my service  startCallActivity");
		Intent intent = new Intent(WSClientService.this, CallActivity.class);
		if (recvMsg!=null)
		{
		   intent.putExtra(MyRtcSip.RING_REMOTE_NAME_EXTRA,recvMsg);		
		}	
		intent.putExtra(MyRtcSip.RING_REMOTE_ID_EXTRA,fromId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    // BE CAREFUL !  must use the flag
		startActivity(intent);
		MyLog.printf(TAG,"startCallActivity111");		
	}		
	
	public  void startMyRtcActivity(String fromId,String recvMsg)
	{
	//	MyLog.printf(TAG,"startMyRtcActivity000");
		MyLog.printf(TAG,"my service  startRtcActivity");
		Intent intent = new Intent(WSClientService.this, MyRTCActivity.class);
		if (recvMsg!=null&&recvMsg.equals(MyRtcSip.ANSWER))
		{
		   intent.putExtra(MyRtcSip.ANSWER_EXTRA,recvMsg);		
		}
		else if (recvMsg!=null&&recvMsg.equals(MyRtcSip.CALL))
		{
			intent.putExtra(MyRtcSip.CALL_EXTRA,recvMsg);					
		}
		else if (recvMsg!=null&&recvMsg.equals(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK))
		{
			intent.putExtra(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK_EXTRA,recvMsg);								
		}	
		intent.putExtra("rtc_id",fromId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    // BE CAREFUL !must use the flag
		startActivity(intent);
		MyLog.printf(TAG,"startMyRtcActivity111");		
	}		
	
	
	public void systemExit()
	{
		
	   new Thread(new Runnable() {			
		@Override
		public void run() {
			// TODO Auto-generated method stub
			MyLog.printf(TAG, "start system exit----------");
	//		SystemClock.sleep(2000);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MyLog.printf(TAG, "----- system exit----------");
			System.exit(0);	
		}
	  }).start();
	   return ;
	   
	}

	
	public   void  stopClientService()
	{
	//	myService.stopService(name)
	}	
	/***********
	 * timer func module
	 * 
	 * ***************/
	private Timer timer;		
	int counter=0;
	public void  timerDoSomethingRepeately()
	{
		
		timer.schedule(new TimerTask() {	
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MyLog.printf(TAG,"timer......................%d",++counter);
			}
		}, 0,1000);// 1000ms
	}
	
	  public String getRunningActivityName()
	  {
		  ActivityManager activityManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		  String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		  MyLog.printf(TAG,"...........getRunningTasks=%d",activityManager.getRunningTasks(1).size());
		  MyLog.printf(TAG,"...........getRunningActivityName=%s",runningActivity);
		  return runningActivity;
	  }	
	  
	  public  MembersInfoBroadcastRecive membersInfoRecive=null;
	  
	   private String mDataAddress;
	   
		private static String  USER_INTENT_EXTRA="usrs_info";		  
		public final static String RECI_COAST="com.mywebrtc.util.WSClientService";

		public  void registerReceiverHandle()
		{
			IntentFilter filter=new IntentFilter(RECI_COAST);  	
			membersInfoRecive=null;
			membersInfoRecive=new MembersInfoBroadcastRecive();	
			registerReceiver(membersInfoRecive, filter);	
			MyLog.printf(TAG,"registerReceiverHandle111");
		}			
		
	   public void mySendBroadcast()
		{
		    MyLog.printf(TAG,"------------mySendBroadcast--------------");
			mDataAddress = "http://" + getResources().getString(R.string.host);
			mDataAddress += (":"+getResources().getString(R.string.port)+"/streams");	
			
			new Thread(){
				@Override  
		        public void run() {  
		            try {  
		                //获取服务器返回的信息  
		                String reslut = HttUtil.getRequest(mDataAddress);  
		                if (reslut==null)
		                {
		                	MyLog.printf(TAG,"reslut==null");
		                	return ;
		                }	
				         Intent intent = new Intent(RECI_COAST);  

		                intent.putExtra(USER_INTENT_EXTRA,reslut);  
		                MyLog.printf(TAG,"reslut=%s",reslut);
		                //发送广播  
		                sendBroadcast(intent);  
		            } 
		            catch (InterruptedException e) 
		            {  
		                e.printStackTrace(); 
		            } catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
		        }   		
			}.start();
		}
	   
	   public static String notifyMsgStr="test";/*MyRtcSip.NEW_USR_LOGIN_ONLINE;*/
	   
	   public static void setNotifyMsgStr(String s)
	   {
		   notifyMsgStr=s;
	   }
	   
	   	public void sendMsgNotifyAllUsers(String msg)
	   	{
	   		if ((membersInfoRecive!=null) && (membersInfoRecive.getMembersHashMap()!=null))
	   		{
		   		Set<String> set=membersInfoRecive.getMembersHashMap().keySet();
		   		Iterator<String> it=set.iterator();
		   		while (it.hasNext())
		   		{
		   			try 
		   			{
						WSClientService.getInstance().client.sendMessage(it.next().toString(),msg,null);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		   		}	
	   		}
	   		return ;
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
	   				 String getjsonName=null;
 	    			 while(it.hasNext())
 	    			 {
 	    				 /*a1*/key=(String)it.next().toString();			 
 	    				 MyLog.printf(TAG,"............keys  str=%s", key/*a1*/);
 	    				 JSONObject  myjsobject=jsonobject.getJSONObject(key/*a1*/);
 	    				 getjsonName=myjsobject.optString("name");
 	    				 membersHashMap.put(key/*a1*/,getjsonName); 		    				 
 	    				 if(getjsonName!=null)
 	    				 {	 
 	    					 if(notifyMsgStr!=null&&MyRtcSip.SEND_RING_MSG.equals(notifyMsgStr))
 	    					 {							
	    						 if (getjsonName.contains("_PAD"))
	    						 {
	  	    						byte[] bytes=WSClientService.getUsrName().getBytes();
	 	    						JSONObject myJsonobj=new JSONObject();
									myJsonobj.put(MyRtcSip.REMOTE_NAME_MSG,bytes);	
	 								MyLog.printf(TAG,"---------------send ring message---------------");

	    							 WSClientService.getInstance().client.sendMessage(key,MyRtcSip.SEND_RING_MSG,myJsonobj);	
 		 	    		 	    //	 WSClientService.getInstance().client.sendMessage(key,notifyMsgStr/*getUsrName()*/,null);								 		  							 
	    						 }
 	    					 }	 
 	    					 else
 	    					 {	 
	 	    					 MyLog.printf(TAG,"............getjsonName  str=%s", getjsonName); 	    						 
	 	    				//	 if (!getjsonName.equals(WSClientService.getUsrName())) 	
	 	    					 MyLog.printf(TAG,"............getjsonName  send key=%s     notifyMsgStr=%s", key,notifyMsgStr); 
	 	    		 	    	 WSClientService.getInstance().client.sendMessage(key,notifyMsgStr/*getUsrName()*/,null);								 		  							 
	 	    					 MyLog.printf(TAG,"............end"); 
 	    					 }
 	    				 }	 	    				 
 	    			 }	 
/*
 * 		    			if (membersHashMap.size()>0)
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

}


