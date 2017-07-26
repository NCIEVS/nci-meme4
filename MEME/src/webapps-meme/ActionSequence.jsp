<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="java.util.*, gov.nih.nlm.meme.MEMEToolkit"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<HTML>
<HEAD>

<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.ActionSequenceBean" />
<jsp:setProperty name="bean" property="*" />

<TITLE>Running Action Sequence <%= bean.getSequence() %> </TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">

<!-- Import error handler -->
<script src="../IFrameErrorHandler.js">
  alert("Error handler javascript page not found");
</script>

<SCRIPT language="JavaScript">
  // Define variables
  var start = null;
  var stop = false;
  var img_array = new Array(101);
  var sessions = new Array(<%= bean.getUsers() %>);
  var user_progress = new Array(<%= bean.getUsers() %>);

  // Handler for button
  function buttonClick(btn) {
    // First state, initiate all sessions, start action sequences
    if (btn.value == "Start") {
      btn.value = "Stop";
      document.getElementById("status").innerHTML = "Running";
      <% for (int i=0; i < bean.getUsers(); i++) { %>
      document.getElementById("vl<%= i %>").style.visibility="visible";
      <% } %>
      initiateSessions();
    // Second state, while running this will stop the action sequences
    } else if (btn.value == "Stop") {
      stop = true;
      btn.value = "Finish";
      document.getElementById("status").innerHTML = "<span id=red>Stopped</span>";
    // Third state, when done running, finish will clean up server resources
    } else if (btn.value == "Finish") {
      btn.value = "Close";
      document.getElementById("status").innerHTML = "Finished";
      <% for (int i=0; i < bean.getUsers(); i++) { %>
      document.getElementById("vl<%= i %>").style.visibility="hidden";
      <% } %>
      terminateSessions();
    // Final state, closes the window
    } else if (btn.value == "Close") {
      window.close();
    }
  }

  // Send URL request to iframe
  function getValueFromURL(user, url) {
    frames["get_value_frame"+user].location.href=url;
  }

  // Initiate action sequences
  // by sending requests to iframes
  function initiateSessions() {
    for (i=0; i < user_progress.length; i++) {
      user_progress[i] = 0;
    }
    <% for (int i=0; i < bean.getUsers(); i++) { %>
    getValueFromURL(<%= i %>, "controller?state=ActionSequenceProgress&sessionId=-1&users=<%= i %>&midService=<%= bean.getMidService() %>&port=<%= bean.getPort() %>&host=<%= bean.getHost() %>&sequence=<%= bean.getSequence() %>");
    <% } %>
    start = new Date();
  }

  // Terminate action sequences
  function terminateSessions() {
    <% for (int i=0; i < bean.getUsers(); i++) { %>
    getValueFromURL(<%= i %>, "controller?state=ActionSequenceProgress&terminate=true&sessionId="+sessions[<%= i %>]+"&users=<%= i %>&midService=<%= bean.getMidService() %>&port=<%= bean.getPort() %>&host=<%= bean.getHost() %>&sequence=<%= bean.getSequence() %>");
    <% } %>
  }

  // Check if done
  function checkFinished() {
    var flag = true;
    for (i=0; i < user_progress.length; i++) {
      if (user_progress[i] < 100) {
        flag = false;
        break;
      }
    }
    if (flag) {
      document.button_panel.btn.value = "Finish";
      document.getElementById("status").innerHTML = "Test complete";
      alert("The test is complete. Click finish when ready to clean up server side resources.");
    }
  }

  // Update progress for a user
  function setProgress(user, progress, session_id) {
    if (progress == -100)
      eval("document.progress"+user+".src='/images/pm/error.gif';");
    else {
      eval("document.progress"+user+".src='/images/pm/"+progress+".gif';");
      setElapsedTime(user, getElapsedTimeAsString());
      sessions[user] = session_id;
      user_progress[user] = progress;
      if (!stop && progress < 100)
      	setTimeout("getValueFromURL('"+user+"', 'controller?state=ActionSequenceProgress&sessionId="+session_id+"&users="+user+"&midService=<%= bean.getMidService() %>&port=<%= bean.getPort() %>&host=<%= bean.getHost() %>')", 1000);
      checkFinished();
    }
  }

  // Update elapsed time
  function setElapsedTime(user, et) {
    eval("document.getElementById('et"+user+"').innerHTML ='"+ et + "';");
  }

  // Convert elasped time to string representation
  function getElapsedTimeAsString() {
    var current = new Date();
    var elapsed = new Date(current.getTime() - start.getTime());
    var sec = elapsed.getSeconds();
    var min = elapsed.getMinutes();
    if (min < 10)
      elapsed_str = "00:0" + min;
    else
      elapsed_str = "00:" + min;
    if (sec < 10)
      elapsed_str = elapsed_str + ":0" + sec;
    else
      elapsed_str = elapsed_str + ":" + sec;

    return elapsed_str;
  }

  // Open a log for the specified user
  function viewLog(user) {
    window.open("controller?state=SessionLog&sessionId="+sessions[user]+"&port=<%= bean.getPort() %>&host=<%= bean.getHost() %>");
  }

