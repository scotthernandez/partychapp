package com.imjasonh.partychapp.server.web;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.filters.SharedURL;
import com.imjasonh.partychapp.filters.SharedURLDAO;
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

@SuppressWarnings("serial")
public class ChannelShareServlet extends AbstractChannelUserServlet {
  
  @Override protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException, ServletException {
	  

      Member member = channel.getMemberByJID(user.getJID());   
      
    SharedURL shareUrl = fromRequest(req, member, channel);
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

      Member member = channel.getMemberByJID(user.getJID());   
      
    SharedURL shareUrl = fromRequest(req, member, channel);
    if (shareUrl == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }  
    
    SharedURLDAO.storeURL(shareUrl);
    ShareHandler.sendShareBroadcast(shareUrl, channel);
    
    resp.sendRedirect(channel.webUrl());
  }

  public static SharedURL fromRequest(HttpServletRequest req, Member member, Channel channel) {
      if (Strings.isNullOrEmpty(req.getParameter("url"))) {
        return null;
      }
      
      URI url;
      try {
        url = new URI(req.getParameter("url"));
      } catch (URISyntaxException err) {
        return null;
      }

      String annotation = req.getParameter("annotation");
      if (annotation == null) {
        annotation = "";
      }

      String title = req.getParameter("title");
      if (title == null) {
        title = "";
      }
      
      String description = req.getParameter("description");
      if (description == null) {
        description = "";
      }
      
      if (title.isEmpty() && description.isEmpty()) {
        UrlInfo urlInfo = ChainedUrlInfoService.DEFAULT_SERVICE.getUrlInfo(url);
        title = urlInfo.getTitle();
        description = urlInfo.getDescription();
      }

      return new SharedURL(channel.getName(), member.getJID(), url.toString(), annotation, title, description);
    }
}
