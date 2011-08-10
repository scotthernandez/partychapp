<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.User"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>

<%@ page import="com.google.appengine.repackaged.com.google.common.collect.Sets"%>

<%@ page import="java.util.Set"%>

<!DOCTYPE html>
<html>
<head>
  <jsp:include page="/include/head.jsp" />
</head>
<body>
<%
	UserService userService = UserServiceFactory.getUserService();
	com.google.appengine.api.users.User gUser = userService.getCurrentUser();
	
	User pUser = Datastore.instance().getOrCreateUser(gUser.getEmail());
	
	String jid = (String) request.getParameter("account");
	if (jid == null){
		response.sendRedirect("/");
	}
	
	User toMerge = Datastore.instance().getUserByJID(jid);
	
	if (toMerge == null){
	    RequestDispatcher disp =
	        getServletContext().getRequestDispatcher("/user/merge");

	    request.setAttribute("message", "Account with that address was not found.  If you wish to just change JID, you can do so through the <a href='/user'>User</a> page.");
	    System.out.println("will send:" + request.getAttribute("message"));
	    disp.forward(request, response);    
	}
%>
<jsp:include page="/include/header.jsp">
	<jsp:param name="subtitle" value='Merge Users'/>
</jsp:include>
<br /><br />

<table class="merge-users-info">
	<tr>
		<td style="width:10%;"></td>
		<th>Current</th>
		<th>Merge with</th>
		<th>New</th>
	</tr>
	<tr>
		<td class="header-col">email</td>
		<td class="reg-col"><%= pUser.getEmail()%></td>
		<td class="reg-col"><%= toMerge.getEmail() %></td>
		<td class="reg-col"><%= pUser.getEmail() %></td>
	</tr>
	<tr>
		<td class="header-col">JID</td>
		<td class="reg-col"><%= pUser.getJID()%></td>
		<td class="reg-col"><%= toMerge.getJID()%></td>
		<td class="reg-col"><%= toMerge.getJID() %></td>
	</tr>
	<tr>
		<td class="header-col">rooms</td>
		<td class="reg-col">
		<%
			for (String className : pUser.channelNames()){
			%>
				<%=className %><br />
			<%
			}
		%>
		</td>
		<td class="reg-col">
		<%
			for (String className : toMerge.channelNames()){
			%>
				<%=className %><br />
			<%
			}
		%>
		</td>
		<td class="reg-col">
		<%
			Set<String> newSet = Sets.newHashSet();
			newSet.addAll(pUser.channelNames());
			newSet.addAll(toMerge.channelNames());
			
			for (String className : newSet){
			%>
				<%=className %><br />
			<%
			}
		%>
		</td>
	</tr>
</table>
<br />
<br />
If this is the setup you desire, click on the button to send a verification email to <i><%= toMerge.getEmail() %></i>.  To complete the merge, go to the URL in the email.
<form id="choose-account" action="/user/merge/request" method="POST" target="result">
	<input type="hidden" name="email" value="<%= pUser.getEmail() %>" />
	<input type="hidden" name="jid" value="<%= toMerge.getJID() %>" />
	<input type="submit" value="Send email"/>
</form>
<br />
<iframe style="border:none" id="result"></iframe>

<jsp:include page="/include/footer.jsp"/>
</body>
</html>