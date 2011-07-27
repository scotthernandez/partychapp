package com.imjasonh.partychapp.filters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.filters.SharedURL;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.command.Command;
import com.imjasonh.partychapp.server.command.CommandHandler;
import com.imjasonh.partychapp.server.command.ShareHandler;
import com.imjasonh.partychapp.urlinfo.UrlInfo;
import com.imjasonh.partychapp.urlinfo.UrlInfoService;


public class LinkFilter implements CommandHandler {
	  private final UrlInfoService urlInfoService;
	  
	  public LinkFilter(UrlInfoService urlInfoService){
		    this.urlInfoService = urlInfoService;
	  }
	
	  private static Pattern pattern =
	      Pattern.compile("(?xi)\\b" +
	      				  "((?:" +
	      				     "https?:\\/\\/" +
	      				     "(www\\d{0,3}[.]" +
	      				     "|[a-z0-9.\\-]+[.][a-z]{2,4}\\/)" +
	      				   ")" +
	      				   "(?:[^\\s()<>]+" +
	      				      "|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)" +
	      				    ")+" +
	      				    "(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)" +
	      				       "|[^\\s`!()\\[\\]{};:'\".,<>?гхрсту]))");

	@Override
	public void doCommand(Message msg) {
		Matcher m = pattern.matcher(msg.content);
		

        Command.BROADCAST.commandHandler.doCommand(msg);
        while(m.find()){
            String urlStr = m.group();
            try{
            	URI url = new URI(urlStr);
                UrlInfo urlInfo = urlInfoService.getUrlInfo(url);
            	SharedURL toShare = new SharedURL(msg.channel.getName(), msg.member.getJID(), url.toString(), "", urlInfo.getTitle(), urlInfo.getDescription());
            	if (SharedURLDAO.storeURL(toShare)){
	            	ShareHandler.sendShareBroadcast(toShare, msg.channel);
            	}
            	
            } catch (URISyntaxException err) {
            	//This should never be called.
                msg.channel.sendDirect("Internal error trying to share parsed link.  Please report.", msg.member);
                return;
            }
        }
		
	}

	@Override
	public boolean matches(Message msg) {
		Matcher m = pattern.matcher(msg.content);
		return m.find();
	}

	@Override
	public String documentation() {
		// Nothing to show in help.
		return null;
	}

	@Override
	public boolean allows(Message msg) {
		return msg.member.hasPermissions(Permissions.MEMBER);
	}

}
