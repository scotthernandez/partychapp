package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Channel.SharedURL;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.command.ShareHandler;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;
import com.imjasonh.partychapp.urlinfo.UrlInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for sharing a URL with a channel (the GET version displays the share
 * form, the POST version sends the shared URL to the channel).
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelShareServlet extends AbstractChannelUserServlet {
  
  @Override protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException, ServletException {
    SharedURL shareUrl = channel.fromRequest(req);
    if (shareUrl == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    RequestDispatcher disp =
      getServletContext().getRequestDispatcher(
          "/channel-share.jsp");
    req.setAttribute("channel", channel);
    req.setAttribute("shareUrl", shareUrl);
    disp.forward(req, resp);
  }
  
  @Override protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
    SharedURL shareUrl = channel.fromRequest(req);
    if (shareUrl == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }  
    
    ShareHandler.sendShareBroadcast(shareUrl, channel);
    
    resp.sendRedirect(channel.webUrl());
  }

}
