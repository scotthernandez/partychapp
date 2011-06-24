package com.imjasonh.partychapp;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class Ofy {
	
	private static Objectify ofy;
	
	public static Objectify instance(){
		if (ofy == null){
			ofy = ObjectifyService.begin();
		}
		return ofy;
	}

}
