package com.imjasonh.partychapp;

import java.util.List;
import java.util.logging.Logger;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Ofy {

	private static final Logger logger = Logger.getLogger(Ofy.class.getName());
	private static Objectify ofy;
	
	public static Objectify instance(){
		if (ofy == null){
			ofy = ObjectifyService.begin();
		}
		return ofy;
	}

	public static void fixAll(){
		//Clear cache
		MemcacheServiceFactory.getMemcacheService().clearAll();
		
		List<User> users = Ofy.instance().query(User.class).list();
		
		for(Channel c : Ofy.instance().query(Channel.class).fetch()){
			c.fixUp();
			for(User u : users){
				u.fixUp(c);
			}
		}
	}
}
