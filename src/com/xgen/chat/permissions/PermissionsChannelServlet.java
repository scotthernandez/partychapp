package com.xgen.chat.permissions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.server.web.AbstractChannelUserServlet;
import com.imjasonh.partychapp.server.web.LeaveChannelServlet;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;

@SuppressWarnings("serial")
public class PermissionsChannelServlet extends AbstractChannelUserServlet {

  private static final Logger logger =
      Logger.getLogger(LeaveChannelServlet.class.getName());
  
  
  @Override
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
	  	
	  	if (channel == null)
	  		logger.severe("Channel for name '" + getChannelName(req) + "' is null");
	  	
	  	PermissionLevel level = PermissionLevel.fromString((String)req.getParameter("permissions")); 
  		
		Member toModify = channel.getMemberByJID((String) req.getParameter("toModify"));
		
	    Member member = channel.getMemberByJID(user.getJID());
	    
	    if (toModify == null){
	    	resp.getWriter().write("Error: Member to change permissions does not exist.");
	    	return;
	    }
	    if (member == toModify){
	    	resp.getWriter().write("Error: Can't change your own permissions.");
	    	return;
	    }
	    

		Datastore.instance().startRequest();
	    if(MemberPermissions.instance().hasLevel(channel, member, level)){
	    	MemberPermissions.instance().setLevel(channel, toModify, level);
    		resp.getWriter().write("success");
    		channel.put();
    		MemberPermissions.instance().put();
	    }else{
			resp.getWriter().write("Error: You don't have enough permissions to change permissions of  " + toModify.getAlias() + ".");
	    }
    
	    Datastore.instance().endRequest();

		return;
  }
}
