package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

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

}
