package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class HiddenHandler extends SlashCommand {
  
  HiddenHandler() {
    super("hidden", "sethidden");
  }

  @Override
  public void doCommand(Message msg, String argument) {

	String youMsg;
	if (argument != null) {
		if (argument.toUpperCase().compareTo("FALSE") == 0) {
			youMsg = "You are no longer hidden from the channel '" + msg.channel + "'";
			msg.member.setHidden(false);
		} else if (argument.toUpperCase().compareTo("TRUE") ==0) {
			msg.member.setHidden(true);
			youMsg = "You are now hidden from the channel '" + msg.channel + "'";
		} else {
			youMsg = "Only valid options for /hidden are true and false";
		}
	} else {
		youMsg = "/hidden must be followed by true or false";
	}

    msg.channel.sendDirect(youMsg, msg.member);
    
    msg.channel.put();
  
  }
  
  public String documentation() {
	//"/hidden - set whether or not you show up in the list of room members"
	//for user admins only
    return null;
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.user.isAdmin();
  }
}
