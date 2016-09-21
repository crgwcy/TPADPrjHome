package com.tpad.hik;


//import android.content.Context;
//import android.content.Intent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_CLIENTINFO;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.RealPlayCallBack;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.MyRtcSip;
import com.mywebrtc.util.WSClientService;

import org.MediaPlayer.PlayM4.Player;
//import org.MediaPlayer.PlayM4.PlayerCallBack.PlayerDisplayCB;

//import java.nio.ByteBuffer;

public class HikView {

	 private final static String TAG = "HIK";
/*	 private final static String TAG4 = "Video";
	 private final static String TAG8 = "8888";
	 private final static String TAGS = "SSSSSSSSSSSSSSSSSSSSSSSS";*/

	 
	 public final static String ACTION_START_RENDERING = "action_start_rendering";
	 public final static String ACTION_DVR_OUTLINE = "action_dvr_outline";
	 public static boolean  audio_play_flag=true;
	 /**
	     * 设备信息
	     * 模拟通道数by channel number
	     * 数字通道数by ip channel number
	 */
	 private NET_DVR_DEVICEINFO_V30 deviceInfo_V30 = null;
	 
	 private HCNetSDK videoCtr;    //网络库sdk
	 private  Player myPlayer = null;  //播放库sdk
	 
	 /**
	 	*登陆标记  -1未登录  0 登陆成功
	 */
	 private int m_iLogID = -1;
	 
	 /**
	    *播放标记 -1未播放，0正在播放
	 */
	 private int m_iPlayID = -1;
	 /***
	  * 语音Flag
	  * 
	  * **/
	 private int m_iSoundSendFlag = 0;
	 
	 /**
	  * SDK INIT flag
	  * 
	  * **/
	 private boolean m_iSDKFlag = false;
	 
	 private int m_iPort = -1;
	 
	 private String ip=null;
	 private int port=-1;
	 private String username=null;
	 private String password=null;
	 
	 private int channel;
	 
	 private SurfaceHolder holder;
	 
	 //private Context context;

	 private static HikView myview = null;
	 
	 //constructor
	 private HikView(){
		 myPlayer = Player.getInstance();
		 
		 setVideoCtr(new HCNetSDK());
	 }
	 
	 public static synchronized HikView getInstance(){
		 if (myview==null)
		 {
			 myview = new HikView();
		 }
		return myview;
	 }
	 
	
	 public int getchannel() {
			return channel + deviceInfo_V30.byStartChan;
			//return  m_iPort;
		}

	 
	 //设备信息
	 public void setDeviceBean(DeviceBean bean){
		 
	        this.ip = bean.getIP();
	        if(bean.getPort()!=null)
	        	this.port = Integer.parseInt(bean.getPort());
	        else
	        	this.port=-1;
	        this.username = bean.getUserName();
	        this.password = bean.getPassWord();
	 }
	 
	 /**
	  * 设置播放画面
	  * @param holder
	  */
	 public void setSurfaceHolder(SurfaceHolder holder){
		 this.holder = holder;
	 }
	 
	 /**
	  * 发送广播上下文
	  * @param context
	  */
//	 public void setContext(Context context){
//		 this.context = context;
//	 }
//	 
	 public void initSDK(){
			if(!m_iSDKFlag)
			{
					// if(getVideoCtr().NET_DVR_Init()){
				 if(myview.videoCtr.NET_DVR_Init()){
				 m_iSDKFlag = true;
				 Log.e(TAG, "SDK init OK");
				 }				 
				else
				{
					Log.e(TAG, "SDK init failed");
					Log.e(TAG, "errorcode = "+myview.videoCtr.NET_DVR_GetLastError());
				}
			}
			else 
				return;
	 }
	 
	 public void setExceptionCallBack(){
		 myview.videoCtr.NET_DVR_SetExceptionCallBack(mExceptionCallBack);  //设置错误回掉函数
	 }
	 
