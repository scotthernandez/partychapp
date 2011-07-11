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
  
  public void testSubstitution() {
	Message msg1 = Message.createForTests("AAAA-1234");
	handler.doCommand(msg1);
	Message msg2 = Message.createForTests("Well, this ticket (CS-9876) is just horrible.");
	handler.doCommand(msg2);
	Message msg3 = Message.createForTests("SERVER-65, can someone check it out?");
	handler.doCommand(msg3);
	
    assertEquals(6, xmpp.messages.size());
//	assertEquals("[neil] http://jira.mongodb.org/browse/" + msg1.content, xmpp.messages.get(0).getBody());
//	assertEquals("[neil] Well, this ticket (http://jira.mongodb.org/browse/CS-9876) is just horrible.", xmpp.messages.get(2).getBody());
//	assertEquals("[neil] http://jira.mongodb.org/browse/SERVER-65, can someone check it out?", xmpp.messages.get(4).getBody());
	

//	assertEquals("_[neil] http://jira.mongodb.org/browse/AAAA-1234_", xmpp.messages.get(1).getBody());
//	assertEquals("_[neil] Well, this ticket (http://jira.mongodb.org/browse/CS-9876) is just horrible._", xmpp.messages.get(3).getBody());
//	assertEquals("_[neil] http://jira.mongodb.org/browse/SERVER-65, can someone check it out?_", xmpp.messages.get(5).getBody());
	
  }
}