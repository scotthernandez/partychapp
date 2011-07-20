package com.imjasonh.partychapp.server.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Message;

/**
 * This handler, to be put after all other SlashCommands, makes
 * sure the channel doesn't see failed attempts of a user trying
 * to make a command.
 * 
 * @author Gabriel
 *
 */
public class SlashMistakeHandler implements CommandHandler{

	  private static Pattern pattern =
	      Pattern.compile("^/.*");

	  public void doCommand(Message msg) {
	    
	    msg.channel.sendDirect("Slash command not recognized. Try '/help'.", msg.member);
	    
	  }

	  public String documentation() {
	    return null;
	  }


	@Override
	public boolean matches(Message msg) {
		Matcher m = pattern.matcher(msg.content);
		return m.matches();
	}

}
