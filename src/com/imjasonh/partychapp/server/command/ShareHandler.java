package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.filters.SharedURL;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.urlinfo.UrlInfo;
import com.imjasonh.partychapp.urlinfo.UrlInfoService;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Command that makes sharing of URLs slightly friendlier (looks up titles).
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ShareHandler extends SlashCommand {
  private final UrlInfoService urlInfoService;

  public ShareHandler(UrlInfoService urlInfoService) {
    super("share");
    this.urlInfoService = urlInfoService;
  }
  
  @Override
  void doCommand(Message msg, String argument) {
    if (Strings.isNullOrEmpty(argument)) {
      msg.channel.sendDirect("No URL to share given.", msg.member);
      return;
    }
    
    String[] pieces = argument.split("\\s+", 2);
    
    URI uri;
    try {
      uri = new URI(pieces[0]);
    } catch (URISyntaxException err) {
      msg.channel.sendDirect("Invalid URL to share given.", msg.member);
      return;
    }
    
    if (!uri.isAbsolute()) {
      msg.channel.sendDirect("URLs to share must be absolute", msg.member);
      return;      
    }
    
    String annotation = null;
    if (pieces.length == 2) {
      annotation = pieces[1];
    }

    UrlInfo urlInfo = urlInfoService.getUrlInfo(uri);
    
    SharedURL shareUrl = new SharedURL(
            msg.member.getJID(),
            uri.toString(),
            annotation,
            urlInfo.getTitle(),
            urlInfo.getDescription());
    
    if (msg.channel.storeShared(shareUrl)){
        sendShareBroadcast(shareUrl, msg.channel);
    }else{
    	msg.channel.sendDirect("_That link is currently being shared._", msg.member);
    }
    

  }
  
  public static void sendShareBroadcast(SharedURL sharedUrl, Channel channel) {
	
	Member m = channel.getMemberByJID(sharedUrl.getJID());
	  
    String shareBroadcast = "_" + m.getAlias() + " is sharing " + sharedUrl.getUrl();
    
    if (!sharedUrl.getTitle().isEmpty()) {
      shareBroadcast += " (" + sharedUrl.getTitle() + ")";
    }
    
    if (!Strings.isNullOrEmpty(sharedUrl.getAnnotation())) {
      shareBroadcast += " : " + sharedUrl.getAnnotation();
    }
    
    shareBroadcast += "_";

    if (!sharedUrl.getDescription().isEmpty()) {
      shareBroadcast += "\n  " + sharedUrl.getDescription();
    }

    channel.broadcastIncludingSender(shareBroadcast);
  }
    
  public String documentation() {
    return "/share http://example.com/ [annotation] - " +
        "shares a URL with the room";
  }
  
}
