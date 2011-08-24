package com.imjasonh.partychapp.server.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;
//import com.xgen.partychapp.clienthub.ClientHubAPI;
//import com.xgen.partychapp.clienthub.ClientHubAPIException;
//import com.xgen.partychapp.clienthub.ClientHubContact;
import com.xgen.chat.clienthub.ClientHubAPI;
import com.xgen.chat.clienthub.ClientHubAPIException;
import com.xgen.chat.clienthub.ClientHubContact;
import com.xgen.chat.permissions.MemberPermissions;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;


public class TestPlaceholderHandler extends SlashCommand {

	  @SuppressWarnings("unused")
	private static final Logger logger = 
	      Logger.getLogger(TestPlaceholderHandler.class.getName());

  public TestPlaceholderHandler() {
    super("test");
  }

  @Override
  public void doCommand(Message msg, String action) {
	  Collection<ClientHubContact> contacts;
	try {
		  contacts = ClientHubAPI.getClientContacts(action).values();
		  StringBuilder builder = new StringBuilder();
		  for (ClientHubContact c : contacts){
			  builder.append(c.toString() + "\n");
		  }
		  msg.channel.sendDirect(builder.toString(), msg.member);
	} catch (ClientHubAPIException e) {
		e.printStackTrace();
	}
	  

//	  if (action == null || action.isEmpty()){
//		  msg.channel.sendDirect(MemberPermissions.instance().getLevel(msg.channel, msg.member).toString(), msg.member);
//	  }else{
//		  PermissionLevel level = PermissionLevel.fromString(action);
//		  if (level != null){
//			  MemberPermissions.instance().setLevel(msg.channel, msg.member, level);
//			  MemberPermissions.instance().put();
//		  }else{
//			  msg.channel.sendDirect("try again", msg.member);
//		  }
//	  }
  }

  public String documentation() {
    return null;
  }
 
}
