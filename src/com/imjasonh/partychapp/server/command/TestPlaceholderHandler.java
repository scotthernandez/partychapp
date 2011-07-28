package com.imjasonh.partychapp.server.command;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;
import com.imjasonh.partychapp.logging.LogJSONUtil;
import com.xgen.partychapp.clienthub.ClientHubAPI;


public class TestPlaceholderHandler extends SlashCommand {

	  private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public TestPlaceholderHandler() {
    super("testlog");
  }

  @Override
  public void doCommand(Message msg, String action) {
	  
	    Channel channel = msg.channel;
		Date now = new Date();
		
		try{
			if (action.matches("\\bbreak\\s.*")){
					System.out.println("BREAK");
					List<LogEntry> log = LogDAO.getLogByDates(channel.getName(), channel.getLogSectionStart(), channel.getLogSectionEnd());
					JSONArray json = LogJSONUtil.entriesMillisecondDate(log);
					
						if(log.size() > 0 && ClientHubAPI.postLogJSON(channel.getName(), json)){
							logger.info("Sent logs from " + channel.getLogSectionStart() 
									    + " to " + msg.channel.getLogSectionEnd() 
									    + " to ClientHub client " + msg.channel.getName() + " successfully.");
	
							channel.setLogSectionStart(now);
						}
				}
			
		}catch(Exception e){
			logger.severe(e.toString());
			e.printStackTrace();
			
		}finally{
			channel.setLogSectionEnd(now);
			channel.put();
			
			channel.broadcast(msg);
				    
			//Always log.
			LogDAO.put(new LogEntry(msg));
		}
  }

  public String documentation() {
    return null;
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
