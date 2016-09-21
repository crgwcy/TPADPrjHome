package com.tpad.hik;

import java.util.Arrays;

import android.R.integer;
import android.util.Log;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_ALARMINCFG_V30;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_V30;
import com.hikvision.netsdk.NET_DVR_COMPRESSION_INFO_V30;
import com.hikvision.netsdk.NET_DVR_CONFIG;
import com.hikvision.netsdk.NET_DVR_DEVICECFG;
import com.hikvision.netsdk.NET_DVR_IPPARACFG_V40;
import com.hikvision.netsdk.NET_DVR_PICCFG_V30;
import com.tpad.hik.HikView;

public class Get_Dvr_Cfg {
	private static final String TAG = "Get_DVR_CFG";
	public int playID;
//	NET_DVR_WIFI_CFG WIFI_CFG;
	int Command;
	NET_DVR_COMPRESSIONCFG_V30 Compress_Cfg;
	public Get_Dvr_Cfg(int playID){
		this.playID = playID;
//		WIFI_CFG = new NET_DVR_WIFI_CFG();
	}
	
	public void DeviceConfig(int Command){
		int channel = 0xFFFFFFFF;
		final int channel_0 = HikView.getInstance().getchannel();
		final int channel_Alarm = 0;
		this.Command = Command;
//		NET_DVR_WIFI_CFG WIFI_CFG;
		switch(this.Command){
			/*
			 * case 1  获取设备参数	read only
			 * */
			case HCNetSDK.NET_DVR_GET_DEVICECFG:{
				channel = 0xFFFFFFFF;
				DvrCfg(channel);
				break;
			}
			/*
			 * case 2 获取报警输入参数
			 * */
			case HCNetSDK.NET_DVR_GET_ALARMINCFG_V30:{
				channel = channel_Alarm;
				AlarmCfg(channel);
				break;
			}
			/*
			 * case 3 IP资源以及IP通道配置
			 * */
			case HCNetSDK.NET_DVR_GET_IPPARACFG_V40:{
				channel = 0;
				IpChanCfg(channel);
				break;
			}
			/*
			 * 获取图像参数
			 * */
			case HCNetSDK.NET_DVR_GET_PICCFG_V30:{
				channel = channel_0;
				PictureCFG(channel);
				break;
			}
			
			case HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30:{
				channel = channel_0;
				COMPRESS_CFG(channel);
				break;
			}
		}
	}
	
	private boolean ConfigGet(int command ,	int channel ,	NET_DVR_CONFIG CFG){
//		HikView.getInstance().getVideoCtr().NET_DVR_GetDVRConfig(playID, HCNetSDK.NET_DVR_GET_AP_INFO_LIST, 0, WIFI_CFG);
		return HikView.getInstance().getVideoCtr().NET_DVR_GetDVRConfig(playID,command,channel,CFG);
	}
	
