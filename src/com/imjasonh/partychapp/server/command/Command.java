package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.GraphScoreHandler;
import com.imjasonh.partychapp.ppb.PPBHandler;
import com.imjasonh.partychapp.ppb.ReasonsHandler;
import com.imjasonh.partychapp.ppb.ScoreHandler;
import com.imjasonh.partychapp.server.command.share.LinkFilter;
import com.imjasonh.partychapp.server.command.share.ShareHandler;
import com.imjasonh.partychapp.server.command.share.SharedHandler;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;
import com.xgen.chat.clienthub.ClientHubSlashCommand;
import com.xgen.chat.commands.TicketFilter;
import com.xgen.chat.permissions.MemberPermissions;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;

import java.util.logging.Logger;


/*
 * List of ways I have modified this class that I should not need to:
 *  - Manual adding of handlers
 *  - Checking permissions.  I couldn't extend either this class (an enum) 
 *    or some of the commands themselves (package protected).
 *    
 *  --gabriel--
 */

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
  //INVITE_ONLY(new InviteOnlyHandler()),
  //TOGGLE_LOGGING(new ToggleShortTermLoggingHandler()),
  INVITE(new InviteHandler(), PermissionLevel.MOD),
  KICK(new KickHandler(), PermissionLevel.MOD),
  STATUS(new StatusHandler()),
  SUMMON(new SummonHandler()),
  DEBUG(new DebugHandler(), PermissionLevel.MOD,Category.HIDDEN),
  STATS(new StatsHandler(), PermissionLevel.MOD,Category.HIDDEN),
  GRAPH_SCORES(new GraphScoreHandler()),
  SNOOZE(new SnoozeHandler()),
  //SET_PHONE_NUMBER(new SetPhoneNumberHandler(),Category.HIDDEN),
  //SET_CARRIER(new SetCarrierHandler(),Category.HIDDEN),
  //BROADCAST_SMS(new SendBroadcastSMSHandler(),Category.HIDDEN),
  DELETELOG(new DeleteLogHandler(), PermissionLevel.ADMIN),
  CLIENTHUBLOG(new BreakLogHandler(), PermissionLevel.MOD),
  ADDWAKE(new AddWakeWordHandler()),
  VIEWWAKE(new ViewWakeWordHandler()),
  REMOVEWAKE(new RemoveWakeWordHandler()),
  IMPORTANT(new ImportantHandler()),
  TEST(new TestPlaceholderHandler()),
  CLIENTHUB(new ClientHubSlashCommand()),
  
  
  //Must go here to filter out bad SlashCommands
  SLASHMISTAKE(new SlashMistakeHandler()),
  
  // these have to be after the slash-commands
  SEARCHREPLACE(new SearchReplaceHandler()),
  PLUSPLUSBOT(new PPBHandler()),
  TICKETFILTER(new TicketFilter()),
  LINKFILTER(new LinkFilter(ChainedUrlInfoService.DEFAULT_SERVICE)),

  // this has to be last
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
  public final PermissionLevel level;

  private Command(CommandHandler commandHandler) {
    this(commandHandler, PermissionLevel.MEMBER, Category.DEFAULT);
  }
  
  private Command(CommandHandler commandHandler, PermissionLevel level) {
	    this(commandHandler, level, Category.DEFAULT);
  }
  
  private Command(CommandHandler commandHandler, PermissionLevel level, Category category){
	  this.commandHandler = commandHandler;
	  this.level = level;
	  this.category = category;
  }

  public static void getCommandHandler(Message msg) {
    for (Command command : Command.values()) {
      if (command.commandHandler.matches(msg)){
    	  if(MemberPermissions.instance().hasLevel(msg.channel, msg.member, command.level)) {
        	  command.commandHandler.doCommand(msg);
    	  }else{
    		  msg.channel.sendDirect("Not enough permissions to do command.", msg.member);
    	  }
    	  return;
      }
    }
    throw new RuntimeException("getCommandHandler should never return null, " +
                               "but we can't find a match. msg = " + msg.toString());
  }
}
