package com.mywebrtc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


public class HttUtil {

	private static  String TAG="HttUtil";
	// ����HttpClient����   
	public static HttpClient httpClient = new DefaultHttpClient();

	/**
	 * 
	 * @param url
	 *            �����ַ
	 * @return ��������Ӧ���ַ���
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	
	public static String getRequest_0(final String url)
			throws InterruptedException, ExecutionException {
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {
					@Override
					public String call() throws Exception {
						// ����HttpGet����
						MyLog.printf(TAG,"0000000000000000");													
						HttpGet get = new HttpGet(url);
						// ����get����
						HttpResponse httpResponse = httpClient.execute(get);
						
						// ����������ɹ�������Ӧ
						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							// ��ȡ��������Ӧ���ַ���							
							HttpEntity   httpEntry = httpResponse.getEntity();							
							MyLog.printf(TAG,"111111111111111");													
				//			entityStr=EntityUtils.toString(httpEntry);
				//			MyLog.printf(TAG,"entityStr:"+entityStr);													
					//      �ص�entity��     add by BRUCE   2015.07.08
					//		SystemClock.sleep(300);
				   			 if (httpEntry!=null)
				   			 {	 
				   	// 			httpEntry.consumeContent();
					//			get.abort();
				   			 }
				   		//	httpClient.getConnectionManager().shutdown();
							MyLog.printf(TAG,"222222222222222");													
							return  EntityUtils.toString(httpResponse.getEntity());
						}
						return null;
					}
				});
		new Thread(task).start();
		return task.get();
	}
	
	private static HttpGet httpGet=null;

	private static String entityStr;	
	//private static  HttpGet httpGet=null; 
	
	public synchronized static String getRequest(final String url)
			throws InterruptedException, ExecutionException {
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {
					@Override
					public String call() throws Exception {
						// ����HttpGet����
						MyLog.printf(TAG,"getRequest000");
		//				SystemClock.sleep(500);  // todo: fix bug for task.get()
/*						if (httpGet!=null)
						{
							MyLog.printf(TAG,"httpGet!=====null");
							httpGet.abort();
							httpGet=null;
					//		return null;
						}
*/					
			//			SystemClock.sleep(50);  // todo: fix bug for task.get()
				//		Thread.sleep(100);
				//		MyLog.printf(TAG,"getRequest000111");		

						httpGet= new HttpGet(url);
						MyLog.printf(TAG,"getRequest000222");		
					//	SystemClock.sleep(50);
						// ����get����
						HttpResponse httpResponse = httpClient.execute(httpGet);
						MyLog.printf(TAG,"getRequest111");	
						
						// ����������ɹ�������Ӧ
						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							// ��ȡ��������Ӧ���ַ���							
							HttpEntity   httpEntry = httpResponse.getEntity();	
							entityStr=EntityUtils.toString(httpEntry);
							if (httpEntry!=null)
				   			 {	 
				   	 			httpEntry.consumeContent();			   	 		
				   			 }
							httpGet.abort();
			   	 			httpGet=null;
							MyLog.printf(TAG,"111111111111111");																								
							return  entityStr;
						}
						httpGet.abort();
		   	 			httpGet=null;
						MyLog.printf(TAG,"getRequest end");																								
						return null;
					}
				});
		new Thread(task).start();
		MyLog.printf(TAG,"start task get");	
		return task.get();
	}
	
	public static void loginByHttpClientGet(/*String name,String usrPass*/) throws FileNotFoundException
	{
		// HttpClient ������ GET��ʽ����  
	//	String uri = "http://192.168.1.105:88/mobile.html?username=admin" + "&userpass=123456" ;
		String uri = "http://192.168.1.105:88/mobile.html?username=admin&userpass=123456";
		// ����HttpGet����
		 MyLog.printf(TAG,"....................loginByHttpClientGet000");
		byte[] bytes=new byte[1024];
		HttpGet httpGet = new HttpGet(uri);  
		MyLog.printf(TAG,"....................loginByHttpClientGet111");		
		HttpResponse  response;
		int statusCode;
		 File file=new File(SendDataDemo.getSDPath()+"/cepsatest");
	     FileOutputStream fos=new FileOutputStream(file);
		// 3. ���õ�һ���д����õ�ʵ���� execute ������ִ�еڶ����д����õ� method ʵ�� 
	      
		try {
			 response=httpClient.execute(httpGet);
			 MyLog.printf(TAG,"....................loginByHttpClientGet222");			
			 statusCode= response.getStatusLine().getStatusCode();  
			 if (statusCode == 200) { //�������200 һ��ok  
				 MyLog.printf(TAG,"....................status==200");
				 HttpEntity entity = response.getEntity();// ����ʵ����� 
				 InputStream is = entity.getContent(); // ��ȡʵ��������
				 while (is.read(bytes)!=-1)
				 {
					 fos.write(bytes);
		//	     	 fos.flush();
				 }
				 is.close();
			 }
			 else
				 MyLog.printf(TAG,"....................status!=200");

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fos!=null)
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	 
	public static boolean loginServer(String username, String password)  
	{
	
	   boolean loginValidate = false;  
	   //ʹ��apache HTTP�ͻ���ʵ��   
       String urlStr ="http://192.168.1.105:88"; //"http://192.168.1.105:88/mobile.html";  
       HttpPost request = new HttpPost(urlStr);  
       //������ݲ�����Ļ������ԶԴ��ݵĲ������з�װ   
	    List<NameValuePair> params = new ArrayList<NameValuePair>();  
       //����û���������   
	     params.add(new BasicNameValuePair("username",username));  
	     params.add(new BasicNameValuePair("password",password));  
	     try  
         {  
           //�������������   
	        request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
      //      HttpClient client = getHttpClient();  
	     //ִ�����󷵻���Ӧ   
	       HttpResponse response = httpClient.execute(request);  
         //�ж��Ƿ�����ɹ�   
			if (response.getStatusLine().getStatusCode() == 200) {
				loginValidate = true;
				MyLog.printf(TAG,"........................loginServer==200");
				// �����Ӧ��Ϣ
				 String responseMsg = EntityUtils.toString(response.getEntity());
				 MyLog.printf(TAG,"CRG:%s",responseMsg);
			}
			else 
				MyLog.printf(TAG,"........................loginServer!=200=%d",response.getStatusLine().getStatusCode());							
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loginValidate;
	}	

}
