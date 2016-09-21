package com.mywebrtc.util;

import java.io.IOException;

import com.RTC.TpadRTC.R;
import android.content.Context;
import android.media.MediaPlayer;

public class MyMediaPlayer {

	private static String TAG="MyMediaPlayer";
	private MediaPlayer mp=null;

	
	public  MyMediaPlayer(Context c)
	{

		mp=MediaPlayer.create(c,R.raw.ring_qq);
		if (mp!=null)
		{
			  /* ��MediaPlayer.OnErrorListener�����е�Listener */ 
			mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {				
				@Override
				 /*���Ǵ������¼�*/ 
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// TODO Auto-generated method stub
					 MyLog.printf(TAG,"MyMediaPlayer  on error............");
					 try 
			            {  
			              /*��������ʱҲ�����Դ��MediaPlayer�ĸ�ֵ*/ 
			              mp.release();  
			            }  
			            catch (Exception e)  
			            {  
			              e.printStackTrace();   
			            }  
					 return false;
				}
			});
		}	
	}
	
	public void startPlayThread()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try 
				{
					if (mp!=null)
					{
						mp.stop();
				        mp.setLooping(true);
						mp.prepare();
						mp.start();
					}	
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	public void stop()
	{
		if (mp!=null)
		{
			mp.stop();
		}	
	}
	public void release()
	{
		try
		{
			if (mp!=null)
			{	
				mp.stop();
				mp.release();
				mp=null;
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
