package com.tpad.hik;

import org.MediaPlayer.PlayM4.Player;

import android.view.SurfaceHolder;

import com.mywebrtc.util.MyLog;

public class PhoneHikUtil {
	public static String TAG="PhoneHikUtil";
	public   Player mphonePlayer/*,audioPlayer*/;
	private static PhoneHikUtil instance;
	int hik_port=-1/*,hik_audio_port=-1*/;
	 /**
	    *���ű�� falseδ���ţ�true���ڲ���
	 */
	 private boolean mHikPlayFlag =false;
	    
	 public boolean getMHikPlayFlag() {
		return mHikPlayFlag;
	 }

	 private void setMHikPlayFlag(boolean playFlag) {
		mHikPlayFlag = playFlag;
	}
	 
	 public static synchronized PhoneHikUtil getInstance()
	{
		if (instance==null)
			instance=new PhoneHikUtil();
		return instance;
	}	
	 private PhoneHikUtil()
	 {
		 mphonePlayer=Player.getInstance();
		// audioPlayer=Player.getInstance();
	 }
	 
	 public synchronized boolean mphonePlayerInit(byte[] playBuffer,int dataSize,SurfaceHolder holder)
	 {
		if(getMHikPlayFlag()==true)    return false;
		setMHikPlayFlag(true);
		hik_port=mphonePlayer.getPort();
		mphonePlayer.setStreamOpenMode(hik_port,1);
		mphonePlayer.openStream(hik_port,playBuffer,dataSize, playBuffer.length);
		mphonePlayer.play(hik_port,holder);	
		mphonePlayer.playSound(hik_port);	//play Audio				
		return true;
	 }
	 
/*
  	 public synchronized boolean audioPlayerInit(byte[] playBuffer,int dataSize,SurfaceHolder holder)
	 {		
		hik_audio_port=audioPlayer.getPort();
		audioPlayer.setStreamOpenMode(hik_audio_port, 1);
		audioPlayer.openStream(hik_audio_port, playBuffer,dataSize,playBuffer.length);
		audioPlayer.playSound(hik_audio_port);	
		return true;		 
	 }
*/
	 

	 public synchronized void mphonePlayerDeInit()
	 {		 
		 if(getMHikPlayFlag()==false)    return ;
		 
		 MyLog.printf(TAG,"...................mphonePlayerDeInit");
		 setMHikPlayFlag(false);

		 //ֹͣ����
   		 if (mphonePlayer.stopSound()) //   stop audio  
			 MyLog.printf(TAG,"ֹͣ�������ز��ųɹ���");
	     else MyLog.printf(TAG,"ֹͣ�������ز���ʧ�ܣ�error=%d",mphonePlayer.getLastError(hik_port));
 
		 //ֹͣ���ض˿�
		 if (mphonePlayer.stop(hik_port)) 
			 MyLog.printf(TAG,"ֹͣ���ض˿ڳɹ���");
	     else  MyLog.printf(TAG,"ֹͣ���ض˿�ʧ�ܣ�error=%d",mphonePlayer.getLastError(hik_port));
		 		 
		// �ر���
        if (mphonePlayer.closeStream(hik_port)) 
        	MyLog.printf(TAG,"�ر���Ƶ���ɹ���");
   	    else  MyLog.printf(TAG,"�ر���Ƶ��ʧ�ܣ�error=%d",mphonePlayer.getLastError(hik_port));
	        
	     // �ͷŲ��Ŷ˿�
        if (mphonePlayer.freePort(hik_port))
        	MyLog.printf(TAG,"�ͷŲ��Ŷ˿ڳɹ���"); 
        else 
        	MyLog.printf(TAG,"�ر���Ƶ��ʧ�ܣ�error=%d",mphonePlayer.getLastError(hik_port));	          
	     // ���Ŷ˿ڸ�λ
	     hik_port = -1;
	     // ���ڲ��ű�Ǹ�λ
	 }
	 
/*
 * 	 public synchronized void audioPlayerDeInit()
	 {
		 if (audioPlayer.stopSound()) //   stop audio  
			 MyLog.printf(TAG,"ֹͣ�������ز��ųɹ���");
	     else MyLog.printf(TAG,"ֹͣ�������ز���ʧ�ܣ�error=%d",audioPlayer.getLastError(hik_audio_port));

		 //ֹͣ���ض˿�
		 if (audioPlayer.stop(hik_audio_port)) 
			 MyLog.printf(TAG,"ֹͣ���ض˿ڳɹ���");
	     else  MyLog.printf(TAG,"ֹͣ���ض˿�ʧ�ܣ�error=%d",audioPlayer.getLastError(hik_audio_port));
		
		 
		 // �ر���
	     if (audioPlayer.closeStream(hik_audio_port)) 
	        	MyLog.printf(TAG,"�ر���Ƶ���ɹ���");
	   	 else  MyLog.printf(TAG,"�ر���Ƶ��ʧ�ܣ�error=%d",audioPlayer.getLastError(hik_audio_port));
		        
		 // �ͷŲ��Ŷ˿�
	     if (audioPlayer.freePort(hik_audio_port))
	        MyLog.printf(TAG,"�ͷŲ��Ŷ˿ڳɹ���"); 
	     else 
	        MyLog.printf(TAG,"�ر���Ƶ��ʧ�ܣ�error=%d",audioPlayer.getLastError(hik_audio_port));	          
		 // ���Ŷ˿ڸ�λ
		  hik_audio_port = -1;
		 // ���ڲ��ű�Ǹ�λ		 		  		 		 
	 }
*/
	 
	 public /*synchronized*/ void mphonePlayerInputData(byte[] hik_bytes,int byte_total)
	 {
		if(getMHikPlayFlag()==false)    return ;	 		
		 if (byte_total > 0 && hik_port != -1)
		 {	 
	 		boolean retFlag=mphonePlayer.inputData(hik_port, hik_bytes, byte_total);	 			
	// 		MyLog.printf(TAG,".................retFlag=%b",retFlag);
		 }	
	/*
		 int error=mphonePlayer.getLastError(hik_port);
	 	MyLog.printf(TAG,"ERROR %d,hik_port=%d",error,hik_port);		 	 
	*/
		 
	 }	 	 
	 
/*	 public   void  audioPlayerInputData(byte[] hik_bytes,int byte_total)
	 {
		 if (getMHikPlayFlag()==false)  return ;
		 if (byte_total>0&& hik_audio_port!=-1)
		 {
			 MyLog.printf(TAG,"inputData..............................");
			 audioPlayer.inputData(hik_audio_port,hik_bytes,byte_total);
		 }
		 else
			 MyLog.printf(TAG,"inputData.........failed");
			 
		 return ;
	 }*/
}
