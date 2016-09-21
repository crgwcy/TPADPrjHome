package com.mywebrtc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.webrtc.DataChannel;
import org.webrtc.DataChannel.Buffer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class SendDataDemo {

	private static FileInputStream fis=null;
	private static  byte[] sendByte=new byte[1024];
	private static  int lenth;
	private static  int capacity=2048/*640*480*4*/;
	private static  Buffer  bufferData=new DataChannel.Buffer(
		     ByteBuffer.allocate(capacity), true);
	private static  Buffer  endBufferData=new DataChannel.Buffer(
		     ByteBuffer.wrap("end".getBytes(Charset.forName("UTF-8"))),false);
	private static String TAG="SendDataDemo";
	
	
	public interface ShowCameraListenner
	{
		void onDraw(Bitmap bm);
	}	
	
	public  ShowCameraListenner  scl=null;
	private static SendDataDemo sdd=null;
	
	public SendDataDemo(ShowCameraListenner scl)
	{
		this.scl=scl;
		sdd=this;	
	}
	
	public static SendDataDemo getInstance()
	{
		return sdd;
	}
	
	public static  DataChannel.Buffer getPictureData(DataChannel dc,int index)
	{
		File file=new File(getSDPath()+"/test/"+"test"+index+".jpg");			
		MyLog.printf(TAG, "getPictureData 000");			
		try {
			if (fis==null)
			{
				MyLog.printf(TAG, "getPictureData 111");		
				fis=new FileInputStream(file);
			
			}
			if (fis!=null)
			{	
				MyLog.printf(TAG, "getPictureData 222");					
				while ((lenth=fis.read(sendByte,0,sendByte.length))>0)
				{
		//			MyLog.printf(TAG, "read size=%d",lenth);
					bufferData.data.rewind();
		//			MyLog.printf(TAG, "hasRemaining=%b",bufferData.data.hasRemaining());
					bufferData.data.put(sendByte);
					bufferData.data.flip();
		//			MyLog.printf(TAG, "bufferData.data position=%d,bufferData.data.limit=%d",bufferData.data.position(),bufferData.data.limit());
			/*		for(int i=0;i<20;i++)		
						MyLog.printf(TAG,"sendByte:%x",sendByte[i]);	
					for(int i=0;i<20;i++)		
						MyLog.printf(TAG,"bufferData.data:%x",bufferData.data.get(i));	
						*/					
					boolean ret=dc.send(bufferData);
		//			MyLog.printf(TAG,"send1 ret=%b",ret);					
	/*
	 * 				ret=dc.send(bufferData);
					MyLog.printf(TAG,"send2 ret=%b",ret);
	*/
				//	return sendByte;				
				}
		//		MyLog.printf(TAG, "getPictureData read=0");								
				fis.close();
				fis=null;
				boolean ret2=dc.send(endBufferData);
		//		MyLog.printf(TAG,"send2 ret2=%b",ret2);	
				endBufferData.data.flip();				
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return bufferData;
	}
	
	@SuppressLint("WrongCall")
	public void showCameraJPG(ByteBuffer bb)
	{
		if (sdd!=null)
		{	
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MyLog.printf(TAG,"showCameraJPG sdd!=null");
			int size=bb.remaining();
			byte[] myBytes=new byte[size];
			if (bb.hasRemaining())
			{
				MyLog.printf(TAG,"showCameraJPG sdd 000");		
				bb.get(myBytes, 0,myBytes.length);
				MyLog.printf(TAG,"showCameraJPG sdd 111");		

				Bitmap bm = BitmapFactory.decodeByteArray(myBytes, 0, myBytes.length);
				if (scl!=null)
				{
					if (bm!=null)
					{	
						MyLog.printf(TAG,"showCameraJPG sdd222");		
						scl.onDraw(bm);
					}
					else
						MyLog.printf(TAG,"showCameraJPG bm==null");	
				} 
			}
		}
		else
			MyLog.printf(TAG,"showCameraJPG sdd==null");
			return ;
	}
	
	
	public static  void savePicDataAsJPGtest(ByteBuffer bb,int index)
	{
		int offset=0;
		
		try {
			MyLog.printf(TAG, "savePicDataAsJPGtest 000");			
			FileOutputStream fos=new FileOutputStream(new File(getSDPath()+"/mytest/"+"test"+index+".jpg"));
			int size=bb.remaining();
			byte[] bytes=new byte[1024];
			
/*			byte[] myBytes=new byte[size];
			
			if (bb.hasRemaining())
			{
				bb.get(myBytes, 0,bytes.length);
				Bitmap bm = BitmapFactory.decodeByteArray(myBytes, 0, myBytes.length);
				scl.onDraw(, bm)
			}
*/
			
			
						

			while(bb.hasRemaining())
			{
				MyLog.printf(TAG, "savePicDataAsJPGtest 111  bb.remaining()=%d",bb.remaining());			
			/*	for(int i=0;i<5;i++)
					MyLog.printf(TAG,"savePicDataAsJPGtest buffer.dataget[%d]:%x",i,bb.get(i));*/
				MyLog.printf(TAG, "savePicDataAsJPGtest 111  bb.position=%d",bb.position());			
				
				
				if (bb.remaining()>bytes.length)
				{	
			/*		bb.get(bytes, offset, 1024);
					offset=offset+1024;
			*/			
					bb.get(bytes, 0,bytes.length);
			//		bb.get(bytes);					
					for(int i=0;i<5;i++)
						MyLog.printf(TAG,"savePicDataAsJPGtest222 bytes[%d]:%x",i,bytes[i]);
					
					try {
/*
 * 						if (offset==1)
						{	
							MyLog.printf(TAG,"offset111111");
							offset++;
							fos.write(bytes,0,1024);
							
						}
						else
						{
							MyLog.printf(TAG,"offset000000");
							offset++;
						}
*/
						fos.write(bytes,0,bytes.length);					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						MyLog.printf(TAG,"222222222");
						e.printStackTrace();
					}
				}	
				else
				{	
					bb.get(bytes,0, bb.remaining()/*size*/);
					try {
						fos.write(bytes, 0,bb.remaining());
					//	fos.write(bytes);
					} catch (IOException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			
			MyLog.printf(TAG, "savePicDataAsJPGtest 222");				
			try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ;
	}
		
	public static String getSDPath()
	{
		File sdDir=null;
		boolean sdCardExist=Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (sdCardExist)
		{	
			sdDir=Environment.getExternalStorageDirectory();  // get root path
			return sdDir.toString();
		}
		return  null;
	}
}
