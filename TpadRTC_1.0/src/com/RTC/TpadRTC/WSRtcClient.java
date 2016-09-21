package com.RTC.TpadRTC;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.odelic.smt.remote03.act.module.ControllerAct;
import jp.co.odelic.smt.remote03.act.module.MainController;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import com.mywebrtc.japan.util.OutDoorCfg;
import com.mywebrtc.util.CepsaIPCameraUtil;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;
import com.tpad.activitys.CallActivity;
import com.tpad.activitys.CameraListActivity;
import com.tpad.activitys.CommunityActivity;
import com.tpad.activitys.RingActivity;
import com.tpad.hik.HikView;
import com.tpad.hik.PhoneHikUtil;
import com.tpad.pad.activitys.HikCameraActivity;
import com.tpad.pad.activitys.PadCommunityActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.webrtc.*;
import org.webrtc.DataChannel.Buffer;


/***bruce change class to public class****/
public class WSRtcClient {
  private final static int MAX_PEER = 5;//2;
  private boolean[] endPoints = new boolean[MAX_PEER];
  private PeerConnectionFactory factory;
  private static HashMap<String, Peer> peers = new HashMap<String, Peer>();
  private static /*bruce add*/ LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
  private PeerConnectionParameters pcParams;
  private static  MediaConstraints pcConstraints = new MediaConstraints();
  private MediaStream localMS;
  private VideoSource videoSource;
  private RTCListener mListener;
  private Socket client;
  private static final  String TAG = WSRtcClient.class.getCanonicalName();
  public static String[] currentCallIDS=new String[8/*MAX_PEER*/];//null;
  public static int currentCallNums=0;
  private static int connectedPeersNum=0;
  private VideoCapturerAndroid  videoCapturer;
  
  // private static boolean out_door_flag=false;
  
  
  public static int getConnectedPeersNum()
  {
	  return connectedPeersNum;
  }
  public static int getCurrentCallNums()
  {
	  return currentCallNums;
  } 
  
  
  private final  Buffer  directionMsgBuffer=new DataChannel.Buffer(
		     ByteBuffer.allocate(64), false);

  public interface RTCListener{
 
	void onCallReady(String callId);

    void onStatusChanged(String newStatus);

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(MediaStream remoteStream, int endPoint);

    void onRemoveRemoteStream(MediaStream remoteStream, int endPoint);
    
  }

  
  private interface Command{	  
    void execute(String peerId, JSONObject payload) throws JSONException; 
  }
  
  private class CreateOfferCommand implements Command{
    public void execute(String peerId, JSONObject payload) throws JSONException {
      Log.e(TAG,"-------------CreateOfferCommand-------------");
      Peer peer = peers.get(peerId);
      peer.pc.createOffer(peer, pcConstraints);
    }
  }

  private class CreateAnswerCommand implements Command{
    public void execute(String peerId, JSONObject payload) throws JSONException {
      Log.e(TAG,"-------------CreateAnswerCommand-------------");
      Peer peer = peers.get(peerId);
      SessionDescription sdp = new SessionDescription(
                                                      SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                                                      payload.optString("sdp")
                                                      );
      MyLog.printf(TAG,"@@@@@@@@@@@@@@@@@@@@@SDP=%s",sdp.description);
      peer.pc.setRemoteDescription(peer, sdp);
      peer.pc.createAnswer(peer, pcConstraints);
    }
  }

  private class SetRemoteSDPCommand implements Command{
    public void execute(String peerId, JSONObject payload) throws JSONException {
      Log.e(TAG,"-------------SetRemoteSDPCommand-------------");
      Peer peer = peers.get(peerId);
      SessionDescription sdp = new SessionDescription(
                                                      SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                                                      payload.optString("sdp")
                                                      );
      peer.pc.setRemoteDescription(peer, sdp);
    }
  }

  private class AddIceCandidateCommand implements Command{
    public void execute(String peerId, JSONObject payload) throws JSONException {
 //   	Log.e(TAG,"-------------AddIceCandidateCommand-------------");
      PeerConnection pc = peers.get(peerId).pc;
      if (pc.getRemoteDescription() != null) {
        IceCandidate candidate = new IceCandidate(
                                                  payload.optString("id"),
                                                  payload.optInt("label"),
                                                  payload.optString("candidate")
                                                  );
   // 	MyLog.printf(TAG,"AddIceCandidateCommand  context  id=%s  label=%d  candidate=%s",payload.optString("id"),payload.optInt("label"),payload.optString("candidate"));
        pc.addIceCandidate(candidate);
      }
    }
  }

  public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
    JSONObject message = new JSONObject();
    message.put("to", to);
    message.put("type", type);
    message.put("payload", payload);
	MyLog.printf(TAG,"---------------send type=%s---------------",type);								
   
    client.emit("message", message);
  }
  
  
