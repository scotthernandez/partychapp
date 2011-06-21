package com.imjasonh.partychapp.server.command;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class SnoozeHandlerTest extends CommandHandlerTestCase {
  SnoozeHandler handler = new SnoozeHandler();
  
  @Override
  public void setUp() {
    super.setUp();
    // 10/21/2009 at 10:21:10 am EDT
    handler.setTimeForTesting(1256134870830L);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/snooze 20m")));
    assertTrue(handler.matches(Message.createForTests(" /snooze 392s")));
  }
  
  public void testBadInput() {
    snoozeAndCheckReply("/snooze", "You must specify a number");
    snoozeAndCheckReply("/snooze onethousandyears", "You must specify a number");
    snoozeAndCheckReply("/snooze 10mm", "You must specify a number");
  }
  
  public void testSimple1() {
    snoozeAndGetDate("/snooze 60s",
                     "Okay, snoozing for 60 seconds (60 seconds)",
                     "October 21, 2009 10:22:10 AM EDT");
  }

  public void testSimple2() {
    snoozeAndGetDate("/snooze 45m",
                     "Okay, snoozing for 45 minutes (2700 seconds)",
                     "October 21, 2009 11:06:10 AM EDT");
  }

  public void testSimple3() {
    snoozeAndGetDate("/snooze 3h",
                     "Okay, snoozing for 3 hours (10800 seconds)",
                     "October 21, 2009 1:21:10 PM EDT");
  }

  public void testSimple4() {
    snoozeAndGetDate("/snooze 4d",
                     "Okay, snoozing for 4 days (345600 seconds)",
                     "October 25, 2009 10:21:10 AM EDT");
  }
  
  public void testIntegerOverflow() {
    snoozeAndGetDate("/snooze 36524d",
                     "Okay, snoozing for 36524 days (3155673600 seconds)",
                     "October 21, 2109 10:21:10 AM EDT");
  }
  
  private void snoozeAndCheckReply(String cmd, String reply) {
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests(cmd));
    assertEquals(1, xmpp.messages.size());
    String msg = xmpp.messages.get(0).getBody();
    assertTrue("expected to find \"" + reply + " in \"" + msg + "\"",
               msg.contains(reply));    
  }
  
  private void snoozeAndGetDate(String cmd, String reply, String date) {
    snoozeAndCheckReply(cmd, reply);
    Date actual = FakeDatastore.fakeChannel().getMemberByAlias("neil").getSnoozeUntil();
    System.err.println("actual snooze: " + actual);
    assertNotNull(actual);
    DateFormat format = DateFormat.getDateTimeInstance(
        DateFormat.LONG, DateFormat.LONG, Locale.US);
    try {
      // divide by 1000 because the expected values are only accurate to the
      // second, not to the millisecond.
      assertEquals(format.parse(date).getTime() / 1000L, actual.getTime() / 1000L);
    } catch (ParseException e) { fail("bad expected value " + date); }
  }  
}