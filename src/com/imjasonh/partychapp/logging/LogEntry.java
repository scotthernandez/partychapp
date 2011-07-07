package com.imjasonh.partychapp.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;
import com.imjasonh.partychapp.Message;


public class LogEntry {


	static final SimpleDateFormat timeStampFormatter = new SimpleDateFormat("MMM/dd '\n' HH:mm");
	
	@Id Long id;
	
	private Date timestamp;
	
	@Unindexed 
	private String content;
	
	private String userID;
	
	private String channelName;
	
	public LogEntry(){} 
	
	public LogEntry(Message msg){
		this.content = msg.content.substring(0, Math.min(LogConfiguration.instance().maxLength(), msg.content.length()));
		this.timestamp = new Date();
		this.userID = msg.user.getEmail();
		this.channelName = msg.channel.getName();
	}
	
	//For the FakeDatastore. Might remove.
	//TODO: Make sure this is needed.
	public Long id(){
		return id;
	}
	
	public String content(){
		return content;
	}
	
	public String timeStamp(){
		return timeStampFormatter.format(timestamp);
	}
	
	public String userID(){
		return userID;
	}
	
	public String channelName(){
		return channelName;
	}

	public void put() {
		new LogDAO().put(this);
	}
	
	@Override
	public String toString() {
		return timestamp.toString()+"("+userID+")" + ": " + content;
	}
}
