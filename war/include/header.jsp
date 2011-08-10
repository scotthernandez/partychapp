<!-- Common markup, meant to be included at the start of the <body> section -->

<%@ page import="com.google.common.base.Strings"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>
<%@ page import="com.imjasonh.partychapp.Configuration"%>

<div id="main"> <!-- closed in footer.jsp -->
  <div id="loginlogout" style="text-align: right">
    <%
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();

      if (user != null) {
    %> <a href="<%=userService.createLogoutURL(HttpUtil.getRequestUri(request), Configuration.webDomain)%>">sign
    out of <%=user.getEmail()%></a> | <a href="/user"> User </a><%
      } else {
     %> <a href="<%=userService.createLoginURL(HttpUtil.getRequestUri(request))%>">sign
    in</a> <%
      }
      if (request.getAttribute("channel") != null) {
        Channel channel = (Channel) request.getAttribute("channel");
        if (channel.getMemberByJID(user.getEmail()) != null){
        	if (channel.getMemberByJID(user.getEmail()).isHidden()) {
     %> 
     |
     <a id="joinbutton" style="height:16px; width:43px; color:#00e; background-color:transparent; text-decoration:underline; border:0px; cursor:pointer;" >join <%=channel.getName()%></a> 
     	        <script>
	    		var button = document.getElementById("joinbutton");
	    		joinOnClick('<%=channel.getName()%>', '<%=user.getEmail()%>', button);
	    		</script>
     <% }
       }
       }%>
     |
     <a href="/about/faq">FAQ</a>
  </div>
  <% if (Strings.isNullOrEmpty(request.getParameter("subtitle"))) { %>
    <div id="header">
      <a href="/"><img src="/images/logo.png" width="393" height="150" alt="Partychat" border="0"></a>
    </div>
  <% } else { %>
    <div id="subtitle-header">
      <a href="/"><img src="/images/small_logo.png" width="106" height="50" alt="Partychat" border="0"></a>
      <div id="subtitle"><%=request.getParameter("subtitle")%></div>
   </div>
 <% } %>
