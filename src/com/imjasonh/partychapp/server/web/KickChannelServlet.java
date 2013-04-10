package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.xgen.chat.permissions.MemberPermissions;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class KickChannelServlet extends AbstractChannelUserServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(LeaveChannelServlet.class.getName());
  
  
  @Override
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
	
  		String email = (String) req.getParameter("member");
  		String alias = email;
  		
		Member toKick = channel.getMemberByJID((String) req.getParameter("member"));
		
	    Member member = channel.getMemberByJID(user.getJID());
	    
	    if (toKick != null){
	    	email = toKick.getJID();
	    	alias = toKick.getAlias();
	    }
	    
	    if (member == toKick){
	    	resp.getWriter().write("Fail: You tried to delete yourself.");
	    	return;
	    }
	    
	    boolean admin = UserServiceFactory.getUserService().isUserAdmin();

		Datastore.instance().startRequest();
	    if(admin || MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.MOD)){
	    	if(channel.kick(email)){
	    		resp.getWriter().write("success");
	    		channel.put();
	    	}else{
	    		resp.getWriter().write("Failed to kick " + alias + ".");
	    	}
	    }else{
			resp.getWriter().write("Not enough permissions to kick " + alias + ".");
	    }
    
	    Datastore.instance().endRequest();

		return;
  }
}
