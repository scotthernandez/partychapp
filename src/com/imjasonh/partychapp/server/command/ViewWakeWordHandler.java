package com.imjasonh.partychapp.server.command;

import java.util.Collection;
import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;


public class ViewWakeWordHandler extends SlashCommand {

	  @SuppressWarnings("unused")
	private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public ViewWakeWordHandler() {
    super("wakewords");
  }

  @Override
  public void doCommand(Message msg, String action) {

      StringBuilder sb = new StringBuilder();
      Collection<String> words = msg.member.wakeWords();
      if(words.size() > 0){
          sb.append("_Words that will wake you from snooze_:\n");
		  for (String word: words){
			  sb.append(word + "\n");
		  }
      }else{
    	  sb.append("You haven't added any wake words.  Add some using /addwake.");
      }
	  msg.channel.sendDirect(sb.toString(), msg.member);
  }

  public String documentation() {
    return "/wakewords - list the words that will wake you from snoozing";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
