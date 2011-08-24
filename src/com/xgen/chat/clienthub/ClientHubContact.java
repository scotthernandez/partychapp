package com.xgen.chat.clienthub;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientHubContact {

	public enum Role{
		CLIENT, ENGN, SALES;
		
		static Role fromString(String s){
			if (s.equals("sales")){
				return SALES;
			}else if (s.equals("engineering")){
				return ENGN;
			}else{
				return CLIENT;
			}
		}
	}
	
	private String email;
	private String name;
	private String jiraUsername;
	private boolean isXgen;
	private boolean isPrimary;
	private boolean isJira;
	private boolean isSFDC;
	private Role role;
	
	public ClientHubContact(JSONObject json) throws JSONException{
		
			this.email = json.getString("email");
			this.jiraUsername = json.getString("jira_username");
			this.name = json.getString("name");
			this.isXgen = json.getBoolean("is_xgen");
			this.isPrimary = json.getBoolean("is_primary");
			this.isJira = json.getBoolean("is_jira");
			this.isSFDC = json.getBoolean("is_sfdc");
			this.role = Role.fromString(json.getString("role"));
			
		
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
	
	public boolean isJira() {
		return isJira;
	}
	
	public boolean isSFDC() {
		return isSFDC;
	}
	
	public boolean isEngineering() {
		return role == Role.ENGN;
	}
	
	public boolean isSales() {
		return role == Role.SALES;
	}
	
	public boolean isClient() {
		return role == Role.CLIENT;
	}
	
	@Override
	public String toString(){
		return "CHContact: { email: " + this.email + "\n"
			  +"             jiraUsername: " + this.jiraUsername + "\n"
			  +"             name: " +this.name + "\n"
			  +"             isXgen: " + this.isXgen + "\n"
			  +"             isJira: " + this.isJira + "\n"
			  +"             isSFDC: " + this.isSFDC + "\n"
			  +"             role: " + this.role + "\n"
			  +"             isPrimary: " + this.isPrimary + "} \n";
	}
}
