/**
 * 
 */
package com.imjasonh.partychapp.ppb;

import com.google.appengine.api.xmpp.JID;
import com.google.common.annotations.VisibleForTesting;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Unindexed;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

@Unindexed
@Cached
public class Reason {
  /** 500 is the maximum length of text fields in AppEngine */
  private static final int MAX_REASON_LENGTH = 500;

  @SuppressWarnings("unused")
  @Id private Long id;
  
  @Transient 
  private Target target;

  @Indexed
  private String targetId;

  @Transient 
  private Member sender;
 
  private String senderJID;
  
  private String reason;

  private String action;

  @Indexed
  private Date timestamp;
  
  private int scoreAfter;

  public Reason(){}
  
  // This should only be called by Target.takeAction()
  Reason(Target t, Member sender, Action act, String reason, int scoreAfter) {
    this.target = t;
    this.targetId = t.key();
    this.sender = sender;
    this.senderJID = sender.getJID();
    this.action = act.name();
    if (reason.length() > MAX_REASON_LENGTH) {
      this.reason = reason.substring(0, MAX_REASON_LENGTH);
    } else {
      this.reason = reason;
    }
    this.timestamp = new Date();
    this.scoreAfter = scoreAfter;
  }

  public Action action() {
    return Action.valueOf(action);
  }

  public Date timestamp() {
    return this.timestamp;
  }

  public Target target() {
    if (target == null) {
      target = Datastore.instance().getTargetByID(targetId);
    }
    return target;
  }

  public String reason() {
    return this.reason;
  }
  
  public int scoreAfter() {
    return scoreAfter;
  }
  
  public Member sender() {
    if (sender == null) {
      sender = target().channel().getMemberByJID(new JID(senderJID));
    }
    return sender;
  }
  
  public String senderAlias() {
    if (sender() != null) {
      return sender().getAlias();
    } else {
      return senderJID;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(action().ifPlusPlusElse("woot!", "ouch!"));
    sb.append(target.name());
    sb.append(" -> ");
    sb.append(scoreAfter);
    return sb.toString();
  }
  
  public void put() {
    Datastore.instance().put(this);
  }
  
  public Reason undo() {
    return target().takeAction(sender(),
                               action().opposite(),
                               "Undo: " + reason());
  }
  
  public String wootString() {
    StringBuilder sb = new StringBuilder();
    sb.append(action().isPlusPlus() ? "[woot! " : "[ouch! ");
    sb.append("now at " + scoreAfter() + "]");
    return sb.toString();
  }
  
  public void fixUp(int rightScore) {
    scoreAfter = rightScore;
    put();
  }
  
  /**
   * For tests, where we re-use objects in the in-memory fake datastore, this
   * method may be used to simulate the non-persistence of the sender field.
   */
  @VisibleForTesting public void clearSender() {
    sender = null;
  }
}