	private void PictureCFG(int channel){
		NET_DVR_PICCFG_V30  PicCfg = new NET_DVR_PICCFG_V30();
		if(ConfigGet(this.Command,channel,PicCfg)){
//			0-不显示，1-显示 
			Log.i(TAG, "是否显示星期	"+PicCfg.byDispWeek);
//			0- 16*16(中)/8*16(英)，1- 32*32(中)/16*32(英)，2- 64*64(中)/32*64(英) 3- 48*48(中)/24*48(英)，0xff-自适应(adaptive) 
			Log.i(TAG, "字体大小	"+PicCfg.byFontSize);
//			0表示24小时制	1表示12小时制
			Log.i(TAG, "小时制	"+PicCfg.byHourOsdType);
			/*
			 * 1－透明，闪烁 
			 * 2－透明，不闪烁
			 * 3－闪烁，不透明
			 * 4－不透明，不闪烁 
			 */
			Log.i(TAG, "OSD属性(透明/闪烁)	"+PicCfg.byOSDAttrib);
			/*
			 * 0－XXXX-XX-XX 年月日
			 * 1－XX-XX-XXXX 月日年
			 * 2－XXXX年XX月XX日
			 * 3－XX月XX日XXXX年
			 * 4－XX-XX-XXXX 日月年
			 * 5－XX日XX月XXXX年 
			 * */
			Log.i(TAG, "OSDOSD类型(年月日格式)	"+PicCfg.byOSDType);
			//0-否	1-是
			Log.i(TAG, "是否启动隐私遮蔽			"+PicCfg.dwEnableHide);
			//0-不显示，1-显示（区域大小704*576）
			Log.i(TAG, "预览的图象上是否显示通道名称	"+PicCfg.dwShowChanName);
			//0-不显示，1-显示（区域大小704*576）
			Log.i(TAG, "预览的图象上是否显示OSD	"+PicCfg.dwShowOsd);
			//0- 不支持，1- NTSC，2- PAL 
			Log.i(TAG, "视频制式				"+PicCfg.dwVideoFormat);
			Log.i(TAG, "OSD的x坐标 			"+PicCfg.wOSDTopLeftX);
			Log.i(TAG, "OSD的y坐标		 	"+PicCfg.wOSDTopLeftY);
			Log.i(TAG, "通道名称显示位置的x坐标 	"+PicCfg.wShowNameTopLeftX);
			Log.i(TAG, "通道名称显示位置的y坐标 	"+PicCfg.wShowNameTopLeftY);
			Log.i(TAG, "通道名称 			 	"+Arrays.toString(PicCfg.sChanName));
			Log.i(TAG, "*****************************************************************");
			Log.i(TAG, "************************遮挡报警参数*********************************");
			//0-否，1-低灵敏度，2-中灵敏度，3-高灵敏度 
			Log.i(TAG, "是否启动遮挡报警			"+PicCfg.struHideAlarm.dwEnableHideAlarm);
			Log.i(TAG, "遮挡区域的高			"+PicCfg.struHideAlarm.wHideAlarmAreaHeight);
			Log.i(TAG, "遮挡区域的宽			"+PicCfg.struHideAlarm.wHideAlarmAreaWidth);
			Log.i(TAG, "遮挡区域的x坐标			"+PicCfg.struHideAlarm.wHideAlarmAreaTopLeftX);
			Log.i(TAG, "遮挡区域的y坐标			"+PicCfg.struHideAlarm.wHideAlarmAreaTopLeftY);
			Log.i(TAG, "*****************************************************************");
			
//			Log.i(TAG, "遮挡区域的y坐标			"+PicCfg.struHideAlarm.struAlarmTime.);
		}	else Log.e(TAG, "Pic Cfg Failed");
	}
	
