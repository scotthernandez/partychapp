package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.xmpp.JID;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.Builder;
import com.imjasonh.partychapp.server.command.Command;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows invite-only and logging to be turned on/off for a channel (by a member
 * of that channel). 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */

@SuppressWarnings("serial")
public class ChannelEditServlet extends AbstractChannelUserServlet {
  @Override
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
	  
    channel.setInviteOnly(
        Boolean.parseBoolean(req.getParameter("inviteonly")));
    channel.setMiniLogDisabled(
        !Boolean.parseBoolean(req.getParameter("minilog")));
    
    com.imjasonh.partychapp.User partychapp_user = Datastore.instance().getUserByJID(user.getEmail());
    Member member = channel.getMemberByJID(user.getEmail());
    Builder builder = new Builder()
    							.setChannel(channel)
    							.setUser(partychapp_user)
    							.setUserJID(new JID(user.getEmail()))
    							.setMember(member)
    							.setServerJID(channel.serverJID())
    							.setMessageType(Message.MessageType.XMPP);
    
    Message msg;
    
    //It's important to let the room know if they are being recorded or not.
    if (Boolean.parseBoolean(req.getParameter("logging"))){
        channel.setLogging(true);
    	msg = builder.setContent(member.getAlias() + " has enabled logging.").build();
    }else{
    	channel.setLogging(false);
    	msg = builder.setContent(member.getAlias() + " has disabled logging.").build();
    }
    
    //Command.LOG.commandHandler.doCommand(msg);
    Command.BROADCAST.commandHandler.doCommand(msg);
    
    channel.put();
    resp.sendRedirect(channel.getName());
  }
}
