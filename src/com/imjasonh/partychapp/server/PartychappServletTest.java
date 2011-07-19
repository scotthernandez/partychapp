package com.imjasonh.partychapp.server;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
//import com.imjasonh.partychapp.InMemoryCachingDatastore;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.testing.FakeDatastore;
import com.imjasonh.partychapp.testing.MockMailService;
import com.imjasonh.partychapp.testing.MockXMPPService;

public class PartychappServletTest extends TestCase {
  private static final Logger logger = 
      Logger.getLogger(PartychappServletTest.class.getName());

  MockXMPPService xmpp = new MockXMPPService();
  PartychappServlet servlet = new PartychappServlet();
  
  @Override
  public void setUp() {
    Datastore datastore = /*new InMemoryCachingDatastore(*/new FakeDatastore()/*)*/;
    Datastore.setInstance(datastore);
    SendUtil.setXMPP(xmpp);
    MailUtil.setMailService(new MockMailService());
  }
  
  class TestMessage {
    boolean incoming;
    String userString;
    String content;
    JID serverJID;
    
    public TestMessage(boolean incoming, String userString, String content) {
      this.incoming = incoming;
      this.userString = userString;
      this.content = content;
      this.serverJID = new JID("pancake@" + Configuration.chatDomain);
    }
    
    public Message toIncomingMessage() {
      assertTrue(incoming);
      return new MessageBuilder()
        .withBody(content)
        .withFromJid(new JID(userString))
        .withRecipientJids(new JID[] { serverJID })
        .build();
    }
    
    public void assertSameAs(Message actual) {
      assertEquals(content, actual.getBody());

      List<JID> actualRecipients = Arrays.asList(actual.getRecipientJids());
      if (userString.startsWith("#")) {
        int expectedNum = Integer.valueOf(userString.substring(1));
        assertEquals(expectedNum, actualRecipients.size());
      } else if (userString.startsWith("-")) {
        String expectedNotToReceive = userString.substring(1);
        for (JID jid : actualRecipients) {
          assertNotSame(expectedNotToReceive, jid.getId());
        }
      } else {
        assertEquals(1, actualRecipients.size());
        assertEquals(userString, actualRecipients.get(0).getId());
      }
      
      assertEquals(serverJID.getId(), actual.getFromJid().getId());
    }
    
    @Override
    public String toString() {
      return "[ TestMessage: " + incoming + ", " +
          userString + ", " +
          "'" + content + "' ]"; 
    }
  }
  
