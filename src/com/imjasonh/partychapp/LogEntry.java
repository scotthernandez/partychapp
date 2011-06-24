package com.imjasonh.partychapp;

import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;


public class LogEntry {

	@Id long id;
	
	private Date timestamp;
	
	@Unindexed 
	private String content;
	
	private String userID;
	
	private String channelName;
	
	public LogEntry(){} 
	
	public LogEntry(Message msg){
		this.content = msg.content;
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
	
	public String userID(){
		return userID;
	}
	
	public String channelName(){
		return channelName;
	}

	public void put() {
		Datastore.instance().put(this);
	}
	
	@Override
	public String toString() {
		return timestamp.toString()+"("+userID+")" + ": " + content;
	}
}
