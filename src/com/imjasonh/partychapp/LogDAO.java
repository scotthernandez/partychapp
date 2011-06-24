package com.imjasonh.partychapp;

import java.util.logging.Logger;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class LogDAO {
	private Objectify ofy = Ofy.instance();
	  
    @SuppressWarnings("unused")
    private static final Logger logger = 
        Logger.getLogger(Datastore.class.getName());
  
    static{
    	ObjectifyService.register(LogEntry.class);
    }
}
