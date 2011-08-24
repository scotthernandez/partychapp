package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class LeaveHandler extends SlashCommand {
  
  public LeaveHandler() {
    super("leave", "exit", "part");
  }

  @Override
  public void doCommand(Message msg, String argument) {
    // TODO: Reject or act on non-null argument
    msg.channel.removeMember(msg.user);
    msg.channel.put();
    
    String youMsg = "You have left the room '" + msg.channel.getName() + "'";
    msg.channel.sendDirect(youMsg, msg.member);

    String reply = msg.member.getAlias() + " has left the room (" + msg.member.getJID() + ")";
    msg.channel.broadcast(reply, msg.member);
  }
  
  public String documentation() {
    return "/leave - leave the room";
  }

}
