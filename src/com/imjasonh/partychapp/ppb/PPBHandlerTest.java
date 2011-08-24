package com.imjasonh.partychapp.ppb;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.command.CommandHandlerTestCase;

public class PPBHandlerTest extends CommandHandlerTestCase {
  PPBHandler ppb = new PPBHandler();
  
  public boolean hasJID(String jid) {
    List<JID> recipients = Arrays.asList(xmpp.messages.get(0).getRecipientJids());
    for (JID j : recipients) {
      if (j.getId().equals(jid)) {
        return true;
      }
    }
    return false;
  }
  
  public void assertResponse(String in, String out) {
    ppb.doCommand(Message.createForTests(in));

    assertEquals(1, xmpp.messages.size());
    assertEquals(out, xmpp.messages.get(0).getBody());

    assertTrue(hasJID("neil@gmail.com"));
  }
  
  public void testSimple() {
    assertResponse("mihai++ for knowing things",
                   "[neil] mihai++ [woot! now at 1] for knowing things");
  }

  public void testInlineEdit() {
    assertResponse("whee x++ and y-- boo",
                   "[neil] whee x++ [woot! now at 1] and y-- [ouch! now at -1] boo");
  }

  public void testNothingAtEnds() {
    assertResponse("x++ y-- z++",
                   "[neil] x++ [woot! now at 1] y-- [ouch! now at -1] z++ [woot! now at 1]");
  }
  
  public void testNoEchoIfOnlyOnBlacklist() {
    ppb.doCommand(Message.createForTests("blah c++ nyah"));

    assertEquals(1, xmpp.messages.size());
    String output = xmpp.messages.get(0).getBody();
    assertEquals("[neil] blah c++ nyah", output);

    assertFalse(hasJID("neil@gmail.com"));
  }
}
