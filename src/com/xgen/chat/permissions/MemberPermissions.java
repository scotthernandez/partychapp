package com.xgen.chat.permissions;

import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.google.common.collect.Maps;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Ofy;

@Unindexed
public class MemberPermissions {

	private static final Logger logger = Logger.getLogger(MemberPermissions.class.getName());
	  
	public enum PermissionLevel {
		  MEMBER("member"),
		  MOD("mod"),
		  ADMIN("admin");
		  
		  private String s;
	
		  private PermissionLevel(String s){
			  this.s = s;
		  }
		  
		  public static PermissionLevel fromString(String s){
			  for (PermissionLevel level : PermissionLevel.values()) {
			      if (s.equalsIgnoreCase(level.s)){
			    	  return level;
			      }
		      }
			  return null; //Not found
		  }
	}
	
	@SuppressWarnings("unused")
	@Id
	private String id = "member";

	@Serialized
	private Map<String, PermissionLevel> memberAuth = Maps.newHashMap();
	
	@Transient
	private static MemberPermissions instance;
	
	static{
		ObjectifyService.register(MemberPermissions.class);
	}
	
	public static MemberPermissions instance(){
		try{
			if (instance == null){
				instance =  Ofy.instance().get(MemberPermissions.class, "member");
			}
		}catch (NotFoundException e){
			instance = new MemberPermissions();
		}
		return instance;
	}

	
	public boolean hasLevel(Channel c, Member m, PermissionLevel p){
		String key = getKey(c, m);
		if(memberAuth.containsKey(key)){
			System.out.println( memberAuth.get(key) +" vs "+ p + " => " + memberAuth.get(key).compareTo(p));
			return memberAuth.get(key).compareTo(p) >= 0 ? true : false;
		}
		return false;
	}
	
	public void setLevel(Channel c, Member m, PermissionLevel p){
		memberAuth.put(getKey(c, m), p);
	}
	
	public PermissionLevel getLevel(Channel c, Member m){
		PermissionLevel level =  memberAuth.get(getKey(c, m));
		if (level == null){
			logger.warning("Member " + m.getJID() + " did not have permissions set.");
			setLevel(c, m, PermissionLevel.MEMBER);
			this.put();
			return PermissionLevel.MEMBER;
		}
		return level;
	}
	
	private String getKey(Channel c, Member m){
		return c.getName() + m.getJID();
	}
	
	public void put(){
		Ofy.instance().put(this);
	}
	
}
