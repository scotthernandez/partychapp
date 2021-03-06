<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Configuration"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.imjasonh.partychapp.User"%>
<%@ page import="com.imjasonh.partychapp.ppb.Reason"%>
<%@ page import="com.imjasonh.partychapp.ppb.Target"%>
<%@ page import="com.xgen.chat.permissions.MemberPermissions"%>
<%@ page import="com.xgen.chat.permissions.MemberPermissions.PermissionLevel"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>

<!DOCTYPE html>
<html>
<head>
<script>if (window.name == 'partychat-share') window.close();</script>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script> 
<script type="text/javascript" src="../js/tabber.js"></script>
<%
  Channel channel = (Channel) request.getAttribute("channel");
  Datastore datastore = Datastore.instance();
  datastore.startRequest();
  User user = datastore.getOrCreateUser((String) request.getAttribute("email"));
  Member member = channel.getMemberByJID(user.getJID());
  
  UserService userService = UserServiceFactory.getUserService();
%>

<jsp:include page="include/head.jsp">
  <jsp:param name="subtitle" value="<%=&quot;Room &quot; + channel.getName()%>"/>
</jsp:include>
</head>
<body class="channel">
  <jsp:include page="include/header.jsp">
    <jsp:param name="subtitle" value="<%=&quot; Room: &quot; + channel.getName()%>"/>
  </jsp:include>
  
<div class="tabber">

  <div class="tabbertab">
    <h3>Members</h3>

	<p style="font-size:20px"><b>Members of <%=channel.getName()%>:</b></p>

	<jsp:include page="include/channel-members.jsp"/>
	<%if (userService.isUserAdmin() || MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.MOD)){%>
	<br><b>Invite others to <%=channel.getName()%>:</b>
	<table>
	  <tr>
	    <td>
	    <form action="/channel/invite" method="post" target="inviteResults">	
	    <input type="hidden" name="name" value="<%=channel.getName()%>"/>
	    Email addresses you would like to invite? (separated by commas)<br>
	    <textarea name="invitees" rows="4" cols="40"></textarea> <br>
	    <input type="submit" value="Invite!"></form>
	    </td>
	    <td><iframe frameborder=0 name="inviteResults"> </iframe></td>
	  </tr>
	</table>
	<%} %>
  </div>


  <div class="tabbertab" title="Logs">
    <p style="font-size:20px"><b>Log History:</b></p>
	<div id="log-history-container">
		<div id="log-table"></div>
			<div id="newerlog-button-container">
				<input id="newerlog-button" class="log-button" type="button" value="Newer" />
			</div>
			<div id="olderlog-button-container">
				<input id="olderlog-button" class="log-button" type="button" value="Older" />
			</div>
		<div style="clear: both;"></div>
	</div>
	<script>	window.myInstance = myLog('<%= channel.getName() %>');
	</script>
	
	<%if (userService.isUserAdmin() || MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.ADMIN)){%>
	<p style="font-size:20px"><b>Manage Logs:</b></p>
	
	<form action="/log/download" method="POST">
		 <input type="hidden" name="name" value="<%=channel.getName()%>"/>
	<table id="date-picker-table">
	  <tr>
	  	<td class="label">Start Date:</td>
	    <td>
		 <div>
			 <input id="start-date-text" type="text" 
			 							 name="startDate" 
			 							 size="10" 
			 							 placeholder="mm/dd/yyyy"
			 							 onfocus="document.getElementById('start-date-picker').style.visibility = 'visible';">
			 <div id="start-date-picker" style="visibility: hidden; position: absolute;"></div>
	     </div>
	    </td>
	    <td>at</td>
	    <td><select id="start-hour-picker" name="startHour"></select></td>
	    <td>:</td> 
	    <td><select id="start-min-picker" name="startMin"></select></td>
	  </tr>
	  <tr>
	  	<td class="label">End Date:</td>
	    <td>
		 <div>
			 <input id="end-date-text" type="text" 
			 							 name="endDate" 
			 							 size="10" 
			 							 placeholder="mm/dd/yyyy"
			 							 onfocus="document.getElementById('end-date-picker').style.visibility = 'visible';">
			 <div id="end-date-picker" style="visibility: hidden; position: absolute;"></div>
		 </div>
		</td>
	    <td>at</td>
	    <td><select id="end-hour-picker" name="endHour"></select></td>
	    <td>:</td> 
	    <td><select id="end-min-picker" name="endMin"></select></td>
	  </tr>
	</table>
		 <input type="submit" name="Download" value="Download Entries">
		 <input type="submit" name="Delete" value="Delete Entries">
	</form>
	
	<script>
	  numbersDropdown('start-hour-picker', 0, 23);
	  numbersDropdown('start-min-picker', 0, 59);
	  
	  numbersDropdown('end-hour-picker', 0, 23);
	  numbersDropdown('end-min-picker', 0, 59);
	</script>
	<%} %>
  </div>

  <div class="tabbertab" title="PlusPlusBot">

	<p style="font-size:20px"><b>The ever-so-useful ++Bot:</b></p>
	<div id="score-table"></div>

	<script>
 	 new partychapp.ScoreTable('<%= channel.getName() %>',
    	                        <%= (String) request.getAttribute("targetInfo") %>);
	</script>

	</div>


  <div class="tabbertab" title="Tips and Tricks">
