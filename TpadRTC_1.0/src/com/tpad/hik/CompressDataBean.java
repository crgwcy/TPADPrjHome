package com.tpad.hik;

public class CompressDataBean {

	private String resolution;
	private String frameRate;
	private String bitRate;
	
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getFrameRate() {
		return frameRate;
	}
	public void setFrameRate(String frameRate) {
		this.frameRate = frameRate;
	}
	public String getBitRate() {
		return bitRate;
	}
	public void setBitRate(String bitRate) {
		this.bitRate = bitRate;
	}
	
	 @Override
	    public String toString() {
		 return "[resolution=" + resolution + "; frameRate=" + frameRate + "; bitRate=" + bitRate + ";]";
	 }
}
