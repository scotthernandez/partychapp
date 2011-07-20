package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;

/**
 * Make a room invite-only.
 * 
 * @author kushaldave@gmail.com
 */
public class InviteOnlyHandler extends SlashCommand {

  public InviteOnlyHandler() {
    super("inviteonly");
  }

  @Override
  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;
    
    msg.channel.setInviteOnly(!msg.channel.isInviteOnly());
    msg.channel.put();
    
    String broadcast = "_" + msg.member.getAlias() + " set the room to " +
        (msg.channel.isInviteOnly() ? "" : "not ") + "invite-only._";
    msg.channel.broadcastIncludingSender(broadcast);
  }

  public String documentation() {
    return "/inviteonly - Set the room to invite-only";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.ADMIN);
  }
}
