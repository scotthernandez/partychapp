package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

import com.imjasonh.partychapp.DebuggingOptions.Option;
import com.imjasonh.partychapp.Member.Permissions;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.logging.ChannelLog;
import com.imjasonh.partychapp.server.MailUtil;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.live.ChannelUtil;
import com.xgen.partychapp.clienthub.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.persistence.Embedded;
import javax.persistence.Id;

@Unindexed
@Cached
@SuppressWarnings("serial")
public class Channel implements Serializable{
	/** start with 1 for all classes */

    
  private static final Logger logger = 
      Logger.getLogger(Channel.class.getName());
  
  /**
   * Channels with more than this many members may have slightly different
   * behavior.
   */
  private static final int LARGE_CHANNEL_THRESHOLD = 50;

  @Id private String name;

  @Serialized
  private Set<Member> members = Sets.newHashSet();

  private Boolean hubLinked = false;
  
  private Boolean inviteOnly = false;

  private List<String> invitedIds = Lists.newArrayList();
  
  //Private map<string, string> sharedURLs of urls mapped to their alias
  
  private Integer sequenceId = 0;
  
  /** 
   * Email addresses of users that have requested invitations.
   */
  private List<String> requestedInvitations = Lists.newArrayList();
  
  /**
   * Turns off storing of recent messages for the room. 
   */
  private Boolean minilogDisabled = false;
  
  @Embedded
  private ChannelLog channelLog = new ChannelLog();

private Date logSectionStart;

private Date logSectionEnd;
  
  public Channel(){}
    
  public Channel(JID serverJID, User creator) throws Exception{

	this.name = serverJID.getId().split("@")[0];
	if (ClientHubAPI.hasClient(this.name)){
		int clientLevel = ClientHubAPI.getContactLevel(this.name, creator.getEmail());
		if (clientLevel > 0) {
			this.addMember(creator);
			this.setInviteOnly(true);
		} else {
			throw new Exception("You are not listed on that clienthub page");
		}
	} else {
		this.addMember(creator).setPermissions(Permissions.ADMIN);
	}
	this.logSectionStart = new Date();
    this.logSectionEnd = new Date();
	if (ClientHubAPI.hasClient(this.name)) 
		hubLinked = true;
  }
   
  public Channel(Channel other) {
    this.name = other.name;
    this.inviteOnly = other.inviteOnly;
    this.invitedIds = Lists.newArrayList(other.invitedIds);
    this.members = Sets.newHashSet();
    for (Member m : other.members) {
      this.members.add(new Member(m));
    }
    this.sequenceId = other.sequenceId;
    this.channelLog = other.channelLog;
  }
  
  public JID serverJID() {
    return new JID(serverJIDAsString());
  }
  
  public String serverJIDAsString() {
    return name + "@" + Configuration.chatDomain;
  }
  
  public String mailingAddress() {
    return name + "@" + Configuration.mailDomain;
  }
  
  public String webUrl() {
    return "http://" + Configuration.webDomain + "/room/" + name;
  } 
  
  public boolean isHubLinked() {
	return hubLinked;
  }

  public void invite(String email) {
    // Need to be robust b/c invitees was added after v1 of this class.
    String cleanedUp = email.toLowerCase().trim();
    if (!invitedIds.contains(cleanedUp)) {
      invitedIds.add(cleanedUp);
    }
    requestedInvitations.remove(cleanedUp);
  }

  public boolean canJoin(String email) {
	if (ClientHubAPI.hasClient(getName()) ){
	    if (ClientHubAPI.getContactLevel(getName(), email) > 0) {
	    	return true;
	    }
	}	
    return !isInviteOnly() ||
        (invitedIds.contains(email.toLowerCase().trim()));
  }

  public void setInviteOnly(boolean inviteOnly) {
    this.inviteOnly = inviteOnly;
  }
  
  public void setMiniLogDisabled(boolean minilogDisabled) {
    this.minilogDisabled = minilogDisabled;
    if (minilogDisabled) {
      // Clear currently logged messages if we're disabling logging.
      fixUp();
    }
  }
  