	 /**
	  * 设备登陆
	  */
	 public void loginDevice(){
		 
		 videoCtr.NET_DVR_SetConnectTime(5000);
		 videoCtr.NET_DVR_SetReconnect(5000, true);

		 deviceInfo_V30 = new NET_DVR_DEVICEINFO_V30();
		 	 
		//ip = getVideoCtr().NET_DVR_GetDVRIPByResolveSvr_EX(ip, 0, null, 0, ip, 0, null);		
		// getVideoCtr().NET_DVR_GetDVRIPByResolveSvr_EX(ip, 0, null, 0, ip, 0, null);
		 m_iLogID = videoCtr.NET_DVR_Login_V30(ip, port, username, password, deviceInfo_V30);
		 
		 m_iPort = myPlayer.getPort();  //获取空闲播放端口
		 Log.i(TAG,"port" + m_iPort);
			

		 if(m_iLogID<0){
			 Log.i(TAG, "Device Log in is failed");
			 Log.i(TAG, "errorcode = "+ videoCtr.NET_DVR_GetLastError());
		 }else{
			 	Log.i(TAG, "Device Log in is OK");
				Log.i(TAG,"下面是设备信息************************");
				Log.i(TAG,"通道开始=" + deviceInfo_V30.byStartChan);
				Log.i(TAG,"通道个数=" + deviceInfo_V30.byChanNum);
				Log.i(TAG,"设备类型=" + deviceInfo_V30.byDVRType);
				Log.i(TAG,"ip通道个数=" + deviceInfo_V30.byIPChanNum);
				byte[] sbbyte = deviceInfo_V30.sSerialNumber;
				String sNo = "";
				for (int i = 0; i < sbbyte.length; i++) {
					sNo += String.valueOf(sbbyte[i]);
				}
				System.out.println("设备序列号=" + sNo);
				System.out.println("************************");
		 }
	 }
	 /**
	  * 设备登陆
	  */
	 public void myLoginDevice(String ip_tmp,int port_tmp,String username_tmp,String password_tmp){
		 
		 videoCtr.NET_DVR_SetConnectTime(5000);
		 videoCtr.NET_DVR_SetReconnect(5000, true);

		 deviceInfo_V30 = new NET_DVR_DEVICEINFO_V30();
		 ip=new String(ip_tmp);
		 port=port_tmp;
		 username=new String(username_tmp);
		 password=new String(password_tmp);
		 	 
		//ip = getVideoCtr().NET_DVR_GetDVRIPByResolveSvr_EX(ip, 0, null, 0, ip, 0, null);		
		// getVideoCtr().NET_DVR_GetDVRIPByResolveSvr_EX(ip, 0, null, 0, ip, 0, null);
		 Handler handler=new Handler(Looper.getMainLooper());
	        handler.post(new Runnable(){  
	            public void run(){  
	            	Toast.makeText(WSClientService.getInstance(),ip+":"+port, Toast.LENGTH_SHORT).show();    
	            }  
	        });
		 m_iLogID = videoCtr.NET_DVR_Login_V30(ip, port, username, password, deviceInfo_V30);
		 
		 m_iPort = myPlayer.getPort();  //获取空闲播放端口
		 Log.e(TAG,"port" + m_iPort);
			

		 if(m_iLogID<0){
			 Log.i(TAG, "Device Log in is failed");
			 Log.i(TAG, "errorcode = "+ videoCtr.NET_DVR_GetLastError());
		 }
		 else{
			 	Log.i(TAG, "Device Log in is OK");
				Log.i(TAG,"下面是设备信息************************");
				Log.i(TAG,"通道开始=" + deviceInfo_V30.byStartChan);
				Log.i(TAG,"通道个数=" + deviceInfo_V30.byChanNum);
				Log.i(TAG,"设备类型=" + deviceInfo_V30.byDVRType);
				Log.i(TAG,"ip通道个数=" + deviceInfo_V30.byIPChanNum);
				byte[] sbbyte = deviceInfo_V30.sSerialNumber;
				String sNo = "";
				for (int i = 0; i < sbbyte.length; i++) {
					sNo += String.valueOf(sbbyte[i]);
				}
				System.out.println("设备序列号=" + sNo);
				System.out.println("************************");
				
		 }
	 }
	 /**
	  * 设备登出
	  */
	 public void logoutDevice(){
		 Handler handler=new Handler(Looper.getMainLooper());
	        handler.post(new Runnable(){  
	            public void run(){  
	            	Toast.makeText(WSClientService.getInstance(),"exit login", Toast.LENGTH_SHORT).show();    
	            }  
	        });
		 if(videoCtr.NET_DVR_Logout_V30(m_iLogID)){
			 m_iLogID = -1;
			 Log.e(TAG, "Device Logout success");
		 }
		 else{
			 m_iLogID = 0;
			 Log.e(TAG, "Device Logout failed");
			 Log.e(TAG, "error code"+videoCtr.NET_DVR_GetLastError());
		 }
		 freeSDK();   //   Bruce  add for free SDK
		 myview=null;
		 
	 }
	 
