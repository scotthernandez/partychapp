package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AutoJoinChannelServlet extends AbstractChannelUserServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(DeleteChannelServlet.class.getName());
  
  @Override
  protected void doChannelPost(HttpServletRequest req, 
		  HttpServletResponse resp,
	      User user,
	      Channel channel)
      throws IOException {
    
    Datastore datastore = Datastore.instance();
  
    datastore.startRequest();
    try {
        if (datastore.getOrCreateUser(user.getEmail()).isAdmin()){
            resp.getWriter().write("Success");
        }else{
            resp.getWriter().write("Sorry, only all powerful people like can join any channel.");
        }
    } finally {
      datastore.endRequest();      
    }
  }
}
