package com.imjasonh.partychapp.server.command;

import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;


public class RemoveWakeWordHandler extends SlashCommand {

	  @SuppressWarnings("unused")
	private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public RemoveWakeWordHandler() {
    super("rmwake", "removewake");
  }

  @Override
  public void doCommand(Message msg, String action) {

      StringBuilder sb = new StringBuilder();
      sb.append("Words removed: \n");
      
	  String[] words = action.split(",");
	  for(int i = 0; i < words.length; i ++){
		  String word = words[i].trim();
		  if (msg.member.removeWakeWord(word)){
			  sb.append(word);
			  if (i < words.length) sb.append("\n");
		  }
	  }
	  
	  msg.channel.sendDirect(sb.toString(), msg.member);
	  msg.channel.put();
  }

  public String documentation() {
    return "/rmwake word1[, word2, ...] - remove words that wake you from snoozing.";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
