package com.imjasonh.partychapp.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;

public class LogEntry {


	static final SimpleDateFormat webFormatter = new SimpleDateFormat("MMM/dd '\n' HH:mm");
	static final SimpleDateFormat logFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss', 'z");
	
	static{
		webFormatter.setTimeZone(TimeZone.getTimeZone("EST"));
		logFormatter.setTimeZone(TimeZone.getTimeZone("EST"));
	}
	
	@Id Long id;
	
	private Date timestamp;
	
	@Unindexed 
	private String content;
	
	private String sender;
	
	private String channelName;
	
	public LogEntry(){} 
	
	public LogEntry(Message msg){
		this.content = msg.content.substring(0, Math.min(msg.channel.logMaxLength(), msg.content.length()));
		this.timestamp = new Date();
		this.sender = msg.user.getEmail();
		this.channelName = msg.channel.getName();
	}
	
	public LogEntry(String content, String sender, Channel channel){
		this.content = content.substring(0, Math.min(channel.logMaxLength(), content.length()));
		this.timestamp = new Date();
		this.sender = sender;
		this.channelName = channel.getName();
		
	}
	
	//For the FakeDatastore. Might remove.
	//TODO: Make sure this is needed.
	public Long id(){
		return id;
	}
	
	public String content(){
		return content;
	}
	
	public String webDate(){
		return webFormatter.format(timestamp);
	}
	
	public Long millisecondDate(){
		return timestamp.getTime();
	}
	
	public String userID(){
		return sender;
	}
	
	public String channelName(){
		return channelName;
	}

	public void put() {
		 LogDAO.put(this);
	}
	
	@Override
	public String toString() {
		return logFormatter.format(timestamp)+"("+sender+")" + ": " + content;
	}
}
