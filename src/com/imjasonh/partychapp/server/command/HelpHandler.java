package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.Permissions;

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
      if (docs != null && command.category != Command.Category.HIDDEN && command.commandHandler.allows(msg)) {
        sb.append("* ")
            .append(command.commandHandler.documentation())
            .append('\n');
      }
    }
    sb.append("* Found a bug? Let us know: https://github.com/circuitlego/10genchat/issues\n")
        .append("* Follow us for announcements at http://twitter.com/partychat");

    msg.channel.sendDirect(sb.toString(), msg.member);
  }

  public String documentation() {
    return "/help - shows this";
  }
  
  @Override
  public boolean allows(Message msg) {
  	return msg.member.hasPermissions(Permissions.MEMBER);
  }
}
