package com.mywebrtc.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.webrtc.DataChannel;
import org.webrtc.DataChannel.Buffer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

public class CepsaIPCameraUtil {
	private DrawCameraListenner scl;
	private static CepsaIPCameraUtil instance;
	private static String  TAG="CepsaIPCameraUtil";
	private static FileInputStream fis=null;
	
	private static  byte[] sendByte=new byte[512/*512*//*1024*/];
	
	private static  int capacity=1024;//1000;//2048/*640*480*4*/;
	private static  Buffer  bufferData=new DataChannel.Buffer(
		     ByteBuffer.allocate(capacity), true);
	
	private static  Buffer  audio_bufferData=new DataChannel.Buffer(
		     ByteBuffer.allocate(capacity), true);
	
	private static  Buffer  endBufferData=new DataChannel.Buffer(
		     ByteBuffer.wrap("end".getBytes(Charset.forName("UTF-8"))),false);
	private static  Buffer  audio_endBufferData=new DataChannel.Buffer(
		     ByteBuffer.wrap("end".getBytes(Charset.forName("UTF-8"))),false);
	private static  Buffer  directionMsgBuffer=new DataChannel.Buffer(
		     ByteBuffer.allocate(256/*64*/), false);
	
/*
 * 	private static  Buffer  msgDirectDataLeft=new DataChannel.Buffer(
			ByteBuffer.wrap("left".getBytes(Charset.forName("UTF-8"))),false);
	private static  Buffer  msgDirectDataRight=new DataChannel.Buffer(
			ByteBuffer.wrap("right".getBytes(Charset.forName("UTF-8"))),false);
	private static  Buffer  msgDirectDataUp=new DataChannel.Buffer(
			ByteBuffer.wrap("up".getBytes(Charset.forName("UTF-8"))),false);
	private static  Buffer  msgDirectDataDown=new DataChannel.Buffer(
			ByteBuffer.wrap("down".getBytes(Charset.forName("UTF-8"))),false);
*/
	
	public interface   DrawCameraListenner
	{
		void onDraw(Bitmap bm);
	}
	
	public CepsaIPCameraUtil(DrawCameraListenner scl)
	{
		this.scl=scl;
		cepsaHttpParamInit();		
		instance=this;	
	}
	
	public CepsaIPCameraUtil(boolean testFlag)
	{
		if (testFlag)
		{	
			HttpConnectionParams.setConnectionTimeout(httpparams, 1000 * timeout);
			HttpConnectionParams.setSoTimeout(httpparams, 1000 * timeout);
			HttpConnectionParams.setSocketBufferSize(httpparams, 8192);			
		}
	}
	
	public static CepsaIPCameraUtil getInstance()
	{
		return instance;
	}
	
