package com.mywebrtc.util;

public class MyRtcSip {
	
    public static final String  ALARM_MSG="alarm_msg";		
    public static final String  SEND_RING_MSG="send_ring_msg";	
    public static final String  RING_REMOTE_ID_EXTRA="ring_id_extra";	
    public static final String  RING_REMOTE_NAME_EXTRA="ring_name_extra";	
    public static final String  RING_ANSWER_MSG="ring_answer";	
    public static final String  RING_DECLINE_MSG="ring_decline";	
    public static final String  CALL_CANCEL_MSG="call_cancel";	   
    
    public static final String  IP_CAMERA_REMOTE_ID_EXTRA="ip_camera_id_extra";	
    public static final String  REMOTE_NAME_MSG="remote_name_msg";	   
  
	
    public static final String  CALL="call_msg";	
    public static final String  CALL_EXTRA="call_msg_extra";	
    public static final String  ANSWER_EXTRA="call_answer_extra";	   
    public static final String  ANSWER="my_answer";	
    public static final String  HANGUP="my_hangup";
    public static final String  REMOTE_RTC_ACT_INIT_ASK="remote_rtc_act_init_ask";	
    public static final String  REMOTE_RTC_ACT_INIT_ASK_EXTRA="remote_rtc_act_init_ask_extra";	
    
    public static final String  REMOTE_RTC_ACT_INIT_HANDLE="remote_rtc_act_init_handle";	   
    public static final String  REMOTE_RTC_ACT_INITED_ANSWER="remote_rtc_act_inited_answer";	
 
    public static final String  ICE_DISCONNECTED_WAIT="my_ICE_disconnected_wait";   
    public static final String  DATA_CHANNEL_ONLY="data_channel_only"; 
    public static final String  IP_CAMERA_LEFT="left";
    public static final String  IP_CAMERA_RIGHT="right";
    public static final String  IP_CAMERA_UP="up";
    public static final String  IP_CAMERA_DOWN="down";  
    public static final String  NEW_USR_LOGIN_ONLINE="NEW USR ONLINE";
    
    public static final String  HIK_CAMERA_LEFT_ING="hik_pwm_left_ing";
    public static final String  HIK_CAMERA_RIGHT_ING="hik_pwm_right_ing";
    public static final String  HIK_CAMERA_UP_ING="hik_pwm_up_ing";
    public static final String  HIK_CAMERA_DOWN_ING="hik_pwm_down_ing"; 
    
    public static final String  HIK_CAMERA_LEFT_END="hik_pwm_left_end";
    public static final String  HIK_CAMERA_RIGHT_END="hik_pwm_right_end";
    public static final String  HIK_CAMERA_UP_END="hik_pwm_up_end";
    public static final String  HIK_CAMERA_DOWN_END="hik_pwm_down_end";      
    public static final String  HIK_CAMERA_VOICE_ON="hik_camera_voice_on";
    public static final String  HIK_CAMERA_VOICE_OFF="hik_camera_voice_off";   
    public static final String  THREE_PEERS_COMMUNICATION="three_peers_communication";	

        
    // BLE light control
    public static final String BLE_DATA_HEAD="ble_head:";
    
    public static boolean  onIceConnected=false;
 //   public  static boolean  ONLY_DATACHANNEL=false;
    public  static boolean  ONLY_TWO_PEERS=true;
    
    public static boolean  NEW_VERSION_WEBRTC=true;
    
    public static boolean OLD_VERSION_OPEN_GL_VIDEO_VIEW=false;
}
