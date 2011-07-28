package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.filters.SlashMistakeHandler;
import com.imjasonh.partychapp.filters.TicketFilter;
import com.imjasonh.partychapp.filters.LinkFilter;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;

import java.util.logging.Logger;

public enum Command {
  // just to avoid craziness, let's assume we only let people broadcast from
  // email and sms, so let's steal these and never let the slash-commands see
  // them.
  INCOMING_EMAIL(new IncomingEmailHandler()),
  INCOMING_SMS(new IncomingSMSHandler()),
  
  // these implicit handlers have to be first
  CREATE_AND_JOIN(new CreateAndJoinCommand()),
  JOIN(new JoinCommand()),
  
  // these can be in any order
  LEAVE(new LeaveHandler()),
  ALERTME(new AlertedHandler()),
  HIDDEN(new HiddenHandler()),
  LIST(new ListHandler()),
  HELP(new HelpHandler()),
  ALIAS(new AliasHandler()),
  SCORE(new ScoreHandler()),
  REASONS(new ReasonsHandler()),
  ME(new MeHandler()),
  SHARE(new ShareHandler(ChainedUrlInfoService.DEFAULT_SERVICE)),
  SHARED(new SharedHandler()),
  INVITE_ONLY(new InviteOnlyHandler()),
  TOGGLE_LOGGING(new ToggleShortTermLoggingHandler()),
  INVITE(new InviteHandler()),
  KICK(new KickHandler()),
  STATUS(new StatusHandler()),
  SUMMON(new SummonHandler()),
  DEBUG(new DebugHandler(),Category.HIDDEN),
  STATS(new StatsHandler(),Category.HIDDEN),
  GRAPH_SCORES(new GraphScoreHandler()),
  SNOOZE(new SnoozeHandler()),
  SET_PHONE_NUMBER(new SetPhoneNumberHandler(),Category.HIDDEN),
  SET_CARRIER(new SetCarrierHandler(),Category.HIDDEN),
  BROADCAST_SMS(new SendBroadcastSMSHandler(),Category.HIDDEN),
  DELETELOG(new DeleteLogHandler()),
  TESTING(new TestPlaceholderHandler()),
  
  
  //Must go here to filter out bad SlashCommands
  SLASHMISTAKE(new SlashMistakeHandler()),
  
  // these have to be after the slash-commands
  SEARCHREPLACE(new SearchReplaceHandler()),
  PLUSPLUSBOT(new PPBHandler()),

  // this has to be last
  TICKETFILTER(new TicketFilter()),
  LINKFILTER(new LinkFilter(ChainedUrlInfoService.DEFAULT_SERVICE)),
  BROADCAST(new BroadcastHandler()),
  ;
  
  public enum Category {
    DEFAULT,
    HIDDEN
  }
  
  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(Command.class.getName());
  
  public final CommandHandler commandHandler;
  public final Category category;

  private Command(CommandHandler commandHandler) {
    this(commandHandler, Category.DEFAULT);
  }
  
  private Command(CommandHandler commandHandler, Category category){
	  this.commandHandler = commandHandler;
	  this.category = category;
  }

  public static void getCommandHandler(Message msg) {
    for (Command command : Command.values()) {
      if (command.commandHandler.matches(msg) && command.commandHandler.allows(msg)) {
    	  command.commandHandler.doCommand(msg);
    	  return;
      }
    }
    throw new RuntimeException("getCommandHandler should never return null, " +
                               "but we can't find a match. msg = " + msg.toString());
  }
}
