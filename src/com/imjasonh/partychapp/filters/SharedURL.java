package com.imjasonh.partychapp.filters;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;
import com.imjasonh.partychapp.urlinfo.UrlInfo;

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
	  
	  public SharedURL(String c, String j, String u, String a, String t, String d){
		  channelName = c;
		  urlString = u;
		  jid = j;
		  annotation = a;
		  title = t;
		  description = d;
		  time = new Date();
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