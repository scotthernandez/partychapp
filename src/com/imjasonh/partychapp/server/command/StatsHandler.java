package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;

public class StatsHandler extends SlashCommand {
  public StatsHandler() {
    super("stats");
  }
  
  @Override
  public void doCommand(Message msg, String argument) {
    Datastore.Stats stats = Datastore.instance().getStats(false);    
    msg.channel.sendDirect(stats.toString(), msg.member);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MOD);
  }
}
