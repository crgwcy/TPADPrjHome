package com.tpad.hik;


//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.InputStream;
//import java.util.Arrays;
//
//
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.os.Environment;
import android.util.Log;
import com.hikvision.netsdk.VoiceDataCallBack;
import com.mywebrtc.util.WSClientService;
import com.tpad.hik.HikView;
//import com.tpad.utils.SDCardUtil;


public class DuplexSound {
	private static String TAG = "DuplexTalk";
	int type;
//	private  AudioRecord mAudioRec;
	public static int voiceHandle = -1;
//	private int sampleRate = 8000;
//	private int mEncOutBufferSize;
//	private int MinBufferSize;
//	private int channel = AudioFormat.CHANNEL_IN_MONO;
//	private int audioRes = MediaRecorder.AudioSource.MIC;
//	private int EncodeDepth = AudioFormat.ENCODING_PCM_16BIT;
	String SDCardPath;
	String FilePath;
	byte[] buffer;
	InputStream is;
	AudioRecoder_thread mThread;
	//private int offSet = 0;
	public void startSend(int userID,int voiceChan,int type){
		Log.i(TAG, "Start Send");		
		this.type = type;
		
//		try {
//			getStreamFSDC();
//		} catch (IOException e) {
//		// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		mThread = new AudioRecoder_thread();
		mThread.RecordInit(type);
		
		if (WSClientService.run_on_pad_flag==true)
		{
			voiceHandle = HikView.getInstance().getVideoCtr().NET_DVR_StartVoiceCom_MR_V30(userID, voiceChan, getvoiceCallBack());
		}
		
		mThread.start();
		if(voiceHandle<0){
			Log.e(TAG,"Start Voice Com Error Error Code = "+HikView.getInstance().getVideoCtr().NET_DVR_GetLastError());
			return;
		}
		else
		{
			Log.e(TAG,"Start Voice Com ");
		}
	}
	
	
	public void voiceHandleGet(int userID,int voiceChan)
	{
		voiceHandle = HikView.getInstance().getVideoCtr().NET_DVR_StartVoiceCom_MR_V30(userID, voiceChan, getvoiceCallBack());
	
	}
	
	private VoiceDataCallBack getvoiceCallBack(){
		return new VoiceDataCallBack(){
			@Override
			public void fVoiceDataCallBack(int VoiceComHandle, byte[] VoiceBuffer, int DataSize, int AudioFlag) {
				// TODO Auto-generated method stub	
//				try {
//					is.read(buffer);
//					HikView.getInstance().getVideoCtr().NET_DVR_VoiceComSendData(VoiceComHandle, buffer, DataSize);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		};
	}

	
	public void stop_duplextalk(){
		if (WSClientService.run_on_pad_flag==true)
		{	
			if(voiceHandle>=0)
			{	
				HikView.getInstance().getVideoCtr().NET_DVR_StopVoiceCom(voiceHandle);
				voiceHandle=-1;
			}	
		}
		AudioRecoder_thread.free();
	}
	
	
//	private void InitRecoder(int type){
//		AudioFormatInit(type);
//		MinBufferSize =  AudioRecord.getMinBufferSize(sampleRate,
//				channel,
//				EncodeDepth);
//		if(mAudioRec==null)
//			mAudioRec = new AudioRecord	(	audioRes,
//											sampleRate,
//											channel,
//											EncodeDepth,
//											MinBufferSize);
//	}
	
//	private void AudioFormatInit(int EncodeType){
//		switch(EncodeType){
//		case 0:
//			sampleRate  = 16000;
//			break;
//		case 1:
//			sampleRate = 8000;
//			break;
//		}
//		Log.i(TAG, "sampleRate = "+sampleRate);
//	}
//	
//	private void getStreamFSDC() throws IOException{
//		if(Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED)){
//		 SDCardPath = Environment.getExternalStorageDirectory().toString()+ File.separator;
//		 Log.e(TAG, SDCardPath);
//		 File file=new File(SDCardPath+"G711/test.g711");
//		 if(file.exists()){
//			 try {
//				is = new FileInputStream(file);
//				buffer = new byte[8000];
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		 }else
//		 {
//			 Log.e(TAG, "test.g711 Do Not Exists");
//		 }
//		}else
//			Log.e(TAG, "SDCard Do Not Ready");
//	}
}
