package com.imjasonh.partychapp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Id;

@Unindexed
public class User implements Serializable {
  
  private static final Logger logger = Logger.getLogger(User.class.getName());

  /**
   * How often the lastSeen field should be updated (it's only used for
   * computing daily active stats, so it's wasteful to cause writes for every
   * single message received). For now only update it every 12 hours.
   */

  private static final long LAST_SEEN_UPDATE_INTERNAL_MS =
      12L * 60L * 60L * 1000L;

  @Id private String jid;

  List<String> channelNames;
  
  @Indexed
  String phoneNumber;
  
  String carrier;
  
  boolean isAdmin;
  
  Date lastSeen;

  // I stole from http://en.wikipedia.org/wiki/List_of_carriers_providing_SMS_transit
  public enum Carrier {
    ATT("at&t", "txt.att.net", false),
    VERIZON("verizon", "vtext.net", true),
    TMOBILE("tmobile", "tmomail.net", true),
    SPRINT("sprint", "messaging.sprintpcs.com", true),
    VIRGIN("virgin", "vmobl.com", true)
    ;

    public final String shortName;
    public final String emailToSmsDomain;
    public final boolean wantsLeadingOne;

    private Carrier(String shortName, String emailToSmsDomain, boolean wantsLeadingOne) {
      this.shortName = shortName;
      this.emailToSmsDomain = emailToSmsDomain;
      this.wantsLeadingOne = wantsLeadingOne;
    }

    public String emailAddress(String phoneNumber) {
      if (phoneNumber == null) {
        return null;
      }
      if (wantsLeadingOne) {
        if (!phoneNumber.startsWith("1")) {
          phoneNumber = "1" + phoneNumber;
        }
      } else {
        if (phoneNumber.startsWith("1")) {
          phoneNumber = phoneNumber.substring(1);
        }
      }
      return phoneNumber + "@" + emailToSmsDomain;
    }
  }
  
  public User(){}

  public User(String jid) {
    this.jid = jid;
    this.channelNames = Lists.newArrayList();
    
    //Hack to make myself admin.  Always. (for now).
    if (jid.compareToIgnoreCase("circuitlego@gmail.com") == 0){
    	this.isAdmin = true;
    }else{
    	this.isAdmin = false;
    }
  }

  public User(User other) { 
    this.channelNames = other.channelNames;
    this.jid = other.jid;
    this.phoneNumber = other.phoneNumber;
    this.carrier = other.carrier;
    this.lastSeen = other.lastSeen;
    this.isAdmin = other.isAdmin;
  }
  
  public boolean isAdmin(){
	  return isAdmin;
  }
  
  public void setAdmin(boolean b){
	  isAdmin = b;
  }

  public String getJID() {
    return jid;
  }
  
  public String getEmail() {
    // TODO(nsanch): this isn't quite right because it's possible to have a
    // jabber account that doesn't accept email, but this is good enough until
    // we have a web UI for this.
    return jid;
  }

  public String phoneNumber() {
    return phoneNumber;
  }
  
  public void setPhoneNumber(String phone) {
    phoneNumber = phone;
  }
  
  public User.Carrier carrier() {
    if (carrier == null) {
      return null;
    } 
    return Carrier.valueOf(carrier);
  }
  
  public void setCarrier(Carrier carrier) {
    this.carrier = carrier.name();
  }

  public boolean canReceiveSMS() {
    return carrier != null && phoneNumber != null;
  }

  public Date lastSeen() {
    return lastSeen;
  }
  
  public void maybeMarkAsSeen() {
    if (lastSeen == null ||
        (new Date().getTime() - lastSeen().getTime() > 
            User.LAST_SEEN_UPDATE_INTERNAL_MS)) {
      lastSeen = new Date();
      put();
    }    
  }  

  public List<String> channelNames() {
    return Collections.unmodifiableList(channelNames);
  }
  
