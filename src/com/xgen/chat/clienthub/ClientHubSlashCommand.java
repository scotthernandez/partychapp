package com.xgen.chat.clienthub;

import java.util.logging.Level;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.command.SlashCommand;

public class ClientHubSlashCommand extends SlashCommand {

	public ClientHubSlashCommand() {
		super("clienthub");
	}

	@Override
	public String documentation() {
		return "/clienthub - tries to link the channel with a clienthub client";
	}

	@Override
	protected void doCommand(Message msg, String argument) {
	    try{
	      if (ClientHubHelper.instance().isClient(msg.channel)){
	    	  ClientHubHelper.instance().addAllContactsIfClient(msg.channel);
	    	  msg.channel.sendDirect("Succesfully linked channel.", msg.member);
	      }else{
	    	  msg.channel.sendDirect("Apparently not a client.", msg.member);
	      }
	    }catch(ClientHubAPIException e){
	  		msg.channel.sendDirect("Failed to create channel. " +
	  				" There was a problem connecting with ClientHub. " +
	  				" Notify the admins and check the log.", msg.member);
	    }
		
	}

}
