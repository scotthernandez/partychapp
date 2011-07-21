package com.imjasonh.partychapp.server.command;


import java.util.Date;
import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;

public class LogHandler implements CommandHandler {

	  private static final Logger logger = 
	      Logger.getLogger(LogHandler.class.getName());

	private static final long MAX_DELAY = 1000*60*10; //10m
	@Override
	public void doCommand(Message msg) {
		Date now = new Date();
		
		if (now.getTime() - msg.channel.getLogSectionEnd().getTime() > MAX_DELAY){
			logger.warning("Message break");
			LogDAO.sendLogToClientHub(msg.channel.getName(), msg.channel.getLogSectionStart(), msg.channel.getLogSectionEnd());
			msg.channel.setLogSectionStart(now);
		}
		
		msg.channel.setLogSectionEnd(now);
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
