package com.xgen.partychapp.clienthub;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientHubContact {

	private String email;
	private String name;
	private String jiraUsername;
	private boolean isXgen;
	private boolean isPrimary;
	
	public ClientHubContact(JSONObject json) throws JSONException{
		
			this.email = json.getString("email");
			this.jiraUsername = json.getString("jira_username");
			this.name = json.getString("name");
			this.isXgen = json.getBoolean("is_xgen");
			this.isPrimary = json.getBoolean("is_primary");
		
	}
	
	public String getEmail(){
		return email;
	}
	
	public String getName(){
		return name;
	}
	
	public String getJiraUsername(){
		return jiraUsername;
	}
	
	public boolean isXgen(){
		return isXgen;
	}
	
	public boolean isPrimary(){
		return isPrimary;
	}
	
	@Override
	public String toString(){
		return "CHContact: { email: " + this.email + "\n"
			  +"             jiraUsername: " + this.jiraUsername + "\n"
			  +"             name: " +this.name + "\n"
			  +"             isXgen: " + this.isXgen + "\n"
			  +"             isPrimary: " + this.isPrimary + "} \n";
	}
}
