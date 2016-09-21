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
			 * case 1  ��ȡ�豸����	read only
			 * */
			case HCNetSDK.NET_DVR_GET_DEVICECFG:{
				channel = 0xFFFFFFFF;
				DvrCfg(channel);
				break;
			}
			/*
			 * case 2 ��ȡ�����������
			 * */
			case HCNetSDK.NET_DVR_GET_ALARMINCFG_V30:{
				channel = channel_Alarm;
				AlarmCfg(channel);
				break;
			}
			/*
			 * case 3 IP��Դ�Լ�IPͨ������
			 * */
			case HCNetSDK.NET_DVR_GET_IPPARACFG_V40:{
				channel = 0;
				IpChanCfg(channel);
				break;
			}
			/*
			 * ��ȡͼ�����
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
//			0-����ʾ��1-��ʾ 
			Log.i(TAG, "�Ƿ���ʾ����	"+PicCfg.byDispWeek);
//			0- 16*16(��)/8*16(Ӣ)��1- 32*32(��)/16*32(Ӣ)��2- 64*64(��)/32*64(Ӣ) 3- 48*48(��)/24*48(Ӣ)��0xff-����Ӧ(adaptive) 
			Log.i(TAG, "�����С	"+PicCfg.byFontSize);
//			0��ʾ24Сʱ��	1��ʾ12Сʱ��
			Log.i(TAG, "Сʱ��	"+PicCfg.byHourOsdType);
			/*
			 * 1��͸������˸ 
			 * 2��͸��������˸
			 * 3����˸����͸��
			 * 4����͸��������˸ 
			 */
			Log.i(TAG, "OSD����(͸��/��˸)	"+PicCfg.byOSDAttrib);
			/*
			 * 0��XXXX-XX-XX ������
			 * 1��XX-XX-XXXX ������
			 * 2��XXXX��XX��XX��
			 * 3��XX��XX��XXXX��
			 * 4��XX-XX-XXXX ������
			 * 5��XX��XX��XXXX�� 
			 * */
			Log.i(TAG, "OSDOSD����(�����ո�ʽ)	"+PicCfg.byOSDType);
			//0-��	1-��
			Log.i(TAG, "�Ƿ�������˽�ڱ�			"+PicCfg.dwEnableHide);
			//0-����ʾ��1-��ʾ�������С704*576��
			Log.i(TAG, "Ԥ����ͼ�����Ƿ���ʾͨ������	"+PicCfg.dwShowChanName);
			//0-����ʾ��1-��ʾ�������С704*576��
			Log.i(TAG, "Ԥ����ͼ�����Ƿ���ʾOSD	"+PicCfg.dwShowOsd);
			//0- ��֧�֣�1- NTSC��2- PAL 
			Log.i(TAG, "��Ƶ��ʽ				"+PicCfg.dwVideoFormat);
			Log.i(TAG, "OSD��x���� 			"+PicCfg.wOSDTopLeftX);
			Log.i(TAG, "OSD��y����		 	"+PicCfg.wOSDTopLeftY);
			Log.i(TAG, "ͨ��������ʾλ�õ�x���� 	"+PicCfg.wShowNameTopLeftX);
			Log.i(TAG, "ͨ��������ʾλ�õ�y���� 	"+PicCfg.wShowNameTopLeftY);
			Log.i(TAG, "ͨ������ 			 	"+Arrays.toString(PicCfg.sChanName));
			Log.i(TAG, "*****************************************************************");
			Log.i(TAG, "************************�ڵ���������*********************************");
			//0-��1-�������ȣ�2-�������ȣ�3-�������� 
			Log.i(TAG, "�Ƿ������ڵ�����			"+PicCfg.struHideAlarm.dwEnableHideAlarm);
			Log.i(TAG, "�ڵ�����ĸ�			"+PicCfg.struHideAlarm.wHideAlarmAreaHeight);
			Log.i(TAG, "�ڵ�����Ŀ�			"+PicCfg.struHideAlarm.wHideAlarmAreaWidth);
			Log.i(TAG, "�ڵ������x����			"+PicCfg.struHideAlarm.wHideAlarmAreaTopLeftX);
			Log.i(TAG, "�ڵ������y����			"+PicCfg.struHideAlarm.wHideAlarmAreaTopLeftY);
			Log.i(TAG, "*****************************************************************");
			
