<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.server.command.Command"%>

<!DOCTYPE html>
<html>
<head>
<jsp:include page="/include/head.jsp">
  <jsp:param name="subtitle" value="FAQ"/>
</jsp:include>
<style>
  b.alt {color: blue;}
  b.alt2 {color: red;}
</style>
</head>
<body>
  <jsp:include page="/include/header.jsp">
    <jsp:param name="subtitle" value="FAQ"/>
  </jsp:include>
  
  <ul> 
    <li><a href="#commands">What are the commands I can use?</a></li>
    <li><a href="#search-and-replace">How can I correct things I (or others) have said?</a></li>
    <li><a href="#plusplusbot">How can I give and take away points?</a></li>
    <li><a href="#logging">What gets logged when I use Partychat?</a></li>
    <li><a href="#web-ui">Can I access Partychat from my browser?</a></li>
    <li><a href="#feedback">How can I give feedback?</a></li>
  </ul>
  
  <h3 id="commands">What are the commands I can use?</h3>
  
  <ul>
  <%
    for (Command command : Command.values()) {
      if (command.commandHandler.documentation() == null || command.category == Command.Category.HIDDEN) continue;
  %>
    <li><%=command.commandHandler.documentation()%></li>
  <%
    }
  %>
  </ul>
  
  <h3 id="search-and-replace">How can I correct things I (or others) have said?</h3>
  
  <p>If you make a typo, you can easily do a search-and-replace to indicate that you really mean. You can do this by saying <code>s/<i>to replace</i>/<i>replacement</i></code>/, for example:</p>
  
  <blockquote>
  <b>me:</b> man, Kushal sure is quit today.<br>
  &nbsp;&nbsp;&nbsp;&nbsp;whoops<br>
  &nbsp;&nbsp;&nbsp;&nbsp;s/qui/quie/<br>
  <b class="alt">partychat:</b> dolapo meant <i>man, Kushal sure is quiet today</i>
  </blockquote>
  
  <p>Even better, you can use this to to <a href="http://www.qwantz.com/index.php?comic=658">correct what others have said</a>, by prefixing it with their name:</p>
  
  <blockquote><b>dolapo:</b> that's corny, kushal<br>
  <b class="alt">kushal:</b> dolapo: s/that's /I'm /<br>
  <b class="alt2">partychat:</b> kushal thinks dolapo meant <i>I'm corny, kushal</i><br>
  <b>dolapo:</b> seeya<br>
  <b class="alt">kushal:</b> dolapo: s/see/boo/<br>
  <b class="alt2">partychat:</b> kushal thinks dolapo meant <i>booya</i></blockquote>
  
  <h3 id="plusplusbot">How can I give and take away points?</h3>
  
	<p>You can give points to things you like by typing ++ at the end of them
	in your message, and the PlusPlusBot will keep track of them for you. For
	example, you might say <code>chat++ for being so handy</code>. This adds
	one to the score for chat, which you cab see by typing <code>/score
	chat</code>. Or you can take points away from things you dislike, such as
	<code>kushal-- for another bad pun</code>.</p>
	
	<p>You can also see all the recent reasons why someone/something got points
	by saying <code>/reasons <i>thing</i></code>. If you use the web page for a
	room (see below), you can see all the things have points in that room,
	along with handy graphs.</p>
  
  <h3 id="logging">What gets logged when I use 10gen Chat?</h3>
  
  <p>What you say in a room in chat may be logged in various places, here's
  the list of them as best as we can tell:</p>
  
  <p><b>By Us:</b> We log everything that is broadcast to all users.  That means that when 
  you type <code>/list</code> it won't be recorded, but a normal message or a 
  command like <code>/kick</code> will.</p>

  <p><b>By Gmail/Google Talk:</b> If you use a Gmail/Google Talk account, chats
  may be archived by default (this is what enables messages that were sent
  while offline to be delivered to you later). If you'd like to turn this off,
  you can enable <a href="http://mail.google.com/support/bin/answer.py?answer=29291&topic=8405">off
  the record</a> mode.</p>

  <p><b>By your chat client:</b> If you use a desktop chat client such as
  <a href="http://www.adiumx.com/">Adium</a>, chat history may be saved by
  that client as well. Check your client's settings or documentation for more
  details.</p>
  
  <h3 id="web-ui">Can I access 10gen chat from my browser?</h3>

  <p>Sort of. Each room has a web page. If you
  <a href="<%=UserServiceFactory.getUserService().createLoginURL("/")%>">sign
  in</a>, you'll see a list of the rooms you're in on the
  <a href="/">homepage</a>, with each room being a link. Alternatively, you
  can visit <code>http://chat.10gen.com/room/<i>&lt;room name&gt;</i></code>
  to go to a room's web page directly.</p>
  
  <p>On the web page, you can change a room's <a href="#logging">logging</a>
  settings, see <a href="#plusplusbot">PlusPlusBot</a> scores, and invite
  other users depending on your level of permissions.</p>
  
  <h3 id="feedback">How can I give feedback?</h3>
  
  <p>You can give feedback to the developers at
	<script type="text/javascript">printEmail()</script>.</p>
  

  <jsp:include page="/include/footer.jsp"/>
</body>
</html>
