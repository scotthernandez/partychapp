package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DeleteChannelServlet extends AbstractChannelUserServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(DeleteChannelServlet.class.getName());
  
  @Override
  protected void doChannelPost(
		  HttpServletRequest req, 
		  HttpServletResponse resp,
	      User user,
	      Channel channel)
      throws IOException {
    
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
        if (UserServiceFactory.getUserService().isUserAdmin()){
        	channel.removeAllUsers();
        	datastore.deleteChannelByName(channel.getName());
            resp.getWriter().write("success");
        }else{
            resp.getWriter().write("Sorry, admins can delete a channel.");
        }
    } finally {
      datastore.endRequest();      
    }
    return;
  }
}