//			Log.i(TAG, "�ڵ������y����			"+PicCfg.struHideAlarm.struAlarmTime.);
		}	else Log.e(TAG, "Pic Cfg Failed");
	}
	
	private void IpChanCfg(int channel){
		NET_DVR_IPPARACFG_V40 IP_PARA_CFG  = new NET_DVR_IPPARACFG_V40();
		if(ConfigGet(this.Command,channel,IP_PARA_CFG)){
			//����ͨ���ķ��䶼��read only
			Log.i(TAG, "ģ��ͨ��������"+IP_PARA_CFG.dwAChanNum);
			Log.i(TAG, "����ͨ��������"+IP_PARA_CFG.dwDChanNum);
			Log.i(TAG, "�豸֧�ֵ�������"+IP_PARA_CFG.dwGroupNum);
			Log.i(TAG, "����ͨ����ʼ"+IP_PARA_CFG.dwStartDChan);		
			/*
			 * Array �±��Ӧ��ģ��ͨ���Ƿ���	0-����	1-����
			 * i = 0 ��ʾ1ͨ������	�Դ�����
			 * */
			//Log.i(TAG, "ģ��ͨ���Ƿ�����"+Arrays.toString(IP_PARA_CFG.byAnalogChanEnable));
			for(int i = 0;i<64;i++){
				if(IP_PARA_CFG.byAnalogChanEnable[i]==1){
					Log.i(TAG, "IP�豸��ͨ����	"+i+"__"+IP_PARA_CFG.struIPChanInfo[i].byChannel);
					Log.i(TAG, "IPͨ������״̬	"+i+"__"+IP_PARA_CFG.struIPChanInfo[i].byEnable);
					Log.i(TAG, "IP�豸ID�ĵ�8λ	"+i+"__"+IP_PARA_CFG.struIPChanInfo[i].byIPID);
				}
				else	Log.e(TAG, "��"+i+"IPͨ��δ����");
			}
	
			for(int i = 0;i<64;i++){
				if(IP_PARA_CFG.struIPDevInfo[i].byEnable==1){
					Log.i(TAG, "byProType	"+i+"__"+IP_PARA_CFG.struIPDevInfo[i].byProType);
					Log.i(TAG, "�˿ں�		"+i+"__"+IP_PARA_CFG.struIPDevInfo[i].wDVRPort);
					Log.i(TAG, "����			"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].byDomain));
					Log.i(TAG, "�û���		"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].sUserName));
					Log.i(TAG, "����			"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].sPassword));
					Log.i(TAG, "IPV4		"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].struIP.sIpV4));
					Log.i(TAG, "IPV6		"+i+"__"+Arrays.toString(IP_PARA_CFG.struIPDevInfo[i].struIP.sIpV6));
				}
				else	Log.e(TAG, "��"+i+"IPͨ��δ����");
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
					Log.i(TAG, "��������");
			else	Log.i(TAG, "��������");
			
			if(Alarm_CFG.byAlarmInHandle==0)
				Log.i(TAG, "�������ܲ�����");
			else	Log.i(TAG, "�������ܴ���");
			
			Log.i(TAG, "�������� = "+new String (Alarm_CFG.sAlarmInName));
			Log.i(TAG, "��������"+Arrays.toString(Alarm_CFG.sAlarmInName));
			Log.i(TAG, "�������봥������ʶ��ͨ��"+Alarm_CFG.byChannel);
			switch(Alarm_CFG.struAlarmHandleType.dwHandleType){
				case 0x00:
					Log.i(TAG, "��������ʽ��"+"����Ӧ");
					break;
				case 0x01:
					Log.i(TAG, "��������ʽ��"+"�������Ͼ���");
					break;
				case 0x02:
					Log.i(TAG, "��������ʽ��"+"��������");
					break;
				case 0x04:
					Log.i(TAG, "��������ʽ��"+"�ϴ�����");
					break;
				case 0x08:
					Log.i(TAG, "��������ʽ��"+"�����������");
					break;
				case 0x10:
					Log.i(TAG, "��������ʽ��"+"Jpegץͼ���ϴ���Email");
					break;
				case 0x20:
					Log.i(TAG, "��������ʽ��"+"�������ⱨ��������");
					break;
				case 0x40:
					Log.i(TAG, "��������ʽ��"+"�������ӵ�ͼ(Ŀǰ��PCNVR֧��)");
					break;
				case 0x200:
					Log.i(TAG, "��������ʽ��"+"ץͼ���ϴ�ftp");
					break;
			}
	//		byte[] a = Alarm_CFG.struAlarmHandleType.byRelAlarmOut;
	//		�������������ͨ����0-��������1-���������
	//		��λ��ʾ���ͨ��������byRelAlarmOut[0]==1��ʾ�������ͨ��1��byRelAlarmOut[1]==1��ʾ�������ͨ��2����������\
			
			Log.i(TAG, "***����ʱ��****");
			Log.i(TAG, "Puzzling");
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStartHour);
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStartMin);
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStopHour);
			Log.i(TAG, ""+Alarm_CFG.struAlarmTime[0][0].byStopMin);
			/*
			 * ����Array Ҫ�о�һ��	�μ�������� ��ʼ����ʽ 
			 * */
			Log.i(TAG, ""+Arrays.toString(Alarm_CFG.struAlarmTime));
			
			/*
			 * ��ʾ Array �±��Ӧ��ͨ���Ƿ���
			 * */
			Log.i(TAG, "����������¼���¼ͨ��"+Arrays.toString(Alarm_CFG.byRelRecordChan));
			Log.i(TAG, "ͨ���Ƿ���������Ԥ�õ�	0��1��"+Arrays.toString(Alarm_CFG.byEnablePreset));
			Log.i(TAG, "Ԥ�õ��			0��1��"+Arrays.toString(Alarm_CFG.byPresetNo));
	//		һ��ͨ��ֻ�ܶ�Ӧһ��Ԥ�õ�
			Log.i(TAG, "�Ƿ����Ѳ��		0��1��"+Arrays.toString(Alarm_CFG.byEnableCruise));
			Log.i(TAG, "Ѳ��·��			0��1��"+Arrays.toString(Alarm_CFG.byCruiseNo));
			
			Log.i(TAG, "�Ƿ���ù켣		0��1��"+Arrays.toString(Alarm_CFG.byEnablePtzTrack));
			Log.i(TAG, "�켣���			0��1��"+Arrays.toString(Alarm_CFG.byPTZTrack));
			Alarm_CFG = null;
			channel = 0xFFFFFFFF;
		}else	Log.e(TAG, "Alarm CFG Failed");
	}
	
	private void DvrCfg(int channel){
		NET_DVR_DEVICECFG DVR_CFG = new NET_DVR_DEVICECFG();
		if(ConfigGet(this.Command,channel,DVR_CFG)){			
			Log.i(TAG, "�豸����"+new String(DVR_CFG.sDVRName));
			Log.i(TAG, "�豸�ͺź����к�(����λ) = "+new String(DVR_CFG.sSerialNumber));
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
			Log.i(TAG, "Audio Enc Type =	"+net_para.byAudioEncType);					//0-G722	1-G711_U	2-G711_A	5-MP2L2	6-G726	7-AAC��0xfe- �Զ�����Դһ�£���0xff-��Ч 
			Log.i(TAG, "Bit Rate Type = 	"+net_para.byBitrateType);					//0-������	1-������
			Log.i(TAG, "Frame Type = 		"+net_para.byIntervalBPFrame);				//0-BBP֡	1-BP֡ 	2-P֡	0xff-��Ч
			Log.i(TAG, "Pic Qua = 			"+net_para.byPicQuality);					//0 ��ߡ�������>5���		0xfe-�Զ�(��Դһ��)
			Log.i(TAG, "Resolution = 		"+net_para.byResolution);					//̫���ˣ�д���£��õ���ʱ����ĵ���
			Log.i(TAG, "Stream Type = 		"+net_para.byStreamType);					//0-������	1-������	0xfe-�Զ�(��Դһ��)
			Log.i(TAG, "Video Enc Type = 	"+net_para.byVideoEncType);					//0-˽��264��1-��׼h264��2-��׼mpeg4��7-M-JPEG��8-MPEG2��0xfe- �Զ�����Դһ�£���0xff-��Ч 
			Log.i(TAG, "Video Bit Rate		"+net_para.dwVideoBitrate);					//��Ƶ����		̫�಻д	���ѯ�ĵ�
			Log.i(TAG, "Video Frame Rate = 	"+net_para.dwVideoFrameRate);				//����֡��		0-ȫ��	1-1/16.....���ĵ�
			Log.i(TAG, "IntervalFrameI = 	"+net_para.wIntervalFrameI);				//I ֡���	0xffee	��Դһ��	0xffff	��Ч
			channel = 0xFFFFFFFF;
//			Compress_Cfg = null;
		}	else Log.e(TAG, "��ȡ��Ƶ����ʧ��");
		
	}
	

}
