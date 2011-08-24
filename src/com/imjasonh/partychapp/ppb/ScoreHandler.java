package com.imjasonh.partychapp.ppb;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.command.SlashCommand;

public class ScoreHandler extends SlashCommand {
  
  public ScoreHandler() {
    super("score\\s+(" + PlusPlusBot.targetPattern + ")");
  }

  @Override
  public void doCommand(Message msg, String name) {
    // TODO: Validate target pattern
    Target target = Datastore.instance().getTarget(msg.channel, name);
    String reply;
    if (target == null) {
      reply = "no scores found";
    } else {
      reply = name + ": " + target.score();
    }
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/score target - see the score of 'target' in plusplusbot";
  }

}