  public void testIntegration() {
    TestMessage script[] = {
      new TestMessage(true, "neil@gmail.com", "hi partychat"),
      new TestMessage(false, "neil@gmail.com", "The channel 'pancake' has been created, and you have joined with the alias 'neil'"),
      // doesn't get sent because the room is empty.
      //"-neil@gmail.com", "[neil]s hi partychat"),

      new TestMessage(true, "neil@gmail.com", "okay, now the room should exist"),
      // doesn't get sent because the room is empty.
      //"-neil@gmail.com", "[neil] okay, now the room should exist"),

      new TestMessage(true, "jason@gmail.com", "i'm joining too!"),
      new TestMessage(false, "jason@gmail.com", "You have joined 'pancake' with the alias 'jason'"),
      new TestMessage(false, "-jason@gmail.com", "jason@gmail.com has joined the channel with the alias 'jason'"),
      new TestMessage(false, "-jason@gmail.com", "[jason] i'm joining too!"),

      new TestMessage(true, "neil@gmail.com", "jason++ for joining"),
      new TestMessage(false, "#2", "[neil] jason++ [woot! now at 1] for joining"),

      new TestMessage(true, "jason@gmail.com", "neil-- for bugs"),
      new TestMessage(false, "#2", "[jason] neil-- [ouch! now at -1] for bugs"),
      
      new TestMessage(true, "neil@gmail.com", "s/jason/intern/"),
      new TestMessage(false, "-neil@gmail.com", "[neil] s/jason/intern/"),
      new TestMessage(false, "#2", "Undoing original actions: jason++ [back to 0]"),
      new TestMessage(false, "#2", "neil meant _intern++ [woot! now at 1] for joining_"),

      new TestMessage(true, "neil@gmail.com", "blah blah blah"),      
      new TestMessage(false, "-neil@gmail.com", "[neil] blah blah blah"),

      new TestMessage(true, "neil@gmail.com", "s/blah/whee/"),
      new TestMessage(false, "-neil@gmail.com", "[neil] s/blah/whee/"),
      new TestMessage(false, "#2", "neil meant _whee blah blah_"),

      new TestMessage(true, "neil@gmail.com", "/alias sanchito"),
      new TestMessage(false, "neil@gmail.com", "You are now known as 'sanchito'"),
      new TestMessage(false, "#2", "'neil' is now known as 'sanchito'"),
      
      new TestMessage(true, "neil@gmail.com", "testing new alias"),
      new TestMessage(false, "-neil@gmail.com", "[sanchito] testing new alias"),
      
      new TestMessage(true, "neil@gmail.com", "/me hopes dolapo is happy"),
      new TestMessage(false, "#2", "_sanchito hopes dolapo is happy_"),
      
      new TestMessage(true, "jason@gmail.com", "/alias intern"),
      new TestMessage(false, "jason@gmail.com", "You are now known as 'intern'"),
      new TestMessage(false, "#2", "'jason' is now known as 'intern'"),

      new TestMessage(true, "neil@gmail.com", "/list"),
      new TestMessage(false, "neil@gmail.com", "Listing members of 'pancake'\n* intern (jason@gmail.com)\n* sanchito (neil@gmail.com)"),

      new TestMessage(true, "neil@gmail.com", "this is a unicode TM symbol: \u2122"),
      new TestMessage(false, "jason@gmail.com", "[sanchito] this is a unicode TM symbol: \u2122"),

      new TestMessage(true, "kushal@kushaldave.com", "now i'm joining"),
      new TestMessage(false, "kushal@kushaldave.com", "You have joined 'pancake' with the alias 'kushal'"),
      new TestMessage(false, "-kushal@kushaldave.com", "kushal@kushaldave.com has joined the channel with the alias 'kushal'"),
      new TestMessage(false, "-kushal@kushaldave.com", "[kushal] now i'm joining"),

      new TestMessage(true, "kushal@kushaldave.com", "/inviteonly"),
      new TestMessage(false, "#3", "_kushal set the room to invite-only._"),

      new TestMessage(true, "david@gmail.com", "i'll try to join but i haven't been invited"),
      new TestMessage(false, "david@gmail.com", "You must be invited to this room."),

      new TestMessage(true, "kushal@kushaldave.com", "/invite david@gmail.com"),
      new TestMessage(false, "#4", "_kushal invited david@gmail.com_"),

      new TestMessage(true, "david@gmail.com", "yay, now i can join"),
      new TestMessage(false, "david@gmail.com", "You have joined 'pancake' with the alias 'david'"),
      new TestMessage(false, "-david@gmail.com", "david@gmail.com has joined the channel with the alias 'david'"),
      new TestMessage(false, "-david@gmail.com", "[david] yay, now i can join"),

      new TestMessage(true, "david@gmail.com", "/status"),
      new TestMessage(false, "david@gmail.com", "You are currently in 'pancake' as 'david.'"),

      new TestMessage(true, "david@gmail.com", "radioheda++"),
      new TestMessage(false, "#4", "[david] radioheda++ [woot! now at 1]"),

      new TestMessage(true, "david@gmail.com", "/undo"),
      new TestMessage(false, "-david@gmail.com", "[david] /undo"),
      new TestMessage(false, "#4", "Undoing original actions: radioheda++ [back to 0]"),

      new TestMessage(true, "neil@gmail.com", "/debug sequenceIds"),
      new TestMessage(false, "neil@gmail.com", "enabling sequenceIds for you"),

      new TestMessage(true, "jason@gmail.com", "test with sequenceIds on"),
      new TestMessage(false, "#2", "[intern] test with sequenceIds on"),
      new TestMessage(false, "neil@gmail.com", "[intern] test with sequenceIds on (27)"),

      new TestMessage(true, "neil@gmail.com", "/debug"),
      new TestMessage(false, "neil@gmail.com", "Your current debug options are: [sequenceIds]"),

      new TestMessage(true, "jason@gmail.com", "test2++ with sequenceIds on"),
      new TestMessage(false, "#3", "[intern] test2++ [woot! now at 1] with sequenceIds on"),
      new TestMessage(false, "neil@gmail.com", "[intern] test2++ [woot! now at 1] with sequenceIds on (28)"),

      new TestMessage(true, "neil@gmail.com", "/me is having fun with sequenceIds"),
      new TestMessage(false, "#3", "_sanchito is having fun with sequenceIds_"),
      new TestMessage(false, "neil@gmail.com", "_sanchito is having fun with sequenceIds_ (29)"),

      new TestMessage(true, "neil@gmail.com", "/debug clear"),
      new TestMessage(false, "neil@gmail.com", "clearing all debug options"),

      new TestMessage(true, "jason@gmail.com", "test with sequenceIds off"),
      new TestMessage(false, "#3", "[intern] test with sequenceIds off"),
      
      new TestMessage(true, "jason@gmail.com", "/alias jason--"),
      new TestMessage(false, "jason@gmail.com", "You are now known as 'jason--'"),
      new TestMessage(false, "#4", "'intern' is now known as 'jason--'"),

      new TestMessage(true, "neil@gmail.com", "/summon jason--"),
      new TestMessage(false, "#3", "[sanchito] /summon jason--"),
      new TestMessage(false, "#4", "_sanchito summoned jason--_"),

      new TestMessage(true, "neil@gmail.com", "/invite CAPS@gmail.com"),
      new TestMessage(false, "#5", "_sanchito invited CAPS@gmail.com_"),
      
      new TestMessage(true, "CAPS@gmail.com", "foo"),
      new TestMessage(false, "caps@gmail.com", "You have joined 'pancake' with the alias 'caps'"),
      new TestMessage(false, "-caps@gmail.com", "caps@gmail.com has joined the channel with the alias 'caps'"),
      new TestMessage(false, "-caps@gmail.com", "[caps] foo"),

      new TestMessage(true, "CAPS@gmail.com", "bar"),
      new TestMessage(false, "-caps@gmail.com", "[caps] bar"),
      
      new TestMessage(true, "neil@gmail.com", "/bug I don't like this behavior"),
      new TestMessage(false, "neil@gmail.com", "http://code.google.com/p/partychapp/issues/entry?summary=I+don%27t+like+this+behavior&comment=Filed+by+user+sanchito+from+channel+pancake"),
      
      new TestMessage(true, "neil@gmail.com", "/list"),
      new TestMessage(false, "neil@gmail.com", "Listing members of 'pancake'\n" +
          "* caps (caps@gmail.com)\n" +
          "* david (david@gmail.com)\n" +
          "* jason-- (jason@gmail.com)\n" +
          "* kushal (kushal@kushaldave.com)\n" +
          "* sanchito (neil@gmail.com)\n" +
          "Room is invite-only."),      
      
      new TestMessage(true, "neil@gmail.com", "/kick caps"),
      new TestMessage(false, "#4", "_sanchito kicked caps_"),

      new TestMessage(true, "neil@gmail.com", "/list"),
      new TestMessage(false, "neil@gmail.com", "Listing members of 'pancake'\n" +
          "* david (david@gmail.com)\n" +
          "* jason-- (jason@gmail.com)\n" +
          "* kushal (kushal@kushaldave.com)\n" +
          "* sanchito (neil@gmail.com)\n" +
          "Room is invite-only.")
    };

    for (int i = 0; i < script.length;) {
      TestMessage line = script[i];
      logger.info("Test message " + i + ": " + line.content);
      
      servlet.doXmpp(line.toIncomingMessage());

      List<Message> sentMessages = xmpp.messages;
      List<TestMessage> expectedMessages = Lists.newArrayList();
      for (++i; (i < script.length) && !script[i].incoming; ++i) {
        expectedMessages.add(script[i]);
      }
      
      assertEquals("wrong number of messages sent for input line '" + line.content + ".'" + expectedMessages + (sentMessages.size() > 0 ? sentMessages.get(0).getBody() : ""),
                   expectedMessages.size(), sentMessages.size());
      for (int it = 0; it < expectedMessages.size(); ++it) {
        TestMessage expected = expectedMessages.get(it);
        Message sent = sentMessages.get(it);
        expected.assertSameAs(sent);
      }
      xmpp.messages.clear();
    }
  }
  
