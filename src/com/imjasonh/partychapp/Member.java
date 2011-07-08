package com.imjasonh.partychapp;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.persistence.Transient;

public class Member implements Serializable {

  private static final long serialVersionUID = 8243978327905416562L;

  @SuppressWarnings("unused")
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
  
  transient private Channel channel;
  
  String phoneNumber;
  
  String carrier;
  
  Date lastLivePing = null;
  
  public enum SnoozeStatus {
    SNOOZING,
    NOT_SNOOZING,
    SHOULD_WAKE;
  }
  
  public Member(){} //Objectify
  
  public Member(Channel c, String jid) {
    this.jid = jid;
    this.alias = this.jid.split("@")[0]; // remove anything after "@" for default alias
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
  
  public SnoozeStatus getSnoozeStatus() {
    Date now = new Date();
    if (snoozeUntil == null) {
      return SnoozeStatus.NOT_SNOOZING;
    } else {
      if (snoozeUntil.before(now)) {
        return SnoozeStatus.SHOULD_WAKE;
      } else {
        return SnoozeStatus.SNOOZING;
      }
    }
  }

  public void setSnoozeUntil(Date snoozeUntil) {
    this.snoozeUntil = snoozeUntil;
  }

  public Date getSnoozeUntil() {
    return snoozeUntil;
  }
  
  public boolean unsnoozeIfNecessary() {
    if (getSnoozeStatus() == SnoozeStatus.SHOULD_WAKE) {
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
    if (debugOptions == null) {
      debugOptions = new DebuggingOptions();
      shouldPut = true;
    }
    if (lastMessages == null) {
      lastMessages = Lists.newArrayList();
      shouldPut = true;
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
}
