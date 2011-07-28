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
  INCOMING_EMAIL(new IncomingEmailHandler(), Type.GRAB),
  INCOMING_SMS(new IncomingSMSHandler(), Type.GRAB),
  
  // these implicit handlers have to be first
  CREATE_AND_JOIN(new CreateAndJoinCommand(), Type.PASS),
  JOIN(new JoinCommand(), Type.PASS),
  //LOG(new LogHandler(), Type.PASS),
  
  // these can be in any order
  LEAVE(new LeaveHandler(), Type.GRAB),
  ALERTME(new AlertedHandler(), Type.GRAB, Category.HIDDEN),
  HIDDEN(new HiddenHandler(), Type.GRAB, Category.HIDDEN),
  LIST(new ListHandler(), Type.GRAB),
  HELP(new HelpHandler(), Type.GRAB),
  ALIAS(new AliasHandler(), Type.GRAB),
  SCORE(new ScoreHandler(), Type.GRAB),
  REASONS(new ReasonsHandler(), Type.GRAB),
  ME(new MeHandler(), Type.GRAB),
  SHARE(new ShareHandler(ChainedUrlInfoService.DEFAULT_SERVICE), Type.GRAB),
  SHARED(new SharedHandler(), Type.GRAB),
  INVITE_ONLY(new InviteOnlyHandler(), Type.GRAB),
  TOGGLE_LOGGING(new ToggleShortTermLoggingHandler(), Type.GRAB),
  INVITE(new InviteHandler(), Type.GRAB),
  KICK(new KickHandler(), Type.GRAB),
  STATUS(new StatusHandler(), Type.GRAB),
  SUMMON(new SummonHandler(), Type.GRAB),
  DEBUG(new DebugHandler(), Type.GRAB, Category.HIDDEN),
  STATS(new StatsHandler(), Type.GRAB, Category.HIDDEN),
  GRAPH_SCORES(new GraphScoreHandler(), Type.GRAB),
  SNOOZE(new SnoozeHandler(), Type.GRAB),
  SET_PHONE_NUMBER(new SetPhoneNumberHandler(), Type.GRAB, Category.HIDDEN),
  SET_CARRIER(new SetCarrierHandler(), Type.GRAB, Category.HIDDEN),
  BROADCAST_SMS(new SendBroadcastSMSHandler(), Type.GRAB, Category.HIDDEN),
  DELETELOG(new DeleteLogHandler(), Type.GRAB),
  TESTING(new TestPlaceholderHandler(), Type.PASS),
  
  //Must go here to filter out bad SlashCommands
  SLASHMISTAKE(new SlashMistakeHandler(), Type.GRAB),
  
  // these have to be after the slash-commands
  SEARCHREPLACE(new SearchReplaceHandler(), Type.GRAB),
  PLUSPLUSBOT(new PPBHandler(), Type.GRAB),

  // this has to be last
  TICKETFILTER(new TicketFilter(), Type.GRAB),
  LINKFILTER(new LinkFilter(ChainedUrlInfoService.DEFAULT_SERVICE), Type.GRAB),
  BROADCAST(new BroadcastHandler(), Type.GRAB),
  ;
  
  public enum Category {
    DEFAULT,
    HIDDEN
  }
  
  public enum Type {
	  GRAB,
	  PASS
  }

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(Command.class.getName());
  
  public final CommandHandler commandHandler;
  public final Category category;
  public final Type type;

  private Command(CommandHandler commandHandler, Type type) {
    this(commandHandler, type, Category.DEFAULT);
  }
  
  private Command(CommandHandler commandHandler, Type type, Category category){
	  this.commandHandler = commandHandler;
	  this.category = category;
	  this.type = type;
  }

  public static void getCommandHandler(Message msg) {
    for (Command command : Command.values()) {
      if (command.commandHandler.matches(msg) && command.commandHandler.allows(msg)) {
    	  command.commandHandler.doCommand(msg);
    	  if (command.type == Type.GRAB){
    		  return; 
    	  }
      }
    }
    throw new RuntimeException("getCommandHandler should never return null, " +
                               "but we can't find a match. msg = " + msg.toString());
  }
}
