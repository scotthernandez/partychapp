package com.imjasonh.partychapp.filters;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;
import com.imjasonh.partychapp.urlinfo.UrlInfo;


  public class SharedURL implements Serializable{
	  

	  //private list sharedURL
	  public static final int SHARED_URL_LIMIT = 5;
	  
	  
	  private final URI url;
	  private final String title;
	  private final String description;
	  private final String annotation;
	  private final Date time;
	  private final Member member;
	  
	  public SharedURL(Member m, URI u, String a, String t, String d){
		  url = u;
		  member = m;
		  annotation = a;
		  title = t;
		  description = d;
		  time = new Date();
		  
	  }
	  
	  public URI getUrl(){
		  return url;
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

	public Member getMember() {
		return member;
	}
	
  }