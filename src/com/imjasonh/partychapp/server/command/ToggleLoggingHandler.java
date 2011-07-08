package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

/**
 * Toggle the logging setting for a room.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ToggleLoggingHandler extends SlashCommand {

  public ToggleLoggingHandler() {
    super("togglelogging", "toggle-logging", "toggle_logging");
  }

  @Override
  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;
    
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
}
