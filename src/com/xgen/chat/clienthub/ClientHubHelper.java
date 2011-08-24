package com.xgen.chat.clienthub;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.server.SendUtil;
//import com.xgen.chat.permissions.PermissionLevel;
//import com.xgen.chat.permissions.PermissionsHelper;
import com.xgen.chat.permissions.MemberPermissions;
import com.xgen.chat.permissions.MemberPermissions.PermissionLevel;

public class ClientHubHelper {
	private static final Logger logger = 
        Logger.getLogger(ClientHubHelper.class.getName());
    
	private Map<String, Boolean> channels = Maps.newHashMap();
	private Map<String, Map<String, ClientHubContact>> clients = Maps.newHashMap();
	
	private static ClientHubHelper instance = null;
	
	public static ClientHubHelper instance() {
		if (instance == null) {
			instance = new ClientHubHelper();
		}
		return instance;
	}
	
	public boolean isClient(Channel channel) throws ClientHubAPIException{
		String channelName = channel.getName();
				
		//Try set first so we don't need to call the API;
		if (channels.get(channelName) != null && channels.get(channelName)){
			return true;
		}
		
		Map<String, ClientHubContact> contacts = ClientHubAPI.getClientContacts(channelName);
		if (contacts == null){
			channels.put(channelName, false);
			return false;
		}else{
			channels.put(channelName, true);
			clients.put(channelName, contacts);
			return true;
		}
	}
	
	public Map<String, ClientHubContact> getContactsIfClient(Channel channel) throws ClientHubAPIException{
		String channelName = channel.getName();
		
		//Try set first so we don't need to call the API;
		if (isClient(channel)){
			return clients.get(channelName);
		}
		return null;
	}

	public boolean isContact(Channel channel, String email) throws ClientHubAPIException{
		Map<String, ClientHubContact> contactMap = getContactsIfClient(channel);
		return contactMap.containsKey(email);
	}
	
	public boolean addContactIfClient(Channel channel, String email){
		try{
			Map<String, ClientHubContact> contactMap = getContactsIfClient(channel);
			if (contactMap != null){
				if (contactMap.containsKey(email)){
					ClientHubContact contact = contactMap.get(email);
					addMember(channel, contact);
				    return true;
				}
			}
			
		}catch(ClientHubAPIException e){
			//
		}
		return false;
	}
	
	public boolean addAllContactsIfClient(Channel channel){
		try{
			Map<String, ClientHubContact> map = getContactsIfClient(channel);
			if (map != null){
				for (ClientHubContact contact : map.values()){
					if (contact.isEngineering() || contact.isJira()){
						if (channel.getMemberByJID(contact.getEmail()) == null){
							addMember(channel, contact);
							SendUtil.invite(contact.getEmail(), channel.serverJID());
						}
					}
				}
				return true;
			}
		}catch(ClientHubAPIException e){
			logger.log(Level.WARNING, e.toString());
		}
		return false;
	}
	
	private void addMember(Channel channel, ClientHubContact contact){
		User user = Datastore.instance().getOrCreateUser(contact.getEmail());
		Member member = channel.addMember(user);
	    SendUtil.invite(member.getJID(), channel.serverJID());
	    
	    //Permissions
	    if (contact.isPrimary()){
	    	MemberPermissions.instance().setLevel(channel, member, PermissionLevel.ADMIN);
	    }else if (contact.isXgen()){
	    	MemberPermissions.instance().setLevel(channel, member, PermissionLevel.MOD);
	    }else{
	    	MemberPermissions.instance().setLevel(channel, member, PermissionLevel.MEMBER);
	    }
	    MemberPermissions.instance().put();
	}
	
	public boolean addMemberIfContact(Channel channel, ClientHubContact contact){
		try{
			if (isContact(channel, contact.getEmail())){
				addMember(channel, contact);
				return true;
			}
			return false;
		}catch(ClientHubAPIException e){
			return false;
		}
	}
}
