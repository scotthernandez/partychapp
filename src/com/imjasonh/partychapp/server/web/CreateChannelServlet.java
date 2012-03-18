package com.imjasonh.partychapp.server.web;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.InviteUtil;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.command.InviteHandler;
import com.xgen.chat.clienthub.ClientHubAPIException;
import com.xgen.chat.clienthub.ClientHubHelper;
import com.xgen.chat.permissions.MemberPermissions;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;

@SuppressWarnings("serial")
public class CreateChannelServlet extends HttpServlet {

  private static final Logger logger =
      Logger.getLogger(CreateChannelServlet.class.getName());
  
  // See http://tools.ietf.org/html/rfc3920#appendix-A.5 for the list of
  // characters that are not allowed in JIDs
  private static final Pattern ILLEGAL_JID_CHARACTERS =
      Pattern.compile("[ \"&'/:<>@]");

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
  
    String name = ILLEGAL_JID_CHARACTERS.matcher(
            req.getParameter("name")).replaceAll(".");
    Datastore datastore = Datastore.instance();
    datastore.startRequest();

    try{
      Channel channel = datastore.getChannelByName(name);
      if (channel != null) {
        resp.getWriter().write("Sorry, room name is taken");
        return;
      }

      // Generate server JID and use it immediately (to send the chat invite). 
      // If we somehow end up with an invalid JID (despite sanitizing above), 
      // the request will be aborted now, before we commit anything to the
      // datastore and end up in an inconsistent state.
      JID serverJID = new JID(name + "@" + Configuration.chatDomain);
      // The creator only gets an XMPP invite, not an email one.
      com.imjasonh.partychapp.User pchapUser = datastore.getOrCreateUser(user.getEmail());
      
      channel = new Channel(serverJID);
    
    try{
      if (ClientHubHelper.instance().isClient(channel)){
    	  if(!ClientHubHelper.instance().isContact(channel, user.getEmail()) || userService.isUserAdmin()){
    		  resp.getWriter().write("Sorry, the room name you chose is the name of a client on clienthub.<br>You must be a member of that clienthub page to register this room.");
    		  //Channel wasn't put() so it won't be persisted.  As if it was never created.
    		  return;
    	  }
    	  ClientHubHelper.instance().addAllContactsIfClient(channel);
      }
    }catch(ClientHubAPIException e){
  		logger.log(Level.WARNING, e.toString());
  	    resp.getWriter().write("There was a problem connecting with ClientHub.  Notify the admins and check the log.");
    }finally{
    	if (userService.isUserAdmin() || pchapUser.is10Gen()){
      	  Member m = channel.addMember(pchapUser);
      	  MemberPermissions.instance().setLevel(channel, m, PermissionLevel.ADMIN);
            SendUtil.invite(user.getEmail(), serverJID);
        }else{
      	  resp.getWriter().write("Failed to create channel.  You have no permission to do so.");
        }
    }
    	
      // works for "true" ignoring case
      if (Boolean.parseBoolean(req.getParameter("inviteonly"))) {
        channel.setInviteOnly(true);
      }
  
      List<String> invitees = Lists.newArrayList();
      if (!req.getParameter("invitees").isEmpty()) {
      	String error = InviteHandler.parseEmailAddresses(
      	    req.getParameter("invitees"), invitees);
      	for (String invitee : invitees) {
      		channel.invite(invitee);
      		InviteUtil.invite(
      		    invitee,
      		    channel,
      		    user.getEmail(),
      		    user.getEmail());
      	}
  	    resp.getWriter().write(error);
      }
  
      channel.put();
      resp.getWriter().write(
          "Created! You are now a member of " + channel.getName() + ". Just accept the chat request and start talking. " +
      		"To add users later, type '/invite user@whatever.com'.");
      
      resp.getWriter().write(
      		"<P>Try messaging <a href=\"im:" + serverJID.getId() + "\">"
      		+ serverJID.getId() + "</a> or visit <a href=\"/channel/" + name
      		+ "\">the room's page</a> for more information.");
    } finally {
      datastore.endRequest();      
    }
  }
}
