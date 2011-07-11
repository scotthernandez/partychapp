package com.imjasonh.partychapp.server.json;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.logging.LogDAO;
import com.imjasonh.partychapp.logging.LogEntry;


public class LogEntriesJsonServlet extends JsonServlet {

	@Override
	protected JSONObject getJson(HttpServletRequest req,
			HttpServletResponse resp, User user, Datastore datastore)
			throws JSONException {
	    String channelName = req.getParameter("channelName");
	    int limit = Integer.parseInt(req.getParameter("limit"));
	    int offset = Integer.parseInt(req.getParameter("offset"));
	    
	    //TODO: some kind of security check?
	    JSONArray entriesJson = new JSONArray();
	    String error = "";
	    if (offset >= 0){
	    	List<LogEntry> log = LogDAO.getLogByChannel(channelName, limit, offset);
		    if (log.size() == 0){
		    	for (int i = 0; i < 50; i++){
			    	Message msg = Message.createForTests("test "+i, channelName);
		    		LogDAO.put(new LogEntry(msg));
		    	}
		    	LogDAO.put(new LogEntry(Message.createForTests("This is a message with a TICKET-9876 and sone <b>html</b>", channelName)));
		    	log = LogDAO.getLogByChannel(channelName, limit, offset);
		    }
		    for(LogEntry entry : log){
		    	JSONObject entryJson = new JSONObject();
		    	entryJson.put("time", entry.timeStamp());
		    	entryJson.put("content", entry.content());
		    	entryJson.put("user", entry.userID());
		    	entriesJson.put(entryJson);
		    }
	    }else{
	    	error = "Offset is less than zero.";
	    }
	    
	    JSONObject response = new JSONObject();
	    response.put("entries", entriesJson);
	    
	    if (!error.isEmpty()){
	    	response.put("error", error);
		}
	    
	    return response;
	}

}
