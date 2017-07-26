<%@ page session="true" contentType="text/html; charset=UTF-8"
         errorPage= "ErrorPage.jsp" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="gov.nih.nlm.meme.exception.*" %>
<%@ page import="gov.nih.nlm.meme.client.*" %>
<%@ page import="gov.nih.nlm.util.*" %>

<%--
     If this is causing a compile error
     get the commons-net.jar file out of the
     VSS "release" project.  Create a library
     out of this jar file and include it in
     the project required paths.  This page heavily
     leverages the RExecClient from the Apache commons
     Net project to communicate with NLM servers via rsh.
--%>

<%@ page import="org.apache.commons.net.bsd.RExecClient" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<jsp:useBean id="lifecycle_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="lifecycle_bean" property="*" />
<html>
<head>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>Server Life Cycle Manager</title>
<style type="text/css">
  select {width: 150}
</style>
<script language="javascript">
  //
  // Submit the form with the specified command
  //
  function goSubmit(f, cmd) {
    f.command.value = cmd;
    f.submit();
  }

  //
  // Send a kill request
  //
  function goKill(f, kill) {
    if (!confirm("Are you sure you want to kill this process?")) {
      return false;
    }
    f.command.value = 'kill';
    f.kill.value = kill;
    f.submit();
  }
</script>
</head>
<body>
<span id="blue"><center>Server Life Cycle</center></span>
<hr width="100%">
<center>
<table width="80%" border="0">
<tr><td colspan="2">
<%

  //
  // Get parameters
  //
  String command = request.getParameter("command");
  String type = request.getParameter("type");

  //
  // Handle login
  //
  if (request.getParameter("user") != null ) {
    request.getSession().setAttribute("user",request.getParameter("user"));
    request.getSession().setAttribute("pwd",request.getParameter("pwd"));
  }
  if (command != null && command.equals("reset")) {
    request.getSession().removeAttribute("user");
    request.getSession().removeAttribute("pwd");
  }
  String user = (String)request.getSession().getAttribute("user");
  String pwd = (String)request.getSession().getAttribute("pwd");

  //
  // Configure environment
  //
  String meme_home = "/d5/MEME4/dev";
  String mrd_home = "/d5/MRD";
  if (request.getParameter("meme_home") != null)
    meme_home = request.getParameter("meme_home");
  if (request.getParameter("mrd_home") != null)
    mrd_home = request.getParameter("mrd_home");

  //
  // Test the server
  //
  if (command != null && command.equals("test")) {
    AdminClient ac = new AdminClient();
    ac.getRequestHandler().setHost(lifecycle_bean.getHost());
    ac.getRequestHandler().setPort(Integer.parseInt(lifecycle_bean.getPort()));
    try {
      ac.ping();
      %><span class="affirmative">Server is running on <%= lifecycle_bean.getHost() %>:<%= lifecycle_bean.getPort() %></span><%
    } catch (MEMEException me) {
      %><span class="negative">Server is NOT running on <%= lifecycle_bean.getHost() %>:<%= lifecycle_bean.getPort() %></span><%
    }
  }

  //
  // Stop the server
  //
  else if (command != null && command.equals("stop")) {
    try {
      RExecClient rc = new RExecClient();
      rc.connect(lifecycle_bean.getHost());
      if (type != null && type.equals("MID")) {
        rc.rexec(user, pwd,
                 meme_home + "/bin/meme_server.sh stop " + lifecycle_bean.getPort() +
		 " a " + meme_home);
      } else if (type != null && type.equals("MRD")) {
        rc.rexec(user, pwd,
                 mrd_home + "/bin/mrd_server.sh stop " + lifecycle_bean.getPort() +
		 " a " + meme_home);
      }
      BufferedReader bin = new BufferedReader(new InputStreamReader(rc.getInputStream()));
      String line = null;
      while (( line = bin.readLine()) != null) {
       %> <%= line %><br> <%
      }
      %><p><span class="affirmative">Server stopped on <%= lifecycle_bean.getHost() %>:<%= lifecycle_bean.getPort() %></span></p><%
    } catch (Exception me) {
      %><p><span class="negative">Server failed to stop on <%= lifecycle_bean.getHost() %>:<%= lifecycle_bean.getPort() %></span></p><%
    }
  }

  //
  // Start the server
  //
  else if (command != null && command.equals("start")) {
    try {
      RExecClient rc = new RExecClient();
      rc.connect(lifecycle_bean.getHost());
      if (type != null && type.equals("MID")) {
        rc.rexec(user, pwd,
                 meme_home + "/bin/meme_server.sh start " +
                 lifecycle_bean.getPort() + " \"" + lifecycle_bean.getMidService() + "\" " + meme_home);
      } else if (type != null && type.equals("MRD")) {
        rc.rexec(user, pwd,
                 mrd_home + "/bin/mrd_server.sh start " +
                 lifecycle_bean.getPort() + " \"" + lifecycle_bean.getMidService() + "\" " + mrd_home);
      }
      BufferedReader bin = new BufferedReader(new InputStreamReader(rc.getInputStream()));
      String line = null;
      while (( line = bin.readLine()) != null) {
       %> <%= line %><br> <%
      }
      %><p><span class="affirmative">Server started on <%= lifecycle_bean.getHost() %>:<%= lifecycle_bean.getPort() %></span></p><%
    } catch (Exception me) {
      %><p><span class="negative">Server failed to start on <%= lifecycle_bean.getHost() %>:<%= lifecycle_bean.getPort() %></span></p><%
    }
  }

  //
  // find matching PIDs
  //
  else if (command != null && command.equals("pid")) {
    try {
      RExecClient rc = new RExecClient();
      rc.connect(lifecycle_bean.getHost());
      if (type != null && type.equals("MID")) {
        rc.rexec(user, pwd,
                 meme_home + "/bin/meme_server.sh pid " +
                 lifecycle_bean.getPort() + " \"" + lifecycle_bean.getMidService()+ "\" " + meme_home);
      } else if (type != null && type.equals("MRD")) {
        rc.rexec(user, pwd, mrd_home + "/bin/mrd_server.sh pid " +
                 lifecycle_bean.getPort() + " \"" + lifecycle_bean.getMidService() + "\" " + mrd_home);
      }
      BufferedReader bin = new BufferedReader(new InputStreamReader(rc.getInputStream()));
      String line = null;
      boolean found = false;
      %><span class="status">Server Process ids (click to kill process):<%
      while (( line = bin.readLine()) != null) {
	if (line.indexOf("pids:") != -1) {
	  found = true;
	  String[] tokens = FieldedStringTokenizer.split(line,":");
          tokens = FieldedStringTokenizer.split(tokens[1]," ");
	  for (int i = 0; i < tokens.length; i++) {
	    if (tokens[i].length()>0) {
	%><a href="javascript:goKill(document.form1,'<%=tokens[i]%>')"><%=tokens[i]%></a>&nbsp;<%
	    }
	  }
	%></span><blockquote>Details:<br><%
	}
	if (line.indexOf("pids:") == -1 && line.length() > 10) {
	  %> <%= line %><br> <%
	}
      }
      if (!found) {
      %></span><p><span class="affirmative">No processes found on <%= lifecycle_bean.getHost() %></span></p><%
      } else {
      %><br></blockquote><%
      }
    } catch (Exception me) {
      %><p><span class="negative">Server failed to find process ids on <%= lifecycle_bean.getHost() %></span></p><%
    }
  }

  //
  // Kill the server
  //
  else if (command != null && command.equals("kill")) {
    try {
      RExecClient rc = new RExecClient();
      rc.connect(lifecycle_bean.getHost());
      rc.rexec(user,pwd, "kill " + request.getParameter("kill"));
      BufferedReader bin = new BufferedReader(new InputStreamReader(rc.getInputStream()));
      String line = null;
      while (( line = bin.readLine()) != null) {
       %> <%= line %><br> <%
      }
      %><p><span class="affirmative">Process <%= request.getParameter("kill") %> on host <%= lifecycle_bean.getHost() %> is killed.</span></p> <%
    } catch (Exception me) {
      %><p><span class="negative">Process <%= request.getParameter("kill") %> on host <%= lifecycle_bean.getHost() %> is NOT killed.</span></p><%
    }
  }