	@SuppressLint("WrongCall")
	public void showCameraJPG(ByteBuffer bb)
	{
		if (instance!=null)
		{	
/*
 * 			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/			
			int size=bb.remaining();
			byte[] myBytes=new byte[size];
			if (bb.hasRemaining())
			{
				bb.get(myBytes, 0,myBytes.length);
				Bitmap bm = BitmapFactory.decodeByteArray(myBytes, 0, myBytes.length);
				if (bm!=null&&scl!=null)
					scl.onDraw(bm);
			}
		}
		else
			MyLog.printf(TAG,"showCameraJPG instance==null");
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
	
/*	public static  DataChannel.Buffer getPictureData(DataChannel dc,int index)
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
					boolean ret=dc.send(bufferData);
				
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
	}*/
	 private static String strURL = "http://" + "192.168.100.5" + ":" +"80"+"/tmpfs/auto.jpg";
	 private static HttpParams httpparams = new BasicHttpParams();
	 private  static HttpGet httpGet ; //= new HttpGet();
	 private  int timeout = 40;
	 private  HttpResponse httpResponse = null;
//	 private  CloseableHttpResponse closeableHttpResponse = null;
	 private  DefaultHttpClient httpClient = null;
	 private static HashMap<String, String> myHashMap = new HashMap<String, String>();;
	 private  HttpEntity httpEntry = null;
	 private  Bitmap bitMap = null;
	 private  String  usrName="admin";	
	 private  String  usrPWD="123456";
	 private  InputStream localInputStream;

	 private   HashMap<String,String> headerHashMap = new HashMap<String,String>();
	 
	  public  HashMap<String, String> getHttpReponseHead(HttpResponse paramHttpResponse)
	  {
	    org.apache.http.Header[] arrayOfHeader = paramHttpResponse.getAllHeaders();
	    for (int i = 0;i<arrayOfHeader.length; i++)
	    {
	   //   if (i >= arrayOfHeader.length)
	    	headerHashMap.put(arrayOfHeader[i].getName().toLowerCase(), arrayOfHeader[i].getValue());
	    }
	    return headerHashMap;
	  } 
	  
	 public void cepsaHttpParamInit()
	 {
	    HttpConnectionParams.setConnectionTimeout(httpparams, 1000 * timeout);
	    HttpConnectionParams.setSoTimeout(httpparams, 1000 * timeout);
	    HttpConnectionParams.setSocketBufferSize(httpparams, 8192);		 
      //  shouldGetJPGflag=true;
	    setShouldGetJPGflag(true);
	 }
	 public static boolean shouldGetJPGflag=false;
	 public  boolean getShouldGetJPGflag()
	 {
		 return shouldGetJPGflag;
	 }
	 public void setShouldGetJPGflag(boolean flag)
	 {
		 shouldGetJPGflag=flag;
	 }	 
	 
	 
	 public void getCEPSAjpgThread()
	 {
		 new Thread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try 
				{
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			
				while(shouldGetJPGflag)
				{
					processLoginGetCEPSAjpg();					
					try 
					{
						Thread.sleep(250/*150*/);
					}
					catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}
		}).start();		 
		return ;
	 }
	  
	 @SuppressLint("WrongCall")
	public  void processLoginGetCEPSAjpg()
	{
		 MyLog.printf(TAG,"processLoginGetCEPSAjpg000");
		 httpGet = new HttpGet();
   //     httpClient= new DefaultHttpClient(httpparams);
    //    CloseableHttpClient
        try {
        	httpGet.setURI(new URI(strURL));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}      		 
		 httpClient= new DefaultHttpClient(httpparams);
        if (httpClient==null)   return ;
        
  //      httpClient.getConnectionManager().closeIdleConnections(idletime, tunit)//(30L, TimeUnit.SECONDS);       
  //      this.httpClient_.setRedirectHandler(new ipcameraActivity.DummyRedirectHandler(ipcameraActivity.this));
       
        myHashMap.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        String auth;
        String usrInfoTmp=usrName+":"+usrPWD;    
        
		try {
			auth = com.mywebrtc.util.Base64.encode(usrInfoTmp.getBytes("UTF-8"));
			myHashMap.put("Authorization", "Basic "+auth);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//myBase64Test("admin:123456");
        Iterator localIterator = myHashMap.entrySet().iterator();
    //    if (!localIterator.hasNext());             
    	while (localIterator.hasNext())
		{
    		Map.Entry localEntry = (Map.Entry)localIterator.next();
    		httpGet.addHeader((String)localEntry.getKey(), (String)localEntry.getValue());
		}
		 try 
		 {	
			 MyLog.printf(TAG,"processLoginGetCEPSAjpg111");

			httpResponse = httpClient.execute(httpGet);
			 MyLog.printf(TAG,"processLoginGetCEPSAjpg222");

			 if ((httpResponse.getStatusLine().getStatusCode() == 200))
				{
				 MyLog.printf(TAG,"processLoginGetCEPSAjpg333");

				   httpEntry = httpResponse.getEntity();
		    	   HashMap<String,String> localHashMap = getHttpReponseHead(httpResponse);
		    	   String str  = ((String)localHashMap.get("content-type"));

		    	    if ("image/jpeg".equalsIgnoreCase(str))
		    	    {	
						try {
							localInputStream = httpEntry.getContent();
										
							WSClientService.getInstance().client.sendCepsaCameraData(localInputStream);

						/*
						 * 	bitMap = BitmapFactory.decodeStream(localInputStream);						
							if(bitMap!=null&&scl!=null)
								scl.onDraw(bitMap);		
						*/
							
							localInputStream.close();										
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		     //       localObject2 = localInputStream;
		    	    }
		/*        	    else
		        	    {
		        	    	MyLog.printf(TAG,"77777777777777");
		        	    	byte[] mybyte=new byte[1024];
			        	    if (str!=null)
			        	    	MyLog.printf(TAG,"..............STR=%s",str);	  
			        	//    InputStream localInputStream;
							try {
								localInputStream = httpEntry.getContent();						
					        //    bitmap_ = BitmapFactory.decodeStream(localInputStream);
						//		MyLog.printf(TAG,"entity:%s",httpEntry_.getContent());
								File file=new File(SendDataDemo.getSDPath()+"/mytest.jpg");
								OutputStream os=new FileOutputStream(file);
								int lenth;
								while((lenth=localInputStream.read(mybyte))>0)
								{
									MyLog.printf(TAG,"8888888888lenth=%d",lenth);
									os.write(mybyte,0,lenth);
									os.flush();
								}
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        	    }*/	
				}
			 	else
					MyLog.printf(TAG,"555555555555=%d",httpResponse.getStatusLine().getStatusCode());			
				
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally
		 {
			 //关掉entity流
			 if (httpEntry!=null)
				try {
					httpEntry.consumeContent();
					httpGet.abort();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }

	// 	httpClient.getConnectionManager().shutdown();//.closeIdleConnections(0, TimeUnit.SECONDS);
		
	 	//	httpClient.clearResponseInterceptors();
	 
      }
	 

	 
	 
	 
	 
	 public void localGetCEPSAjpgThread()
	 {
		 new Thread(new Runnable() {			
			@Override
			public void run() {
				// TODO Auto-generated method stub
/*				try 
				{
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
*/			
				while(shouldGetJPGflag)
				{
					localProcessLoginGetCEPSAjpg();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}
		}).start();
		 
		 return ;
	 }
	  
	 @SuppressLint("WrongCall")
	public  void localProcessLoginGetCEPSAjpg()
	{
	//	 MyLog.printf(TAG,"processLoginGetCEPSAjpg000");
		 httpGet = new HttpGet();
   //     httpClient= new DefaultHttpClient(httpparams);
    //    CloseableHttpClient
        try {
        	httpGet.setURI(new URI(strURL));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}      		 
		 httpClient= new DefaultHttpClient(httpparams);
        if (httpClient==null)   return ;
        
        myHashMap.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        String auth;
        String usrInfoTmp=usrName+":"+usrPWD;    
        
		try {
			auth = com.mywebrtc.util.Base64.encode(usrInfoTmp.getBytes("UTF-8"));
			myHashMap.put("Authorization", "Basic "+auth);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//myBase64Test("admin:123456");
        Iterator localIterator = myHashMap.entrySet().iterator();
    //    if (!localIterator.hasNext());             
    	while (localIterator.hasNext())
		{
    		Map.Entry localEntry = (Map.Entry)localIterator.next();
    		httpGet.addHeader((String)localEntry.getKey(), (String)localEntry.getValue());
		}
		 try 
		 {	
	//		 MyLog.printf(TAG,"processLoginGetCEPSAjpg111");
			httpResponse = httpClient.execute(httpGet);
	//		 MyLog.printf(TAG,"processLoginGetCEPSAjpg222");

			 if ((httpResponse.getStatusLine().getStatusCode() == 200))
				{
	//			 MyLog.printf(TAG,"processLoginGetCEPSAjpg333");

				   httpEntry = httpResponse.getEntity();
		    	   HashMap<String,String> localHashMap = getHttpReponseHead(httpResponse);
		    	   String str  = ((String)localHashMap.get("content-type"));

		    	    if ("image/jpeg".equalsIgnoreCase(str))
		    	    {	
						try {
							localInputStream = httpEntry.getContent();
							
			//				WSClientService.getInstance().client.sendCepsaCameraData(localInputStream);
							
							bitMap = BitmapFactory.decodeStream(localInputStream);						
							if(bitMap!=null&&scl!=null)
								scl.onDraw(bitMap);			
							localInputStream.close();										
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		     //       localObject2 = localInputStream;
		    	    }
				}
			 	else
					MyLog.printf(TAG,"555555555555=%d",httpResponse.getStatusLine().getStatusCode());			
				
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally
		 {
			 //关掉entity流
			 if (httpEntry!=null)
				try {
					httpEntry.consumeContent();
					httpGet.abort();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }

	// 	httpClient.getConnectionManager().shutdown();//.closeIdleConnections(0, TimeUnit.SECONDS);
		
	 	//	httpClient.clearResponseInterceptors();	 
      }	 	 	 
	 
	 
	 

//	private static String hostURL = "http://" + "192.168.1.105" + ":" +"88"; 
	 private static String hostURL = "http://" + "192.168.100.5" + ":" +"80"; 
	public void sendPtzThread(String msg)
	{
		new Thread(new Runnable() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sendPtzDirectMsg("left");
			}
		}).start();
	}
	
	public  void sendPtzDirectMsg(String msg)
	{
		 MyLog.printf(TAG,"sendPtlDirectMsg000");
		 httpGet = new HttpGet();
   //     httpClient= new DefaultHttpClient(httpparams);
    //    CloseableHttpClient
        try {
        	httpGet.setURI(new URI(hostURL+"/web/cgi-bin/hi3510/yt"+msg+".cgi"));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}      		 
		 MyLog.printf(TAG,"sendPtlDirectMsg111");

		 httpClient= new DefaultHttpClient(httpparams);
        if (httpClient==null)   return ;
        
  //      httpClient.getConnectionManager().closeIdleConnections(idletime, tunit)//(30L, TimeUnit.SECONDS);       
  //      this.httpClient_.setRedirectHandler(new ipcameraActivity.DummyRedirectHandler(ipcameraActivity.this));
       
        myHashMap.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        String auth;
        String usrInfoTmp=usrName+":"+usrPWD;    
        
		try {
			auth = com.mywebrtc.util.Base64.encode(usrInfoTmp.getBytes("UTF-8"));
			myHashMap.put("Authorization", "Basic "+auth);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//myBase64Test("admin:123456");
        Iterator localIterator = myHashMap.entrySet().iterator();
    //    if (!localIterator.hasNext());             
    	while (localIterator.hasNext())
		{
    		Map.Entry localEntry = (Map.Entry)localIterator.next();
    		httpGet.addHeader((String)localEntry.getKey(), (String)localEntry.getValue());
		}
		 try 
		 {	
			 MyLog.printf(TAG,"sendPtlDirectMsg222");

			httpResponse = httpClient.execute(httpGet);
			 MyLog.printf(TAG,"sendPtlDirectMsg333");
			
			 if ((httpResponse.getStatusLine().getStatusCode() == 200))
				{
				 MyLog.printf(TAG,"sendPtlDirectMsg333getStatusCode() == 200");
				   httpEntry = httpResponse.getEntity();
		    	   HashMap<String,String> localHashMap = getHttpReponseHead(httpResponse);
		    	   String str  = ((String)localHashMap.get("content-type"));
				}
			 	else
			 	{
					 MyLog.printf(TAG,"sendPtlDirectMsg333getStatusCode() !!!!= 200");
					MyLog.printf(TAG,"555555555555=%d",httpResponse.getStatusLine().getStatusCode());			
			 	}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			 MyLog.printf(TAG,"sendPtlDirectMsg  ClientProtocolException");
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			 MyLog.printf(TAG,"sendPtlDirectMsg  IOException");			
			e.printStackTrace();
		}
		 finally
		 {
			 //关掉entity流
			 MyLog.printf(TAG,"sendPtlDirectMsg444");

			 if (httpEntry!=null)
				try {
					httpEntry.consumeContent();
					httpGet.abort();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }

	// 	httpClient.getConnectionManager().shutdown();//.closeIdleConnections(0, TimeUnit.SECONDS);
		
	 	//	httpClient.clearResponseInterceptors();
	 
      }	 
	
	public   boolean testIpCameraOnline()
	{
		
		 MyLog.printf(TAG,"testIpCameraOnline");
		 httpGet = new HttpGet();
		 boolean ret=false;
       try {
       	httpGet.setURI(new URI(hostURL));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}      		 
		 MyLog.printf(TAG,"testIpCameraOnline");
		 httpClient= new DefaultHttpClient(httpparams);
       if (httpClient==null)
       { 
			httpGet.abort();
    	   return false;
       }
       myHashMap.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
       String auth;
       String usrInfoTmp=usrName+":"+usrPWD;    
       
		try {
			auth = com.mywebrtc.util.Base64.encode(usrInfoTmp.getBytes("UTF-8"));
			myHashMap.put("Authorization", "Basic "+auth);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       Iterator localIterator = myHashMap.entrySet().iterator();
   //    if (!localIterator.hasNext());             
   	while (localIterator.hasNext())
		{
   			Map.Entry localEntry = (Map.Entry)localIterator.next();
   			httpGet.addHeader((String)localEntry.getKey(), (String)localEntry.getValue());
		}
		 try 
		 {	
			 MyLog.printf(TAG,"testIpCameraOnline222");
			httpResponse = httpClient.execute(httpGet);
			 MyLog.printf(TAG,"testIpCameraOnline333");
			
			 if ((httpResponse.getStatusLine().getStatusCode() == 200))
				{
				 	MyLog.printf(TAG,"testIpCameraOnline333getStatusCode() == 200");
				 	ret=true;
/*				   httpEntry = httpResponse.getEntity();
		    	   HashMap<String,String> localHashMap = getHttpReponseHead(httpResponse);
		    	   String str  = ((String)localHashMap.get("content-type"));*/
				}
			 	else
			 	{
					MyLog.printf(TAG,"testIpCameraOnline333getStatusCode() !!!!= 200");
					ret=false;
			 	}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			 MyLog.printf(TAG,"testIpCameraOnline  ClientProtocolException");
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			 MyLog.printf(TAG,"testIpCameraOnline  IOException");			
			e.printStackTrace();
		}
		 finally
		 {
			 //关掉entity流
			 MyLog.printf(TAG,"testIpCameraOnline444");
			httpGet.abort();
		 }		
		return ret;
	}
	
	public  class sendPtzMessageThread extends Thread
	{
		private String directMsg=null;
		public sendPtzMessageThread(String msg) {
			// TODO Auto-generated constructor stub
			this.directMsg=msg;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
		//	super.run();
			if (directMsg!=null)
				sendPtzDirectMsg(directMsg);
		}
	}
	 
	public boolean sendRemoteDirectMessage(DataChannel dc,String msg)
	{
		boolean ret=false;
		if (msg.equals(MyRtcSip.IP_CAMERA_LEFT)||msg.equals(MyRtcSip.IP_CAMERA_RIGHT)
				||msg.equals(MyRtcSip.IP_CAMERA_UP)||msg.equals(MyRtcSip.IP_CAMERA_DOWN))
		{
			directionMsgBuffer.data.clear();
			directionMsgBuffer.data.put(msg.getBytes(Charset.forName("UTF-8")));
			directionMsgBuffer.data.flip();
			ret=dc.send(directionMsgBuffer);			
		}	
		return ret;
	}
	
	public boolean sendDataChannelBytes(InputStream is,DataChannel dc)
	{
		int len=0,pos,limit;
		InputStream localInputStream=is;
		try {
			while((len=localInputStream.read(sendByte,0,sendByte.length))>0)
			{
				MyLog.printf(TAG, "read size=%d",len);
			//	bufferData.data.rewind();
				bufferData.data.clear();
								
	/*			
				pos=bufferData.data.position();
				limit=bufferData.data.limit();
				MyLog.printf(TAG, "read bufferData.data   pos=%d ,limit=%d",pos,limit);
	*/							
				bufferData.data.put(sendByte,0,len);//put(sendByte, 0, len);//put(sendByte);
				bufferData.data.flip();
	//			MyLog.printf(TAG, "bufferData.data position=%d,bufferData.data.limit=%d",bufferData.data.position(),bufferData.data.limit());
		/*
		 * 		for(int i=0;i<20;i++)		
					MyLog.printf(TAG,"sendByte:%x",sendByte[i]);	
				for(int i=0;i<20;i++)		
					MyLog.printf(TAG,"bufferData.data:%x",bufferData.data.get(i));	
		*/					
			
				boolean ret=dc.send(bufferData);
			}
			//MyLog.printf(TAG, "end read size=%d",len);

		//	localInputStream.close();
			localInputStream=null;
			
			boolean ret2=dc.send(endBufferData);
	//		MyLog.printf(TAG,"send2 ret2=%b",ret2);	
			endBufferData.data.flip();	
	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return false;
	}
	
	public static boolean sendHikCameraMessage(DataChannel dc,String msg)
	{
		boolean ret=false;
		directionMsgBuffer.data.clear();
		directionMsgBuffer.data.put(msg.getBytes(Charset.forName("UTF-8")));
		directionMsgBuffer.data.flip();
		ret=dc.send(directionMsgBuffer);					
		return ret;
	}	
	
	
	
	public synchronized static boolean sendHikDataChannelBytes(byte[] bts,int btsTotal,DataChannel dc)
	{		
		int len=0,pos=0,limit,total=0;
		total=btsTotal/*bts.length*/;			
		while(/*(len=bts.read(sendByte,0,sendByte.length))*/total>0)
		{						
	//		MyLog.printf(TAG, "read size  total=%d, pos=%d",total,pos);
		//	bufferData.data.rewind();
			bufferData.data.clear();				
/*			
			pos=bufferData.data.position();
			limit=bufferData.data.limit();
			MyLog.printf(TAG, "read bufferData.data   pos=%d ,limit=%d",pos,limit);
*/							
		//	bufferData.data.put(sendByte,0,len);//put(sendByte, 0, len);//put(sendByte);
			if (total>sendByte.length)
			{	
				bufferData.data.put(bts,pos,sendByte.length);
				pos=pos+sendByte.length;
				total=total-sendByte.length;
			}	
			else
			{
				bufferData.data.put(bts,pos,total);		
				pos=pos+total;
				total=0;
			}				
			bufferData.data.flip();	
/*
 * 	  		for(int i=0;i<20;i++)		
				MyLog.printf(TAG,"sendByte:%x",sendByte[i]);	
			for(int i=0;i<20;i++)		
				MyLog.printf(TAG,"bufferData.data:%x",bufferData.data.get(i));
*/							
	//		MyLog.printf(TAG,".............................bufferdAmount=%d",dc.bufferedAmount());
			boolean ret=dc.send(bufferData);							
		}
//		MyLog.printf(TAG, "end read size=%d",len);
/*		if (dc!=null)
			dc.send(endBufferData);
//		MyLog.printf(TAG,"send2 ret2=%b",ret2);	
		endBufferData.data.flip();	
*/			
		return false;
	}		
	
	public synchronized static boolean sendHikAudioBytes(byte[] bts,int btsTotal,DataChannel dc)
	{		
		int len=0,pos=0,limit,total=0;
		total=btsTotal/*bts.length*/;			
		while(/*(len=bts.read(sendByte,0,sendByte.length))*/total>0)
		{						
//			MyLog.printf(TAG, "read size  total=%d, pos=%d",total,pos);
		//	bufferData.data.rewind();
			audio_bufferData.data.clear();				
/*			
			pos=bufferData.data.position();
			limit=bufferData.data.limit();
			MyLog.printf(TAG, "read bufferData.data   pos=%d ,limit=%d",pos,limit);
*/							
		//	bufferData.data.put(sendByte,0,len);//put(sendByte, 0, len);//put(sendByte);
			if (total>sendByte.length)
			{	
				audio_bufferData.data.put(bts,pos,sendByte.length);
				pos=pos+sendByte.length;
				total=total-sendByte.length;
			}	
			else
			{
				audio_bufferData.data.put(bts,pos,total);		
				pos=pos+total;
				total=0;
			}	
			audio_bufferData.data.flip();
	
/*
  	  		for(int i=0;i<20;i++)		
				MyLog.printf(TAG,"sendByte:%x",sendByte[i]);	
			for(int i=0;i<20;i++)		
				MyLog.printf(TAG,"bufferData.data:%x",bufferData.data.get(i));
*/							
//			MyLog.printf(TAG, "..................sendHikDataChannelBytes000");
			boolean ret;
			if (audio_bufferData!=null)
			{	
				if (dc==null)
					MyLog.printf(TAG, "..................dataChannel is null 000");
				else
					ret=dc.send(audio_bufferData);		
			}	
			else
				MyLog.printf(TAG,"................audio_bufferDta............is null");			
//			MyLog.printf(TAG, "..................sendHikDataChannelBytes111");	

		}
	//	MyLog.printf(TAG, "end read size=%d",len);
		if (dc!=null)
		/*boolean ret2=*/dc.send(audio_endBufferData);
//		MyLog.printf(TAG,"send2 ret2=%b",ret2);	
		audio_endBufferData.data.flip();				
		return false;
	}	
}
