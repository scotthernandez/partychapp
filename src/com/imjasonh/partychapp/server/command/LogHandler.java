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

public class LogHandler extends SlashCommand {

	  private static final Logger logger = 
	      Logger.getLogger(LogHandler.class.getName());
	  
	public LogHandler(){
		super("break", "commit");
	}

	@Override
	public void doCommand(Message msg, String argument) {
		Channel channel = msg.channel;
		if (channel.isHubLinked()){
			channel.logToClientHub();
		}else{
			channel.sendDirect("Channel isn't linked to CH.  If it should, report.", msg.member);
		}
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
