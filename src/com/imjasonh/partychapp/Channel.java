package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

import com.imjasonh.partychapp.DebuggingOptions.Option;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.filters.SharedURL;
import com.imjasonh.partychapp.server.MailUtil;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.live.ChannelUtil;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;
import com.imjasonh.partychapp.urlinfo.UrlInfo;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

@Unindexed
public class Channel implements Serializable {
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
  
  /**
   * Turns off all messages for the room. 
   */
  private Boolean loggingDisabled = false;
  
  public Channel(){}
    
  public Channel(JID serverJID) {
    this.name = serverJID.getId().split("@")[0];
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
    this.minilogDisabled = other.minilogDisabled;
    this.loggingDisabled = other.loggingDisabled;
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

  public void invite(String email) {
    // Need to be robust b/c invitees was added after v1 of this class.
    String cleanedUp = email.toLowerCase().trim();
    if (!invitedIds.contains(cleanedUp)) {
      invitedIds.add(cleanedUp);
    }
    requestedInvitations.remove(cleanedUp);
  }

  public boolean canJoin(String email) {
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
	    this.loggingDisabled = loggingDisabled;
  }
  
  /**
   * Adds a member to the channel. This may alter the member's alias by
   * prepending a _ if the channel already has a member with that alias. Removes
   * from invite list if invite-only room.
   */
  public Member addMember(User userToAdd) {
    String jidNoResource = userToAdd.getJID().split("/")[0];
    String email = jidNoResource;
    if (invitedIds == null || !invitedIds.remove(email.toLowerCase())) {
      if (isInviteOnly()) {
        throw new IllegalArgumentException("Not invited to this room");
      }
    }
    Member addedMember = new Member(this, jidNoResource);
    String dedupedAlias = addedMember.getAlias();
    while (null != getMemberByAlias(dedupedAlias)) {
      dedupedAlias = "_" + dedupedAlias;
    }
    addedMember.setAlias(dedupedAlias);
    mutableMembers().add(addedMember);
    
    userToAdd.addChannel(getName());
    userToAdd.put();
    
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
   * Remove a user or invitee by alias or ID.
   * @return True if someone was removed
   */
  public boolean kick(String id) {
    Member member = getMemberByAlias(id);
    if (member == null) {
      member = getMemberByJID(new JID(id));
    }
    if (member != null) {
      removeMember(Datastore.instance().getUserByJID(member.getJID()));
      return true;
    }
    if (invitedIds.remove(id)) {
      return true;
    }
    if (requestedInvitations.remove(id)) {
      return true;
    }
    return false;
  }

  public void put() {
    Datastore.instance().put(this);
  }

  public void delete() {
    Datastore.instance().delete(this);
  }
  
  public boolean isMiniLogDisabled() {
    return minilogDisabled;
  }
  
  public boolean isLoggingDisabled() {
	  return loggingDisabled;
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
    if (loggingDisabled == null) {
    	loggingDisabled = true;
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
  }
  

  @Serialized
  private List<SharedURL> shared = Lists.newArrayListWithCapacity(SharedURL.SHARED_URL_LIMIT);
  
  public boolean storeShared(SharedURL toShare){
	  for (SharedURL existing : shared){
		  if (existing.getUrl().equals(toShare.getUrl())){
			  return false;
		  }
	  }
	  shared.add(0, toShare);
	  if (shared.size() > SharedURL.SHARED_URL_LIMIT){
		  shared.remove(SharedURL.SHARED_URL_LIMIT);
	  }
	  return true;
  }
  

  public List<SharedURL> getShared(){
	  return Collections.unmodifiableList(shared);
  }
  
  public URI getLink(int index){
	  if (shared.size() > index){
		  return shared.get(index).getUrl();
	  }
	  return null;
  }
}