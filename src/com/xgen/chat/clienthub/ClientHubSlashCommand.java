package com.xgen.chat.clienthub;

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
			if (argument == null){
			      if (ClientHubHelper.instance().isClient(msg.channel)){
			    	  StringBuilder b = new StringBuilder();
			    	  b.append("Room is recognized as client.  To link do /clienthub join. \n");
			    	  b.append("The following contacts are not members of the room and will be invited when linked: \n");
			    	  for (ClientHubContact c : ClientHubHelper.instance().getContactsIfClient(msg.channel).values()){
			    		  if (msg.channel.getMemberByJID(c.getEmail()) == null){
				    		  b.append(c.getEmail() + "\n");
			    		  }
			    	  }
			    	  msg.channel.sendDirect(b.toString(), msg.member);
			      }else{
			    	  msg.channel.sendDirect("Room not recognized as client", msg.member);
			      }
				
			}else if (argument.equals("link")){
			      if (ClientHubHelper.instance().isClient(msg.channel)){
			    	  StringBuilder b = new StringBuilder();
			    	  b.append("Succesfully linked channel. The following contacts were invited: \n");
			    	  for (Member m : ClientHubHelper.instance().addAllContactsIfClient(msg.channel)){
			    		  b.append(m.getJID() + "\n");
			    	  }
			    	  msg.channel.sendDirect(b.toString(), msg.member);
			      }else{
			    	  msg.channel.sendDirect("Apparently not a client.", msg.member);
			      }
			}else{
				msg.channel.sendDirect("Unknown command.", msg.member);
			}
		}catch(ClientHubAPIException e){
	  		msg.channel.sendDirect("Failed to create channel. " +
	  				" There was a problem connecting with ClientHub. " +
	  				" Notify the admins and check the log.", msg.member);
	    }
		
	}

}
