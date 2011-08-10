package com.imjasonh.partychapp;

import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Id;

@Unindexed
public class User implements Serializable{
	/** start with 1 for all classes */
	private static final long serialVersionUID = 1L;
  
  private static final Logger logger = Logger.getLogger(User.class.getName());

  /**
   * How often the lastSeen field should be updated (it's only used for
   * computing daily active stats, so it's wasteful to cause writes for every
   * single message received). For now only update it every 12 hours.
   */

  private static final long LAST_SEEN_UPDATE_INTERNAL_MS =
      12L * 60L * 60L * 1000L;

  @Id private String jid;
  
  @Indexed private String email;

  List<String> channelNames = Lists.newArrayList();
  
  String phoneNumber;
  
  String carrier;
  
  private boolean isAdmin = false;
  
  public String defaultAlias = null;
  
  private String key = null;
  
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
  
  //Leave until positive all data is migrated
  void importOldUser(@AlsoLoad("jid") String jid){
	  if (this.email == null){
		  this.email = jid;
	  }
  }

  public User(String address) {
    this.jid = address;
    this.email = address;
    
    //Hack to make myself admin.  Always. (for now).
    if (address.compareToIgnoreCase("circuitlego@gmail.com") == 0){
    	this.isAdmin = true;
    }
  }

  public User(User other) { 
    this.channelNames = other.channelNames;
    this.jid = other.jid;
    this.email = other.email;
    this.phoneNumber = other.phoneNumber;
    this.carrier = other.carrier;
    this.lastSeen = other.lastSeen;
    this.isAdmin = other.isAdmin;
    this.defaultAlias = other.defaultAlias;
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
    return email;
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
    return "[User: jid: " + jid + ", email: " + email + ", phoneNumber: " + phoneNumber +
      ", carrier: " + carrier + ", channelNames: " + channelNames +
      "]";
  }  
  
  @Override public boolean equals(Object o){
	  if (o instanceof User){
		  User other = (User) o;
		  return (this.email.equals(other.email) && this.jid.equals(other.jid));
	  }
	  return false;
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
  
  public void fixUp(){
	  if (this.email == null){
		  this.email = this.jid;
	  }
  }
  
  // The remaining methods deal with manipulation of the User/Channel 
  // relationship and should called by {@link Channel} and {@link Datastore} 
  // implementations only.
   
  @VisibleForTesting public void addChannel(String c) {
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
  
  public void setEmail(String email){
	  this.email = email;
  }
  
  private void setJID(String jid){
	  this.jid = jid;
  }
  
  public static User changeJID(User old, String newJid){
	  Datastore ds = Datastore.instance();
	 // String oldJid = old.jid;
	  
	  User newU = new User(old);
	  newU.setJID(newJid);
	  
	  List<String> copy = Lists.newArrayList(old.channelNames);
	  for (String channelName : copy){
		  Channel c = ds.getChannelByName(channelName);
		  
		  c.changeMember(old, newU);
		  
		  c.put();
	  }
	  
	  Datastore.instance().delete(old);
	  newU.put();
	  return newU;
  }
  
  public static User mergeForJID(User emailUser, User jidUser){

	    if(emailUser.equals(jidUser)){
	    	logger.severe("Tried merging user with itself.");
	    	return emailUser;
	    }
	    
	    User newUser = new User(emailUser);
	    newUser.setJID(jidUser.jid);
	    
	    Set<String> allChannels = Sets.newHashSet();
	    allChannels.addAll(emailUser.channelNames);
	    allChannels.addAll(jidUser.channelNames);
	    
	    for (String srcChannelName : allChannels) {
	      Channel c = Datastore.instance().getChannelByName(srcChannelName);

	      c.changeMember(emailUser, jidUser, newUser);
	      if(!newUser.channelNames.contains(srcChannelName)){
	    	  newUser.channelNames.add(srcChannelName);
	      }
	      c.put();
	    }

	    Datastore.instance().delete(jidUser);
	    Datastore.instance().delete(emailUser);
	    
	    newUser.put();
	    return newUser;
  }
  
  public boolean is10Gen(){
	  String domain = email.split("@")[1];
	  if (domain.compareTo("10gen.com") == 0){
		  return true;
	  }
	  return false;
  }
  
	public static String asHex (byte buf[]) {
	      StringBuffer strbuf = new StringBuffer(buf.length * 2);
	      int i;

	      for (i = 0; i < buf.length; i++) {
	       if (((int) buf[i] & 0xff) < 0x10)
		    strbuf.append("0");

	       strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
	      }

	      return strbuf.toString();
	     }
	
	public static String newKey(User user, User other) throws Exception{
		  // Get the KeyGenerator
	
	    KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    kgen.init(128); // 192 and 256 bits may not be available
	
	
	    // Generate the secret key specs.
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	    
	    user.key = asHex(skeySpec.getEncoded());
	    other.key = user.key;
	    
	    user.put();
	    other.put();
	    
	    return user.key;
	}
	
	public static boolean verifyMerge(String key, User user, User other){
		  if (user.key != null && other.key != null && key != null){
			  if( key.compareTo(user.key) == 0 && key.compareTo(other.key) == 0){
				  user.key = null;
				  other.key = null;
			      user.put();
			      other.put();
			      return true;
			  }
		  }
		  return false;
	}
}