/*
    private static Activity instance;
  public static  void setActivityInstance(Activity ins)
  {
	  instance=ins;
  }
  public static  Activity getActivityInstance()
  {
	  return instance;
  }
*/
  
  private  void toastShowMsg(final String msg)
  {
	  Handler handler=new Handler(Looper.getMainLooper());
      handler.post(new Runnable(){  
          public void run(){  
          	Toast.makeText(WSClientService.getInstance(),msg, Toast.LENGTH_SHORT).show();     
          }  
      });
  }

  /**************************
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * Inner MessageHandler class
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   *
   */
  private class MessageHandler {
    private HashMap<String, Command> commandMap;
    private MessageHandler() {
      this.commandMap = new HashMap<String, Command>();
      commandMap.put("init", new CreateOfferCommand());
      commandMap.put("offer", new CreateAnswerCommand());
      commandMap.put("answer", new SetRemoteSDPCommand());
      commandMap.put("candidate", new AddIceCandidateCommand());
    }
    
    public void newUserShowHandle(final String sss)
    {
/*    	Activity instance=null; 
    	if (PadStandbyActivity.getInstance()!=null)
    		instance=PadStandbyActivity.getInstance();
    	else if (MainMenuActivity.getInstance()!=null)
    		instance=MainMenuActivity.getInstance();
    	else if (CommunityActivity.getInstance()!=null)
    		instance=CommunityActivity.getInstance();
    	else if (PadCommunityActivity.getInstance()!=null)
    		instance=PadCommunityActivity.getInstance();    	
    	Toast.makeText(getActivityInstance(),MyRtcSip.NEW_USR_LOGIN_ONLINE,Toast.LENGTH_SHORT).show();
*/

    /*
       if (instance!=null)
    		Toast.makeText(PadStandbyActivity.getInstance(),MyRtcSip.NEW_USR_LOGIN_ONLINE,Toast.LENGTH_SHORT).show();
	*/
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){  
            public void run(){             	
/*
				Toast toast=Toast.makeText(WSClientService.getInstance(),
            			sss, Toast.LENGTH_SHORT);
            	  toast.setGravity(Gravity.CENTER, 0, 200);
            	  LinearLayout toastView = (LinearLayout) toast.getView();
            	  ImageView imageView = new ImageView(WSClientService.getInstance());
            	  imageView.setImageResource(R.drawable.human_online_icon);
            	  toastView.setBackgroundColor(Color.TRANSPARENT);
            	  toastView.addView(imageView, 0);
            	  toast.show();   
*/    
            	LayoutInflater  layoutInflater= LayoutInflater.from(WSClientService.getInstance());
                View   myToastView=layoutInflater.inflate(R.layout.toast_view,null);
                TextView  myTextView=(TextView)myToastView.findViewById(R.id.my_toast_info_id);
                myTextView.setText(sss);
            	Toast toast=Toast.makeText(WSClientService.getInstance(),"",Toast.LENGTH_SHORT);
            	toast.setView(myToastView);
            	toast.setGravity(Gravity.BOTTOM,0, 0);
            	toast.show(); 	  
            }  
        });

    	return ;
    }
    
    public Emitter.Listener onMessage = new Emitter.Listener() {
      @Override
      public void call(Object... args) {
        JSONObject data = (JSONObject) args[0];
        try 
        {
          final String from = data.getString("from");
          final String type = data.getString("type");
          MyLog.printf(TAG,".................from=%s.......type=%s",from,type);
/*    
 *       if (type.equals(MyRtcSip.DATA_CHANNEL_ONLY))
        	  MyLog.printf(TAG,"============================== data only=true type=%s DATAONLY=%s ",type,MyRtcSip.DATA_CHANNEL_ONLY);
          else
        	  MyLog.printf(TAG,"============================== data only=false type=%s DATAONLY=%s ",type,MyRtcSip.DATA_CHANNEL_ONLY);
*/    
          if (type.contains(MyRtcSip.DATA_CHANNEL_ONLY))
          {     	  
        	  if(type.contains("camera0"))
        	  {
        		  MyLog.printf(TAG,"data_channel_only_camera00000000000000");
        		  CameraListActivity.setHikCameraIndex(0);
        	  }  
        	  else if(type.contains("camera1"))
        	  {	
        		  MyLog.printf(TAG,"data_channel_only_camera11111111111111");
        		  CameraListActivity.setHikCameraIndex(1);
        	  }	  
        	  //WSClientService.getInstance().startIPCameraActivity(from,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK);
      
        	  WSClientService.getInstance().startHikCameraActivity(from,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK);
          }
          else if(type.contains(MyRtcSip.BLE_DATA_HEAD))
          {
        	  MyLog.printf(TAG,"..........MyRtcSip.BLE_DATA_HEAD");          	
        	  if (WSClientService.run_on_pad_flag)
        	  {
        		  String s=type.substring(MyRtcSip.BLE_DATA_HEAD.length());
        		 if (s.contains("light_on"))
        		 {	 
	        		  if(ControllerAct.getInstance()!=null)
	        		  {
	        			  ControllerAct.getInstance().ct_switch_all(true);
	        		  }
        		 }
        		 else if (s.contains("light_off"))
        		 {
        		  if(ControllerAct.getInstance()!=null)
           		  {
           			  ControllerAct.getInstance().ct_switch_all(false);
           		  }
        		 }
        		 else if (s.contains("light_open"))
        		 {
        			 if(ControllerAct.getInstance()==null)
	           		  {
	           		//	  ControllerAct.getInstance().ct_switch_all(false);
        				 WSClientService.getInstance().startBleWelcomeActivity();       				 
	           		  }        				
        		 }      		 
        		 else if (s.contains("light_connect"))
        		 {
        			 if(ControllerAct.getInstance()!=null)
	           		  {
	           			//  ControllerAct.getInstance().ct_switch_all(false);
     /*   				 new Thread(new Runnable() {
 							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if (MainController.getInstance()!=null)
									MainController.getInstance().ct_connect_all(true);
							}
						}).start();
	*/       					
					     Handler handler=new Handler(Looper.getMainLooper());
					        handler.post(new Runnable(){  	
					            public void run(){  
					            	if (MainController.getInstance()!=null)
										MainController.getInstance().ct_connect_all(true);      
					            }  
					        });
	           		  }       			 
        		 }	         			 
        	  }	        	  
          }           
          else if (type.contains("online:")/*equals(MyRtcSip.NEW_USR_LOGIN_ONLINE)*/)
          {
              MyLog.printf(TAG,"................1111111111111111 online");            
/*            
 * 			  String s=type.substring(7);
              if (s.contains("_PAD"))
              {	  
            	 String name= s.substring(0,s.length()-4);
            	 newUserShowHandle(name);  
              }
              else 
            	  newUserShowHandle(s); 
*/
              if (type.contains("_PAD"))
              {	  
            	 String name= type.substring(0,type.length()-4);
            	 newUserShowHandle(name);  
              }
              else 
            	  newUserShowHandle(type);
              
              if (PadCommunityActivity.getInstance()!=null)
            	  PadCommunityActivity.getInstance().mySendBroadcast();
              else if (CommunityActivity.getInstance()!=null)
            	  CommunityActivity.getInstance().mySendBroadcast();         
          }
          else if (type.contains("offline:"))
          {
              MyLog.printf(TAG,"................off line");
              if (type.contains("_PAD"))
              {	  
            	 String name= type.substring(0,type.length()-4);
            	 newUserShowHandle(name);  
              }
              else 
            	  newUserShowHandle(type);
          } 
          else if (type.equals(MyRtcSip.ALARM_MSG))
          {
        	  WSClientService.getInstance().startAlarmActivity();      	  
          }
          else if (type.equals(MyRtcSip.CALL))
          {     	  
              MyLog.printf(TAG,"........CALL");
        	  if(MyRtcSip.onIceConnected==true)
        	  {	  
                MyLog.printf(TAG,"........onIceConnected==true");           
      			try 
      			{
					Thread.sleep(300);   //sleep 500ms
				} 
      			catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		WSClientService.getInstance().client.sendMessage(from,MyRtcSip.ICE_DISCONNECTED_WAIT,null);
        	  }	  
        	  else  
        	  {
                MyLog.printf(TAG,"........onIceConnected==false");
  				try 
  				{
					Thread.sleep(300);   //sleep 500ms
				} 
  				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}                 
        		WSClientService.getInstance().startMyRtcActivity(from,MyRtcSip.ANSWER);
        	  }	  
          }
          else if (type.equals(MyRtcSip.SEND_RING_MSG))
          {       	 
        //	  WSClientService.getInstance().getRunningActivityName();    
/*
         	  if (PadCommunityActivity.getInstance()!=null)
        		  PadCommunityActivity.getInstance().finish();
        	  if (CommunityActivity.getInstance()!=null)
        		  CommunityActivity.getInstance().finish();
*/   	  
	          JSONObject payload = null;
    		  MyLog.printf(TAG,".................payload000"); 	  	          
	          payload = data.optJSONObject("payload");
    		  MyLog.printf(TAG,".................payload111"); 	  
	          if (payload!=null)
	          {
        		  MyLog.printf(TAG,".................payload!=null"); 	  
	        //	  String name=(String)payload.optString(MyRtcSip.REMOTE_NAME_MSG);
        		  byte[] bytes=(byte[])payload.opt(MyRtcSip.REMOTE_NAME_MSG);
        		  String str;
        		  try 
        		  {
        			  str = new String(bytes,"UTF-8");
		        	  if (str!=null)
		        	  { 
		        		  MyLog.printf(TAG,".................recv name=%s",str);
		        		  WSClientService.getInstance().startRingActivity(from, str);  
/*
 * 		        		  out_door_flag=false;
		        		  if(str.contains("_DOOR"))
		        			  out_door_flag=true;
*/
		        	  }  	
				  } 
        		  catch (UnsupportedEncodingException e) 
        		  {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  }		  
	          } 
	          else	
	          {  
        		  MyLog.printf(TAG,".................payload==null"); 	  	        	  
	        	  WSClientService.getInstance().startRingActivity(from, from);
	          } 
        //	  WSClientService.getInstance().startRingActivity(from, from);
          }
          else if (type.equals(MyRtcSip.REMOTE_RTC_ACT_INIT_ASK))
          {
     //   	  WSClientService.getInstance().startRingActivity(from, from);
        	  if (MyRTCActivity.getMyRTCActivity_created_flag()
        			  ||HikCameraActivity.getIpCameraActivity_created_flag())
        	  {      		  
             //   int endpoint=findEndPoint();
        		  currentCallNums++;
                 currentCallIDS[currentCallNums]=from;
                 
                 MyLog.printf(TAG,"############################init currentCallNums=%d   currentCallIDS[endpoint]=%s",currentCallNums,currentCallIDS[currentCallNums]);
              	try 
            	{
    				Thread.sleep(100*2);				
    			} 
            	catch (InterruptedException e) 
            	{
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}  
        		WSClientService.getInstance().client.sendMessage(from,MyRtcSip.REMOTE_RTC_ACT_INITED_ANSWER,null);        	  		  
        	  }
        	  else
        	  {
          		WSClientService.getInstance().client.sendMessage(from,MyRtcSip.REMOTE_RTC_ACT_INIT_HANDLE,null);        	  		        		  
        	  }  
              	  
          } 
          else if (type.equals(MyRtcSip.REMOTE_RTC_ACT_INIT_HANDLE))
          {
        	try 
        	{
				Thread.sleep(100*2);				
			} 
        	catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      		WSClientService.getInstance().client.sendMessage(from,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK,null);        	  
          }
          else if (type.equals(MyRtcSip.REMOTE_RTC_ACT_INITED_ANSWER))
          {        	  
         //   	int endpoint=findEndPoint();
        	  	currentCallNums++;
        	    currentCallIDS[currentCallNums]=from; 
        	  	MyLog.printf(TAG,"############################answer currentCallNums=%d   currentCallIDS[currentCallNums]=%s",currentCallNums,currentCallIDS[currentCallNums]);       	    
				try 
				{
					Thread.sleep(200);
				} 				
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (currentCallIDS[currentCallNums]!=null)
				{
		            MyLog.printf(TAG,"........MyRtcSip.ANSWER333");
					WSClientService.getInstance().client.newMyCall(currentCallIDS[currentCallNums]);//myCallTest(currentCallId);
				}			
          }                   
          else if (type.equals(MyRtcSip.RING_ANSWER_MSG))
          {
        	  if (CallActivity.getcreatedCallActivityInstance()!=null)
        	  {
        		  CallActivity.getcreatedCallActivityInstance().finish();
        		  WSClientService.getInstance().startMyRtcActivity(from,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK/*MyRtcSip.CALL*/);      		  
        	  }
        	  else   // todo: bruce add at 20150604
        	  {  
        		  if(MyRtcSip.onIceConnected==true)
        		  {	
         		   	WSClientService.getInstance().client.sendMessage(from,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK,null);
         		   	JSONObject obj=new JSONObject();
         		   	obj.put("id",currentCallIDS[1]);
         		   	MyLog.printf(TAG,"##################################%s",currentCallIDS[1]);
         		   	WSClientService.getInstance().client.sendMessage(from,MyRtcSip.THREE_PEERS_COMMUNICATION, obj);
        		  } 	
        		  else       			  
        		 	WSClientService.getInstance().startMyRtcActivity(from,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK/*MyRtcSip.CALL*/);      		       		         		  
        	  }  
        //	  WSClientService.getInstance().client.sendMessage(from,MyRtcSip.CALL,null);        	  
          }
          else if (type.equals(MyRtcSip.THREE_PEERS_COMMUNICATION))
          {
	          JSONObject playload = null;
    //		  MyLog.printf(TAG,".................payload  THREE_PEERS_COMMUNICATION"); 	  	          
	          playload = data.optJSONObject("payload");    
	         final String id=playload.optString("id", "null");
	         MyLog.printf(TAG,".................payload  THREE_PEERS_COMMUNICATION  id=%s",id); 	  	          
    
	         new Thread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try 
					{
						Thread.sleep(2000);
					} 
					catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				     if (!(id.equals("null")))
				     {	 
						try 
						{
							WSClientService.getInstance().client.sendMessage(id,MyRtcSip.REMOTE_RTC_ACT_INIT_ASK/*MyRtcSip.RING_ANSWER_MSG*/,null);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				     }	
				}
			}).start();        
          }  
          else if (type.equals(MyRtcSip.RING_DECLINE_MSG))
          {
        	  if (CallActivity.getcreatedCallActivityInstance()!=null)
        	  {
        		  CallActivity.getcreatedCallActivityInstance().finish();       		  
        	  }       	  
          } 
          else if (type.equals(MyRtcSip.CALL_CANCEL_MSG))
          {
        //	  WSClientService.getInstance().client.sendMessage(from,MyRtcSip.CALL_CANCEL_MSG,null);      	  
        	  if(RingActivity.getRingActivityInstance()!=null)
        	  {
        		  RingActivity.getRingActivityInstance().finish();
        	  }
          }  
          else if (type.equals(MyRtcSip.ANSWER))
          {
              MyLog.printf(TAG,"........MyRtcSip.ANSWER");
              
              final int endpoint=findEndPoint();
        	  currentCallIDS[endpoint]=from;
        	  
        	  new Thread(new Runnable() {		
				@Override
				public void run() {
					// TODO Auto-generated method stub
				while(true)
				{
					if (MyRTCActivity.getMyRTCActivity_created_flag()==false)
					{
			            MyLog.printf(TAG,"........MyRtcSip.ANSWER111");
						try 
						{
							Thread.sleep(200);
						} 
						catch (InterruptedException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}	
					else
					{
			            MyLog.printf(TAG,"........MyRtcSip.ANSWER222");
						try 
						{
							Thread.sleep(300);
						} 
						catch (InterruptedException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
						if (currentCallIDS[endpoint]!=null)
						{
				            MyLog.printf(TAG,"........MyRtcSip.ANSWER33311111");
					//		WSClientService.getInstance().client.myCallTest(currentCallId);
				            WSClientService.getInstance().client.newMyCall(currentCallIDS[endpoint]);
						}				            
						MyLog.printf(TAG,"........MyRtcSip.ANSWER444");
						break;
					}	
				}						
				}
			}).start();
          }
          else if (type.equals(MyRtcSip.ICE_DISCONNECTED_WAIT))
          {
            MyLog.printf(TAG,"........ICE_DISCONNECTED_WAIT");
        	try 
        	{
				Thread.sleep(300);
			} 
        	catch (InterruptedException e) 
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  		
        	WSClientService.getInstance().client.sendMessage(from,MyRtcSip.CALL,null);
        	//  if (mListener!=null)
          }  
          else if (type.equals(MyRtcSip.HANGUP))
          {
        	  new Thread(new Runnable() {			
				@Override
				public void run() {
					// TODO Auto-generated method stub
/* 
  					  int endpointId=-2;
		              int endpoint=findEndPoint();

		        	  if (currentCallId[endpoint]!=null)
		        	  {	  
		        		  endpointId=myClosePCResource(currentCallId[endpoint]);
		        		  MyLog.printf(TAG,"currentCallId !=null");
		        	  }
		        	  else
		        		  MyLog.printf(TAG,"currentCallId ==null");
*/      	         	  
	        		  myClosePCResource(from);		        	  		        	  
		        	  if(CepsaIPCameraUtil.getInstance()!=null)
		        	  {  
		        		  if(CepsaIPCameraUtil.getInstance().getShouldGetJPGflag())
		        			  CepsaIPCameraUtil.getInstance().setShouldGetJPGflag(false);
		            	  try 
		            	  {
		   					Thread.sleep(350);
			   			  }
		            	  catch (InterruptedException e) 
			   			  {
			  				// TODO Auto-generated catch block
			  				e.printStackTrace();
			   			  }
		        	  } 		        	  
		      		         		  
			          try 
			          {
						 Thread.sleep(100);
					  } 
			          catch (InterruptedException e) 
			          {
							// TODO Auto-generated catch block
						  e.printStackTrace();
						  Log.e(TAG,Log.getStackTraceString(e));						  
			          }
                	  MyLog.printf(TAG,"------------mListener==null");		  		  

		        	  if (mListener!=null)
		        	  {  
		        	//	  if(currentids==0)
	                	  MyLog.printf(TAG,"------------connectedPeersNum=%d------------",connectedPeersNum);		  		  
	                	  new Thread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(connectedPeersNum==0)
				        		 {  
									 currentCallNums=0;
				                	 MyLog.printf(TAG,"------endpointId==0---------");		  
				        			 mListener.onStatusChanged(MyRtcSip.HANGUP);	  
				        			 mListener=null;    //Bruce add for fix bug  when ice disconnected!!!!!!!!
				        		 }
							}
						}).start();		        		  
		        	  }
					}
				}).start();
          }  
          else        	  
          {	  
        	  MyLog.printf(TAG,"ELSE-------------------------------------------------");
	          JSONObject payload = null;
	          if(!type.equals("init")) {
	         //   payload = data.getJSONObject("payload");
	        	  payload = data.optJSONObject("payload");
	          }
	         
	          if(!peers.containsKey(from))  // if peer is unknown, try to add it
	          {
	            // if MAX_PEER is reach, ignore the call
	        	  MyLog.printf(TAG,"ELSE-------------------------- not containsKey and add the peer");
	            int endPoint = findEndPoint();
	            
	            if(endPoint != MAX_PEER) {	   
		        	  MyLog.printf(TAG,"ELSE-------------------------- not contain  endPoint=%d",endPoint);

	              Peer peer = myAddPeer(from, endPoint,true);
	        //      if (!getDataChannelOnlyFlag())	              	          	              
	              	//bruce delete it .date:2015.08.13
	          /*
	           *     if (localMS!=null)
	                peer.pc.addStream(localMS); 
	           */	              	              
	              commandMap.get(type).execute(from, payload);             
	            }
	          } 
	          else 	
	          {
	   //     	 MyLog.printf(TAG,"ELSE-------------------------- contains");
	            commandMap.get(type).execute(from, payload);
	          }
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    };

    public Emitter.Listener onId = new Emitter.Listener() {
      @Override
      public void call(Object... args) { 	  
/*
 *      String id = (String) args[0];
        mListener.onCallReady(id);
*/
 //   	 MyLog.printf(TAG,"onId...............usr_name=%s",WSClientService.getUsrName());
    	 start_connect(new String(WSClientService.getUsrName()));
      }
    };
  }
  
  
  
  public void start_connect(String name){
    try 
    {    	
      MyLog.printf(TAG,"-----------------start connect-------------");
      JSONObject message = new JSONObject();
      message.put("name", name);	      
      client.emit("readyToStream", message);
    } 
    catch (JSONException e) 
    {
      e.printStackTrace();
    }
  }
  
  
  
  private static String preferCodec(
	      String sdpDescription, String codec, boolean isAudio) {
	    String[] lines = sdpDescription.split("\r\n");
	    int mLineIndex = -1;
	    String codecRtpMap = null;
	    // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
	    String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
	    Pattern codecPattern = Pattern.compile(regex);
	    String mediaDescription = "m=video ";
	  //  String mediaDescription = "m=video\r\n b=AS:400";
	    
	    if (isAudio) {
	      mediaDescription = "m=audio ";
	    }
	    for (int i = 0; (i < lines.length)
	        && (mLineIndex == -1 || codecRtpMap == null); i++) {
	      if (lines[i].startsWith(mediaDescription)) {
	        mLineIndex = i;
	        continue;
	      }
	      Matcher codecMatcher = codecPattern.matcher(lines[i]);
	      if (codecMatcher.matches()) {
	        codecRtpMap = codecMatcher.group(1);
	        continue;
	      }
	    }
	    if (mLineIndex == -1) {
	      Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
	      return sdpDescription;
	    }
	    if (codecRtpMap == null) {
	      Log.w(TAG, "No rtpmap for " + codec);
	      return sdpDescription;
	    }
	    Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at "
	        + lines[mLineIndex]);
	    String[] origMLineParts = lines[mLineIndex].split(" ");
	    if (origMLineParts.length > 3) {
	      StringBuilder newMLine = new StringBuilder();
	      int origPartIndex = 0;
	      // Format is: m=<media> <port> <proto> <fmt> ...
	      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
	      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
	      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
	      newMLine.append(codecRtpMap);
	      for (; origPartIndex < origMLineParts.length; origPartIndex++) {
	        if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
	          newMLine.append(" ").append(origMLineParts[origPartIndex]);
	        }
	      }
	      lines[mLineIndex] = newMLine.toString();
	      Log.d(TAG, "Change media description: " + lines[mLineIndex]);
	    } else {
	      Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
	    }
	    StringBuilder newSdpDescription = new StringBuilder();
	    for (String line : lines) {
	      newSdpDescription.append(line).append("\r\n");
  
		      if (line.contains("a=mid:video"))
		      {	  
		  	     MyLog.printf(TAG,"$$$$$$$a=mid:video");
	             if(isAudio==false)
	             {	 
		    	    newSdpDescription.append("b=AS:360").append("\r\n");
	                newSdpDescription.append("b=AS:370").append("\r\n");
	             }
		      }  
	    }
	  //   newSdpDescription.toString().replace( "a=mid:video" , "a=mid:video\r\nb=AS:256\r\n");
	    MyLog.printf(TAG,"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$new SDP=%s",newSdpDescription.toString());
	    return newSdpDescription.toString();
	  }    
  
  //private static LinkedList<DataChannel.Buffer> expectedBuffers = new LinkedList<DataChannel.Buffer>();
   
  /**************************
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * Inner class -->  Peer
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   ***************************
   */  
    
  private class Peer implements SdpObserver, PeerConnection.Observer,DataChannel.Observer,StatsObserver {
    private PeerConnection pc;
    
    private String id;
    private int endPoint;
    private boolean dataChannelOpenFlag=false;
    
    private DataChannel dataChannel,hikVoiceDataChannel;
//    private LinkedList<DataChannel.State> expectedStateChanges =new LinkedList<DataChannel.State>();
//    private LinkedList<String> expectedRemoteDataChannelLabels =new LinkedList<String>();
	@Override
	public void onStateChange() {
		// TODO Auto-generated method stub
		if (dataChannel.state()==DataChannel.State.CONNECTING)
		{	
			MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel onStateChange CONNECTING");
		}
		else if (dataChannel.state()==DataChannel.State.OPEN)
		{
			MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel onStateChange OPEN");
			if (dataChannelOpenFlag==false)
			{	
				dataChannelOpenFlag=true;					
	 			if(HikCameraActivity.getInstance()!=null)
	 			{
	 				try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 				if (WSClientService.run_on_pad_flag==true)
	 					HikCameraActivity.getInstance().startPlay();
	 			}	
	 				
			}	

		}	
		else if (dataChannel.state()==DataChannel.State.CLOSING)
		{
			MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel onStateChange CLOSING");
			dataChannelOpenFlag=false;
			PhoneHikUtil.getInstance().mphonePlayerDeInit();
		}	
		else if (dataChannel.state()==DataChannel.State.CLOSED)
		{
			dataChannelOpenFlag=false;
			PhoneHikUtil.getInstance().mphonePlayerDeInit();
			MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel onStateChange CLOSED");			
		//	MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel  start diposed");			
		//	dataChannel.dispose();
		//	MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel  end diposed");		
/*
  			if (MyRTCActivity.getInstance()!=null)	
			{	
				MyRTCActivity.getInstance().finish();
			}	
			else if (IpCameraActivity.getInstance()!=null) 
			{	
				IpCameraActivity.getInstance().finish();
			}		
*/		
		}
		else
			MyLog.printf(TAG,"XXXXXXX DataChannel ........dataChannel  status else");						
	}
	
    @Override
    public void onDataChannel(DataChannel dataChannel) 
    {
    	 MyLog.printf(TAG,"XXXXXXXXXXXXX...onDataChannel"); 
    	 MyLog.printf(TAG,"XXXXXXXXXXXXX...onDataChannel  lable:%s",dataChannel.label()); 	
/*
        assertEquals(expectedRemoteDataChannelLabels.removeFirst(),remoteDataChannel.label());
*/    	 
    	 if(dataChannel.label().equals("offering"))
    	 	setDataChannel(dataChannel);
/*    	 else if (dataChannel.label().equals("hik_voice_channel"))
    		 setHikVoiceDataChannel(dataChannel);
*/   	 
    }

    @Override
    public void onRenegotiationNeeded() {
    	MyLog.printf(TAG,"XXXXXXXXXXXXX...onRenegotiationNeeded"); 	
    }
    
    public void sendCepsaDataChannelBuffer(InputStream is)
    {
    	CepsaIPCameraUtil.getInstance().sendDataChannelBytes(is,dataChannel);
    	return ;
    }
    
    public void sendHikCameraDataChannelBuffer(byte[] bytes,int total)
    {
    	CepsaIPCameraUtil.sendHikDataChannelBytes(bytes,total,dataChannel);
    	return ;
    }   
    
    public void sendHikCameraMsg(String msg)
    {
    	CepsaIPCameraUtil.sendHikCameraMessage(dataChannel, msg);
    	return ;
    }
    
/*    
   public void sendHikAudioDataChannelBuffer(byte[] bytes,int total)
    {
    	if (hikVoiceDataChannel!=null)
    		CepsaIPCameraUtil.sendHikAudioBytes(bytes,total,hikVoiceDataChannel);
    	return ;
    }
    public void sendHikAudioMsg(String msg)
    {
    	CepsaIPCameraUtil.sendHikCameraMessage(hikVoiceDataChannel, msg);
    	return ;
    }  
*/    
    public void sendCepsaDataChannelDirMsg(String msg)
    {
    	CepsaIPCameraUtil.getInstance().sendRemoteDirectMessage(dataChannel, msg);
    	return ;
    }    

    public boolean sendHikCameraDataChannelDirMsg(String msg)
    {
		boolean ret=false;	
		directionMsgBuffer.data.clear();
		directionMsgBuffer.data.put(msg.getBytes(Charset.forName("UTF-8")));
		directionMsgBuffer.data.flip();
		ret=dataChannel.send(directionMsgBuffer);				
    	return ret;   	
    }   
    	
    public final   ByteBuffer hikPicByteBuffer=ByteBuffer.allocate(1024*1024) ;
     
     final byte[] hik_bytes=new byte[1024*10];//[1024*200];
     final byte[] playBuffer=new byte[1024*1024];//[1024*1024];  
     
     /************************************************
      * 
      * 
      * dataChannel onMessage
      * 
      * 
      ***********************************************/
     
	@Override
	public void onMessage(Buffer buffer) {
		// TODO Auto-generated method stub				
	//	MyLog.printf(TAG,"XXXXXXXXXXXXXXX DataChannel ........onMessage");		
		
		Buffer dataBuffer=buffer;	
		if(buffer.binary==false)
		{				
			Charset set = Charset.forName("UTF-8");		
			StringBuilder sb=new StringBuilder();
			sb.append(String.valueOf(set.decode(dataBuffer.data)).trim());
			String msg=new String(sb);
						
		   if (msg.equals("hik_video_frame_end"))
			{
		//		MyLog.printf(TAG,"XXXXXXXXXXXXXXX DataChannel ........hik_video_frame_end flag");				
		 		hikPicByteBuffer.flip();
		 		int byte_total=hikPicByteBuffer.remaining();
				hikPicByteBuffer.get(hik_bytes,0,byte_total);		
/*		    	Calendar calendar=Calendar.getInstance();
		    	int hour=calendar.get(Calendar.HOUR_OF_DAY);
		    	int min=calendar.get(Calendar.MINUTE);
		    	int sec=calendar.get(Calendar.SECOND);
		    	int mill=calendar.get(Calendar.MILLISECOND);
		    	String timeStr=String.format("%02d:%02d:%02d:%02d",hour,min,sec,mill);
				MyLog.printf(TAG,"#####################time=%s",timeStr);
*/
	//			MyLog.printf(TAG,"#####################hik_video_frame_end_idx=%s, byte_total=%d",msg,byte_total);

	//			MyLog.printf(TAG,"XXXXXXXXXXXXXXX DataChannel ........byte_total:%d",byte_total);	
				PhoneHikUtil.getInstance().mphonePlayerInputData(hik_bytes, byte_total);	
/*		    	 int hour1=calendar.get(Calendar.HOUR_OF_DAY);
		    	 int min1=calendar.get(Calendar.MINUTE);
		    	 int sec1=calendar.get(Calendar.SECOND);
		    	 int mill1=calendar.get(Calendar.MILLISECOND);	
		    	 String timeStr1=String.format("%02d:%02d:%02d:%02d",hour1,min1,sec1,mill1);
				MyLog.printf(TAG,"#####################time111=%s",timeStr1);
*/
				hikPicByteBuffer.clear();				
			}	
		   
		   else if (msg.equals("hik_head"))
		   {
				MyLog.printf(TAG,"..................hik_head");	    						
		   }  
		    else if (msg.equals("end"))
			{
				MyLog.printf(TAG,"..................On dataChannel Message  .end.");	    				
			}
			else if (msg.equals(MyRtcSip.HIK_CAMERA_LEFT_ING)||msg.equals(MyRtcSip.HIK_CAMERA_RIGHT_ING)||
					msg.equals(MyRtcSip.HIK_CAMERA_UP_ING)||msg.equals(MyRtcSip.HIK_CAMERA_DOWN_ING)||
					msg.equals(MyRtcSip.HIK_CAMERA_LEFT_END)||msg.equals(MyRtcSip.HIK_CAMERA_RIGHT_END)||
					msg.equals(MyRtcSip.HIK_CAMERA_UP_END)||msg.equals(MyRtcSip.HIK_CAMERA_DOWN_END))
			{
				if (WSClientService.run_on_pad_flag==true)
					HikView.getInstance().directionControlFunc(msg);
			}	   
			else if (msg.equals(MyRtcSip.HIK_CAMERA_VOICE_ON))
			{
				HikView.audio_play_flag=true;	
			}
			else if (msg.equals(MyRtcSip.HIK_CAMERA_VOICE_OFF))
			{
				HikView.audio_play_flag=false;	
			}
		}	
		else 	
		{
			if (buffer.data.remaining()==40&&(PhoneHikUtil.getInstance().getMHikPlayFlag()==false))  //head data
			{
				MyLog.printf(TAG,"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx40");		
				buffer.data.get(playBuffer, 0, 40);
				boolean ret=PhoneHikUtil.getInstance().mphonePlayerInit(playBuffer, 40, HikCameraActivity.getInstance().hikSurface.getHolder());									
				if (ret==false)  return ;
			}												
			else if((buffer.data.remaining()==92)&&(buffer.data.get(0)==-128)&&(buffer.data.get(1)==-30)/*&&buffer.data.get(2)==0xe7*/)      //audio data
			{
		//	  	MyLog.printf(TAG,"-----------data=[0]:%b,[1]:%b",buffer.data.get(0)==-60/*0x80*/,buffer.data.get(1)==-30/*0xe2*/);
		//	  	MyLog.printf(TAG,"-----------audio data[0]:%d,[1]:%d,[2]:%08x",buffer.data.get(0),buffer.data.get(1),buffer.data.get(2));
				buffer.data.get(playBuffer, 0, 92);
				PhoneHikUtil.getInstance().mphonePlayerInputData(playBuffer,92);
				return ;
			}
			else			// video stream data	
			{	
				hikPicByteBuffer.put(buffer.data);			
				return ;
			}	
		}					
	}  
    
	 public synchronized void setDataChannel(DataChannel dataChannel) {
	    if (dataChannel!=null)
	    {	
	       this.dataChannel=dataChannel;
	       this.dataChannel.registerObserver(this);
	       String label=dataChannel.label();
	       MyLog.printf(TAG,"-----------------------------label=%s",label);
	   }  
	}	
	 
/*
  	 public synchronized void setHikVoiceDataChannel(DataChannel dataChannel) {
		    if (dataChannel!=null)
		    {	
		       this.hikVoiceDataChannel=dataChannel;
		       this.hikVoiceDataChannel.registerObserver(new MyDataChannelObserver(dataChannel));
		       String label=dataChannel.label();
		       MyLog.printf(TAG,"-----------------------------label=%s",label);
		   }  
		}	
*/
 
    @Override
    public void onCreateSuccess(final SessionDescription origSdp) {
      MyLog.printf(TAG,"##########################################################");
      MyLog.printf(TAG,"......................onCreateSuccess");
      String sdpDescription = origSdp.description;
	  if(!WSClientService.getInstance().client.getDataChannelOnlyFlag())
	  {
		  sdpDescription = preferCodec(sdpDescription, /*"G722"*//*"opus"*/"G722", true);
		   sdpDescription = preferCodec(sdpDescription, "VP9", false);
	  }
      final SessionDescription sdp = new SessionDescription(
          origSdp.type, sdpDescription);
      
      JSONObject payload = new JSONObject();
      
	  try 
	  {
		  payload.put("type", sdp.type.canonicalForm());
	      payload.put("sdp", sdp.description);
	      sendMessage(id, sdp.type.canonicalForm(), payload);
	      pc.setLocalDescription(Peer.this, sdp); 		
	  } 
	  catch (JSONException e) 
	  {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  }   
	  
    }
    
    @Override
    public void onSetSuccess() {
        MyLog.printf(TAG,"......................onSetSuccess");    	
    }
    
    @Override
    public void onCreateFailure(final String error) {
   //   reportError("createSDP error: " + error);
        MyLog.printf(TAG,"......................onCreateFailure");    	   	
    	MyLog.printf(TAG,"createSDP error: %s",error);
    }

    @Override
    public void onSetFailure(final String error) {
   //   reportError("setSDP error: " + error);
        MyLog.printf(TAG,"......................onSetFailure");    	   	    	
    	MyLog.printf(TAG,"setSDP error: %s",error);
    }      
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
    	MyLog.printf(TAG,"XXXXXXXXXXXXX...onSignalingChange"); 	
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//    	MyLog.printf(TAG,"XXXXXXXXXXXXX...onIceConnectionChange iceConnectionState=%d",iceConnectionState); 	
    	Log.e(TAG,"XXXXXXXXXXXXX...onIceConnectionChange iceConnectionState="+iceConnectionState.toString());
    	if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) 
      {
        if(OutDoorCfg.IS_OUTDOOR_CALL_FLAG==true) OutDoorCfg.IS_OUTDOOR_CALL_FLAG=false;
    	MyLog.printf(TAG,"onIceConnectionChange  on ice disconnected");
		
        if (myid!=null/*mListener!=null*/)
        {
        	connectedPeersNum=0;  // JUST FOR TWO PEERS
        	currentCallNums=0;
        	return ;
        }	
	/*
	 * 
	 * BRUCE DELETE IT AT 2015.10.22  JUST FOR MUTIPLE PEERS COMMUNICATION !!!
	 * 
	 * 
	 * 
	 */    	
    	myClosePCResource(myid);          	
    	myid=null;   //fix bug for myid!=null  when ice disconnected  !!!   	 	

        toastShowMsg("ICE -- DISCONNECTED");
        if (mListener!=null)
  	  	{  
      	  new Thread(new Runnable() {				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
										
				//	if(connectedPeersNum==0)
	        		 {  
						connectedPeersNum=0;
						 currentCallNums=0;
	                	 MyLog.printf(TAG,"------endpointId==0---------");		  
	        			 mListener.onStatusChanged(MyRtcSip.HANGUP);	  
	        			 mListener=null;    //Bruce add for fix bug  when ice disconnected!!!!!!!!
	        		 }
				}
			}).start();	        
  	  	}
  //      mListener.onStatusChanged("ICE -- DISCONNECTED");                     
