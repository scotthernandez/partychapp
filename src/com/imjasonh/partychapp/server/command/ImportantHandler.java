package com.imjasonh.partychapp.server.command;

import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;


public class ImportantHandler extends SlashCommand {

	  @SuppressWarnings("unused")
	private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public ImportantHandler() {
    super("!", "important");
  }

  @Override
  public void doCommand(Message msg, String action) {
	  StringBuilder b = new StringBuilder();
	  b.append(msg.member.getAliasPrefix() + "\n");
	  b.append("===============\n");
	  b.append("===Important===\n");
	  b.append("===============\n");
	  b.append(action);
	  b.append("\n");
	  b.append("===============\n");
	  
	  msg.channel.awakenAllSnoozers();
	  msg.channel.broadcastIncludingSender(b.toString());
  }

  public String documentation() {
    return "/! message - Get your point accross with big headers. Use a lot if you want to be kicked.";
  }
}
