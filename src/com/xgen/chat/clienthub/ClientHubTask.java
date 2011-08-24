package com.xgen.chat.clienthub;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;
import com.imjasonh.partychapp.datastoretask.DatastoreTask;
import com.imjasonh.partychapp.datastoretask.TestableQueue;
import com.xgen.chat.clienthub.ClientHubHelper;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class ClientHubTask extends DatastoreTask {
  private static final Logger logger =
      Logger.getLogger(ClientHubTask.class.getName());
  
  @Override
  public void handle(WebRequest url, TestableQueue q) {

    Datastore.instance().startRequest();
    List<String> keys = keys(url);

    //Long now = System.currentTimeMillis();
    
    for (String key : keys) {
      Channel channel = Datastore.instance().getChannelByName(key);
      try {
    	  if (ClientHubHelper.instance().isClient(channel)) {
//	    	  if (now - channel.getLogSectionEnd().getTime() > 15*60*1000){ //more than 15 mins
//	    		  logger.log(Level.INFO, "Will send logs of channel " + channel.getName());
//
//				  List<LogEntry> log = LogDAO.getLogByDates(channel.getName(), channel.getLogSectionStart(), channel.getLogSectionEnd());
//				  JSONArray json = LogJSONUtil.entriesMillisecondDate(log);
//				  if(log.size() > 0){
//					
//					ClientHubAPI.postLogJSON(channel.getName(), json);
//					
//					channel.setLogSectionStart(new Date(now));
//					channel.setLogSectionEnd(new Date(now));
//					channel.put();
//					  
//				  }
//			  }
    		  //TODO: Add logging.
    	  }
	    } catch (Exception e) {
			logger.warning("Something got screwed up in the cron logger with channel: " + channel.getName());
		}
    }
    
    Datastore.instance().endRequest();
  }

  @Override
  public Iterator<String> getKeyIterator(String lastKeyHandled) {
    return Datastore.instance().getAllEntityKeys(Channel.class, lastKeyHandled);
  }
}