  public void setLoggingDisabled(boolean loggingDisabled) {
	    channelLog.enable(!loggingDisabled);
  }
  

//TODO: Date is mutable, so this is unsafe.
public Date getLogSectionStart() {
	return channelLog.sectionStart;
}

public void setLogSectionStart(Date logSectionStart) {
	channelLog.sectionStart = logSectionStart;
}

public Date getLogSectionEnd() {
	return channelLog.sectionEnd;
}

public void setLogSectionEnd(Date logSectionEnd) {
	channelLog.sectionEnd = logSectionEnd;
}

/**
   * Adds a member to the channel. This may alter the member's alias by
   * prepending a _ if the channel already has a member with that alias. Removes
   * from invite list if invite-only room.
   */
  public Member addMember(User userToAdd) {
    String jidNoResource = userToAdd.getJID().split("/")[0];
    String email = jidNoResource;
    if (!canJoin(email))
    	throw new IllegalArgumentException("Not invited to this room");
    Member addedMember = new Member(this, jidNoResource);
    String dedupedAlias = addedMember.getAlias();
    while (null != getMemberByAlias(dedupedAlias)) {
      dedupedAlias = "_" + dedupedAlias;
    }
    addedMember.setHidden(false);
    addedMember.setAlerted(true);
    addedMember.setAlias(dedupedAlias);
    if (ClientHubAPI.hasClient(getName()) ){
    	int perm = ClientHubAPI.getContactLevel(getName(), email);
    	if (perm == 3){
    		addedMember.setPermissions(Permissions.ADMIN);
    	} else if (perm == 2){
    		addedMember.setPermissions(Permissions.MOD);
    	}
    }
    mutableMembers().add(addedMember);
    userToAdd.addChannel(getName());
    userToAdd.put();
    this.put();
    return addedMember;    
  }
  
  private Set<Member> mutableMembers() {
    return members;
  }

  public void removeMember(User userToRemove) {
    Member memberToRemove = getMemberByLiteralJID(userToRemove.getJID());
    
    if (!mutableMembers().remove(memberToRemove)) {
      logger.warning(
          userToRemove.getJID() + " was not actually in channel " +
          getName() + " when removing");
    }

    userToRemove.removeChannel(getName());
    userToRemove.put();
  }

  private List<Member> getMembersToSendTo() {
    return getMembersToSendTo(null);
  }

  /**
   * @param exclude
   *          a JID to exclude (for example the person sending the broadcast message)
   * @return an array of JIDs to send a message to, excluding snoozing members.
   */
  private List<Member> getMembersToSendTo(Member exclude) {
    List<Member> recipients = Lists.newArrayList();
    for (Member member : getMembers()) {
      if (!member.equals(exclude)
          && member.getSnoozeStatus() != SnoozeStatus.SNOOZING) {
        recipients.add(member);
      }
    }
    
    return recipients;
  }

  public String getName() {
    return name;
  }

  public Set<Member> getMembers() {
    return Collections.unmodifiableSet(mutableMembers());
  }

  public Member getMemberByJID(JID jid) {
    return getMemberByJID(jid.getId());
  }
  
  public Member getMemberByJID(String jid) {
    String shortJID = jid.split("/")[0];
    for (Member member : getMembers()) {
      if (member.getJID().equalsIgnoreCase(shortJID)) {
        return member;
      }
    }
    return null;
  }

  /**
   * Unlike {@link Channel#getMemberByJID(JID)}, does a literal (case-sensitive)
   * comparison of JIDs. This should only be used when merging or removing users
   * from channels.
   */
  Member getMemberByLiteralJID(String jid) {
    String shortJID = jid.split("/")[0];
    for (Member member : getMembers()) {
      if (member.getJID().equals(shortJID)) {
        return member;
      }
    }
    return null;
  }  

  public Member getMemberByAlias(String alias) {
    for (Member member : getMembers()) {
      if (member.getAlias().equals(alias)) {
        return member;
      }
    }
    return null;
  }
 
  public Member getOrSuggestMemberFromUserInput(String input, StringBuilder suggestion) {
    Member found = getMemberByAlias(input);
    if (found != null) {
      return found;
    }
    if (input.indexOf('@') != -1) {
      found = getMemberByJID(new JID(input));
      if (found != null) {
        return found;
      }
    } else {
      for (Member m : getMembers()) {
        if (m.getJID().startsWith(input + "@")) {
          return m;
        }
      }
    }

    suggestion.append("Could not find member with input '" + input + ".'");
    for (Member m : getMembers()) {
      if (m.getAlias().contains(input) ||
          m.getJID().contains(input)) {
        suggestion.append(" Maybe you meant '" + m.getAlias() + ".'");
      }
    }
    return null;
  }
 
  
  public Member getMemberByPhoneNumber(String phoneNumber) {
    for (Member member : getMembers()) {
      User memberUser = Datastore.instance().getUserByJID(member.getJID());
      String memberPhone = memberUser.phoneNumber();
      if ((memberPhone != null) && memberPhone.equals(phoneNumber)) {
        return member;
      }
    }
    if (phoneNumber.startsWith("1")) {
      return getMemberByPhoneNumber(phoneNumber.substring(1));
    } else {
      return getMemberByPhoneNumber("1" + phoneNumber);
    }
  }

