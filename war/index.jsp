<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Ofy"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>
<%@ page import="com.xgen.partychapp.clienthub.ClientHubAPI"%>


<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp"/>
</head>
<body>
<jsp:include page="include/header.jsp"/>

<p>Create chat rooms with your friends or coworkers using Google
Talk or XMPP.</p>

<%
	//ClientHubAPI.testPostToCH();
	if (Configuration.isConfidential) {
%>
<h3>Are messages confidential?</h3>
Yup! We're running on an internal instance of AppEngine, so everything
stays safe. <%
  	}
  %>
<h3>How do I create a room?</h3>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();

	if (user != null) {
%>
<div id="create-button-container">
  <input type="button" value="Create a new room" onclick="showCreateForm()" />
</div>
<form onsubmit="return submitCreateRoom()">
<table id="channel-settings-table" class="hidden channel-form-table">
  <tr>
    <td class="label">Room name:</td>
    <td><input type="text" size="40" id="room-name"></td>
  </tr>
  <tr>
    <td class="label">Room type:</td>
    <td>
      <table class="radio-option-table">
        <tr>
          <td><input type="radio" name="inviteonly" value="true" checked="yes" id="inviteonly-true"></td>
          <td><label for="inviteonly-true">Invite-only</label></td>
        </tr>
        <tr class="description">
          <td></td>
          <td>Only invited people may join</td>
        </tr>
      </table>

      <table class="radio-option-table">
        <tr>
          <td><input type="radio" name="inviteonly" value="false" id="inviteonly-false"></td>
          <td><label for="inviteonly-false">Open</label></td>
        </tr>
        <tr class="description">
          <td></td>
          <td>Anyone may join</td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td class="label">
      Others to invite:
      <div class="description">Email addresses,<br>separated by commas</div>
    </td>
    <td>
      <textarea id="invitees" rows="4"></textarea>
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

<%
	} else {
%> The easiest way to create a room is to <a style="font-weight: bold"
	href="<%=userService.createLoginURL(HttpUtil.getRequestUri(request))%>">sign
in</a> and do it right here. <br />
<br />
Or you can add <tt>[roomname]@<%=Configuration.chatDomain%></tt> to your
buddy list and send it a message to join the room. If a room of that
name doesn't exist, a new one will be created. <%
	}
%>

<h3>How do I join a room?</h3>
The easiest way to join a room is to be invited. If the room has already
been created, have someone in the room type <tt>/invite
youremailaddress@anydomain.com</tt>. <br>
<br>
You should see an invitation from <tt>[roomname]@<%=Configuration.chatDomain%></tt>
in your chat window. Accept the invitation, and then <b>send a
message to your new buddy</b>, such as "hi." This will finish adding you to
the room. <br>
<br>
Alternatively, if a room is not invite-only, you can just add <tt>[roomname]@<%=Configuration.chatDomain%></tt>
to your buddy list and send it a message.

<h3 id="nowwhat">Okay, I'm in a room, now what?</h3>
<p>Besides just sending messages and having everyone see them, most of the
things you can do take the form of commands you type as special chat
messages starting with a /.</p>
<p style="text-align: center">
<img
	src="http://1.bp.blogspot.com/_qxrodbRnu8Q/SyL57yANfsI/AAAAAAAAD4w/pRdYP3wI_a4/s400/pchapp-shot.png"
  width="399" height="256" alt="Partychat demo">
</p>

<p>You can get a full list of commands by sending the chat message <tt>/help</tt>
to the room, or <a href="/about/faq#commands">on this page</a>. Some key ones:</p>
<ul>
	<li><tt>/leave</tt> Leave this chat room. You can rejoin by
	sending another message to the room. If the room is invite-only, you
	may need to be re-invited.</li>
	<li><tt>/list</tt> See who is in the chat room.</li>
	<li><tt>/alias <i>newalias</i></tt> Change what name you show up
	as in the room.</li>
	<li><tt>/inviteonly</tt> Toggle whether this room is invite only.</li>
	<li><tt>/invite <i>someemail</i></tt> Invite someone to the room.</li>
	<li><tt>/me <i>someaction</i></tt> Tell the room what you're up
	to. If you type <tt>/me is rolling his eyes</tt>, everyone sees <tt>[youralias]
	is rolling his eyes</tt>.</li>
</ul>

<p>There are also more advanced things like <a href="/about/faq#search-and-replace">search-and-replace</a> and <a href="/about/faq#plusplusbot">the PlusPlusBot</a> that you may want to <a href="/about/faq">read about</a>.</p>

<h3>Does anyone use this thing?</h3>

<%
  Datastore datastore = Datastore.instance();
  datastore.startRequest();
  Datastore.Stats stats = datastore.getStats(true);
%>

<p>
  It looks like it. <b><%=stats.getFormattedNumChannels()%> rooms</b> have been
  created, and in the past week <b><%=stats.getFormattedSevenDayActiveUsers()%>
  people</b> have used them.
</p>

<%
  datastore.endRequest();
%>

<jsp:include page="include/footer.jsp"/>
</body>
</html>