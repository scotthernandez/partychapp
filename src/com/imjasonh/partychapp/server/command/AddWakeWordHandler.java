package com.imjasonh.partychapp.server.command;

import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;


public class AddWakeWordHandler extends SlashCommand {

	  @SuppressWarnings("unused")
	private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public AddWakeWordHandler() {
    super("addwake");
  }

  @Override
  public void doCommand(Message msg, String action) {

      StringBuilder sb = new StringBuilder();
      sb.append("Words added (case insensitive): \n");
      
	  String[] words = action.split(",");
	  for(int i = 0; i < words.length; i ++){
		  String word = words[i].trim();
		  if (msg.member.addWakeWord(word)){
			  sb.append(word);
			  if (i < words.length) sb.append("\n");
		  }
	  }
	  
	  msg.channel.sendDirect(sb.toString(), msg.member);
	  msg.channel.put();
	  
  }

  public String documentation() {
    return "/addwake word1[, word2, ...] - add words that will wake you from snoozing";
  }
  
}
