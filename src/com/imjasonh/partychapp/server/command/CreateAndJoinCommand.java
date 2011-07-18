package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;
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

    msg.channel = new Channel(msg.serverJID);
    msg.member = msg.channel.addMember(msg.user);
    msg.member.setPermissions(Permissions.ADMIN);
    msg.channel.put();
    
    String reply = "The channel '" + msg.channel.getName() + "' has been created, " +
        "you have joined with the alias '" + msg.member.getAlias() + "', and you are an administrator.";
    msg.channel.sendDirect(reply, msg.member);
    
  }

  public String documentation() {
    return null;
  }

  public boolean matches(Message msg) {
    return (msg.channel == null) && msg.messageType.equals(MessageType.XMPP);
  }
}
