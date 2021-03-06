<!-- Table of members of a channel -->

<%@ page import="java.util.Collections"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.common.collect.Lists"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.xgen.chat.permissions.MemberPermissions"%>
<%@ page import="com.xgen.chat.permissions.MemberPermissions.PermissionLevel"%>

<%
	Channel channel = (Channel) request.getAttribute("channel");
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	com.imjasonh.partychapp.User u = Datastore.instance().getOrCreateUser(user.getEmail());
	Member member = channel.getMemberByJID(u.getJID());
	
%>

<h3>Members</h3>
<table class="channel-table">
  <tr>
    <th>Alias</th>
    <th>Jabber ID </th>
<%  if (member != null && MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.MOD)){
%>
    <th>Kick</th>
    <th>Permissions</th>
<% 
  } 
%>
  </tr>
  <%
    List<Member> members = Lists.newArrayList(channel.getMembers());
    Collections.sort(members, new Member.SortMembersForListComparator());
    int i = 0;
    for (Member m : members) {
    	if (m.isHidden()) {  
    	  continue;  	
    	}
    	i++;
  %>
		  <tr id="<%=i+"-kick-row"%>">
		    <td><%=m.getAlias()%></td>
		    <td><%=m.getJID()%></td>
<%  if (member != null && MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.MOD)){
%>
		    <td>
	    		<div style="height:16px; width:43px; color:#00e; background-color:transparent; text-decoration:underline; border:0px; cursor:pointer;" id="<%=i+"-kick-button"%>"><b>[x]</b></div>
	    		<script>
	    		var button = document.getElementById('<%=i+"-kick-button"%>');
	    		var row = document.getElementById('<%=i+"-kick-row"%>');
	    		kickOnClick('<%=channel.getName()%>', '<%=m.getJID()%>', button, row);
	    		</script>
			</td>
			    <td>
			    <%if (MemberPermissions.instance().hasLevel(channel, member, MemberPermissions.instance().getLevel(channel, m))){ %>
			    	<select id=<%=i+"-permissions-select"%>>
			    		<option>Member</option>
						<% if (MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.MOD)){%>
							<option>Mod</option>
						<% }%>
						<% if (MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.ADMIN)){%>
							<option>Admin</option>
						<% }%>
			    	</select>
			    	<script>
		    		var select = document.getElementById('<%=i+"-permissions-select"%>');
		    		adminOnClick('<%=channel.getName()%>', '<%=m.getJID()%>', select, '<%=MemberPermissions.instance().getLevel(channel, m)%>');
		    		</script>
			    <%}else{%>
			    	<%= MemberPermissions.instance().getLevel(channel, m)%>
			    <%} %>
		    	</td>
			  </tr>
<%}} %>
</table>

<% if (!channel.getInvitees().isEmpty()) {%>
  <h3>Invited</h3>
  <table class="channel-table">
    <tr>
      <th>Email address</th>
<%  if (member != null && MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.ADMIN)){
%>
    <th>Kick</th>
<%  } 
%>
    </tr>
    <%
      List<String> invitedMembers = Lists.newArrayList(channel.getInvitees());
      Collections.sort(invitedMembers);
      i = 0;
      for (String invitedMember : invitedMembers) {
    	  i++;
    %>
      <tr id="<%=i+"-kick-invitee-row"%>" >
        <td><%=invitedMember%></td>
<%  if (member != null && MemberPermissions.instance().hasLevel(channel, member, PermissionLevel.ADMIN)){
%>
		    <td>
	    		<div style="height:16px; width:43px; color:#00e; background-color:transparent; text-decoration:underline; border:0px; cursor:pointer;" id="<%=i+"-kick-invitee-button"%>" ><b>[x]</b></div>
	    		<script>
	    		var button = document.getElementById('<%=i+"-kick-invitee-button"%>');
	    		var row = document.getElementById('<%=i+"-kick-invitee-row"%>');
	    		kickOnClick('<%=channel.getName()%>', '<%=invitedMember%>', button, row);
	    		</script>
			</td>
		  </tr>
<%    }
%>
      </tr>
    <% } %>
  </table>
<% } %>

