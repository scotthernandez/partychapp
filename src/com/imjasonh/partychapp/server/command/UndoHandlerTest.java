package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PPBHandler;
import com.imjasonh.partychapp.ppb.Target;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class UndoHandlerTest extends CommandHandlerTestCase {
  private PPBHandler ppbHandler = new PPBHandler();
  private UndoHandler handler = new UndoHandler();
  
  private int getScore(String target) {
    Target t = Datastore.instance().getTarget(FakeDatastore.fakeChannel(),
                                              target);
    return t.score();
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/undo")));
    assertTrue(handler.matches(Message.createForTests("/undo ")));
    assertTrue(handler.matches(Message.createForTests("/undo unused")));
  }
  
  public void testUndo() {
    ppbHandler.doCommand(Message.createForTests("x++ y-- z-- hello x++ /combine"));
    assertEquals(2, getScore("x"));
    assertEquals(-1, getScore("y"));
    assertEquals(-1, getScore("z"));
    
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/undo"));
    assertEquals(0, getScore("x"));
    assertEquals(0, getScore("y"));
    assertEquals(0, getScore("z"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /undo",
                 xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: x++ [back to 1], y-- [back to 0], z-- [back to 0], x++ [back to 0]",
                 xmpp.messages.get(1).getBody());
  }
}
