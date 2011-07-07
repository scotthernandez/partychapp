package com.imjasonh.partychapp.logging;

public class LogConfiguration {
	private static final int FINAL_MAX = 500;
	private static LogConfiguration INSTANCE;
	
	public static LogConfiguration instance(){
		if(INSTANCE == null){
			INSTANCE = new LogConfiguration();
		}
		return INSTANCE;
	}

	private boolean LogginEnabled = true;
	private int MaxLength = FINAL_MAX;
	
	public int maxLength() {
		return MaxLength;
	}
	
	public int maxLength(int l){
		if (l < FINAL_MAX){
			MaxLength = l;
		}else{
			MaxLength = FINAL_MAX;
		}
		return MaxLength;
	}
	
	public void enableLoggin(boolean b){
		LogginEnabled = b;
	}
	
	
}