	 public synchronized void realPlay(){
		 try{
/*			 if(m_iLogID<0){
				 Log.e(TAG, "Try Login again");
				 int count = 0;
	             while (count < 1) {
	            	 Log.i(TAG, "正在第" + (count + 1) + "次重新登入");
	            	 loginDevice();
	            	 if (m_iLogID < 0) {
	            		 count++;
	            		 Thread.sleep(200);
	             		} else {
	             			//get error   推送出去
	            	 		Log.i(TAG, "第" + (count + 1) + "次登入成功");
	            	 		break;
	            	 	}
	             }
	         if (m_iLogID < 0) { 
	             Log.e(TAG, "尝试登入" + count + "次均失败！");
	             return;
	             }
			 }*/
	         if (m_iLogID < 0) { 
	             Log.e(TAG, "尝试登入" + 1 + "次均失败！");
	             return;
	             }
	                
			 if(getM_iPlayID()<0){
				 NET_DVR_CLIENTINFO ClientInfo = new NET_DVR_CLIENTINFO();
				 ClientInfo.lChannel = channel + deviceInfo_V30.byStartChan;
				 ClientInfo.lLinkMode = 0x80000000;
				 // 多播地址，需要多播预览时配置 
	             ClientInfo.sMultiCastIP = null;
	             setM_iPlayID(videoCtr.NET_DVR_RealPlay_V30(getM_iLogID()/*ClientInfo.lChannel*//*channel*/, ClientInfo, getRealPlayerCallBack(), true));
	             //videoCtr.NET_DVR_StartVoiceCom_MR_V30(arg0, arg1, arg2)
	             	             
	             if(getM_iPlayID()<0){
	            	 Log.e(TAG, "RealPlay is failed");
	            	 Log.e(TAG,"Error Code"+videoCtr.NET_DVR_GetLastError());
	             }
	             else{
	            	 Log.i(TAG, "realPlay is started");
	             }
			 }else{
				 Log.i(TAG, "Play Engine has been launched");
			 }
		 }catch(Exception e){
			 Log.e(TAG, "异常：" + e.toString());
		 }
	 }
	 

