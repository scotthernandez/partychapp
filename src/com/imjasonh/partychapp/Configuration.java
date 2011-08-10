package com.imjasonh.partychapp;

public class Configuration {
  public static final String domain = "10genchat";
  public static final String chatDomain = domain + ".appspotchat.com";
  public static final String webDomain = "chat.10gen.com";
  //public static final String webDomain = domain + ".appspot.com";
  public static final String mailDomain = domain + ".appspotmail.com";

  public static final boolean isConfidential = false;
  
  // A nightly stats email is sent to this address. (See StatsCronJob.)
  public static final String statsEmailAddress = "circuitlego@gmail.com";
  
  private static PersistentConfiguration pc;
  private static long pcLoadTimeMillis = 0;
  private static final long MAX_PC_AGE_MILLIS = 60 * 1000L;
  
  public static void reloadPersistentConfig() {
    pc = null;
    persistentConfig();
  }
  
  public static long getPeristentConfigLoadTimeMillis() {
    return pcLoadTimeMillis;
  }
  
  public static PersistentConfiguration persistentConfig() {
    long now = System.currentTimeMillis();
    long pcAge = now - pcLoadTimeMillis;
    if (pc == null || pcAge > MAX_PC_AGE_MILLIS) {
      pcLoadTimeMillis = now;
      pc = Datastore.instance().getPersistentConfig();
      if (pc == null) {
        pc = new PersistentConfiguration();
      }
    }
    return pc;
  }
}
