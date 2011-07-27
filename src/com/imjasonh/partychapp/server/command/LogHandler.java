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

public class LogHandler implements CommandHandler {

	  private static final Logger logger = 
	      Logger.getLogger(LogHandler.class.getName());

	private static final long MAX_DELAY = 1000*60*10; //10m
	@Override
	public void doCommand(Message msg) {
		Channel channel = msg.channel;
		Date now = new Date();
		
		//TODO if(msg.channel.isHubLinked()){
		if (now.getTime() - msg.channel.getLogSectionEnd().getTime() > MAX_DELAY){
			System.out.println("BREAK");
			List<LogEntry> log = LogDAO.getLogByDates(channel.getName(), channel.getLogSectionStart(), channel.getLogSectionEnd());
			JSONArray json = LogJSONUtil.entriesMillisecondDate(log);
			try{
				if(ClientHubAPI.postLogJSON(channel.getName(), json)){
					logger.info("Sent logs from " + channel.getLogSectionStart() 
							    + " to " + msg.channel.getLogSectionStart() 
							    + " to ClientHub client " + msg.channel.getName() + "successfully.");
				}else{
					logger.warning("Failed to send logs to ClientHub client " + channel.getName());
				}
			}catch(Exception e){
				logger.severe(e.toString());
				e.printStackTrace();
			}
			
			channel.setLogSectionStart(now);
		}
		
		channel.setLogSectionEnd(now);
		LogDAO.put(new LogEntry(msg));
	}

	@Override
	public boolean matches(Message msg) {
		return !msg.channel.isLoggingDisabled();
	}

	@Override
	public String documentation() {
		// Return null so it doesn't come up in documentation.
		return null;
	}

	@Override
	public boolean allows(Message msg) {
		return msg.member.hasPermissions(Permissions.MEMBER);
	}
}
