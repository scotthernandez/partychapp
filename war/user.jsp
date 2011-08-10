<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.server.json.UserInfoJsonServlet"%>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    com.imjasonh.partychapp.User pchappUser = datastore.getOrCreateUser(user.getEmail());
    pchappUser.put();
%>

<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp">
    <jsp:param name="subtitle" value="User Info"/>
  </jsp:include>
</head>
<body>
<jsp:include page="include/header.jsp">
	<jsp:param name="subtitle" value="User Info"/>
</jsp:include>

<jsp:include page="include/userinfo.jsp"/>

<h3>My Account</h3>
<table>
	<tr>
		<td>User email:</td>
		<td><%=pchappUser.getEmail() %></td>
	</tr>
	<tr>
		<td>Jabber ID:</td>
		<td> <%=pchappUser.getJID() %> </td>
	</tr>
	<tr>
		<td>Default Alias:</td>
		<td> <%=pchappUser.defaultAlias == null ? "No default alias yet." : pchappUser.defaultAlias%> </td>
	</tr>
</table>
<br />
<table>
<form id="user-edit-form">
<tr>
    <td class="column"><b>JID:</b></td>
    <td>
          <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="jid" value=<%=pchappUser.getJID() %> id="same" checked="checked" onclick="enableUserRadioButton(false)"></td>
	          <td><label for="to-email">Leave as now</label></td>
	        </tr>
	        
	      </table>
        <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="jid" value =<%=pchappUser.getEmail() %> id="email" onclick="enableUserRadioButton(false)"></td>
	          <td><label for="to-email">Change to user email</label></td>
	        </tr>
	        
	      </table>
	
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="jid" id="custom" onclick="enableUserRadioButton(true)"></td>
	          <td><label for="custom">Other: </label><input type="text" name="newJID" id="jid-input" disabled="true"></td>
	        </tr>
	      </table>
    </td>
</tr>
<tr>
     <td class="column"><b>Default Alias:</b></td>
     <td><input type="text" name="alias" id="alias-input"  default="old"> </td>
</tr>
</form>
<tr>
<td></td>
<td><button onclick="submitUserForm()">Submit</button></td>
<tr/>

</table>
<div id="results"></div>

<h3>Merge</h3>
<p>It's possible that you can't change your JID because you mistakenly created an account with that address.</p>  

<p>Don't worry though, you can merge two accounts by clicking <a href="/user/merge">here</a> and following the instructions.  Make sure you're signed in
with the address you want as 'email' and merge with the account you want as 'jid'.</p>

<jsp:include page="include/footer.jsp"/>
</body>
</html>