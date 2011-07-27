package com.imjasonh.partychapp.logging;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LogJSONUtil {

	public static JSONObject entryNoDate(LogEntry entry){
    	JSONObject entryJson = new JSONObject();
		
		try{
	    	entryJson.put("content", entry.content());
	    	entryJson.put("user", entry.userID());
    	}catch (JSONException e){
			return null;
		}
    	
    	return entryJson;
	}
	
	public static JSONArray entriesWebDate(List<LogEntry> log){
		JSONArray entriesJson = new JSONArray();
    	
    	try{
		    for(LogEntry entry: log){
		    	JSONObject entryJson = entryNoDate(entry);
		    	entryJson.put("time", entry.webDate());
		    	entriesJson.put(entryJson);
		    }
    	}catch (JSONException e){
			return null;
		}
    	
    	return entriesJson;
	}
	
	public static JSONArray entriesMillisecondDate(List<LogEntry> log){
		JSONArray entriesJson = new JSONArray();
    	
    	try{
		    for(LogEntry entry: log){
		    	JSONObject entryJson = entryNoDate(entry);
		    	JSONObject date = new JSONObject();
		    	date.put("$date", entry.millisecondDate());
		    	entryJson.put("time", date);
		    	entriesJson.put(entryJson);
		    }
    	}catch (JSONException e){
			return null;
		}
    	
    	return entriesJson;
	}
}


