package com.tpad.hik;
//
//import java.util.Arrays;

import com.hik.audiocodec.AudioCodec;
import com.mywebrtc.util.MyLog;
import com.mywebrtc.util.WSClientService;
import com.tpad.hik.HikView;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class AudioRecoder_thread extends Thread{
	
	private static final int AUDIO_CODEC_G711 = 1;
	private static final int AUDIO_CODEC_G722 = 0;
	private static final String TAG = "AudioRecoder";

	private int sampleRateInHz;
	private int channel = AudioFormat.CHANNEL_IN_MONO;
	private int EncodeDepth = AudioFormat.ENCODING_PCM_16BIT;
	private int audioRes = MediaRecorder.AudioSource.MIC;
	
	private		int 		minBufferSize;
	protected 	byte []     PCMBuffer;
	byte []   buffer_1280=new byte[1280];
	protected 	byte []     G722Buffer;
	public static AudioRecord mRecoder;
	public static int mEncoderHandle = -1;
	static boolean keep_run;
	public void RecordInit(int type)
	{
		switch(type){
			case AUDIO_CODEC_G722:
				sampleRateInHz = 16000;
				break;
			case AUDIO_CODEC_G711:
				sampleRateInHz = 8000;
				break;
		}
		
		keep_run = true;
		
		minBufferSize =  AudioRecord.getMinBufferSize(sampleRateInHz,channel,EncodeDepth);
		
		if(minBufferSize<=1280)
			minBufferSize = 1280;
		else
			minBufferSize = minBufferSize+1280-minBufferSize%1280;

		Log.e(TAG, "---------------------MinBufferSize = "+minBufferSize+"----sampleRateInHz:"+sampleRateInHz+"----channel"+channel+"----EncodeDepth"+EncodeDepth);
		mRecoder = new AudioRecord(	audioRes,
									sampleRateInHz,
									channel,
									EncodeDepth,
									minBufferSize);
		if(mRecoder==null)
			Log.e(TAG, "AudioRecord 初始化失败");
		else 	
			Log.e(TAG, "AudioRecord 初始化成功");
		
		PCMBuffer = new byte[minBufferSize];
		mEncoderHandle = AudioCodec.getInstance().OpenAudioEncoder(type);
		G722Buffer = new byte[AudioCodec.G722_ENC_SIZE];
		mRecoder.startRecording();
	}
	
	public void run()
	{
		int outSize=0;
		boolean runOnPadFlag=WSClientService.run_on_pad_flag;
		try{
			while(keep_run){
				int inSize = mRecoder.read(PCMBuffer,0,minBufferSize);
				//System.out.println(Arrays.toString(PCMBuffer));
	//			int outSize = AudioCodec.getInstance().EncodeAudioData(mEncoderHandle, PCMBuffer, inSize, G722Buffer);
				for (int i=0;i<minBufferSize/1280;i++)
				{
					System.arraycopy(PCMBuffer, 1280*i, buffer_1280, 0, 1280);
					 outSize = AudioCodec.getInstance().EncodeAudioData(mEncoderHandle, buffer_1280, inSize, G722Buffer);
					 
					if (runOnPadFlag)
					{							
						boolean SendFlag = HikView.getInstance().getVideoCtr().NET_DVR_VoiceComSendData(DuplexSound.voiceHandle, G722Buffer, 80);
						if(SendFlag){
							Log.e(TAG, "int Size = "+inSize+" OutSize = "+outSize);
							//System.out.println(Arrays.toString(G722Buffer));
							//Log.i(TAG, "duplexSound.voiceHandle = "+duplexSound.voiceHandle);
						}
						else
							Log.e(TAG, "send Failed Error Code = "+HikView.getInstance().getVideoCtr().NET_DVR_GetLastError());
							//System.out.println(Arrays.toString(G722Buffer));
					}
					else
					{
		    			if (WSClientService.getInstance().client.getRemoteID()!=null)
		    			{
		    			//	if (audio_play_flag==true)
		    				{	
		    					MyLog.printf(TAG,"...............send phoneAudioData");
		    					WSClientService.getInstance().client.sendHikCameraData(G722Buffer,80);		    						    					
		    				}	
		    			}											
					}	
					
				}	
			}
		}
		catch(Exception e){
			e.printStackTrace();
	    }
	}
	
    public static void free()
    {
    	keep_run = false ;
    	
    	if(mRecoder!=null){
	    	mRecoder.stop();
	    	mRecoder.release();
	    	mRecoder = null;
	    	Log.i(TAG,"Recoder释放");
	    }
    	if(mEncoderHandle==-1){
    		if(AudioCodec.getInstance().CloseAudioEncoder(mEncoderHandle)==1){
	    		mEncoderHandle = -1;
	    		Log.i(TAG,"解码释放");
    		}
	    	else{
	    		Log.i(TAG, "Close Audio Encoder Error"+HikView.getInstance().getVideoCtr().NET_DVR_GetLastError());
	    	}
    	}
        try {
            Thread.sleep(1000) ;
        }catch(Exception e) {
            Log.d("sleep exceptions...\n","") ;
        }
    }
	
}