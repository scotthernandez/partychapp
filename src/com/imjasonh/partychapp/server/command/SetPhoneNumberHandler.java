package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;

public class SetPhoneNumberHandler extends SlashCommand {
  public SetPhoneNumberHandler() {
    super("set-phone-number");
  }

  @Override
  public void doCommand(Message msg, String phone) {
    msg.user.setPhoneNumber(phone);
    msg.channel.sendDirect("Set your phone number to " + phone, msg.member);
    msg.user.put();
  }

  public String documentation() {
    return null;
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
