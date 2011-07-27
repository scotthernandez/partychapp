package com.imjasonh.partychapp.filters;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.command.CommandHandlerTestCase;


public class TicketFilterTest extends CommandHandlerTestCase {
  TicketFilter handler = new TicketFilter();

  public void testPatternMatching() {
	assertTrue( handler.matches(Message.createForTests("AAAA-1234")));
	assertTrue( handler.matches(Message.createForTests("C-1")));
	assertTrue( handler.matches(Message.createForTests("AF-123")));
	assertTrue( handler.matches(Message.createForTests("AASDFGVAA-123423459")));
	assertTrue( handler.matches(Message.createForTests("asd AAAA-1234 fsadf")));
	assertTrue( handler.matches(Message.createForTests(" asdf. sdsdf . C-1 asdfas")));
	assertTrue( handler.matches(Message.createForTests("afsdf AF-123")));
	assertTrue( handler.matches(Message.createForTests("AASDFGVAA-123423459 asdfuehrs")));
	assertFalse( handler.matches(Message.createForTests("-1234")));
	assertFalse( handler.matches(Message.createForTests("AAAA-")));
	assertFalse( handler.matches(Message.createForTests("-")));
	assertFalse( handler.matches(Message.createForTests("qsdff-12")));
	assertFalse( handler.matches(Message.createForTests("sdfAAAA-1234")));
	assertFalse( handler.matches(Message.createForTests("AAAA-1234sdf")));
   
  }
  
  
}