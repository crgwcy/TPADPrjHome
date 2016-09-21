package com.tpad.hik;

public class DeviceBean {

	private String ip;
    private String port;
    private String userName;
    private String passWord;
    private String channel;
	
    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
    	this.channel = channel;
    }
    
    @Override
    public String toString() {
        return "[IP=" + ip + "; PORT=" + port + "; USERNAME=" + userName + "; PASSWORD=" + passWord + "; CHANNEL=" + channel + ";]";
    }
    
}