  /**
   * Gets all of the channels the user is actually in (and which exist).
   */
  public List<Channel> getChannels() {
	//tempFix();
    boolean shouldPut = false;
    
    List<Channel> channels =
        Lists.newArrayListWithExpectedSize(channelNames.size());
    
    List<String> toRemove = Lists.newArrayList();
    
    for (String channelName : channelNames) {
      Channel channel = Datastore.instance().getChannelByName(channelName);
      if (channel != null) {
        if (channel.getMemberByJID(jid) != null) {
          channels.add(channel);
        }
      } else {
        logger.warning(
            "User " + jid + " was in non-existent channel " + channelName +
            ", removing");
        toRemove.add(channelName);
        shouldPut = true;
      }
    }
    
    for (String name : toRemove){
  	  removeChannel(name);
    }
    
    // While we have all these channels loaded, also take the opportunity to
    // to do other fixUps
    for (Channel channel : channels) {
      fixUp(channel);
    }
    
    if (shouldPut) {
      put();
    }
    
    return channels;
  }
  
  public void put() {
    Datastore.instance().put(this);
  }  
  
  @Override public String toString() {
    return "[User: jid: " + jid + ", phoneNumber: " + phoneNumber +
      ", carrier: " + carrier + ", channelNames: " + channelNames +
      "]";
  }  
  
  /**
   * Makes sure that the user <-> channel relationship is consistent.
   * 
   * @param channel channel that the user is IM-ing (and may be in). It is
   * considered the source of truth, the user object will be updated based on
   * its members list.
   */
  public void fixUp(Channel channel) {
    boolean shouldPut = false;
    
    String channelName = channel.getName();
    if (channel.getMemberByJID(jid) == null &&
        channelNames.contains(channelName)) {
      logger.warning(
          "User " + jid + " wasn't actually in " + channelName + ", removing");
      removeChannel(channelName);
      shouldPut = true;
    }
    
    if (channel.getMemberByJID(jid) != null &&
        !channelNames.contains(channelName)) {
      logger.warning(
          "User " + jid + " was supposed to be in " + channelName + ", adding");
      addChannel(channelName);
      shouldPut = true;
    }
    
    if (shouldPut) {
      put();
    }
  }  
  
  public void tempFix(){
		if (channelNames == null){
			channelNames = Lists.newArrayList();
			System.out.println("hm...");
		}
  }
  
  // The remaining methods deal with manipulation of the User/Channel 
  // relationship and should called by {@link Channel} and {@link Datastore} 
  // implementations only.
   
  @VisibleForTesting public void addChannel(String c) {
	//tempFix();
    if (!channelNames.contains(c)) {
      channelNames.add(c);
      
    }
  }
  
  @VisibleForTesting public void removeChannel(String c) {
    if (channelNames.contains(c)) {
      channelNames.remove(c);
    }
  }
  
  /**
   * Merge {@code src} into this user by adding them to all the channels that
   * src is in, and removing src from them. src will also be deleted from the
   * datastore. Can only merge if src is not the same as this user and if src is
   * just a differently capitalized variant of this user.
   */
  void merge(User src) {
    Preconditions.checkArgument(
        !src.jid.equals(jid), "Can't merge into the same user (%s)", jid);
    Preconditions.checkArgument(
        src.jid.equalsIgnoreCase(jid),
        "Can't merge non-equivalent users (%s vs. %s)",
        jid, src.jid);
    
    List<Channel> channelsToRemoveSrcFrom = Lists.newArrayList();
    
    for (String srcChannelName : src.channelNames) {
      Channel srcChannel =
          Datastore.instance().getChannelIfUserPresent(srcChannelName, src.jid);
      
      // The source user wasn't actually in the channel. We don't need to do
      // any fixing up, since we're going to delete the source user anyway.
      if (srcChannel == null) {
        logger.warning(src.jid + " was supposed to be in " + srcChannelName + 
            " but wasn't, ignored during merge");
        continue;
      }
      
      if (!channelNames.contains(srcChannelName)) {
        srcChannel.addMember(this);
      }
      
      channelsToRemoveSrcFrom.add(srcChannel);
    }

    for (Channel srcChannel : channelsToRemoveSrcFrom) {
      srcChannel.removeMember(src);
    }
    
    Datastore.instance().delete(src);
  }
}
