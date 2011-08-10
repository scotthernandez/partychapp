<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Ofy"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>
<%@ page import="com.imjasonh.partychapp.server.web.UserMergeVerify"%>
<%@ page import="com.xgen.partychapp.clienthub.ClientHubAPI"%>


<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp"/>
</head>
<body>
<jsp:include page="include/header.jsp"/>
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	
	
	if (user == null) {
%>
	<br />
	<h3>Welcome!</h3>
	This is 10gen's client and employee chat service.  Please  <a style="font-weight: bold"
	href="<%=userService.createLoginURL(HttpUtil.getRequestUri(request))%>">sign
    in</a> to learn more and manage your account.
    
<%  }else{
		boolean is10gen = user.getEmail().split("@")[1].compareTo("10gen.com") == 0;
	  	if(is10gen || user.getEmail().compareTo("circuitlego@gmail.com") == 0){ %>

<p>Easily communicate with your co-workers and clients using Google Talk or XMPP.</p>

<h3>How do I create a room?</h3>

The easiest way to create a room is to do it right here. There are two types of rooms, 10gen rooms and ClientHub rooms.  
If the room name is the same as a ClientHub account's, then it will be a ClientHub room.  If the name isn't taken, it will be a 10gen room.<br /><br />

<form onsubmit="return submitCreateRoom()">
<input type="hidden" name="inviteonly" value="true" checked="yes" id="inviteonly-true">
<table id="channel-settings-table" class="channel-form-table">
  <tr>
    <td class="label">Room name:</td>
    <td><input type="text" size="40" id="room-name"></td>
  </tr>
  <tr>
    <td class="label">
      Others to invite:
      <div class="description">Email addresses,<br>separated by commas.<br><br> If it's a ClientHub room,<br> all contacts will be<br> automatically invited. </div>
    </td>
    <td>
      <textarea id="invitees" rows="8"></textarea>
    </td>
  </tr>
  <tr>
    <td class="buttons" colspan="2">
      <input type="submit" value="Create!">
    </td>
  </tr>
</table>

<div id="create-result" class="hidden"></div>
</form>

<jsp:include page="include/userinfo.jsp"/>

<h3>How do I join a room?</h3>
The easiest way to join a room is to be invited. If the room has already
been created, have someone in the room type <tt>/invite
youremailaddress@anydomain.com</tt>. <br>
<br>
You should see an invitation from <tt>[roomname]@<%=Configuration.chatDomain%></tt>
in your chat window. Accept the invitation, and then <b>send a
message to your new buddy</b>, such as "hi." This will finish adding you to
the room. <br>

<%  }else{
	%>
	
	<h3>Welcome!</h3>
	You are viewing this page because you are probably a 10gen client.  Our chat service will help you keep in touch with
	the necessary people so you can ask for advice.  This page will teach you a little about the features of the service.  
	<br /><br />
	If you are <b>not</b> a client, but a 10gen employee, please sign in with your 10gen email address.
	
	
	<jsp:include page="include/userinfo.jsp"/>

	<h3>How do I join a room?</h3>
	<p>
	Your company's chat room whould already be created.  If you do not see a section title <b>My rooms</b> above, notify your 10gen contact.
	We should then be able to get the room up and started.  Once the room is created just chat with it using your favorite XMPP client
	or Google Talk.  Everyone in the room will be able to see each other's messages.</p>
	<p>
	If you wish to use a different email address for chating than the one you used to sign in here, you can change it.  Click <a href="/user">here</a> to change those settings.
<%}
%>



<h3 id="nowwhat">Okay, I'm in a room, now what?</h3>
<p>Besides just sending messages and having everyone see them, most of the
things you can do take the form of commands you type as special chat
messages starting with a /.</p>

<p>You can get a full list of commands by sending the chat message <tt>/help</tt>
to the room, or <a href="/about/faq#commands">on this page</a>. Some key ones:</p>
<ul>
	<li><tt>/leave</tt> Leave this chat room. You can rejoin by
	sending another message to the room. If the room is invite-only, you
	may need to be re-invited.</li>
	<li><tt>/list</tt> See who is in the chat room.</li>
	<li><tt>/alias <i>newalias</i></tt> Change what name you show up
	as in the room.</li>
	<li><tt>/share <i>someaddress</i></tt> Share a URL with the room.</li>
	<li><tt>/shared </tt> View what others have shared with the room.</li>
	<li><tt>/invite <i>someemail</i></tt> Invite someone to the room.</li>
	<li><tt>/me <i>someaction</i></tt> Tell the room what you're up
	to. If you type <tt>/me is rolling his eyes</tt>, everyone sees <tt>[youralias]
	is rolling his eyes</tt>.</li>
</ul>

<p>There are also more advanced things like <a href="/about/faq#search-and-replace">search-and-replace</a> and 
<a href="/about/faq#plusplusbot">the PlusPlusBot</a> that you may want to <a href="/about/faq">read about</a>.  There
are also filters that help you share urls and JIRA issues without fuss.</p>

<%
	}
%>

<jsp:include page="include/footer.jsp"/>
</body>
</html>