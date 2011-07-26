package com.imjasonh.partychapp.server.json;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;

@SuppressWarnings("serial")
public class LogEntriesJsonServlet extends JsonServlet {

	@Override
	protected JSONObject getJson(HttpServletRequest req,
			HttpServletResponse resp, User user, Datastore datastore)
			throws JSONException {
	    JSONObject response = new JSONObject();
	    String error = "";
	    
	    String channelName = req.getParameter("channelName");
	    
	    try{
	    int limit = Integer.parseInt(req.getParameter("limit"));
	    int offset = Integer.parseInt(req.getParameter("offset"));
	    
	    //TODO: some kind of security check?
	    if (offset >= 0){
		    JSONArray entriesJson = new JSONArray();
		    
	    	List<LogEntry> log = LogDAO.getLogByChannel(channelName, limit, offset);
	    	
		    for(LogEntry entry : log){
		    	JSONObject entryJson = new JSONObject();
		    	entryJson.put("time", entry.webTimestamp());
		    	entryJson.put("content", entry.content());
		    	entryJson.put("user", entry.userID());
		    	entriesJson.put(entryJson);
		    }
		    
		    response.put("entries", entriesJson);
	    }else{
	    	error = "Offset is less than zero.";
	    }
	    
	   
	}catch(NumberFormatException e){
		error = "Number parsing error.";
		response.put("limit", req.getParameter("limit"));
		response.put("offset", req.getParameter("offset"));
	}
	
	if (!error.isEmpty()){
    	response.put("error", error);
	}
	
	return response;
	}

}
