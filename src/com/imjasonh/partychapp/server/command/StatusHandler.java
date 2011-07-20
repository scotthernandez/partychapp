package com.imjasonh.partychapp.server.command;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.DebuggingOptions.Option;
import com.imjasonh.partychapp.Member.Permissions;

public class StatusHandler extends SlashCommand {
  
  public StatusHandler() {
    super("status");
  }

  @Override
  public void doCommand(Message msg, String argument) {
    String reply = "You are currently in '" + msg.channel.getName() + "' as '" + msg.member.getAlias() + ".'";
    User u = msg.user;
    if (u.phoneNumber() != null) {
      reply += " Your phone number is " + u.phoneNumber() + ".";
    }
    if (u.carrier() != null) {
      reply += " Your carrier is " + u.carrier().shortName + ".";
    }
    if (msg.member.debugOptions().isEnabled(Option.SEQUENCE_IDS)) {
      reply += "\nCurrent sequence ID: " + msg.channel.getSequenceId();
    }
    msg.channel.sendDirect(reply, msg.member);
  }
  
  public String documentation() {
    return "/status - show what room you're in";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
