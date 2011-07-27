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
import com.imjasonh.partychapp.logging.LogJSONUtil;

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
	    
	    //TODO: some kind of permissions check?
	    if (offset >= 0){
	    	List<LogEntry> log = LogDAO.getLogByChannel(channelName, limit, offset);
		    JSONArray entriesJson = LogJSONUtil.entriesWebDate(log);
		    
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
