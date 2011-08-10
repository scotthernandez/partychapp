package com.imjasonh.partychapp.server.web;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;

public class UserChangeJIDServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	private static Pattern pattern = Pattern.compile("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$");
	
	
		@Override
		  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
			    UserService userService = UserServiceFactory.getUserService();
			    com.google.appengine.api.users.User gUser = userService.getCurrentUser();

			    Datastore datastore = Datastore.instance();
			    
			    User pUser = datastore.getUserByJID(gUser.getEmail());
			    assert(pUser != null);
			    
			    datastore.startRequest();
			    try {
			        String newJID = req.getParameter("jid");
			        String jidResp = setJID(newJID, pUser, gUser);
			        if (jidResp.compareTo("Logout:") == 0){
			        	jidResp += userService.createLogoutURL("/");
			        }
			        
			        resp.getWriter().write(jidResp);
			        
			       
			    } finally {
			    	datastore.endRequest();
			    }  
		  }
		
		 private String setJID(String newJID, User pUser, com.google.appengine.api.users.User gUser){
		        if (!isValidEmail(newJID)){
		        	return "Didn't recognize the email as valid.";
		        }
		        if (newJID != null){
		        	User u = Datastore.instance().getUserByJID(newJID);
		        	if (u == null || u.equals(pUser)){
		        		if(newJID.compareToIgnoreCase(pUser.getJID()) != 0){
				        	User.changeJID(pUser, newJID);
				        	if (gUser.getEmail().compareTo(pUser.getEmail()) != 0){
				        		return "Logout:";
				        	}else{
				        	    return "Success"; 
				        	}
		        		}
		        		return "Success";
		        	}else{
			        	return "JID is already in use. If you believe this account is yours, follow the <a href=\"/user/merge\"> merge accounts </a> procedure."; 
		        	}
		        	
		        } else {
		        	return "Encountered a problem.  Please report with context."; 
		        }
		 }
		  
		 private boolean isValidEmail(String email){
			 Matcher m = pattern.matcher(email);
			 return m.matches();
		 }
}