</SCRIPT>

</HEAD>
<BODY>
<% for (int i=0; i < bean.getUsers(); i++) { %>
<iframe name="get_value_frame<%= i %>" style="visibility:hidden" width=0 height=0></iframe>
<% } %>
<SPAN id="blue"><CENTER>Running Action Sequence <%=bean.getSequence() %></CENTER></SPAN>
<P><HR WIDTH="100%">

<BLOCKQUOTE>
  <TABLE border="0">
    <TR>
      <TD>Status</TD>
      <TD><b><div style="color:#0000A0" id="status" align"right">Not started yet</div></b></TD>
    </TR>
    <TR>
      <TD>Host</TD>
      <TD><b><%= bean.getHost() %></b></TD>
    </TR>
    <TR>
      <TD>Port</TD>
      <TD><b><%= bean.getPort() %></b></TD>
    </TR>
    <TR>
      <TD>Mid Service</TD>
      <TD><b><%= bean.getMidService().equals("") ? "&lt;default&gt;" : bean.getMidService() %></b></TD>
    </TR>
    <TR>
      <TD>Sequence #</TD>
      <TD><b><%= bean.getSequence() %></b></TD>
    </TR>
    <TR>
      <TD>Users</TD>
      <TD><b><%= bean.getUsers() %></b></TD>
    </TR>
    <TR>
      <TD>&nbsp;</TD>
    </TR>
    <TR>
      <TD>Progress of the test shown below.</TD>
    </TR>
  </TABLE>
  <p>
  <CENTER>
    <TABLE border="0" cellspacing="1" width="90%">
      <TR><TH>User #</TH><TH>Progress</TH><TH>Elapsed Time</TH><TH>&nbsp;</TH></TR>
      <TR><TD colspan="4">&nbsp;</TD></TR>
      <% for (int i=0; i < bean.getUsers(); i++) { %>
      <TR height="24"><TD align="middle"><%= i+1 %></TD>
          <TD align="middle">0% <IMG align="middle" name="progress<%= i %>" height="22" width="402" src="/images/pm/0.gif"> 100%</TD>
          <TD align="middle"><DIV id="et<%= i %>">00:00:00</DIV></TD>
          <TD align="middle">
          <INPUT id="vl<%= i %>" style="visibility:hidden" type="button" value="View Log" onClick="viewLog(<%= i %>); return true;"
               OnMouseOver="window.status='Click to view log.'; return true;"
               OnMouseOut="window.status=''; return true;">
          </TD>
      </TR>
      <% } %>
      <TR><TD colspan="4">&nbsp;</TD></TR>
    </TABLE>
    <FORM name="button_panel">
      <INPUT name="btn" type="button" value="Start" style="width:80" onClick="buttonClick(this); return true;">
    </FORM>
  </CENTER>
</BLOCKQUOTE>

<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
