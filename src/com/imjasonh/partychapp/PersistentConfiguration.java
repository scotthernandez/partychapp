package com.imjasonh.partychapp;

import java.util.logging.Logger;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

/**
 * Used to configure private, per-installation or frequently changed information
 * that shouldn't be checked into SVN. Can be viewed and edited at the
 * /admin/config URL.
 *
 * @author nsanch
 */

@Unindexed
public class PersistentConfiguration {
  
  @SuppressWarnings("unused")
private static final Logger logger = 
      Logger.getLogger(Channel.class.getName());

  @SuppressWarnings("unused")
  @Id private String name = "config";
  
  /** AuthSub session token for updating the stats spreadsheet */
  private String sessionToken;
  
  /** GData feed URL for the stats spreadsheet */
  private String listFeedUrl;
  
  /** Whether channel stats are being recorded or not (has overhead) */
  private Boolean areChannelStatsEnabled;
  
  public String sessionToken() { return sessionToken; }
  public String listFeedUrl() { return listFeedUrl; }
  public boolean areChannelStatsEnabled() {
    return areChannelStatsEnabled != null &&
        areChannelStatsEnabled.booleanValue();
  }
  
  // Setters are meant for use by {@link ConfigEditServlet} only
  
  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }
  
  public void setListFeedUrl(String listFeedUrl) {
    this.listFeedUrl = listFeedUrl;
  }
  
  public void setChannelStatsEnabled(boolean areChannelStatsEnabled) {
    this.areChannelStatsEnabled = areChannelStatsEnabled;
  }
  
}