	 /*
	  * 播放实时回调
	  * @return
	  */
	 private RealPlayCallBack getRealPlayerCallBack() {
	        return new RealPlayCallBack() {
	        	/**
	        	 * iRealHandle 当前的预览句柄
	        	 * iDataType 数据类型
	        	 * pDataBuffer 存放数据的缓冲区指针
	        	 * iDataSize 缓冲区大小
	        	 */
	            @Override
	            public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
	            	//Log.i(TAG,"播放回调启用");
	            	//m_iPort = Player.getInstance().getPort();
	                processRealData(iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME);
	            }
	        };
	    }
	 
	 	 
	 
	 /**
	  *   stop play
	  */
	 
	 public synchronized void stopPlay(){
		 if (getM_iPlayID() < 0) {
	            Log.d(TAG, "stoped or no open");
	            return;
	        }
		 //停止网络
		 if (videoCtr.NET_DVR_StopRealPlay(getM_iPlayID())) {
			 	setM_iPlayID(-1);
			 	channel=0;
	            Log.i(TAG, "停止实时播放成功！");
	        } else {
	            Log.e(TAG, "停止实时播放失败！" + videoCtr.NET_DVR_GetLastError());
	            //return;
	     }		 
		 //停止声音
		 if (myPlayer.stopSound()) {
	            Log.i(TAG, "停止本声音地播放成功！");
	        } else {
	            Log.e(TAG, "停止本声音地播放失败！"+myPlayer.getLastError(m_iPort));
	            //return;
	        }
		 
		 //停止本地
		 if (myPlayer.stop(m_iPort)) {
	            Log.i(TAG, "停止本地播放成功！");
	        } else {
	            Log.e(TAG, "停止本地播放失败！"+myPlayer.getLastError(m_iPort));
	            //return;
	        }
		 

		// 关闭流
	        if (myPlayer.closeStream(m_iPort)) {
	            Log.i(TAG, "关闭视频流成功！");
	        } else {
	            Log.e(TAG, "关闭视频流失败！"+myPlayer.getLastError(m_iPort));
	            //return;
	        }
	        
	     // 释放播放端口
	        if (myPlayer.freePort(m_iPort)) {
	            Log.i(TAG, "释放播放端口成功！");
	        } else {
	            Log.e(TAG, "释放播放端口失败！"+myPlayer.getLastError(m_iPort));
	            //return;
	        }
	        
	    
	     // 播放端口复位
	     m_iPort = -1;
	     // 正在播放标记复位
	     setM_iPlayID(-1);
	    // freeSDK();
	     Log.i(TAG, "停止播放成功！");
	 }
	 
	    /**
	     * 释放海康SDK
	     */
	    public void freeSDK() {
	    	if(m_iSDKFlag=true)
		        if (videoCtr.NET_DVR_Cleanup()) {
		        	m_iSDKFlag = false;
		            Log.i(TAG, "释放SDK资源成功！");
		        } 
		        else 
		        {
		            Log.e(TAG, "释放SDK资源失败！");
		        }
	    }
	    
	    /**
	     * stream encode
	     * 
	     * @param iDataType
	     * @param pDataBuffer
	     * @param iDataSize
	     * @param iStreamMode
	     */
	//    List<ByteBuffer>  audioDataBufferList=new ArrayList<ByteBuffer>();
	//    ByteBuffer   audioFrameData=ByteBuffer.allocate(2048);
	    static int audioFrameCount=0;
	    static long timeStamp;
	    // int test_count=0;
	    private void processRealData(int iDataType, byte[] pDataBuffer, int iDataSize, int iStreamMode){
	    	try{
    			//	Log.i(TAGS, "SIZE"+iDataSize);
		    		switch(iDataType){
			    		case HCNetSDK.NET_DVR_SYSHEAD:           //      1			    			
			    			Log.i("head", "iDataSize"+iDataSize);
			    			MyLog.printf(TAG,"####################################NET_DVR_SYSHEAD");
			    			if (WSClientService.getInstance().client.getRemoteID()!=null)
			    			{	
			    				WSClientService.getInstance().client.sendHikCameraMsg("hik_head");				
			    				WSClientService.getInstance().client.sendHikCameraData(pDataBuffer,iDataSize);
			    			}			    			
			        		if (m_iPort==-1)  //获取播放库未使用的通道号
			        		{
			        			Log.i(TAG, "get port failed");
			        			break;
			        		}
			        		else	
			        			Log.i(TAG, "get port successed");	        		
			        		if (iDataSize > 0)
			        		{
			        			if (!myPlayer.setStreamOpenMode(m_iPort, iStreamMode))  //设置实时流播放模式
			        			{
			        				Log.e(TAG, "set Stream Open Mode Failed");
			        				break;
			        			}
			        			else
			        				Log.e(TAG, "set Stream Open Mode success"+myPlayer.getLastError(m_iPort));
		
			        			if (!myPlayer.openStream(m_iPort, pDataBuffer, iDataSize, 1024*1024)) //打开流接口
			        			{
			        				Log.e(TAG, "openStream Failed"+myPlayer.getLastError(m_iPort));
			        				break;
			        			}
			        			else
			        				Log.e(TAG, "openStream success");
		
			        			if (!myPlayer.play(m_iPort, holder)) //播放视频开始
			        			{
			        				Log.e(TAG, "play Failed"+myPlayer.getLastError(m_iPort));
			        				break;
			        			}
			        			else
			        				Log.e(TAG, "play success");
			        			
			        			if (!myPlayer.playSound(m_iPort))   //播放音频开始
			        			{
			        				Log.e(TAG, "play Sound Failed"+myPlayer.getLastError(m_iPort));
			        				break;
			        			}
			        			else
			        				Log.e(TAG, "play  SOUND  success"+myPlayer.getLastError(m_iPort));
			        		}
			        		else
			        		{
			        			Log.i(TAG, "Stream without data");
			        		}
			        	break;		        		
		    		case HCNetSDK.NET_DVR_STD_VIDEODATA:    //   4		    			
		    	//		Log.e(TAG, "video_data 4__data_size"+iDataSize);
		    			
//						MyLog.printf(TAG,"--------------video data frame start-----------------");    			
//		    			for(int i=0;i<10;i++)		
//							MyLog.printf(TAG,"videoHeadByte:%x",pDataBuffer[i]);	
//						MyLog.printf(TAG,"--------------video data frame end-----------------");
						
		    			if (WSClientService.getInstance().client.getRemoteID()!=null)
		    			{
		    				/*
		    				 * audioFrameCount++;
			    				if (audioFrameCount<5)
			    			*/
		    			//	test_count++;
		    			//	if (test_count<10)
		    				{	
		    				WSClientService.getInstance().client.sendHikCameraData(pDataBuffer,iDataSize); 
			         		WSClientService.getInstance().client.sendHikCameraMsg("hik_video_frame_end"/*String.format("hik_video_frame_end_%d", test_count)*/);		
				  //  		WSClientService.getInstance().client.sendHikCameraMsg("cuironngen"/*String.format("hik_video_frame_end_%d", test_count)*/);			    	
		    				}
			    	//		Thread.sleep(1);

		    			}
		    			else
		    			{  	    				 	
			        		 if (iDataSize > 0 && m_iPort != -1)
			        		{
			        			if (!myPlayer.inputData(m_iPort, pDataBuffer, iDataSize))
			        			{
			        				break;
			        			} 
			        		}
		    			}
		        		break;
		    		case 5:							// G722 codec
		   // 			Log.e(TAG, "5__"+iDataSize);
		    		case 8:     //   audioData      G711    codec
		    	//		Log.e(TAG, "8__audioData"+iDataSize);
			    //		WSClientService.getInstance().client.sendHikCameraMsg("crgwcy"/*String.format("hik_video_frame_end_%d", test_count)*/);			    	


		    		   // long lastTime = System.nanoTime();
		    	
		    			 /*long durationInMs =
		                 TimeUnit.NANOSECONDS.toMillis(timeStamp-tmp);*/
		   /* 			long tmp=timeStamp;
		    			timeStamp=System.nanoTime();
		    			 Log.e(TAG,"...................timeStamp="+TimeUnit.NANOSECONDS.toMillis(timeStamp));
		    	*/
		    			 //	MyLog.printf(TAG,"audio data  head:%08x",);
//						MyLog.printf(TAG,"--------------audio data frame start-----------------");    			
//		    			for(int i=0;i<10;i++)		
//							MyLog.printf(TAG,"sendByte:%x",pDataBuffer[i]);	
//						MyLog.printf(TAG,"--------------audio data frame end-----------------");
						
		    			/*
		    			 * Log.e(TAG, "8__"+iDataSize);
		    			if (WSClientService.getInstance().client.getRemoteID()!=null)
		    			{	
			    			audioFrameCount++;
			    			if (audioFrameCount<4)
			    			{	
			    		//		audioFrameData.clear();
			    				audioFrameData.put(pDataBuffer,0,iDataSize);		 
			    		//		audioFrameData.flip();
			    		//		audioDataBufferList.add(audioFrameData);		    				
			    		//		WSClientService.getInstance().client.sendHikAudioData(pDataBuffer,iDataSize);		    				
			    			}
			    			else
			    			{
			    				audioFrameData.put(pDataBuffer,0,iDataSize);
			    				audioFrameData.flip();
			    				MyLog.printf(TAG,"............audioFrameData.remaining=%d",audioFrameData.remaining());
			    				WSClientService.getInstance().client.sendHikAudioDataByteBuffer(audioFrameData);		    				
			    				WSClientService.getInstance().client.sendHikAudioMsgData("hik_audio_frame_end");    			 
			    				audioFrameData.clear();
			    				audioFrameCount=0;		    				
			    			}  			
		    			}
		    			*/
		    					    					    					    			
		    			if (WSClientService.getInstance().client.getRemoteID()!=null)
		    			{
		    				if (audio_play_flag==true)
		    				{	
		    		//			MyLog.printf(TAG,"...............send audioData  size=%d",iDataSize);		    					
		    					WSClientService.getInstance().client.sendHikCameraData(pDataBuffer,iDataSize);		    					
			    					
		    	/*				
		    					WSClientService.getInstance().client.sendHikAudioData(pDataBuffer,iDataSize);
		    					//MyLog.printf(TAG,"...............send msg:hik_audio_frame_end111");			   
		    					audioFrameCount++;
		    					if (audioFrameCount>5)
		    					{	
		    						audioFrameCount=0;
		    						WSClientService.getInstance().client.sendHikAudioMsgData("hik_audio_frame_end");    			 
		    						//MyLog.printf(TAG,"...............send msg:hik_audio_frame_end");	
		    					}
		    	*/		    					
		    				}	
		    			}			
		    			else
		    			{	
		    				if (audio_play_flag==true)
		    				{
				    			if (iDataSize > 0 && m_iPort != -1)
				        		{
				        			if (!myPlayer.inputData(m_iPort, pDataBuffer, iDataSize))
				        			{
				        				break;
				        			}			
				        		}
		    				}	
		    			}		    			
		    			
		    			break;
		    			
		    			default:
		    				break;
		    		}	    		
	    	} catch (Exception e) {
	            Log.e(TAG, "解码异常！" + e.toString());
	    	}	    	
	    }
	    
	    public int getM_iPlayID() {
	 //   	MyLog.printf(TAG,"---------------------PLAY ID=%d",m_iPlayID);
			return m_iPlayID;
		}

		private void setM_iPlayID(int m_iPlayID) {
			this.m_iPlayID = m_iPlayID;
		}
		
	    public int getM_iLogID() {
	  //  	MyLog.printf(TAG,"---------------------LogID =%d",m_iLogID);	    	
			return m_iLogID;
		}

