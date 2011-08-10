package com.imjasonh.partychapp.server.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;

import java.util.logging.Logger;



public class UserMergeVerify extends HttpServlet{

	private static final long serialVersionUID = 1L;

	  private static final Logger logger = Logger.getLogger(UserMergeVerify.class.getName());

	
		@Override
		  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {

			    Datastore datastore = Datastore.instance();
			    
			    
			    datastore.startRequest();
			    try {
			        String jid = (String) req.getParameter("jid");
			        String email = (String) req.getParameter("email");
			        String key = (String) req.getParameter("key");
			        
			        String message = "";
			        
			        if (jid == null || email == null || key == null){
			        	logger.warning("Some parameters were null when trying to verify user merge.");
			        	message = "Unable to merge users.  Make sure you copy and pasted the verification link correctly.";
			        }
			        
			        User jUser = Datastore.instance().getUserByJID(jid);
			        User eUser = Datastore.instance().getUserByJID(email);
			        if (jUser == null || eUser == null){
			        	logger.warning("One of the users was not found when trying to verify merge.");
			        	message = "Unable to merge users. Make sure you copy and pasted the verification link correctly and that the users haven't already been merged";
			        }
			        
			        if(User.verifyMerge(key, jUser, eUser)){
			        	User newU = User.mergeForJID(eUser, jUser);
			        	forwardSuccess(req, resp, newU.getEmail(), newU.getJID());
			        	return;
			        }else{
			        	logger.warning("The verification failed.  ");
			        	message = "Unable to merge users.  The verification failed.  Make sure you copy and pasted the verification link correctly and that the users haven't already been merged.";
			        }
			        
			       forwardFail(req, resp, message);
			      
			    } finally {
			    	datastore.endRequest();
			    }  
		  }
		
		  private void forwardFail(
			      HttpServletRequest req,
			      HttpServletResponse resp,
			      String message) throws ServletException, IOException {
			    RequestDispatcher disp =
			        getServletContext().getRequestDispatcher("/merge-verify.jsp");
			    
			    req.setAttribute("error", true);
			    req.setAttribute("message", message);
			    disp.forward(req, resp);    
			  }
		  
		  private void forwardSuccess(
			      HttpServletRequest req,
			      HttpServletResponse resp,
			      String email,
			      String jid) throws ServletException, IOException {
			    RequestDispatcher disp =
			        getServletContext().getRequestDispatcher("/merge-verify.jsp");
			    
			    req.setAttribute("error", false);
			    req.setAttribute("email", email);
			    req.setAttribute("jid", jid);
			    disp.forward(req, resp);    
			  }
		
}
