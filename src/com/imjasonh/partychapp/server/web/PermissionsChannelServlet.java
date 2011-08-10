package com.imjasonh.partychapp.server.web;

import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Member.Permissions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class PermissionsChannelServlet extends AbstractChannelUserServlet {

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
	
	  	Permissions permissions = Permissions.fromString((String)req.getParameter("permissions")); 
  		
  		
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
	    if(member.hasPermissions(permissions)){
	    	toModify.setPermissions(permissions);
    		resp.getWriter().write("success");
    		channel.put();
	    }else{
			resp.getWriter().write("Error: You don't have enough permissions to change permissions of  " + toModify.getAlias() + ".");
	    }
    
	    Datastore.instance().endRequest();

		return;
  }
}
