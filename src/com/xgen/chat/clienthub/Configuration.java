package com.xgen.chat.clienthub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

class Configuration {
  private static final Logger logger = 
        Logger.getLogger(Configuration.class.getName());
    
  public static final String CH_CHANNELS_KEY = "ClientHubChannels";

  static String SCHEME = null;
  static String HOST = null;
  static int PORT = 0;
  static String REALM = null;
  static String USERNAME = null;
  static String PASSWORD = null;
  static String CONTACTS_PATH = null;
  static String UPLOAD_PATH = null;
  
  static {
  	try {
  	    Properties appProperties = new Properties();
  		appProperties.load(new FileInputStream("config.properties"));
  		SCHEME = appProperties.getProperty("scheme");
  		HOST = appProperties.getProperty("host");
  		PORT = Integer.parseInt(appProperties.getProperty("port"));
  		REALM = appProperties.getProperty("realm");
  		USERNAME = appProperties.getProperty("username");
  		PASSWORD = appProperties.getProperty("password");
  		CONTACTS_PATH = appProperties.getProperty("contacts_path");
  		UPLOAD_PATH = appProperties.getProperty("upload_path");
  		
	} catch (FileNotFoundException e) {
		logger.severe("ClientHubAPI config properties file was not found.  Won't be authenticated.");
	} catch (IOException e) {
		logger.severe(e.toString());
	} 
  }
  
}
