package com.mywebrtc.util;


import android.util.Log;

public class MyLog { 
	public static void printf(String tag,String fmt,Object ...args)
	{
		Log.e(tag,String.format(fmt,args));
	}
	
}
