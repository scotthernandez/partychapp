package com.imjasonh.partychapp.server.command;

import com.google.common.collect.Lists;

import java.util.List;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.MailUtil;
import com.imjasonh.partychapp.testing.FakeDatastore;
import com.imjasonh.partychapp.testing.MockMailService;

public class InviteHandlerTest extends CommandHandlerTestCase {
  InviteHandler handler = new InviteHandler();
  MockMailService mailer = new MockMailService();
  
  @Override
  public void setUp() {
	super.setUp();
    MailUtil.setMailService(mailer);
  }
  
  public void testInviteAndEmailIsSent() {
    handler.doCommand(Message.createForTests("/invite dan@gmail.com"));
    List<String> invited = FakeDatastore.fakeChannel().getInvitees();
    assertEquals(1, invited.size());
    assertEquals("dan@gmail.com", invited.get(0));
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("_neil invited dan@gmail.com_", xmpp.messages.get(0).getBody());
    
    assertEquals(1, xmpp.invited.size());
    assertEquals("dan@gmail.com", xmpp.invited.get(0).getId());
    
    assertEquals(1, mailer.sentMessages.size());
    assertEquals("neil invited you to 'pancake'",
                 mailer.sentMessages.get(0).getSubject());
    assertTrue("Actual Message: " + mailer.sentMessages.get(0).getTextBody(),
               mailer.sentMessages.get(0).getTextBody().startsWith(
                   "neil (neil@gmail.com) invited you to a chatroom named 'pancake'"));
    assertEquals("pancake@" + Configuration.mailDomain,
                 mailer.sentMessages.get(0).getSender());
  }
  
  public void testNoInput() {
    handler.doCommand(Message.createForTests("/invite"));
    assertEquals(
        "Please list some email addresses to invite",
        xmpp.messages.get(0).getBody());
  }

  public void testMultipleInvitees() {
    handler.doCommand(Message.createForTests("/invite dan@gmail.com, roro@gmail.com"));
    List<String> invited = FakeDatastore.fakeChannel().getInvitees();
    assertEquals(2, invited.size());
    assertEquals("dan@gmail.com", invited.get(0));
    assertEquals("roro@gmail.com", invited.get(1));
    
    assertEquals(2, xmpp.messages.size());
    assertEquals("_neil invited dan@gmail.com_", xmpp.messages.get(0).getBody());
    assertEquals("_neil invited roro@gmail.com_", xmpp.messages.get(1).getBody());
    
    assertEquals(2, xmpp.invited.size());
    assertEquals("dan@gmail.com", xmpp.invited.get(0).getId());
    assertEquals("roro@gmail.com", xmpp.invited.get(1).getId());
    
    assertEquals(2, mailer.sentMessages.size());
  }
  
  public void testParseInvitees() {
    List<String> parsed = Lists.newArrayList();
    String error = InviteHandler.parseEmailAddresses(
        "neil@gmail.com , " +
        "jason@gmail.com, " +
        "bad thing at foo dot com, " +
        "x <foo@bar.com>, " + 
        "foo," +
        "foo@" + Configuration.chatDomain + "," +
        "foo@40.latest." + Configuration.chatDomain + "," +
        "neil@gmail.com?",
        parsed);

    assertEquals(3, parsed.size());
    assertEquals("neil@gmail.com", parsed.get(0));
    assertEquals("jason@gmail.com", parsed.get(1));
    assertEquals("foo@bar.com", parsed.get(2));
    assertEquals(
        "Could not invite \"bad thing at foo dot com\". " +
            "Is it a valid email address?\n" +
        "Could not invite \"foo\". Did you mean \"foo@gmail.com\"?\n" +
        "Could not invite \"foo@" + Configuration.chatDomain + "\" " +
            "(cannot invite other rooms)\n" +
        "Could not invite \"foo@40.latest." + Configuration.chatDomain + "\" " +
            "(cannot invite other rooms)\n" +
        "Could not invite \"neil@gmail.com?\". " +
            "Is it a valid email address?\n",
        error);
  }
}