  /**
   * Remove a user or invitee by alias ID.
   * @return True if someone was removed
   */
  public boolean kick(String id) {
    Member member = getMemberByAlias(id);
    if (member == null) {
      member = getMemberByJID(new JID(id));
    }
    if (member != null) {
      invite(member.getJID());
      removeMember(Datastore.instance().getUserByJID(member.getJID()));
      this.put();
      return true;
    }
    if (invitedIds.remove(id)) {
      this.put();
      return true;
    }
    if (requestedInvitations.remove(id)) {
      this.put();
      return true;
    }
    return false;
  }

  public void put() {
	try{
		throw new Exception("");
	}catch (Exception e){
	logger.log(Level.INFO, "Channel put by " + 
                             e.getStackTrace()[1].getClassName() + 
                             "." +
                             e.getStackTrace()[1].getMethodName() + 
                             "()!" );
	}
    Datastore.instance().put(this);
  }

  public void delete() {
    Datastore.instance().delete(this);
  }
  
  public boolean isMiniLogDisabled() {
    return minilogDisabled;
  }
  
  public boolean isLoggingDisabled() {
	  return channelLog.isEnabled();
  }

  public boolean isInviteOnly() {
    return inviteOnly;
  }
 
  public List<String> getInvitees() {
    return invitedIds;
  }
  
  public void removeInvitee(String invitee) {
    invitedIds.remove(invitee.toLowerCase().trim());
  }  
  
  public List<String> getRequestedInvitations() {
    return requestedInvitations;
  }
  
  public boolean hasRequestedInvitation(String email) {
    return requestedInvitations.contains(email.toLowerCase().trim());
  }
  
  public void addRequestedInvitation(String email) {
    String cleanedUp = email.toLowerCase().trim();
    if (!requestedInvitations.contains(cleanedUp)) {
      requestedInvitations.add(cleanedUp);
    }
  }    
  
  private void sendMessage(String message, List<Member> recipients) {
    List<JID> withSequenceId = Lists.newArrayList();
    List<JID> noSequenceId = Lists.newArrayList();
    for (Member m : recipients) {
      if (!m.isAlerted()) {
    	  continue;
      }
      if (m.debugOptions().isEnabled(Option.SEQUENCE_IDS)) {
        withSequenceId.add(new JID(m.getJID()));
      } else {
        noSequenceId.add(new JID(m.getJID()));
      }
    }
    
    // For small channels, also send messages to all invitees. That way as soon 
    // as they accept the chat request, they'll start getting messages, even 
    // before they message the bot and are added to the room in JoinCommand.
    if (members.size() < LARGE_CHANNEL_THRESHOLD) {
      for (String invitee : getInvitees()) {
        noSequenceId.add(new JID(invitee));
      }
    }

    Set<JID> errorJIDs = sendMessage(message, withSequenceId, noSequenceId);
    
    for (JID errorJID : errorJIDs) {
      // Skip over invitees, they're not members and so don't have debug options
      if (invitedIds.contains(errorJID.getId())) {
        continue;
      }
      Member member = getMemberByJID(errorJID);
      if (member == null) {
        logger.warning(
            "Could not find member " + errorJID.getId() + " in channel" + name);
        continue;
      }
      if (member.debugOptions().isEnabled(Option.ERROR_NOTIFICATIONS)) {
        sendDirect(
            "Attempted to send \"" + message + "\" to you but got an error",
            member);
      }
    }
    
    // TODO(mihaip): add uniform interface for XMPP and Channel endpoints, so
    // that Channel doesn't have to know about either SendUtil or ChannelUtil.
    for (Member recipient : recipients) {
      ChannelUtil.sendMessage(this, recipient, message);
    }
  }
  
  private Set<JID> sendMessage(
        String message, List<JID> withSequenceId, List<JID> noSequenceId) {
    incrementSequenceId();
    awakenSnoozers();

    String messageWithSequenceId = message + " (" + sequenceId + ")";

    Set<JID> errorJIDs = Sets.newHashSet();
    errorJIDs.addAll(SendUtil.sendMessage(message, serverJID(), noSequenceId));
    errorJIDs.addAll(
        SendUtil.sendMessage(messageWithSequenceId, serverJID(), withSequenceId));
    
    put();
    
    return errorJIDs;
  }
  
  public void sendDirect(String message, Member recipient) {
    SendUtil.sendMessage(message,
                         serverJID(),
                         Collections.singletonList(new JID(recipient.getJID())));
    ChannelUtil.sendMessage(this, recipient, message);
  }
  
  public void broadcast(String message, Member sender) {
    sendMessage(message, getMembersToSendTo(sender));
  }

  public void broadcastIncludingSender(String message) {
    sendMessage(message, getMembersToSendTo());
  }
  
  public String sendMail(String subject,
                         String body,
                         String recipient) {
    return MailUtil.sendMail(subject, body, this.mailingAddress(), recipient);
  }

