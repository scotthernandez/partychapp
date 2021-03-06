package com.imjasonh.partychapp.server.command;


import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;
import com.imjasonh.partychapp.logging.LogJSONUtil;
import com.xgen.chat.clienthub.ClientHubAPI;
import com.xgen.chat.clienthub.ClientHubAPIException;
import com.xgen.chat.clienthub.ClientHubHelper;

public class BreakLogHandler extends SlashCommand {

	  private static final Logger logger = 
	      Logger.getLogger(BreakLogHandler.class.getName());
	  
	public BreakLogHandler(){
		super("break", "commit");
	}

	@Override
	public void doCommand(Message msg, String argument) {
		Channel channel = msg.channel;
		try {
			if (ClientHubHelper.instance().isClient(channel)){
				try{
					List<LogEntry> log = LogDAO.getLogByDates(channel.getName(), channel.getLogSectionStart(), channel.getLogSectionEnd());
					JSONArray json = LogJSONUtil.entriesMillisecondDate(log);
					ClientHubAPI.postLogJSON(channel.getName(), json);
					channel.setLogSectionStart(new Date());
					channel.setLogSectionEnd(new Date());
					channel.put();
					channel.sendDirect("Post to ClientHub was successful.", msg.member);
				}catch (Exception e) {
				      channel.sendDirect("Posting to ClientHub failed.  Check the GAE log, and notify to dcrosta.", msg.member);
						
					logger.warning("Weird exception trying to /break.");
				}
			}else{
				channel.sendDirect("Channel isn't linked to CH.  If it should, report.", msg.member);
			}
		} catch (ClientHubAPIException e) {
			logger.log(Level.WARNING, e.toString());
		}
	}

	@Override
	public String documentation() {
		// Return null so it doesn't come up in documentation.
		return null;
	}
}
