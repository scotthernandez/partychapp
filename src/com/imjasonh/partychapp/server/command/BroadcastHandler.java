package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.Message.MessageType;

public class BroadcastHandler implements CommandHandler {
  public void doCommand(Message msg) {
    msg.channel.broadcast(msg);
    msg.member.addToLastMessages(msg.content);
    msg.channel.put();
  }

  public String documentation() {
    // return null so this doesn't show up in /help documentation.
    return null;
  }

  public boolean matches(Message msg) {
    // This has to be last in the list, because it swallows every XMPP message.
    return msg.messageType.equals(MessageType.XMPP);
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
