package com.imjasonh.partychapp.server.web;

import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LeaveChannelServlet extends AbstractChannelUserServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(LeaveChannelServlet.class.getName());

  @Override
  protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
    Member member = channel.getMemberByJID(user.getJID());
    String broadcast =
        member.getAlias() + " has left the room (" + member.getJID() + ")";
    channel.broadcast(broadcast, member);

    channel.removeMember(user);
    channel.put();
    
    resp.sendRedirect("/");
  }
}