/*
 *         new Thread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
			try
		    {
				Thread.sleep(1000*6);   //sleep 2s
			    MyRtcSip.onIceConnected=false;
			}
		    catch (InterruptedException e) 
			{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}).start();  
*/      
      }
      else if (iceConnectionState==PeerConnection.IceConnectionState.CONNECTED)
      {
    	  MyLog.printf(TAG,"onIceConnectionChange  ICE STATE: CONNECTED");   	
    	  connectedPeersNum++;
    	  MyRtcSip.onIceConnected=true;
//   	  mListener.onStatusChanged("ICE -- CONNECTED");     
          toastShowMsg("ICE -- CONNECTED");
		 this.pc.getStats(this, null);  

    /*      final Peer pr=this;
          final PeerConnection fpc=this.pc;
          
          
      	 Timer timer=new Timer();
      	timer.schedule(new TimerTask() {	
			@Override
			public void run() {
				// TODO Auto-generated method stub
	//			MyLog.printf(TAG,"timer1......................%d",counter);
				fpc.getStats(pr, null);  
			}
		}, 0,1000*5);*/
/* 
     	  if (mListener!=null)
    		  mListener.onStatusChanged(MyRtcSip.HANGUP);	
*/	  
      }  
      else if (iceConnectionState==PeerConnection.IceConnectionState.CLOSED)
      {
    	  myid=null;   //fix bug for myid!=null  when ice closed  !!!
          if(OutDoorCfg.IS_OUTDOOR_CALL_FLAG==true) OutDoorCfg.IS_OUTDOOR_CALL_FLAG=false;
    	  
    	  CameraListActivity.setHikCameraIndex(0);
    	  MyLog.printf(TAG,"onIceConnectionChange  ICE STATE: CLOSED");  
    	  connectedPeersNum--;
    	  if (connectedPeersNum<0)
    		  connectedPeersNum=0;
    	  
    	  if (connectedPeersNum==0)
/*
      	  if (mListener!=null)
    		  mListener.onStatusChanged("ICE -- CLOSED");   
*/
    	  toastShowMsg("ICE -- CLOSED");
           new Thread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try 
				{
					Thread.sleep(1000*1);   //sleep 2s
					if (connectedPeersNum==0)
						MyRtcSip.onIceConnected=false;
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();		
      }       
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
  //  	MyLog.printf(TAG,"XXXXXXXXXXXXX...onIceGatheringChange");
    	if (iceGatheringState==PeerConnection.IceGatheringState.NEW)
    	{	
    		MyLog.printf(TAG,"XXXXXXXXXXXXX...onIceGatheringChange new");
    	}
    	else if(iceGatheringState==PeerConnection.IceGatheringState.GATHERING)
    	{	
    		MyLog.printf(TAG,"XXXXXXXXXXXXX...onIceGatheringChange   GATHERING");
    	}	
    	else if (iceGatheringState==PeerConnection.IceGatheringState.COMPLETE)
    	{	
    		MyLog.printf(TAG,"XXXXXXXXXXXXX...onIceGatheringChange   GATHERING");
    	}
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
      try {
        JSONObject payload = new JSONObject();
        payload.put("label", candidate.sdpMLineIndex);
        payload.put("id", candidate.sdpMid);
        payload.put("candidate", candidate.sdp);
        sendMessage(id, "candidate", payload);
      } 
      catch (JSONException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
      Log.e(TAG,"onAddStream "+mediaStream.label());
      // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
	  if(!WSClientService.getInstance().client.getDataChannelOnlyFlag())
	  {
		  if (mListener!=null)
			  mListener.onAddRemoteStream(mediaStream, endPoint+1);
	  }
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
      Log.e(TAG,"onRemoveStream "+mediaStream.label());         // bruce add 	
	  if(!WSClientService.getInstance().client.getDataChannelOnlyFlag())
	  {
		  if (mListener!=null)
    	  mListener.onRemoveRemoteStream(mediaStream, endPoint);
		  removePeer(id);
	  }
    }
    
	@Override
	public void onComplete(StatsReport[] reports) {
		// TODO Auto-generated method stub	
		MyLog.printf(TAG,"...................................StatsReport  oncomplete");		
		for(StatsReport rep:reports)
			MyLog.printf(TAG,"reports:%s",rep.toString());
	}
    
    public Peer(String id, int endPoint) {
      Log.d(TAG,"new Peer: "+id + " " + endPoint);
 /*     if (iceServers==null)
    	  MyLog.printf(TAG,"iceServers==null");
      else if (pcConstraints==null)
    	  MyLog.printf(TAG,"pcConstraints==null");
      else if (this==null)
    	  MyLog.printf(TAG,"this==null");
 */
	  MyLog.printf(TAG,"ccccccccccccccc");

      this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
      if (pc==null)
    	  MyLog.printf(TAG,"pc...........................=NULL");
      else
    	  MyLog.printf(TAG,"pc..........................!=NULL");      
            
      if (MyRTCActivity.getInstance()==null)
      {  
	      if (WSClientService.run_on_pad_flag)
	      {  	  
	    	  //  DATA CHANNEL 1
	    	  this.setDataChannel(this.pc.createDataChannel("offering",new DataChannel.Init()));    	  
	    	  //  DATA CHANNEL 2, now just not use it  !!!
	    	  //  this.setHikVoiceDataChannel(this.pc.createDataChannel("hik_voice_channel",new DataChannel.Init()));
	      }
      }    
/*    
 * 	this.expectDataChannel("offeringDC");
   	this.expectStateChange(DataChannel.State.CONNECTING);
*/	
      this.id = id;
      this.endPoint = endPoint;
  //    if (!getDataChannelOnlyFlag())
      {	  
    	  if (localMS!=null)
    	  pc.addStream(localMS); //, new MediaConstraints()
      }
      mListener.onStatusChanged("CONNECTING");
      
    }
     // new version webrtc add these interfaces !
	@Override
	public void onBufferedAmountChange(long previousAmount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIceConnectionReceivingChange(boolean receiving) {
		// TODO Auto-generated method stub
		
	}
  }
  
  
  public void  setRTCListenerAndFactory(RTCListener myRTCListener, PeerConnectionParameters params)
  {
	  MyLog.printf(TAG,"################ setRTCListenerAndFactory()");
	  mListener=myRTCListener;
	  pcParams = params;
/*
 * 	  if(MyRtcSip.ONLY_DATACHANNEL==true)
	  {	  
		  factory = new PeerConnectionFactory();			  
		  return ;
	  }  
*/

	//  if (factory==null)
	  {	  
		  if (MyRtcSip.NEW_VERSION_WEBRTC==false)  
			  factory = new PeerConnectionFactory();
		  else
		  {
			  factory= new PeerConnectionFactory(null);
			  factory.setVideoHwAccelerationOptions(VideoRendererGui.getEglBaseContext(), VideoRendererGui.getEglBaseContext());
		  }  
		  if ( !getDataChannelOnlyFlag())
		  {
			  MediaConstraints videoConstraints = new MediaConstraints();
			  videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight",Integer.toString(pcParams.videoHeight)));
			  videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth",Integer.toString(pcParams.videoWidth)));		
			  //bruce add 
		//	  videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minHeight", "240"/*Integer.toString(pcParams.videoHeight)*/));
		//	  videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minWidth", "320"/*Integer.toString(pcParams.videoWidth)*/));		  			  
		//	  videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
		//	  videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));	    
			  MyLog.printf(TAG,"##################################### setRTCListenerAndFactory()--------videoFps=%d",pcParams.videoFps);  
			  videoSource = factory.createVideoSource(getVideoCapturer(), videoConstraints);
		  }
	  }
  } 

  public WSRtcClient(/*RTCListener listener,*/ String host) 
  { 
    MessageHandler messageHandler = new MessageHandler();
    try {
      client = IO.socket(host);
    }
    catch (URISyntaxException e) 
    {
      e.printStackTrace();
    }
    
    client.on("id", messageHandler.onId);
    client.on("message", messageHandler.onMessage);
    client.connect();
            
    iceServers.add(new PeerConnection.IceServer("stun:121.40.34.48:3478"));   
        
    iceServers.add(new PeerConnection.IceServer(
				"turn:121.40.34.48:3478?transport=udp", "ninefingers",
				"youhavetoberealistic"));    
     
    iceServers.add(new PeerConnection.IceServer(
				"turn:121.40.34.48:3478?transport=tcp", "ninefingers",
			"youhavetoberealistic")); 
    
    iceServers.add(new PeerConnection.IceServer(
		"turns:121.40.34.48:3478?transport=tcp", "ninefingers",
		"youhavetoberealistic"));  
    
     
/*    iceServers.add(new PeerConnection.IceServer("stun:47.88.136.111:3478"));
    iceServers.add(new PeerConnection.IceServer(
				"turn:47.88.136.111:3478?transport=udp", "ninefingers",
				"youhavetoberealistic"));    
    
    iceServers.add(new PeerConnection.IceServer(
				"turn:47.88.136.111:3478?transport=tcp", "ninefingers",
			"youhavetoberealistic")); 
    
    iceServers.add(new PeerConnection.IceServer(
		"turns:47.88.136.111:3478?transport=tcp", "ninefingers",
		"youhavetoberealistic")); 		
*/

/*    iceServers.add(new PeerConnection.IceServer("stun:114.215.177.233:3478"));
    iceServers.add(new PeerConnection.IceServer(
				"turn:114.215.177.233:3478?transport=udp", "ninefingers",
				"youhavetoberealistic"));    
    iceServers.add(new PeerConnection.IceServer(
				"turn:114.215.177.233:3478?transport=tcp", "ninefingers",
			"youhavetoberealistic"));   
    iceServers.add(new PeerConnection.IceServer(
		"turns:114.215.177.233:3478?transport=tcp", "ninefingers",
		"youhavetoberealistic"));  
*/
 
    
 /****bruce add  turns config****/
 
 /*
      iceServers.add(new PeerConnection.IceServer("stuns:121.40.34.48:3478"));
 */
    
    /* 
        iceServers.add(new PeerConnection.IceServer(
		"turns:121.40.34.48:3478?transport=udp", "ninefingers",
		"youhavetoberealistic")); 
	*/    
/*	if(MyRtcSip.ONLY_DATACHANNEL==true)
	{
		pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
		pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
	}
	else
*/
	{
		pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
		pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));		
	}		
  //  pcConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));   
    pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    
   new Thread(new Runnable() {	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try 
		{
			Thread.sleep(2000);
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String msg="online:"+ /*" -visitor-"*/WSClientService.getUsrName();
	//    MyLog.printf(TAG," ONLINE NAME:"+WSClientService.getUsrName());
	    WSClientService.setNotifyMsgStr(msg);
	    WSClientService.getInstance().mySendBroadcast();
	}
   }).start(); 
  }