  public void testFixUp() {
    Datastore datastore = Datastore.instance();
    // Initially the channel and the user don't exist
    assertNull(datastore.getUserByJID("neil@gmail.com"));
    assertNull(datastore.getChannelByName("pancake"));

    // Channel and user creation based on the incoming message
    sendMessage("neil@gmail.com", "hi partychat");
  
    assertTrue(userInChannel("neil@gmail.com", "pancake"));
    assertTrue(channelHasMember("pancake", "neil@gmail.com"));
    
    // Regular leaving should put things in a consistent state
    sendMessage("neil@gmail.com", "/leave");

    assertFalse(userInChannel("neil@gmail.com", "pancake"));
    assertFalse(channelHasMember("pancake", "neil@gmail.com"));  
    
    // Simulate the User object being in a channel isn't not supposed to be
    // in
    datastore.startRequest();
    User u = datastore.getUserByJID("neil@gmail.com");
    u.addChannel("pancake");
    u.put();
    datastore.endRequest();
    
    // Things are inconsistent
    assertTrue(userInChannel("neil@gmail.com", "pancake"));
    assertFalse(channelHasMember("pancake", "neil@gmail.com"));  
    
    // If they IM the room now, they're auto-join, and things will be consistent
    // again.
    sendMessage("neil@gmail.com", "hi partychat");

    assertTrue(userInChannel("neil@gmail.com", "pancake"));
    assertTrue(channelHasMember("pancake", "neil@gmail.com"));
    
    // If we make the room invite-only, leave, and simulate the same 
    // inconsistency...
    sendMessage("neil@gmail.com", "/inviteonly");
    sendMessage("neil@gmail.com", "/leave");
    assertFalse(userInChannel("neil@gmail.com", "pancake"));
    assertFalse(channelHasMember("pancake", "neil@gmail.com"));  
    datastore.startRequest();
    u = datastore.getUserByJID("neil@gmail.com");
    u.addChannel("pancake");
    u.put();
    datastore.endRequest();
    assertTrue(userInChannel("neil@gmail.com", "pancake"));
    assertFalse(channelHasMember("pancake", "neil@gmail.com"));  

    // Then the next time they IM the room, the inconsistency will still be 
    // fixed (they're not in room)
    sendMessage("neil@gmail.com", "hi partychat");
    assertFalse(userInChannel("neil@gmail.com", "pancake"));
    assertFalse(channelHasMember("pancake", "neil@gmail.com"));
    
    // Make the room not be invite-only and rejoin, and then make it invite-only
    // again.
    datastore.startRequest();
    Channel c = datastore.getChannelByName("pancake");
    c.setInviteOnly(false);
    c.put();
    datastore.endRequest();
    sendMessage("neil@gmail.com", "hi partychat");
    assertTrue(userInChannel("neil@gmail.com", "pancake"));
    assertTrue(channelHasMember("pancake", "neil@gmail.com"));
    sendMessage("neil@gmail.com", "/inviteonly");
    
    // Simulate the other inconsistency, where we're supposed to be in the room
    // but we're not
    datastore.startRequest();
    u = datastore.getUserByJID("neil@gmail.com");
    u.removeChannel("pancake");
    u.put();
    datastore.endRequest();
    assertFalse(userInChannel("neil@gmail.com", "pancake"));
    assertTrue(channelHasMember("pancake", "neil@gmail.com"));
    
    // It should get fixed up (we rejoin, even if the room is invite only)
    sendMessage("neil@gmail.com", "hi partychat");
    assertTrue(userInChannel("neil@gmail.com", "pancake"));
    assertTrue(channelHasMember("pancake", "neil@gmail.com"));    
    
    // Also check that resources in JIDs are dropped when creating the user 
    // object
    sendMessage("neil@gmail.com", "/inviteonly");
    sendMessage("mihai@gmail.com/Adium", "hi partychat");
    assertTrue(userInChannel("mihai@gmail.com", "pancake"));
    assertTrue(channelHasMember("pancake", "mihai@gmail.com"));    
  }
  
  private void sendMessage(String userJid, String message) {
    servlet.doXmpp(new TestMessage(true, userJid, message).toIncomingMessage());
  }
  
  private boolean userInChannel(String userJid, String channelName) {
    User user = Datastore.instance().getUserByJID(userJid);
    assertNotNull(user);
    return user.channelNames().contains(channelName);
  }
  
  private boolean channelHasMember(String channelName, String userJid) {
    Channel channel = Datastore.instance().getChannelByName(channelName);
    assertNotNull(channel);
    return channel.getMemberByJID(userJid) != null;
  }
}