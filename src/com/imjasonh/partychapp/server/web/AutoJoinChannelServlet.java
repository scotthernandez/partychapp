package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
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
  protected void doChannelPost(
		  HttpServletRequest req, 
		  HttpServletResponse resp,
	      User user,
	      Channel channel)
      throws IOException {
    
    Datastore datastore = Datastore.instance();
    
    datastore.startRequest();
    try {
        if (channel == null || !UserServiceFactory.getUserService().isUserAdmin()){
          resp.getWriter().write("you are not an admin user and should not have this button in the first place sorry");
          return;
        } else {
        	Member member = channel.getMemberByJID(user.getJID());
        	member.setHidden(false);
        	member.setAlerted(true);
        	channel.put();
            resp.getWriter().write("success");
        }
    } finally {
      datastore.endRequest();      
    }
    return;
  }
}
