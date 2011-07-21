package com.imjasonh.partychapp.server.command;

import java.util.List;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;

public class SendBroadcastSMSHandler extends SlashCommand {

  public SendBroadcastSMSHandler() {
    super("broadcast-sms");
  }
  
  @Override
  void doCommand(Message msg, String argument) {
    List<Member> recipients = msg.channel.broadcastSMS(msg.member.getAliasPrefix() + argument);
    String memberNames = "";
    for (Member m : recipients) {
      if (!memberNames.isEmpty()) {
        memberNames += ", ";
      }
      memberNames += m.getAlias();
    }

    msg.channel.broadcastIncludingSender("[" + msg.member.getAlias() + " **broadcasting to sms**] " +
                                         argument);
    msg.channel.sendDirect("message was sent to: " + memberNames, msg.member);
  }

  public String documentation() {
    return null;
  }

  @Override
  public boolean allows(Message msg) {
  	return true;
  }
}
