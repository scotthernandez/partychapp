package com.imjasonh.partychapp.server.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.ppb.Target;
import com.xgen.chat.permissions.MemberPermissions;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;


public class ChannelServlet extends HttpServlet {
  public static final long serialVersionUID = 985749740983755L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (Strings.isNullOrEmpty(req.getPathInfo())) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    
    // Strip leading slash to get channel name
    String channelName = req.getPathInfo().substring(1);
    
    if (Strings.isNullOrEmpty(channelName)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      Channel channel = datastore.getChannelByName(channelName);
		
      if (channel == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      com.imjasonh.partychapp.User pcUser = datastore.getOrCreateUser(user.getEmail());
		
      if (channel.getMemberByJID(pcUser.getJID()) != null) {
        handleChannelWithMember(req, resp, channel, pcUser.getJID());
      } else if (userService.isUserAdmin()) {
    	if (!channel.canJoin(pcUser.getJID())) {
    		channel.invite(pcUser.getJID());
    	}
    	Member member = channel.addMember(pcUser);
    	MemberPermissions.instance().setLevel(channel, member, PermissionLevel.ADMIN);
    	member.setAlerted(false);
    	member.setHidden(true);
    	channel.put();
    	pcUser.put();
    	handleChannelWithMember(req, resp, channel, pcUser.getJID());
      } else if (channel.canJoin(pcUser.getJID())) {
        handleChannelWithInvitee(req, resp, channel);
      } else if (channel.isInviteOnly()) {
        handleChannelRequestInvitation(req, resp, channel);
      } else {
        handleChannelGetInvitation(req, resp, channel);
      }
    } finally {
      datastore.endRequest();
    }
  }
  
  private void handleChannelWithMember(
      HttpServletRequest req,
      HttpServletResponse resp,
      Channel channel,
      String email) throws ServletException, IOException {
    RequestDispatcher disp =
        getServletContext().getRequestDispatcher("/channel.jsp");
    
    JSONArray targetsJson = new JSONArray();
    try {
      List<Target> targets = Datastore.instance().getTargetsByChannel(channel);
      for (Target t : targets) {
        JSONObject target = new JSONObject();
        target.put("name", t.name());
        target.put("score", t.score());
        targetsJson.put(target);
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    
    req.setAttribute("targetInfo", targetsJson.toString());
    req.setAttribute("channel", channel);
    req.setAttribute("email", email);
    disp.forward(req, resp);    
  }
  
  private void handleChannelWithInvitee(
      HttpServletRequest req,
      HttpServletResponse resp,
      Channel channel) throws ServletException, IOException { 
    RequestDispatcher disp =
      getServletContext().getRequestDispatcher("/channel-invitee.jsp");
    req.setAttribute("channel", channel);
    disp.forward(req, resp);        
  }
  
  private void handleChannelRequestInvitation(
      HttpServletRequest req,
      HttpServletResponse resp,
      Channel channel) throws ServletException, IOException { 
    RequestDispatcher disp =
      getServletContext().getRequestDispatcher(
          "/channel-request-invitation.jsp");
    req.setAttribute("channel", channel);
    disp.forward(req, resp);        
  }  
  
  private void handleChannelGetInvitation(
      HttpServletRequest req,
      HttpServletResponse resp,
      Channel channel) throws ServletException, IOException { 
    RequestDispatcher disp =
      getServletContext().getRequestDispatcher(
          "/channel-get-invitation.jsp");
    req.setAttribute("channel", channel);
    disp.forward(req, resp);        
  }
}