//		private void setM_iLogID(int m_iLogID) {
//			this.m_iLogID = m_iLogID;
//		}
		
		

		public HCNetSDK getVideoCtr() {
			return videoCtr;
		}

		public void directionControlFunc(String msg)
		{
			if(msg.equals(MyRtcSip.HIK_CAMERA_LEFT_ING))
				getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,23,0);			
			else 	if(msg.equals(MyRtcSip.HIK_CAMERA_RIGHT_ING))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,24,0);
			else	if(msg.equals(MyRtcSip.HIK_CAMERA_UP_ING))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,21,0);	
			else	if(msg.equals(MyRtcSip.HIK_CAMERA_DOWN_ING))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,22,0);
			else	if(msg.equals(MyRtcSip.HIK_CAMERA_LEFT_END))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,23,1);
			else	if(msg.equals(MyRtcSip.HIK_CAMERA_RIGHT_END))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,24,1);
			else	if(msg.equals(MyRtcSip.HIK_CAMERA_DOWN_END))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,22,1);
			else	if(msg.equals(MyRtcSip.HIK_CAMERA_UP_END))
						getVideoCtr().NET_DVR_PTZControl_Other(HikView.getInstance().getM_iLogID(),1,21,1);
		}
		
		public void setVideoCtr(HCNetSDK videoCtr) {
			MyLog.printf(TAG,"---------------------setVideoCtr---------------------------------");
			this.videoCtr = videoCtr;
		}

		ExceptionCallBack mExceptionCallBack = new ExceptionCallBack() {
			@Override
			public void fExceptionCallBack(int arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				System.out.println("异常回掉函数运行！");
			}
		};
}