%>
  </td></tr>
  <tr>
    <form name="form1" method="POST">
    <input name="state" type="hidden" value="ServerLifeCycle">
    <input name="command" type="hidden" value="check">
    <input name="meme_home" type="hidden" value="<%= meme_home %>">
    <input name="mrd_home" type="hidden" value="<%= mrd_home %>">
    <input name="kill" type="hidden" value="">
      <TH colspan="2">Server Parameters</TH>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</TD>
    </tr>
<%
  //
  // If needed, allow for login info
  //
  if (request.getSession().getAttribute("user") == null) {
%>
    <TR>
      <TD width="30%">Username:</TD>
      <TD width="70%">
        <input style="width:150" type="text" name="user">
      </TD>
    </TR>
    <TR>
      <TD width="30%">Password:</TD>
      <TD width="70%">
        <input style="width:150" type="password" name="pwd">
      </TD>
    </TR>
<%
  //
  // Otherwise indicate login and allow for reset
  //
  } else {
%>
    <TR>
      <TD width="30%">Username:</TD>
      <TD width="70%"><%= request.getSession().getAttribute("user") %>&nbsp;
        <input style="width:75" type="button" value="Reset" onClick="goSubmit(this.form,'reset')">
      </TD>
    </TR>
<%
  }
%>
    <tr><td>Set Environment:</td><td>
        <input style="width:150" type="button" name="meme" value="Set MEME_HOME"
               onClick="var mh = prompt('Set \$MEME_HOME Value:',this.form.meme_home.value); if (mh != null) { this.form.meme_home.value=mh;}">
    </td></tr>
    <tr><td>&nbsp;</td><td>
        <input style="width:150" type="button" name="mrd" value="Set MRD_HOME"
               onClick="var mh = prompt('Set \$MRD_HOME Value:',this.form.mrd_home.value); if (mh != null) { this.form.mrd_home.value=mh;}">
    </td></tr>
    <TR>
      <TD width="30%">Host:</TD>
      <TD width="70%">
        <meme:hostList name="host" submit="false" initialValue="<%= lifecycle_bean.getHost() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Port:</TD>
      <TD width="70%">
        <INPUT style="width:150" type="text" name="port" size="12"
               value="<jsp:getProperty name="lifecycle_bean" property="port" />">
      </TD>
    </TR>
    <TR>
      <TD width="30%">MID Service:</TD>
      <TD width="70%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= lifecycle_bean.getMidService() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Server Type:</TD>
      <TD width="70%">
        <select style="width:150" name="type">
	  <option <%= type != null && type.equals("MID") ? "SELECTED" : "" %>>MID</option>
          <option <%= type != null && type.equals("MRD") ? "SELECTED" : "" %>>MRD</option>
        </select>
      </TD>
    </TR>
   <tr><td colspan="2">&nbsp;</td></tr>
    <TR>
      <TD colspan="2" align="center">
        <INPUT style="width:85" type="button" value="Start" onClick="goSubmit(this.form,'start')"
                 OnMouseOver="window.status='Click to start the server.'; return true;"
               OnMouseOut="window.status=''; return true;"> &nbsp;&nbsp;
        <INPUT style="width:85" type="button" value="Stop" onClick="goSubmit(this.form,'stop')"
                 OnMouseOver="window.status='Click to stop the server.'; return true;"
               OnMouseOut="window.status=''; return true;">&nbsp;&nbsp;
        <INPUT style="width:85" type="button" value="Test" onClick="goSubmit(this.form,'test')"
                 OnMouseOver="window.status='Click to test the server connection.'; return true;"
               OnMouseOut="window.status=''; return true;"> &nbsp;&nbsp;
        <INPUT style="width:85" type="button" value="Check PID" onClick="goSubmit(this.form,'pid')"
                 OnMouseOver="window.status='Click to find matching process ids.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
</TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/apelon.html" text="Apelon Online Documentation Index" />
</body>
</html>
