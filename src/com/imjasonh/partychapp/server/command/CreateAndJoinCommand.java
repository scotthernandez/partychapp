package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

/**
 * Action taken when a user messages a channel that does not exist yet. Channel is created and user
 * automatically joins the channel
 * 
 * @author imjasonh@gmail.com
 */
public class CreateAndJoinCommand implements CommandHandler {

  public void doCommand(Message msg) {
    assert msg.channel == null;
    assert msg.member == null;
    try {
	    msg.channel = new Channel(msg.serverJID, msg.user);
	    msg.channel.put();
	    msg.member = msg.channel.getMemberByJID(msg.userJID);
	    
	    String reply = "The channel '" + msg.channel.getName() + "' has been created, " +
	        "you have joined with the alias '" + msg.member.getAlias() + "', and you are an administrator.";
	    msg.channel.sendDirect(reply, msg.member);
  }catch (Exception e) {
		 e.printStackTrace();
  }     
  }

  public String documentation() {
    return null;
  }

  public boolean matches(Message msg) {
    return (msg.channel == null) && msg.messageType.equals(MessageType.XMPP);
  }
  
  @Override
  public boolean allows(Message msg) {
  	return true;
  }
}
