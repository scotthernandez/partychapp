package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
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
    if (msg.channel.storeShared(
            msg.member,
            uri,
            annotation,
            urlInfo.getTitle(),
            urlInfo.getDescription())){
        sendShareBroadcast(
                msg.channel,
                msg.member,
                uri,
                annotation,
                urlInfo.getTitle(),
                urlInfo.getDescription());
    }else{
    	msg.channel.sendDirect("_That link is currently being shared._", msg.member);
    }
    

  }
  
  public static void sendShareBroadcast(
      Channel channel,
      Member member,
      URI url,
      String annotation,
      String title,
      String description) {
    String shareBroadcast = "_" + member.getAlias() + " is sharing " + url;
    
    if (!title.isEmpty()) {
      shareBroadcast += " (" + title + ")";
    }
    
    if (!Strings.isNullOrEmpty(annotation)) {
      shareBroadcast += " : " + annotation;
    }
    
    shareBroadcast += "_";

    if (!description.isEmpty()) {
      shareBroadcast += "\n  " + description;
    }

    channel.broadcastIncludingSender(shareBroadcast);
  }
    
  public String documentation() {
    return "/share http://example.com/ [annotation] - " +
        "shares a URL with the room";
  }
}