	private void IpChanCfg(int channel){
		NET_DVR_IPPARACFG_V40 IP_PARA_CFG  = new NET_DVR_IPPARACFG_V40();
		if(ConfigGet(this.Command,channel,IP_PARA_CFG)){
			//关于通道的分配都是read only
			Log.i(TAG, "模拟通道最大个数"+IP_PARA_CFG.dwAChanNum);
			Log.i(TAG, "数字通道最大个数"+IP_PARA_CFG.dwDChanNum);
			Log.i(TAG, "设备支持的总组数"+IP_PARA_CFG.dwGroupNum);
			Log.i(TAG, "数字通道起始"+IP_PARA_CFG.dwStartDChan);		
			/*
			 * Array 下标对应的模拟通道是否开启	0-禁用	1-启用
			 * i = 0 表示1通道启用	以此类推
			 * */
			//Log.i(TAG, "模拟通道是否启用"+Arrays.toString(IP_PARA_CFG.byAnalogChanEnable));
			for(int i = 0;i<64;i++){
				if(IP_PARA_CFG.byAnalogChanEnable[i]==1){
					Log.i(TAG, "IP设备的通道号	"+i+"__"+IP_PARA_CFG.struIPChanInfo[i].byChannel);
					Log.i(TAG, "IP通道在线状态	"+i+"__"+IP_PARA_CFG.struIPChanInfo[i].byEnable);
					Log.i(TAG, "IP设备ID的低8位	"+i+"__"+IP_PARA_CFG.struIPChanInfo[i].byIPID);
				}
				else	Log.e(TAG, "第"+i+"IP通道未开启");
			}
	
			for(int i = 0;i<64;i++){
				if(IP_PARA_CFG.struIPDevInfo[i].byEnable==1){
					Log.i(TAG, "byProType	"+i+"__"+IP_PARA_CFG.struIPDevInfo[i].byProType);
					Log.i(TAG, "端口号		"+i+"__"+IP_PARA_CFG.struIPDevInfo[i].wDVRPort);
					Log.i(TAG, "域名			"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].byDomain));
					Log.i(TAG, "用户名		"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].sUserName));
					Log.i(TAG, "密码			"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].sPassword));
					Log.i(TAG, "IPV4		"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].struIP.sIpV4));
					Log.i(TAG, "IPV6		"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].struIP.sIpV6));
				}
				else	Log.e(TAG, "第"+i+"IP通道未开启");
			}
			IP_PARA_CFG = null;
			channel = 0xFFFFFFFF;
		}
		else 
			{
				Log.e(TAG, "IP_CFG_Failed");
				Log.i(TAG, ""+HikView.getInstance().getVideoCtr().NET_DVR_GetLastError());
			}
	}
	
	private void AlarmCfg(int channel){
		NET_DVR_ALARMINCFG_V30 Alarm_CFG = new NET_DVR_ALARMINCFG_V30();
		if(ConfigGet(this.Command,channel,Alarm_CFG)){
			if(Alarm_CFG.byAlarmType==0)
					Log.i(TAG, "报警常开");
			else	Log.i(TAG, "报警常闭");
			
			if(Alarm_CFG.byAlarmInHandle==0)
				Log.i(TAG, "报警接受不处理");
			else	Log.i(TAG, "报警接受处理");
			
			Log.i(TAG, "报警名称 = "+new String (Alarm_CFG.sAlarmInName));
			Log.i(TAG, "报警名称"+Arrays.toString(Alarm_CFG.sAlarmInName));
			Log.i(TAG, "报警输入触发智能识别通道"+Alarm_CFG.byChannel);
			switch(Alarm_CFG.struAlarmHandleType.dwHandleType){
				case 0x00:
					Log.i(TAG, "报警处理方式："+"无响应");
					break;
				case 0x01:
					Log.i(TAG, "报警处理方式："+"监视器上警告");
					break;
				case 0x02:
					Log.i(TAG, "报警处理方式："+"声音警告");
					break;
				case 0x04:
					Log.i(TAG, "报警处理方式："+"上传中心");
					break;
				case 0x08:
					Log.i(TAG, "报警处理方式："+"出发报警输出");
					break;
				case 0x10:
					Log.i(TAG, "报警处理方式："+"Jpeg抓图并上传到Email");
					break;
				case 0x20:
					Log.i(TAG, "报警处理方式："+"无线声光报警器联动");
					break;
				case 0x40:
					Log.i(TAG, "报警处理方式："+"联动电子地图(目前仅PCNVR支持)");
					break;
				case 0x200:
					Log.i(TAG, "报警处理方式："+"抓图并上传ftp");
					break;
			}
	//		byte[] a = Alarm_CFG.struAlarmHandleType.byRelAlarmOut;
	//		报警触发的输出通道，0-不触发，1-触发输出，
	//		按位表示输出通道，例如byRelAlarmOut[0]==1表示触发输出通道1，byRelAlarmOut[1]==1表示触发输出通道2，依次类推\
			
			Log.i(TAG, "***布防时间****");
			Log.i(TAG, "Puzzling");
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStartHour);
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStartMin);
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStopHour);
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStopMin);
			/*
			 * 这种Array 要研究一下	参见反编译的 初始化方式 
			 * */
			Log.i(TAG, ""+Arrays.toString(Alarm_CFG.struAlarmTime));
			
			/*
			 * 表示 Array 下标对应的通道是否开启
			 * */
			Log.i(TAG, "报警触发的录像记录通道"+Arrays.toString(Alarm_CFG.byRelRecordChan));
			Log.i(TAG, "通道是否启动调用预置点	0否1是"+Arrays.toString(Alarm_CFG.byEnablePreset));
			Log.i(TAG, "预置点号			0否1是"+Arrays.toString(Alarm_CFG.byPresetNo));
	//		一个通道只能对应一个预置点
			Log.i(TAG, "是否调用巡航		0否1是"+Arrays.toString(Alarm_CFG.byEnableCruise));
			Log.i(TAG, "巡航路径			0否1是"+Arrays.toString(Alarm_CFG.byCruiseNo));
			
			Log.i(TAG, "是否调用轨迹		0否1是"+Arrays.toString(Alarm_CFG.byEnablePtzTrack));
			Log.i(TAG, "轨迹序号			0否1是"+Arrays.toString(Alarm_CFG.byPTZTrack));
			Alarm_CFG = null;
			channel = 0xFFFFFFFF;
		}else	Log.e(TAG, "Alarm CFG Failed");
	}
	
	private void DvrCfg(int channel){
		NET_DVR_DEVICECFG DVR_CFG = new NET_DVR_DEVICECFG();
		if(ConfigGet(this.Command,channel,DVR_CFG)){			
			Log.i(TAG, "设备类型"+new String(DVR_CFG.sDVRName));
			Log.i(TAG, "设备型号和序列号(最后九位) = "+new String(DVR_CFG.sSerialNumber));
			channel = 0xFFFFFFFF;
			DVR_CFG = null;
		}	else Log.e(TAG, "Dvr Cfg Failed");
	}
	
	public void COMPRESS_CFG(int channel){
		Compress_Cfg = new NET_DVR_COMPRESSIONCFG_V30();
		if(ConfigGet(this.Command,channel,Compress_Cfg)){
			NET_DVR_COMPRESSION_INFO_V30 net_para = Compress_Cfg.struNetPara;
			NET_DVR_COMPRESSION_INFO_V30 record_para = Compress_Cfg.struNormHighRecordPara;
			NET_DVR_COMPRESSION_INFO_V30 alarm_para = Compress_Cfg.struEventRecordPara;
			/*
				int[] net_para_video = {0};
				net_para_video[0] = net_para.dwVideoBitrate;
				net_para_video[1] = net_para.dwVideoFrameRate;//			byte[] net_para_array = {0};
				net_para_array[0] = net_para.byAudioEncType;
				net_para_array[1] = net_para.byBitrateType;
				net_para_array[2] = net_para.byIntervalBPFrame;
				net_para_array[3] = net_para.byPicQuality;
				net_para_array[4] = net_para.byResolution;
				net_para_array[5] = net_para.byStreamType;
				net_para_array[6] = net_para.byVideoEncType;
				net_para_array[7] = (byte)net_para.wIntervalFrameI;
			*/
			Log.i(TAG, "Audio Enc Type =	"+net_para.byAudioEncType);					//0-G722	1-G711_U	2-G711_A	5-MP2L2	6-G726	7-AAC，0xfe- 自动（和源一致），0xff-无效 
			Log.i(TAG, "Bit Rate Type = 	"+net_para.byBitrateType);					//0-变码率	1-定码率
			Log.i(TAG, "Frame Type = 		"+net_para.byIntervalBPFrame);				//0-BBP帧	1-BP帧 	2-P帧	0xff-无效
			Log.i(TAG, "Pic Qua = 			"+net_para.byPicQuality);					//0 最高————>5最低		0xfe-自动(和源一致)
			Log.i(TAG, "Resolution = 		"+net_para.byResolution);					//太多了，写不下，用到的时候查文档吧
			Log.i(TAG, "Stream Type = 		"+net_para.byStreamType);					//0-视屏流	1-复合流	0xfe-自动(和源一致)
			Log.i(TAG, "Video Enc Type = 	"+net_para.byVideoEncType);					//0-私有264，1-标准h264，2-标准mpeg4，7-M-JPEG，8-MPEG2，0xfe- 自动（和源一致），0xff-无效 
			Log.i(TAG, "Video Bit Rate		"+net_para.dwVideoBitrate);					//视频码率		太多不写	请查询文档
			Log.i(TAG, "Video Frame Rate = 	"+net_para.dwVideoFrameRate);				//视屏帧率		0-全部	1-1/16.....查文档
			Log.i(TAG, "IntervalFrameI = 	"+net_para.wIntervalFrameI);				//I 帧间隔	0xffee	和源一致	0xffff	无效
			channel = 0xFFFFFFFF;
//			Compress_Cfg = null;
		}	else Log.e(TAG, "获取视频参数失败");
		
	}
	

}