  public List<Member> sendSMS(String body, Collection<Member> recipients) {
    List<Member> realRecipients = Lists.newArrayList();
    List<String> addresses = Lists.newArrayList();
    for (Member m : recipients) {
      User memberUser = Datastore.instance().getUserByJID(m.getJID());
      if (memberUser.canReceiveSMS()) {
        addresses.add(
            memberUser.carrier().emailAddress(memberUser.phoneNumber()));
        realRecipients.add(m);
      }
    }

    for (String addr : addresses) {
      sendMail("(sent from partychat)",
               body,
               addr);
    }
    
    return realRecipients;
  }
  
  public List<Member> broadcastSMS(String body) {
    return sendSMS(body, getMembers());
  }
  
  private void awakenSnoozers() {
    // awaken snoozers and broadcast them awaking.
    Set<Member> awoken = Sets.newHashSet();
    for (Member member : getMembers()) {
      if (member.unsnoozeIfNecessary()) {
        awoken.add(member);
      }
    }
    
    if (!awoken.isEmpty()) {
      put();
      StringBuilder sb = new StringBuilder();
      for (Member m : awoken) {
        if (sb.length() > 0) {
          sb.append("\n");
        }
        sb.append("_" + m.getAlias() + " is no longer snoozing_");
      }
      broadcastIncludingSender(sb.toString());
    }
  }

  private void incrementSequenceId() {
    ++sequenceId;
    if (sequenceId >= 100) {
      sequenceId = 0;
    }
  }
  
  public int getSequenceId() {
    return sequenceId;
  }
  
  public void fixUp() {
    boolean shouldPut = false;
    if (sequenceId == null) {
      sequenceId = 0;
      shouldPut = true;
    }
    if (members == null) {
      members = Sets.newHashSet();
      shouldPut = true;
    }
    if (inviteOnly == null) {
      inviteOnly = false;  
      shouldPut = true;
    }
    if (minilogDisabled == null) {
      // Default large rooms to disabled logging, so that their Channel entities
      // are smaller.
      minilogDisabled = members.size() > LARGE_CHANNEL_THRESHOLD;
      shouldPut = true;
    }
    if (invitedIds == null) {
      invitedIds = Lists.newArrayList();
      shouldPut = true;
    }
    if (requestedInvitations == null) {
      requestedInvitations = Lists.newArrayList();
      shouldPut = true;
    }

    List<String> membersToRemove = Lists.newArrayList();
    
    for (Member m : mutableMembers()) {
      // Don't allow channels to be in other channels {@link InviteHandler#
      // parseEmailAddresses} should be forbidding this, but just in case,
      // also fix this at read time.
      if (m.getJID().endsWith(Configuration.chatDomain) ||
          m.getJID().endsWith(Configuration.mailDomain)) {
        logger.warning(
            "Remove " + m.getJID() + " from " + name + " since it's a " +
            "possible infinite loop");
        membersToRemove.add(m.getJID());
        continue;
      }
      String jid = m.getJID().toLowerCase();
      if (invitedIds.contains(jid)) {
        invitedIds.remove(jid);
      }
      if (m.fixUp(this)) {
        shouldPut = true;
        logger.warning("Put member");
      }
    }
    
    if (!membersToRemove.isEmpty()) {
      for (String jid : membersToRemove) {
        User user = Datastore.instance().getUserByJID(jid);
        if (user != null) {
          removeMember(user);
        } else {
          // If we can't find a matching User, we should still remove the
          // member from the channel
          logger.warning("Could not find a User object for " + jid);
          Member memberToRemove = getMemberByJID(jid);
          mutableMembers().remove(memberToRemove);
        }
      }
      shouldPut = true;
    }
    
    if (shouldPut) {
      logger.warning("Channel " + name + " needed fixing up");
      put();
    }
    if (mutableMembers().size() == 0){
  	  Datastore.instance().delete(this);
  	  logger.warning("Channel " + name + "was removed. It had no members.");
    }
  }

public int logMaxLength() {
	return channelLog.maxLength();
}
public void removeAllUsers(){
    Datastore.instance().startRequest();
    List<String> membersToRemove = Lists.newArrayList();
    
    for (Member m : mutableMembers()) {
        membersToRemove.add(m.getJID());
    }
    for (String jid : membersToRemove){
        User user = Datastore.instance().getUserByJID(jid);
        if (user != null) {
          removeMember(user);
        } else {
          // If we can't find a matching User, we should still remove the
          // member from the channel
          logger.warning("Could not find a User object for " + jid);
          Member memberToRemove = getMemberByJID(jid);
          mutableMembers().remove(memberToRemove);
        }
   }
    Datastore.instance().endRequest();
}
}