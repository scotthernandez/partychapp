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

public class ChangeDefaultAliasServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	    public static final String ALIAS_REGEX = "[\\pL\\pS\\pN\\-_'\\*.]+";
	
	
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
			    	String alias = req.getParameter("alias");
			        String aliasResp = setAlias(alias, pUser);
			        
			        resp.getWriter().write(aliasResp);
			        
			       
			    } finally {
			    	datastore.endRequest();
			    }  
		  }
		
		
		 private String setAlias(String alias, User pUser){
			 if((pUser.defaultAlias != null && alias.compareTo(pUser.defaultAlias) == 0) || alias == null || alias.compareTo("") == 0){
				 return "Default alias was not changed.";
			 }
			 
			 if (!alias.matches(ALIAS_REGEX)) {
			      return "That alias contains invalid characters";
		     }
			 
			 pUser.defaultAlias = alias;
			 pUser.put();
			 return "Success changing your default alias.";
		 }
		 
}
