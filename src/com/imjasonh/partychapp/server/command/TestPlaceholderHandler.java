package com.imjasonh.partychapp.server.command;

import java.util.logging.Logger;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.User;


public class TestPlaceholderHandler extends SlashCommand {

	  @SuppressWarnings("unused")
	private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public TestPlaceholderHandler() {
    super("");
  }

  @Override
  public void doCommand(Message msg, String action) {
  }

  public String documentation() {
    return null;
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
