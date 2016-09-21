package com.tpad.hik;

import java.io.Console;

import android.util.Log;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_V30;
import com.hikvision.netsdk.NET_DVR_CONFIG;
import com.tpad.hik.CompressDataBean;
import com.tpad.hik.DeviceBean;
import com.tpad.hik.HikView;

public class SetDVRConfig {
	private static final String TAG = "Set DVR Config";
	public int playID;
	int Command;
	final int channel_0 = HikView.getInstance().getchannel();
	final int channel_Alarm = 0;
	private int  bitrate;
	private byte resolution_;
	private int  faramerate;
	
	public void setCompressDataBean(CompressDataBean cdb){
		if(cdb.getBitRate()!="")
			this.bitrate = Integer.valueOf(cdb.getBitRate());
		if(cdb.getResolution()!="")
			this.resolution_ = Byte.valueOf(cdb.getResolution());
		Log.i(TAG, "resolution_ = "+this.resolution_);
		if(cdb.getFrameRate()!="")
			this.faramerate = Integer.valueOf(cdb.getFrameRate());
		Log.i(TAG, "faramerate = "+this.faramerate);
	}
	
	public SetDVRConfig(int playID){
		this.playID = playID;
	}
	
	private boolean ConfigSet(int command ,	int channel ,	NET_DVR_CONFIG CFG){
//		HikView.getInstance().getVideoCtr().NET_DVR_GetDVRConfig(playID, HCNetSDK.NET_DVR_GET_AP_INFO_LIST, 0, WIFI_CFG);
		Log.e(TAG, "playID = "+playID);
		Log.e(TAG, "command = "+command);
		Log.e(TAG, "channel = "+channel);
		if(HikView.getInstance().getVideoCtr().NET_DVR_SetDVRConfig(playID,command,channel,CFG)){
			return  true;
		}
		else{
			Log.e(TAG, "Set Fail"+HikView.getInstance().getVideoCtr().NET_DVR_GetLastError());
			return false;
		}
	}
	
	public void SetDevice(int Command,int bitRate){
		int channel = 0xFFFFFFFF;
		this.Command = Command;
		switch(Command){
			case HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30:
			{
				channel = channel_0;
				Log.e(TAG, "channel"+channel);
				Log.e(TAG, "para"+bitRate);
				CompressSetting(channel,bitRate);
				channel = 0xFFFFFFFF;
				break;
			}
		}
	}
//	public void SetDevice(int Command){
//		int channel = 0xFFFFFFFF;
//		this.Command = Command;
//		switch(Command){
//			case HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30:
//			{
//				channel = channel_0;
//				Log.e(TAG, "channel"+channel);
//				CompressSetting(channel);
//				channel = 0xFFFFFFFF;
//				break;
//			}
//		}
//	}
	
	public void SetDevice(int Command){
		int channel = 0xFFFFFFFF;
		this.Command = Command;
		switch(Command){
			case HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30:
			{
				channel = channel_0;
				Log.e(TAG, "channel"+channel);
				CompressSetting(channel, this.bitrate, this.resolution_, this.faramerate);
				channel = 0xFFFFFFFF;
				break;
			}
		}
	}
	
	
	private void CompressSetting(int channel,int bitRate){
//		NET_DVR_COMPRESSIONCFG_V30 CompressCfg = new NET_DVR_COMPRESSIONCFG_V30();
		Get_Dvr_Cfg GF = new Get_Dvr_Cfg(HikView.getInstance().getM_iLogID());
		GF.DeviceConfig(HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30);
//		CompressCfg = GF.Compress_Cfg;
		Log.e(TAG, "**************************************************************");
//		Log.i(TAG, ""+CompressCfg.struNetPara.dwVideoBitrate);
//	.	CompressCfg.struNetPara.dwVideoBitrate = CompressCfg.struNetPara.dwVideoBitrate;
		
		if(GF.Compress_Cfg!=null){
			GF.Compress_Cfg.struNetPara.dwVideoBitrate = bitRate;
			GF.Compress_Cfg.struNetPara.byResolution = 16;			//640*480
			GF.Compress_Cfg.struNetPara.dwVideoFrameRate = 8;		//6帧
			Log.e(TAG, "码率改变成"+bitRate);
			if(ConfigSet(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30,channel,GF.Compress_Cfg)){
	//			if(ConfigSet(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30,channel,CompressCfg)){
				Log.i(TAG, "Set OK");
				GF.DeviceConfig(HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30);
			}else	Log.e(TAG, "Setting is  Failed");
		}
		else	Log.e(TAG, "Compress_Cfg==null");
	}
	
