package com.imjasonh.partychapp.logging;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;

import com.imjasonh.partychapp.Channel;
import com.xgen.partychapp.clienthub.ClientHubAPI;

public class ChannelLog implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private static final int FINAL_MAX = 500;
	private int MaxLength = FINAL_MAX;


	private Boolean loggingEnabled = true;

	public Date sectionStart = new Date();
	public Date sectionEnd = new Date();
	
	public ChannelLog(){
		
	}
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
	
	public void enable(boolean b){
		loggingEnabled = b;
	}
	public boolean isEnabled(){
		return loggingEnabled;
	}

}
