package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class HelpHandler extends SlashCommand {

  public HelpHandler() {
    super("help", "commands");
  }

  @Override
  public void doCommand(Message msg, String argument) {
    // TODO: Reject or act on non-null argument

    StringBuilder sb = new StringBuilder().append("List of commands:").append('\n');
    for (Command command : Command.values()) {
      String docs = command.commandHandler.documentation();
      if (docs != null && command.category != Command.Category.HIDDEN) {
        sb.append("* ")
            .append(command.commandHandler.documentation())
            .append('\n');
      }
    }

    msg.channel.sendDirect(sb.toString(), msg.member);
  }

  public String documentation() {
    return "/help - shows this";
  }
}
