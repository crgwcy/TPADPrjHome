package com.tpad.activitys;

import java.util.List;

import com.RTC.TpadRTC.R;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TestMjpegActivity extends Activity implements SurfaceHolder.Callback{

	 private static final String TAG="TestMjpegActivity";
	 private SurfaceView mSurfaceview = null;  // SurfaceView����(��ͼ���)��Ƶ��ʾ
	 private SurfaceHolder mSurfaceHolder = null;  // SurfaceHolder����(����ӿ�)SurfaceView֧����
	 private Camera mCamera =null;     // Camera�������Ԥ��
	 private boolean bIfPreview=false;
	 private int mPreviewWidth,mPreviewHeight;
	 private int YUVIMGLEN;
	 private byte[]  mYUV420SPSendBuffer;
/*	 private PreviewCallback mJpegPreviewCallback=new Camera.PreviewCallback()
	  {
		   @Override
		   public void onPreviewFrame(byte[] data, Camera camera) 
		   {
		    //���ݽ�����data,Ĭ����YUV420SP��
		// TODO Auto-generated method stub    
		    try
		    {
		     Log.i(TAG, "going into onPreviewFrame");
		     //mYUV420sp = data;   // ��ȡԭ����YUV420SP����
		     YUVIMGLEN = data.length;
		     
		     // ����ԭ��yuv420sp����
		     mYuvBufferlock.acquire();
		     System.arraycopy(data, 0, mYUV420SPSendBuffer, 0, data.length);
		     //System.arraycopy(data, 0, mWrtieBuffer, 0, data.length);
		     mYuvBufferlock.release();
		     
		     // ���������̣߳��翪��PEG���뷽ʽ�߳�
		     mSendThread1.start();		     
		    } catch (Exception e)
		    {
		     Log.v("System.out", e.toString());
		    }// endtry    
		   }// endonPriview  
		  };*/	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_mjpeg);
		initSurfaceView();
		
	}
	 private void initSurfaceView()
	 {
	  mSurfaceview = (SurfaceView) this.findViewById(R.id.test_mjpeg_sfv_id);
	  mSurfaceHolder = mSurfaceview.getHolder(); // ��SurfaceView��ȡ��SurfaceHolder����
	  mSurfaceHolder.addCallback(this); // SurfaceHolder����ص��ӿ�
	  // mSurfaceHolder.setFixedSize(176, 144); // Ԥ����С�O��
	  mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// �O���@ʾ����ͣ�setType��������	  	  
	 }
	 /*��SurfaceHolder.Callback �ص�������*/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		 // SurfaceView����ʱ/����ʵ������Ԥ�����汻����ʱ���÷��������á�
		// TODO Auto-generated method stub
		 mCamera = Camera.open();// ��������ͷ��2.3�汾��֧�ֶ�����ͷ,�贫������� 
		 try
		  {   
		    Log.e(TAG, "SurfaceHolder.Callback��surface Created");
		    mCamera.setPreviewDisplay(mSurfaceHolder);//set the surface to be used for live preview
		  } 
		 catch (Exception ex)
		  {
		   if(null != mCamera)
		   {
		    mCamera.release();
		    mCamera = null;     
		   }
		   Log.e(TAG+"initCamera", ex.getMessage());
		  }
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		 // ��SurfaceView/Ԥ������ĸ�ʽ�ʹ�С�����ı�ʱ���÷���������
		 Log.e(TAG, "SurfaceHolder.Callback��Surface Changed");
		  //mPreviewHeight = height;
		  //mPreviewWidth = width;
		  initCamera();  	
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// SurfaceView����ʱ���÷���������
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		  Log.i(TAG, "SurfaceHolder.Callback��Surface Destroyed");
		  if(null != mCamera)
		  {
		   mCamera.setPreviewCallback(null); //�������������ǰ����Ȼ�˳�����
		   mCamera.stopPreview(); 
		   bIfPreview = false; 
		   mCamera.release();
		   mCamera = null;     
		  }		
	}
	
	/*��2�������Ԥ����*/
	 private void initCamera()//surfaceChanged�е���
	 {
	  Log.i(TAG, "going into initCamera");
	  if (bIfPreview)
	  {
	   mCamera.stopPreview();//stopCamera();
	  }
	  if(null != mCamera)
	  {
	   try
	   {
	    /* Camera Service settings*/    
	    Camera.Parameters parameters = mCamera.getParameters();
	    // parameters.setFlashMode("off"); // �������
	    parameters.setPictureFormat(PixelFormat.JPEG); //Sets the image format for picture �趨��Ƭ��ʽΪJPEG��Ĭ��ΪNV21    
	    parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP); //Sets the image format for preview picture��Ĭ��ΪNV21
	    /*��ImageFormat��JPEG/NV16(YCrCb format��used for Video)/NV21(YCrCb format��used for Image)/RGB_565/YUY2/YU12*/
	    
	    // �����ԡ���ȡcaera֧�ֵ�PictrueSize�������ܷ����ã���
	    List<Size> pictureSizes = mCamera.getParameters().getSupportedPictureSizes();
	    List<Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
	    List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
	    List<Integer> previewFrameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
	    Log.i(TAG+"initCamera", "cyy support parameters is ");
	    Size psize = null;
	    for (int i = 0; i < pictureSizes.size(); i++)
	    {
	     psize = pictureSizes.get(i);
	     Log.i(TAG+"initCamera", "PictrueSize,width: " + psize.width + " height" + psize.height);
	    }
	    for (int i = 0; i < previewSizes.size(); i++)
	    {
	     psize = previewSizes.get(i);
	     Log.i(TAG+"initCamera", "PreviewSize,width: " + psize.width + " height" + psize.height);
	    }
	    Integer pf = null;
	    for (int i = 0; i < previewFormats.size(); i++)
	    {
	     pf = previewFormats.get(i);
	     Log.i(TAG+"initCamera", "previewformates:" + pf);
	    }
	    
	    // �������պ�Ԥ��ͼƬ��С
	    parameters.setPictureSize(640, 480); //ָ������ͼƬ�Ĵ�С
	    parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); // ָ��preview�Ĵ�С 
	//���������� ����������������õĺ���ʵ�ֻ��Ĳ�һ��ʱ���ͻᱨ��
	    
	// ��������ͷ�Զ�����
	    if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) 
	    {
	     parameters.set("orientation", "portrait"); //
	     parameters.set("rotation", 90); // ��ͷ�Ƕ�ת90�ȣ�Ĭ������ͷ�Ǻ��ģ� 
	     mCamera.setDisplayOrientation(90); // ��2.2���Ͽ���ʹ��
	    } else// ����Ǻ���
	    {
	     parameters.set("orientation", "landscape"); //
	     mCamera.setDisplayOrientation(0); // ��2.2���Ͽ���ʹ��
	    } 
	    
	    /* ��Ƶ�����봦�� */ 
	    //��Ӷ���Ƶ��������
	    
	    
	// �趨���ò���������Ԥ��
	    mCamera.setParameters(parameters); // ��Camera.Parameters�趨��Camera    
//	    mCamera.setPreviewCallback(mJpegPreviewCallback);

	    mCamera.startPreview(); // ��Ԥ������
	    bIfPreview = true;
	    
	    // �����ԡ����ú��ͼƬ��С��Ԥ����С�Լ�֡��
	    Camera.Size csize = mCamera.getParameters().getPreviewSize();
	    mPreviewHeight = csize.height; //
	    mPreviewWidth = csize.width;
	    Log.i(TAG+"initCamera", "after setting, previewSize:width: " + csize.width + " height: " + csize.height);
	    csize = mCamera.getParameters().getPictureSize();
	    Log.i(TAG+"initCamera", "after setting, pictruesize:width: " + csize.width + " height: " + csize.height);
	    Log.i(TAG+"initCamera", "after setting, previewformate is " + mCamera.getParameters().getPreviewFormat());
	    Log.i(TAG+"initCamera", "after setting, previewframetate is " + mCamera.getParameters().getPreviewFrameRate());
	   } catch (Exception e)
	   { 
	    e.printStackTrace();
	   }
	  }
	 }

}
