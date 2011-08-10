package com.imjasonh.partychapp.server.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.server.MailUtil;

public class UserMergeRequest extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(UserMergeVerify.class.getName());
	  
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	      throws IOException, ServletException  {
		Datastore.instance().startRequest();
		String email = (String) req.getParameter("email");
		String jid = (String) req.getParameter("jid");
	
		
	   User crypto = Datastore.instance().getOrCreateUser(email);
	   User crypto2 = Datastore.instance().getOrCreateUser(jid);
	   
	   try{
	       String real = User.newKey(crypto, crypto2);
	       String webURL = Configuration.webDomain + "/user/merge/verify?key=" + real + "&email=" + email + "&jid=" + jid;
	       MailUtil.sendMail("Chat User Merge Confirmation", "You have requested a merge between 10gen chat users " + email + " and " + jid +".  " +
	       		"To complete the merge, visit the following URL: \n \n" + webURL + " \n\n " +
	       		"If you didn't request the merge, please ignore this email and do not click on the link.", "confirm@" + Configuration.mailDomain, jid);
	       
	       resp.getWriter().write("Success sending email.");
	   }catch (Exception e){
		   logger.severe("Failed to request a merge.");
	       resp.getWriter().write("Failed to request a merge. If problem persists, please report.");
	   }finally{
		   Datastore.instance().endRequest();
	   }
	}
}
