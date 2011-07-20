package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;

/**
 * Toggle the logging setting for a room.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ToggleShortTermLoggingHandler extends SlashCommand {

  public ToggleShortTermLoggingHandler() {
    super("togglelogging", "toggle-logging", "toggle_logging");
  }

  @Override
  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if(!msg.member.hasPermissions(Permissions.MOD)){
    	msg.channel.sendDirect("You do not have enough permissions to /invite someone.", msg.member);
    	return;
    }
    
    msg.channel.setMiniLogDisabled(!msg.channel.isMiniLogDisabled());
    msg.channel.put();
    
    String broadcast = "_" + msg.member.getAlias() + " has " +
        (msg.channel.isMiniLogDisabled() ? "disabled" : "enabled") +
        " logging._";
    msg.channel.broadcastIncludingSender(broadcast);
  }

  public String documentation() {
    return "/togglelogging - Enable or disable logging for a room.";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MOD);
  }
}