	private void CompressSetting(int channel){
//		NET_DVR_COMPRESSIONCFG_V30 CompressCfg = new NET_DVR_COMPRESSIONCFG_V30();
//		CompressCfg.struNetPara.
		Get_Dvr_Cfg GF = new Get_Dvr_Cfg(HikView.getInstance().getM_iLogID());
		GF.DeviceConfig(HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30);
//		CompressCfg = GF.Compress_Cfg;
		Log.e(TAG, "**************************************************************");
//		Log.i(TAG, ""+CompressCfg.struNetPara.dwVideoBitrate);
//	.	CompressCfg.struNetPara.dwVideoBitrate = CompressCfg.struNetPara.dwVideoBitrate;
		
		if(GF.Compress_Cfg!=null){
			DeviceBean db = new DeviceBean();
			GF.Compress_Cfg.struNetPara.dwVideoBitrate =0;
			GF.Compress_Cfg.struNetPara.dwVideoBitrate = 0;
			GF.Compress_Cfg.struNetPara.dwVideoFrameRate = 0;
			Log.e(TAG, "码率改变成"+0);
			if(ConfigSet(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30,channel,GF.Compress_Cfg)){
	//			if(ConfigSet(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30,channel,CompressCfg)){
				Log.i(TAG, "Set OK");
				GF.DeviceConfig(HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30);
			}else	Log.e(TAG, "Setting is  Failed");
		}
		else	Log.e(TAG, "Compress_Cfg==null");
	}
	
	private void CompressSetting(int channel, int bitRate, byte resoulution, int frameRate){
//		NET_DVR_COMPRESSIONCFG_V30 CompressCfg = new NET_DVR_COMPRESSIONCFG_V30();
		Get_Dvr_Cfg GF = new Get_Dvr_Cfg(HikView.getInstance().getM_iLogID());
		GF.DeviceConfig(HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30);
//		CompressCfg = GF.Compress_Cfg;
		Log.e(TAG, "**************************************************************");
//		Log.i(TAG, ""+CompressCfg.struNetPara.dwVideoBitrate);
//	.	CompressCfg.struNetPara.dwVideoBitrate = CompressCfg.struNetPara.dwVideoBitrate;
		
		if(GF.Compress_Cfg!=null){
			GF.Compress_Cfg.struNetPara.dwVideoBitrate = bitRate;
			GF.Compress_Cfg.struNetPara.byResolution = resoulution;			//640*480
			GF.Compress_Cfg.struNetPara.dwVideoFrameRate = frameRate;		//6帧
			
			/*GF.Compress_Cfg.struNetPara.byAudioEncType=0;
			GF.Compress_Cfg.struEventRecordPara.byAudioEncType=0;
			GF.Compress_Cfg.struNormHighRecordPara.byAudioEncType=0;
			*/
			Log.e(TAG, "码率改变成"+bitRate);
			if(ConfigSet(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30,channel,GF.Compress_Cfg)){
	//			if(ConfigSet(HCNetSDK.NET_DVR_SET_COMPRESSCFG_V30,channel,CompressCfg)){
				Log.i(TAG, "Set OK");
				GF.DeviceConfig(HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30);
			}else	Log.e(TAG, "Setting is  Failed");
		}
		else	Log.e(TAG, "Compress_Cfg==null");
	}
}
