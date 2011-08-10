package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for servlets that need to operate on a channel that the requesting
 * user is currently in (the channel name is specified via the "name"
 * parameter, but subclasses can override that behavior via the
 * {@link #getChannelName} method). Handles authentication and verification.
 * Subclasses should implement {@link #doChannelGet} and/or
 * {@link #doChannelPost}.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */

@SuppressWarnings("serial")
public abstract class AbstractChannelUserServlet extends HttpServlet {
  private interface MethodAdapter {
    void invokeMethod(
        HttpServletRequest req,
        HttpServletResponse resp,
        com.imjasonh.partychapp.User user,
        Channel channel)
        throws IOException, ServletException;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    doMethod(req, resp, new MethodAdapter() {
      @Override public void invokeMethod(
          HttpServletRequest req,
          HttpServletResponse resp,
          com.imjasonh.partychapp.User user,
          Channel channel) throws IOException, ServletException {
        doChannelGet(req, resp, user, channel);
      }
    });
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    doMethod(req, resp, new MethodAdapter() {
      @Override public void invokeMethod(
          HttpServletRequest req,
          HttpServletResponse resp,
          com.imjasonh.partychapp.User user,
          Channel channel) throws IOException, ServletException {
        doChannelPost(req, resp, user, channel);
      }
    });
  }
  
  protected String getChannelName(HttpServletRequest req) {
    return req.getParameter("name");
  }
  
  private void doMethod(HttpServletRequest req, HttpServletResponse resp, MethodAdapter methodAdapter)
      throws IOException, ServletException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    com.imjasonh.partychapp.User pUser = Datastore.instance().getUserByJID(user.getEmail());
    
    String channelName = getChannelName(req);
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      if (pUser != null){
          Channel channel =
              datastore.getChannelIfUserPresent(channelName, pUser.getJID());
          if (channel == null) {
              resp.sendError(HttpServletResponse.SC_FORBIDDEN);
              return;
            }

          methodAdapter.invokeMethod(req, resp, pUser, channel);
      }
      
    } finally {
    	datastore.endRequest();
    }        
  }
  
  protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      com.imjasonh.partychapp.User user,
      Channel channel)
      throws IOException, ServletException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }
  
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      com.imjasonh.partychapp.User user,
      Channel channel)
      throws IOException, ServletException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }  
}
