package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class ScoreHandlerTest extends CommandHandlerTestCase {
  ScoreHandler handler = new ScoreHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/score x")));
    assertTrue(handler.matches(Message.createForTests(" /score x")));
    assertTrue(handler.matches(Message.createForTests("/score xyz")));
    assertTrue(handler.matches(Message.createForTests("/score a_b-c.d")));
    assertFalse(handler.matches(Message.createForTests("x /score")));
  }
  
  public void testScore() {
    PPBHandler ppb = new PPBHandler();
    ppb.doCommand(Message.createForTests("x++"));
    xmpp.messages.clear();
    
    handler.doCommand(Message.createForTests("/score x"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("x: 1", xmpp.messages.get(0).getBody());
    
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/score doesnotexit"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("no scores found", xmpp.messages.get(0).getBody());    
  }
}