<p style="font-size:20px"><b>Tips and tricks:</b></p>

	<p>Send messages to this room by chatting with <a href="xmpp:<%=channel.serverJIDAsString()%>"><%=channel.serverJIDAsString()%></a>.</p>
	
	<p>When you're on the go, you can also email them to <a href="mailto:<%=channel.mailingAddress()%>"><%=channel.mailingAddress()%></a> (as long as the account you're emailing from is in the room).</p>

	<p>If you'd like to share links with this room, you can drag the <a href="javascript:window.open('http://<%=Configuration.webDomain%>/room/share?name=<%=channel.getName()%>&url='+escape(location.href),'partychat-share','scrollbars=no,width=700,height=315,top=175,left=75,status=yes,resizable=yes');void(0);
" class="bookmarklet" onclick="alert('Drag this link to your bookmarks bar');return false;">Share with <%=channel.getName()%></a> link to your bookmarks bar and press it when you're on the page you want to share.</p>



  </div>



  <div class="tabbertab" title="Settings">
  
  
	<%if (userService.isUserAdmin() || MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.ADMIN)){%>
<p style="font-size:20px"><b><%=channel.getName()%>'s settings:</b></p>
<div id="settings-container">
	<form action="/channel/edit" method="POST">
	<input type="hidden" name="name" value="<%=channel.getName()%>"/>
	<table id="channel-settings-table" class="channel-form-table">
	  <tr>
	    <td class="label">Room type:</td>
	    <td>
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="inviteonly" value="true" <% if (channel.isInviteOnly()) {out.print("checked");} %> id="inviteonly-true"></td>
	          <td><label for="inviteonly-true">Invite-only</label></td>
	        </tr>
	        <tr class="description">
	          <td></td>
	          <td>Only invited people may join</td>
	        </tr>
	      </table>
	
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="inviteonly" value="false" <% if (!channel.isInviteOnly()) {out.print("checked");} %> id="inviteonly-false"></td>
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
	    <td class="label">Short-term log:</td>
	    <td>
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="minilog" value="true" <% if (!channel.isMiniLogDisabled()) {out.print("checked");} %> id="minilog-true"></td>
	          <td><label for="minilog-true">Enabled</label></td>
	        </tr>
	        <tr class="description">
	          <td></td>
	          <td>Recent messages are recorded for <a href="/about/faq#search-and-replace">search-and-replace</a></td>
	        </tr>
	      </table>
	
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="minilog" value="false" <% if (channel.isMiniLogDisabled()) {out.print("checked");} %> id="minilog-false"></td>
	          <td><label for="minilog-false">Disabled</label></td>
	        </tr>
	        <tr class="description">
	          <td></td>
	          <td><a href="/about/faq#search-and-replace">Search-and-replace</a> is disabled.</td>
	        </tr>
	      </table>
	    </td>
	  </tr>
	   <tr>
	    <td class="label">Logging:</td>
	    <td>
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="logging" value="true" <% if (channel.isLogging()) {out.print("checked");} %> id="logging-true"></td>
	          <td><label for="logging-true">Enabled</label></td>
	        </tr>
	        <tr class="description">
	          <td></td>
	          <td>All messages are recorded for history, which is default behavior.  The room will be notified of a change in this setting.</td>
	        </tr>
	      </table>
	
	      <table class="radio-option-table">
	        <tr>
	          <td><input type="radio" name="logging" value="false" <% if (!channel.isLogging()) {out.print("checked");} %> id="logging-false"></td>
	          <td><label for="logging-false">Disabled</label></td>
	        </tr>
	        <tr class="description">
	          <td></td>
	          <td>No messages will be recorded.  The room will be notified of a change in this setting.</td>
	        </tr>
	      </table>
	    </td>
	  </tr>
	
	  <tr>
	    <td class="buttons" colspan="2">
	      <input type="submit" value="Save settings">
	    </td>
	  </tr>
	</table>
	</form>
<% if (userService.isUserAdmin() || MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.ADMIN)) {%>
	  <button id="removechannel">Delete Channel</button>
	<script>
	    		var button = document.getElementById('removechannel');
	    		deleteOnClick('<%=channel.getName()%>','<%=user.getEmail()%>', button);
	</script>
<%   } %>
<%} else {%>
<p style="font-size:20px"><b>Sorry:</b></p>
  You must be Admin to view or edit the settings.
<%} %>
</div>
  </div>

  </div>



<%datastore.endRequest();%>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
