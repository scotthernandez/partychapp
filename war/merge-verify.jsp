
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore" %>
<%@ page import="com.imjasonh.partychapp.User" %>

<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp"/>
</head>
<body>
<%
	UserService userService = UserServiceFactory.getUserService();
	com.google.appengine.api.users.User gUser = userService.getCurrentUser();
 	String msg = (String) request.getAttribute("message");
  	boolean error = (Boolean) request.getAttribute("error");
%>
<jsp:include page="include/header.jsp">
	<jsp:param name="subtitle" value='<%= error ? "Merge Unsuccessful" : "Successful Merge"%>'/>
</jsp:include>
<br /><br />
<% if (error) {
%>
	<%=msg %>
<% }else{
	String email = (String) request.getAttribute("email");
	String jid = (String) request.getAttribute("jid");
	
	if ((email != null && jid != null && gUser != null) &&
	    (email.compareTo(gUser.getEmail()) == 0 || jid.compareTo(gUser.getEmail()) == 0)){
		User u = Datastore.instance().getUserByJID(jid);
		%>
			The merge was completed successfully.  Your new user has: 
	<table class="merge-users-info">
		<tr>
			<td></td>
			<th>Current</th>
			<th>Merge with</th>
			<th>New</th>
		</tr>
		<tr>
			<td class="header-col">email</td>
			<td><%=email %></td>
		</tr>
		<tr>
			<td class="header-col">JID</td>
			<td><%=jid %></td>
		</tr>
		<tr>
			<td class="header-col">rooms</td>
			<td class="reg-col">
			<%
				
				for (String className : u.channelNames()){
				%>
					<%=className %><br />
				<%
				}
			%>
			</td>
		</tr>
	</table>
		<%
	}else{
		%>
		The merge was completed successfully.  Sign in as <%= email %> or <%= jid %> <a href='<%= Configuration.webDomain + "/user" %>'>here</a> to go to your user page.
		<%
	}
%>
<% }
%>
<jsp:include page="include/footer.jsp"/>
</body>
</html>