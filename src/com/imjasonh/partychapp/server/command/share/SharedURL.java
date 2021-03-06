package com.imjasonh.partychapp.server.command.share;

import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

@Unindexed
public class SharedURL {
	  

	  public static final int SHARED_URL_LIMIT = 5;
	  public static final int MAX_TITLE_SIZE = 25;
	  
	  @Id
	  private String urlString;
	  
	  @Indexed
	  private String channelName;
	  
	  @Indexed
	  private Date time;
	  
	  private String title;
	  private String description;
	  private String annotation;
	  private String jid;
	  
	  public SharedURL(){}
	  
	  public SharedURL(String channel, String jid, String url, String annotation, String title, String description){
		  this.channelName = channel;
		  this.urlString = url;
		  this.jid = jid;
		  this.annotation = annotation;
		  this.title = title.substring(0, Math.min(MAX_TITLE_SIZE, title.length()));
		  this.description = description;
		  this.time = new Date();
	  }
	  
	  public String getUrl(){
		  return urlString;
	  }
  
	  public String getTitle(){
		  return title;
	  }

	  public String getDescription() {
		  return description;
	  }

	  public String getAnnotation() {
		  return annotation;
	  }

	  public Date getTime() {
		  return time;
	  }

	  public String getJID() {
		  return jid;
	  }
	  
	  public String getChannel(){
		  return channelName;
	  }

	
  }