/*  public void setCamera_1(){
	  MyLog.printf(TAG,".................setCamera()");

  	  if(MyRtcSip.ONLY_DATACHANNEL==true) 
	  {
		  MyLog.printf(TAG,".................getDataChannelOnlyFlag()==true");
		  localMS=null;
		  videoSource=null;
	      return ;
	  }	  

	  localMS = factory.createLocalMediaStream("ARDAMS");
	  if(pcParams.videoCallEnabled)
	  {	    	 
		    if (getDataChannelOnlyFlag()==true)
		    {	
		    	 restartVideoSource();
      //  bruce add stopVideoSource() and twice for fix bug about stopVideoSource failed!!!!!!!!!!!!!!!!!!
  		     
   		//		stopVideoSource();
				stopVideoSource();

		    }	 
		    else
		    {	
		    	restartVideoSource();  	
		    }
		    if(videoSource != null)
		    	localMS.addTrack(factory.createVideoTrack("ARDAMSv0", videoSource));
	    }	    	    	    
	    AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
	    localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));	   
	//    if (!getDataChannelOnlyFlag())
	    mListener.onLocalStream(localMS);
  }*/
  public void setCamera(){	  
	  MyLog.printf(TAG,".................setCamera()  getDataChannelOnlyFlag=%b",getDataChannelOnlyFlag());
	  if ( !getDataChannelOnlyFlag())
	  {	  
	    localMS = factory.createLocalMediaStream("ARDAMS");
	    if(pcParams.videoCallEnabled)
	    {
	        if(OutDoorCfg.IS_OUTDOOR_CALL_FLAG==false)
	        {
			    if (getDataChannelOnlyFlag()==true)
			    {	
			    	restartVideoSource();
			    }	 
			    else
			    {	
			    	restartVideoSource();  	
			    }
			    
			    if(videoSource != null)
			    	localMS.addTrack(factory.createVideoTrack("ARDAMSv0", videoSource));	
	        }
	    }
	    	    	    
	    AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
	    localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));	   
	//    if (!getDataChannelOnlyFlag())
	    mListener.onLocalStream(localMS);
	 }
	 else
	 {
		 if (WSClientService.run_on_pad_flag==true)
		 {	 
	  		 localMS=null;
			 videoSource=null;	
		 }
		 else
		 {	 
			 localMS = factory.createLocalMediaStream("ARDAMS");
			 AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
			    localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));
			 mListener.onLocalStream(localMS);		
		 }	 
	 }	  
  }
    

  public void stopVideoSource() {
    if(videoSource != null) 
    {	
    	MyLog.printf(TAG,"stopVideoSource");
    	videoSource.stop();
 //   	videoSource.stop();  // maybe bug,shoud stop twice!!!!!!!   	
 //  	videoSource.dispose();
    }
  }

  public void restartVideoSource() {
    if(videoSource != null)
    {	
    	MyLog.printf(TAG,"restartVideoSource");    	
    	videoSource.restart();
    }	  	
  }

  public void disconnect() {
    Iterator it = peers.values().iterator();
    while(it.hasNext()){
      Peer peer = (Peer) it.next();
      peer.pc.dispose();
    }

/*
 *  videoSource.stop();//dispose();
    factory.dispose();
*/
    client.disconnect();
    client.close();
  }
  
 private int findCurrentIdIndex(String id)
 {
	 int index=-1;
	 for(int i=0;i<currentCallIDS.length;i++)
	 {
		 if ((currentCallIDS[i]!=null )&&(id!=null)&&(id.equals(currentCallIDS[i])) )
		 {
			 index=i;
			 return index;
		 }	 
	 }	 
	 return index;	 
 }

  private int findEndPoint() {
    for(int i = 0; i < MAX_PEER; i++) 
    	if (!endPoints[i]) return i;
    return (MAX_PEER-1);
  }

  public void start(String name){
    try {
      JSONObject message = new JSONObject();
      message.put("name", name);
      client.emit("readyToStream", message);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private VideoCapturer getVideoCapturer() {
	 if (MyRtcSip.NEW_VERSION_WEBRTC==false)
	 {	 
		// String frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
		// return VideoCapturerAndroid.create(frontCameraDeviceName);
		 return null;
	 }
	 else
	 {
		  String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
		  String frontCameraDeviceName =
		          CameraEnumerationAndroid.getNameOfFrontFacingDevice();
		  if (frontCameraDeviceName != null) 
		  {
		     cameraDeviceName = frontCameraDeviceName;
		  }
	      Log.e(TAG, "Opening camera: " + cameraDeviceName);
	      videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null,
	              /*peerConnectionParameters.captureToTexture ? renderEGLContext :*/ null);
	      if (videoCapturer==null)
	    	  Log.e(TAG,"videoCapturer is nullllllllllllllllllllllllll!");
	      return videoCapturer;
	 }	 
	 
  }
  
/*  {
	  String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
      String frontCameraDeviceName =
          CameraEnumerationAndroid.getNameOfFrontFacingDevice();
      if (numberOfCameras > 1 && frontCameraDeviceName != null) {
        cameraDeviceName = frontCameraDeviceName;
      }
      Log.d(TAG, "Opening camera: " + cameraDeviceName);
      videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null,
          peerConnectionParameters.captureToTexture ? renderEGLContext : null);
      if (videoCapturer == null) {
        reportError("Failed to open camera");
        return;
      }
  }*/
  

/*
 *   private Peer addPeer(String id, int endPoint) {
    Peer peer = new Peer(id, endPoint);
    peers.put(id, peer);

    endPoints[endPoint] = true;
    return peer;
  }
 */
  public  Peer myAddPeer(String id, int endPoint,boolean flag) {
//	if (!getDataChannelOnlyFlag())
	    setCamera();	
	//setMyAudio();	  	
    Peer peer = new Peer(id, endPoint);
    peers.put(id, peer);
    myid=id;
    endPoints[endPoint] = true;
    return peer;
  }
  
  private void removePeer(String id) {
    Peer peer = peers.get(id);
    peer.pc.close();    //bruce delete it just for test!!!
    peer.pc.dispose();      
    peers.remove(peer.id);
    endPoints[peer.endPoint] = false;
  }
  
  public  int  myClosePCResource(String id)
  {
	 MyLog.printf(TAG,"myClosePCResource......");
	 Peer peer = peers.get(id);
	 //  peer.pc.close();    //bruce delete it just for test!!!
	 if (peer!=null )
	 { 
/*
  		 if( peer.dataChannel!=null)
		 {
		//	 peer.dataChannel.close();
			 peer.dataChannel.dispose();
		 }	
*/ 
		 
/*
  		 if (getDataChannelOnlyFlag())
			 peer.pc.dispose_DatatChannelOnly();
		 else	
*/ 
	//	 if (peer.endPoint==0) 
			 peer.pc.dispose();
	/*
	  	 else
			 peer.pc.close();
	*/
		String peerID=peer.id;
		int peerEndpoint=peer.endPoint;
		
		 peers.remove(peer.id);
		 int index=findCurrentIdIndex(peerID);
		 if (index!=-1)
			 currentCallIDS[index]=null;
		 			 			 
		 MyLog.printf(TAG,"myClosePCResource................%d",peerEndpoint);
		 endPoints[peerEndpoint] = false;	
	//	 if (peerEndpoint==0)    		     // todo:   add this to 3 members communication ,and free res when endpoint ==0
		 {
			 MyLog.printf(TAG,"+++++++++++++++++stopVideoSource++++++++++++++++++");
			 stopVideoSource();      //????????????????????????????????????   stop every PeerConnection
		 }	 
		 return peerEndpoint;
	//	 videoSource.dispose();		 		 		 
/*
  		 factory.dispose();
		 factory=null;
*/
	//	 setCamera(); 
	//	 mListener.onLocalStream(localMS);		 
	//   Toast.makeText(WSClientService.getInstance().getApplicationContext(),"VS stopped",Toast.LENGTH_SHORT).show();
	 }
	 return -1;
  }
  
  
  public  void fromActivityHangupHandle()
  {
	  if (MyRtcSip.ONLY_TWO_PEERS)
	  {	  
		  String  remoteID=WSClientService.getInstance().client.getRemoteID();	
		  WSClientService.getInstance().client.myClosePCResource(remoteID);			
			try 
			{
				WSClientService.getInstance().client.sendMessage(remoteID,MyRtcSip.HANGUP,null);
			}
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  currentCallNums=0;
	  }
	  else
	  {    
		  String idStr=null;
		  int connectNum=currentCallNums/*connectedPeersNum*/;
	
		  MyLog.printf(TAG,".......................findEndPoint()=%d,connectNum=%d",findEndPoint(),connectNum);
		  
		  for(int i=1;i<=connectNum/*findEndPoint()*/;i++)
		  {   
			MyLog.printf(TAG,"......currentCallIDS[%d]=%s",i,currentCallIDS[i]);  
		    idStr=currentCallIDS[i];	    
			WSClientService.getInstance().client.myClosePCResource(idStr/*currentCallIDS[i]*/);			
			try 
			{
				WSClientService.getInstance().client.sendMessage(idStr/*currentCallIDS[i]*/,MyRtcSip.HANGUP,null);
			}
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  currentCallNums=0;
	  }
  }
  
  /*************bruce add func*****************/
  String myid=null;
/*  public void  myCallTest(String id)
  {
	//  myid=id;
	  myAddPeer(id,0,true);   //bruce add
      Peer peer = peers.get(id);
	  peer.pc.createOffer(peer, pcConstraints);	  
  } */
  public void  myCallTest(String id)
  {
	 MyLog.printf(TAG,"XXXXXXXXXXXXXXXXXXXXXXXXmyCallTestXXXXXXXXXXXXXXXXX"); 
	//  myid=id;
	  int endPoint=findEndPoint();

	  myAddPeer(id,endPoint,true);   //bruce add
      Peer peer = peers.get(id);
	  peer.pc.createOffer(peer, pcConstraints);	  
  } 
  
  public void newMyCall(String id)
  {
		 MyLog.printf(TAG,"XXXXXXXXXXXXXXXXXXXXXXXXnewCallXXXXXXXXXXXXXXXXX"); 

	  int endPoint=findEndPoint();
	  myAddPeer(id, endPoint, true);
	  Peer peer = peers.get(id);
	  peer.pc.createOffer(peer, pcConstraints);
	  
  }
  
  
  
  public String getRemoteID()
  {
	  return myid;
  }
  
  
  public void sendCepsaCameraData(InputStream is)
  {
	  if (myid!=null)
	  {  
		  MyLog.printf(TAG,".....................mysendDataTest  myid!=null");
		  Peer peer = peers.get(myid);
		  if (peer!=null)
		  peer.sendCepsaDataChannelBuffer(is);
	  } 
	  else
		  MyLog.printf(TAG,".....................mysendDataTest  myid==null");		  
	  return ;
  }

  public  void sendHikCameraData(byte[] btys,int btsTotal)
  {
	  if (myid!=null)
	  {  
//		  MyLog.printf(TAG,".....................sendHikCameraData  myid!=null  length=%d",btsTotal);
		  Peer peer = peers.get(myid);
		  if (peer!=null)
		  {	  
			  peer.sendHikCameraDataChannelBuffer(btys,btsTotal);
		  }
		  else
			  MyLog.printf(TAG,"..........peer obj is null");
			  
	  } 
	  else
		  MyLog.printf(TAG,".....................sendHikCameraData  myid==null");		  
	  return ;
  }  
  
  
  
  public synchronized void sendHikCameraMsg(String msg)
  {
	  if (myid!=null)
	  {  
	//	  MyLog.printf(TAG,".....................sendHikCameraMsg  myid!=null ");
		  Peer peer = peers.get(myid);
		  if (peer!=null)
			  peer.sendHikCameraMsg(msg);
	  } 
	  else
		  MyLog.printf(TAG,".....................sendHikCameraMsg  myid==null");		  
	  return ;
  }  
  
/*  public synchronized void sendHikAudioData(byte[] btys,int btsTotal)
  {
	  if (myid!=null)
	  {  
	//	  MyLog.printf(TAG,".....................sendHikAudioData  myid!=null  length=%d",btsTotal);
		  Peer peer = peers.get(myid);
		  if (peer!=null)
		  {	  
			  peer.sendHikAudioDataChannelBuffer(btys,btsTotal);
		  }
		  else
			  MyLog.printf(TAG,"..........peer obj is null");
			  
	  } 
	  else
		  MyLog.printf(TAG,".....................sendHikCameraData  myid==null");		  
	  return ;
  }*/
  
  
/*  byte[]  sendAudioBytes=new byte[2048]; 
  
  public synchronized void sendHikAudioDataByteBuffer(ByteBuffer byteBuffer)
  {
	  int  total=byteBuffer.remaining(); 
	  
	  byteBuffer.get(sendAudioBytes,0,total);
	  if (myid!=null)
	  {  
	//	  MyLog.printf(TAG,".....................sendHikAudioData  myid!=null  length=%d",btsTotal);
		  Peer peer = peers.get(myid);
		  if (peer!=null)
		  {	  
			  
			  peer.sendHikAudioDataChannelBuffer(sendAudioBytes,total);
		  }
		  else
			  MyLog.printf(TAG,"..........peer obj is null");
			  
	  } 
	  else
		  MyLog.printf(TAG,".....................sendHikCameraData  myid==null");		  
	  return ;
  } */ 
  
  
/*  public synchronized void sendHikAudioMsgData(String msg)
  {
	  if (myid!=null)
	  {  
	//	  MyLog.printf(TAG,".....................sendHikCameraMsg  myid!=null ");
		  Peer peer = peers.get(myid);
		  if (peer!=null)
			  peer.sendHikAudioMsg(msg);
	  } 
	  else
		  MyLog.printf(TAG,".....................sendHikCameraMsg  myid==null");		  
	  return ;
  }  
  
  public synchronized  void sendCepsaCameraDirMsgfunc(String msg)
  {
	  if (myid!=null)
	  {  
		  MyLog.printf(TAG,".....................sendCepsaCameraDirMsgfunc  myid!=null");
		  Peer peer = peers.get(myid);
		  if (peer!=null)
			  peer.sendCepsaDataChannelDirMsg(msg);
	  } 
	  else
		  MyLog.printf(TAG,".....................sendCepsaCameraDirMsgfunc  myid==null");	  
	  return ;
  }  */
  public synchronized  void sendHikCameraDirMsgfunc(String msg)
  {
	  if (myid!=null)
	  {  
		  MyLog.printf(TAG,".....................sendCepsaCameraDirMsgfunc  myid!=null");
		  Peer peer = peers.get(myid);
		  if (peer!=null)
			  peer.sendHikCameraDataChannelDirMsg(msg);
	  } 
	  else
		  MyLog.printf(TAG,".....................sendCepsaCameraDirMsgfunc  myid==null");	  
	  return ;
  }  
  
  interface DrawJPGListener
  {
	  void onDraw(Context c,Bitmap bm);
  }
  
  public boolean dataChannelOnlyFlag=false;
  
  public void setDataChannelOnlyFlag(boolean onlyFlag)
  {
	  dataChannelOnlyFlag=onlyFlag;
  } 
  
  public boolean getDataChannelOnlyFlag()
  {
	  return dataChannelOnlyFlag;  
  } 	
  
}
