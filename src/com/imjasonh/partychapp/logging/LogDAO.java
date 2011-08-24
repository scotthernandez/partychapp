package com.imjasonh.partychapp.logging;

import java.util.Date;

import java.util.List;
import java.util.logging.Logger;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.imjasonh.partychapp.Ofy;


public class LogDAO {
	private static Objectify ofy = Ofy.instance();
	  
    @SuppressWarnings("unused")
	private static final Logger logger = 
        Logger.getLogger(LogDAO.class.getName());
  
    static{
    	ObjectifyService.register(LogEntry.class);
    }
    
    public static void put(Object o){
    	if (o instanceof LogEntry){
    		ofy.put(o);
    	}
    }
    
    public static void deleteAll(){
    	ofy.delete(ofy.query(LogEntry.class).fetchKeys());
    }
    
    public static List<LogEntry> getLogByChannel(String channel, int limit, int offset){
  	  List<LogEntry> log = ofy.query(LogEntry.class)
  		  .filter("channelName", channel)
  		  .order("-timestamp")
  		  .limit(limit)
  		  .offset(offset)
  		  .list();
  	  return log;
    }
    
    public static List<LogEntry> getLogByDates(String channel, Date start, Date finish){
    	List<LogEntry> log = ofy.query(LogEntry.class)
		  .filter("channelName", channel)
		  .filter("timestamp >=", start)
		  .filter("timestamp <=", finish)
		  .order("-timestamp")
		  .list();
	  return log;
    }
    
    public static void deleteLogByDates(String channel, Date start, Date finish){
	  ofy.delete(ofy.query(LogEntry.class)
			  		.filter("channelName", channel)
			  		.filter("timestamp >=", start)
			  		.filter("timestamp <=", finish)
			  		.order("-timestamp")
			  		.fetchKeys());
    }

}
