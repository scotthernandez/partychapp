package com.imjasonh.partychapp.server.web;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;
import com.imjasonh.partychapp.logging.LogJSONUtil;
import com.xgen.partychapp.clienthub.ClientHubAPI;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

public class CronClientHubLogServlet  extends HttpServlet {

	@SuppressWarnings("unused")
	private static final Logger logger = 
			Logger.getLogger(CronClientHubLogServlet.class.getName());
  
	public static final long serialVersionUID = 985749740983755L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

      Datastore.instance().startRequest();
      Long now = System.currentTimeMillis();

      List <Channel> channels = Datastore.instance().getAllChannels();
      
      for (Channel channel : channels){
	    try {
    	  if (channel.isHubLinked()) {
	    	  if (now - channel.getLogSectionEnd().getTime() > 1*60*1000){ //more than 1 mins
				  List<LogEntry> log = LogDAO.getLogByDates(channel.getName(), channel.getLogSectionStart(), channel.getLogSectionEnd());
				  JSONArray json = LogJSONUtil.entriesMillisecondDate(log);
				  if(log.size() > 0){
					  if (ClientHubAPI.postLogJSON(channel.getName(), json)){
						  channel.setLogSectionStart(new Date(now));
						  channel.setLogSectionEnd(new Date(now));
						  channel.put();
					  }
				  }
			  }
    	  }
	    } catch (Exception e) {
			e.printStackTrace();
		} finally {
	      Datastore.instance().endRequest();
	    }
      }
   }
  
  
}
