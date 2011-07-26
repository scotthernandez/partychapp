package com.imjasonh.partychapp.filters;

import java.util.List;
import java.util.logging.Logger;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.imjasonh.partychapp.Ofy;

public class SharedURLDAO {
	private static Objectify ofy = Ofy.instance();
	  
    @SuppressWarnings("unused")
    private static final Logger logger = 
        Logger.getLogger(SharedURLDAO.class.getName());
  
    static{
    	ObjectifyService.register(SharedURL.class);
    }
    
    public static void put(Object o){
    	if (o instanceof SharedURL){
    		  ofy.put(o);
    	}
    }
    
    public static void deleteAll(){
    	ofy.delete(ofy.query(SharedURL.class).fetchKeys());
    }
    
    public static boolean storeURL(SharedURL toShare){
    	List<SharedURL> shared = getURLsByChannelByDate(toShare.getChannel());
		for (SharedURL existing : shared){
			  if (existing.getUrl().equals(toShare.getUrl())){
				  return false;
			  }
		  }
		  shared.add(0, toShare);
		  if (shared.size() > SharedURL.SHARED_URL_LIMIT){
			  ofy.delete(shared.get(SharedURL.SHARED_URL_LIMIT));
			  shared.remove(SharedURL.SHARED_URL_LIMIT);
		  }
		  put(toShare);
		  return true;
    }
    
    public static List<SharedURL> getURLsByChannelByDate(String channel){
  	  List<SharedURL> urls = ofy.query(SharedURL.class)
  		  .filter("channelName", channel)
  		  .order("-time")
  		  .list();
  	  return urls;
    }
    
    
    public static String getURLByIndex(String channel, int index){
      List<SharedURL> shared = getURLsByChannelByDate(channel);
  	  if (shared.get(index) != null){
  		  return shared.get(index).getUrl();
  	  }
	  return null;
    }
}
