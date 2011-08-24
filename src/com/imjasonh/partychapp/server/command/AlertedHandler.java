package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class AlertedHandler extends SlashCommand {
  
  AlertedHandler() {
    super("alert", "setalert", "alerted", "setalerted", "alertme");
  }

  @Override
  public void doCommand(Message msg, String argument) {
		String youMsg;
		
		if (argument != null) {
			if (argument.toUpperCase().compareTo("FALSE") == 0) {
				youMsg = "You will no longer receive messages from the channel '" + msg.channel.getName() + "'";
				msg.member.setAlerted(false);
			} else if (argument.toUpperCase().compareTo("TRUE") == 0) {
				msg.member.setAlerted(true);
				youMsg = "You will now receive messages from the channel '" + msg.channel.getName() + "'";
			} else {
				youMsg = "Only valid options for /alert are true and false";
			}
		} else {
			youMsg = "/alert must be followed by true or false";
		}

	    msg.channel.sendDirect(youMsg, msg.member);
	    
	    msg.channel.put();
	  
	  }
	  
	  public String documentation() {
		//"/alertme - set whether or not you receive messages and alerts for this room"
		//for user admins only
	    return null;
	  }
	  
	}
