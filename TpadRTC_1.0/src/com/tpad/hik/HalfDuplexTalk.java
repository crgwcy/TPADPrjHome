package com.tpad.hik;


public class HalfDuplexTalk {
	
	public static final String TAG = "HalfDuplexTalk";
	public static boolean HalfDuplexTak = false;
	public DuplexSound duplexSoundManger;
	
	public HalfDuplexTalk(){
			duplexSoundManger = new DuplexSound();
	}
	
	public void StartHalfDuplexTalk(int userID, int voiceChan, int type){
		HalfDuplexTak = true;
		duplexSoundManger.startSend(userID, voiceChan, type);
	}
	
	public void StopHalfDuplexTalk(){
		HalfDuplexTak = false;
		duplexSoundManger.stop_duplextalk();
		//duplexSoundManger = null;
	}
	
}
