package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;


public class LogHandlerTest extends CommandHandlerTestCase {
  LogHandler handler = new LogHandler();

  public void testAddToLastMessages() {
	Message msg = Message.createForTests("test");
	for (int i = 0; i < 100; i++){
		handler.doCommand(msg);
	}
    
  }
}