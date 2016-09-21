package com.mywebrtc.util;

public class YuvFormatTransform
{
	 //yv12 ת yuv420p  yvu -> yuv  
    public static  void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height)   
    {        
        System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);  
        System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);  
        System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);    
    } 
    
    public static  void swapYV12toI420SemiPlanar(byte[] yv12bytes, byte[] i420bytes, int width, int height)   
    {    
    	 System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height); 
    	 for(int i=0;i<width*height/4;i++)
    	 {
    			i420bytes[2*i+width*height] = yv12bytes[i + width*height/4+width*height];
    			i420bytes[2*i+1+width*height] = yv12bytes[i + width*height];
    	 }
    } 
    public static void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes,
			int width, int height) {
		System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
		for (int i = width * height; i < nv21bytes.length; i += 2) {
			i420bytes[i] = nv21bytes[i + 1];
			i420bytes[i + 1] = nv21bytes[i];
		}
	}
    public static  void NV21ToI420(byte[] nv21bytes, byte[] i420bytes,
			int width, int height)
	{
		System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
		
		for(int i=0;i<width * height/4;i++)
		{
			i420bytes[i+width * height]=nv21bytes[width * height+2*i+1]; //u
			i420bytes[i+width * height+width * height/4]=nv21bytes[width * height+2*i];  
		}
	}
    public static  void NV21ToYV12(byte[] nv21bytes, byte[] i420bytes,
			int width, int height)
	{
		System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
		
		for(int i=0;i<width * height/4;i++)
		{
			i420bytes[i+width * height]=nv21bytes[width * height+2*i]; 
			i420bytes[i+width * height+width * height/4]=nv21bytes[width * height+2*i+1]; //u 
		}
	}

}
