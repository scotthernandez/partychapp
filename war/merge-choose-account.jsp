<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.User"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>

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
	String message = (String) request.getAttribute("message");
	
%>
<jsp:include page="/include/header.jsp">
	<jsp:param name="subtitle" value='Merge Users'/>
</jsp:include>
<br /><br />

<!-- First the account to merge with is chosen. -->
Enter the email or Jabber ID of the account you wish to merge with.
<form id="choose-account" action="/user/merge/choose-addresses" method="POST">
	<input  type="hidden" name="user" value="<%= pUser.getEmail() %>" />
	<table>
		<tr>
			<td>Account:</td>
			<td><input type="text" name="account" /></td>
		</tr>
	</table>
	<input type="submit" value="Next" />
</form>
<br />
<div id="result"><%= message != null ? message : ""%></div>

<jsp:include page="/include/footer.jsp"/>
<script>
var chooseAccount = function(){
	
}
</script>
</body>
</html>