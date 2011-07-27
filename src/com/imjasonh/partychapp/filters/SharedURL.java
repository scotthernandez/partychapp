package com.imjasonh.partychapp.filters;

import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

@Unindexed
public class SharedURL {
	  

	  //private list sharedURL
	  public static final int SHARED_URL_LIMIT = 5;
	  
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
		  this.title = title;
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