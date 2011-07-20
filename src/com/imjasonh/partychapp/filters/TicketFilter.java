package com.imjasonh.partychapp.filters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.filters.SharedURL;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.command.Command;
import com.imjasonh.partychapp.server.command.CommandHandler;
import com.imjasonh.partychapp.server.command.ShareHandler;


public class TicketFilter implements CommandHandler {
	
	  private static Pattern pattern =
	      Pattern.compile("\\b[A-Z]+-[0-9]+\\b");

	@Override
	public void doCommand(Message msg) {
		Matcher m = pattern.matcher(msg.content);
		

        Command.BROADCAST.commandHandler.doCommand(msg);
        while(m.find()){
            String urlStr = "http://jira.mongodb.org/browse/" + m.group();
            try{
            	URI url = new URI(urlStr);
            	SharedURL toShare = new SharedURL(msg.channel.getName(), msg.member.getJID(), url.toString(), "", m.group(), "");
            	if (SharedURLDAO.storeURL(toShare)){
	            	ShareHandler sh = (ShareHandler) Command.SHARE.commandHandler;
	            	sh.sendShareBroadcast(toShare, msg.channel);
            	}
            	
            } catch (URISyntaxException err) {
            	//This should never be called.
                msg.channel.sendDirect("Internal error trying to share Jira ticket.  Please report.", msg.member);
                return;
            }
        }
		
	}

	@Override
	public boolean matches(Message msg) {
		Matcher m = pattern.matcher(msg.content);
		return m.find();
	}

	@Override
	public String documentation() {
		// Nothing to show in help.
		return null;
	}


}
