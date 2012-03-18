package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Member implements Serializable{
	/** start with 1 for all classes */
	private static final long serialVersionUID = 1L;
  
  private static final Logger logger = Logger.getLogger(Member.class.getName());

  /**
   * Maximum length of the message that we'll persist for s/r (the full message
   * is still broadcast).
   */
  private static final int MAX_PERSISTED_MESSAGE_LENGTH = 512;

  private String jid;

  private String alias;

  private Date snoozeUntil;
  
  private List<String> lastMessages = Lists.newArrayList();

  private DebuggingOptions debugOptions = new DebuggingOptions();
  
  private boolean alerted = true;//TODO:cleanup
  
  private boolean hidden = false;//TODO:cleanup
  
  private Set<String> wakeWords = Sets.newHashSet();
  
  transient private Channel channel;
  
  transient private User user;
  
  String phoneNumber;
  
  String carrier;
  
  Date lastLivePing = null;
  
  public enum SnoozeStatus {
    SNOOZING,
    NOT_SNOOZING,
    SHOULD_WAKE;
  }
  
 
  
  public Member(){} //Objectify
  
  public Member(Channel c, User u) {
    this.jid = u.getJID();
    this.alias =  u.defaultAlias != null ? u.defaultAlias : this.jid.split("@")[0]; // remove anything after "@" for default alias
    this.debugOptions = new DebuggingOptions();
    this.channel = c;
  }
  
  public Member(Member other) {
    this.jid = other.jid;
    this.alias = other.alias;
    this.snoozeUntil = other.snoozeUntil;
    if (other.lastMessages != null) {
      this.lastMessages = Lists.newArrayList(other.lastMessages);
    }
    this.debugOptions = new DebuggingOptions(other.debugOptions());
    this.phoneNumber = other.phoneNumber;
    this.carrier = other.carrier;
    // to simulate the not-persistent-ness, let's zero these out
    this.channel = null;
    
  }

  
//TODO:cleanup
  public boolean isAlerted() {
	  return alerted;
  }
  //TODO:cleanup
  public void setAlerted(boolean b){
	  alerted = b;
  }
//TODO:cleanup
  public boolean isHidden() {
	  return hidden;  
  }
  //TODO:cleanup
  public void setHidden(boolean b){
	  hidden = b;
  }
  
  public String getAlias() {
    return alias;
  }
  
  public String getAliasPrefix() {
    return "[" + alias + "] ";
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }
  
  public String getJID() {
    return jid;
  }
  //TODO:cleanup
  void setJID(String jid) {
	  this.jid = jid;
  }
  
  public SnoozeStatus getSnoozeStatus(String msg) {
    Date now = new Date();
    if (snoozeUntil == null) {
      return SnoozeStatus.NOT_SNOOZING;
    } else {
      if (snoozeUntil.before(now) || hasWakeWords(msg)) {
        return SnoozeStatus.SHOULD_WAKE;
      } else {
        return SnoozeStatus.SNOOZING;
      }
    }
  }

  private boolean hasWakeWords(String msg) {	  
	if (wakeWords == null){
	  wakeWords = Sets.newHashSet();
    }
	for (String word : wakeWords){
		if (msg.toLowerCase().contains(word)){
			return true;
		}
	}
	return false;
  }

  public boolean addWakeWord(String word){
	  if (wakeWords == null){
		  wakeWords = Sets.newHashSet();
	  }
	  return wakeWords.add(word.toLowerCase());
  }

  public boolean removeWakeWord(String word){
	  if (wakeWords == null){
		  wakeWords = Sets.newHashSet();
	  }
	  return wakeWords.remove(word);
  }

  public Collection<String> wakeWords(){
	  if (wakeWords == null){
		  wakeWords = Sets.newHashSet();
	  }
	  return Collections.unmodifiableSet(wakeWords);
  }

public void setSnoozeUntil(Date snoozeUntil) {
    this.snoozeUntil = snoozeUntil;
  }

  public Date getSnoozeUntil() {
    return snoozeUntil;
  }
  
  public boolean unsnoozeIfNecessary(String msg) {
    if (getSnoozeStatus(msg) == SnoozeStatus.SHOULD_WAKE) {
      setSnoozeUntil(null);
      return true;
    }
    return false;
  }
  
  private List<String> mutableLastMessages() {
    return lastMessages;
  }

  public List<String> getLastMessages() {
    return Collections.unmodifiableList(mutableLastMessages());
  }
  
  public void addToLastMessages(String toAdd) {
    if (channel != null && channel.isMiniLogDisabled()) {
      return;
    }
    if (toAdd.length() > MAX_PERSISTED_MESSAGE_LENGTH) {
      toAdd = toAdd.substring(0, MAX_PERSISTED_MESSAGE_LENGTH);
    }
    setSnoozeUntil(null);
    List<String> messages = mutableLastMessages();
    messages.add(0, toAdd);
    if (messages.size() > 10) {
      messages.remove(10);
    }    
  }

  public DebuggingOptions debugOptions() {
    return debugOptions;
  }

  public boolean fixUp(Channel c) {
    boolean shouldPut = false;

    if (channel != c) {
      channel = c;
    }
    if (channel.isMiniLogDisabled() && !lastMessages.isEmpty()) {
      clearLastMessages();
      shouldPut = true;
    }
    
    return shouldPut;
  }
  
  public static class SortMembersForListComparator implements Comparator<Member> {
    public int compare(Member first, Member second) {
      // TODO: sort by online/offline, snoozing
      return first.getAlias().compareTo(second.getAlias());
    }
  }

  /**
   * Meant for admin UI use only.
   */
  public void clearLastMessages() {
    lastMessages.clear();
  }
  
  public void onLivePing() {
    this.lastLivePing = new Date();
  }
  
  public void clearLivePing() {
    this.lastLivePing = null;
  }  
  
  public Date getLastLivePing() {
   return lastLivePing; 
  }
  //TODO: cleanup
  public void changeJID(String jid) {
	  this.jid = jid;
  }
  
  //I'm trying to get rid of these permissions.  However, because I had deployed, the data needs
  //to be migrated to using MemberPermissions.
//  public enum Permissions {
//	  MEMBER("member"),
//	  MOD("mod"),
//	  ADMIN("admin");
//
//	  String s;
//
//	  private Permissions(String s){
//		  this.s = s;
//	  }
//
//	  public static Permissions fromString(String s){
//		  for (Permissions permissions : Permissions.values()) {
//		      if (s.equals(permissions.s)){
//		    	  return permissions;
//		      }
//	      }
//		  return null; //Not found
//	  }
//  }
//  
//
//  public Permissions permissions = Permissions.MEMBER;
//  

}
