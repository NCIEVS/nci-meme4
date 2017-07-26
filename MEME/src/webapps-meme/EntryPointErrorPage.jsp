<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.exception.*, java.util.*, java.io.*, gov.nih.nlm.meme.MIDServices"
         isErrorPage="true" errorPage= "ErrorPage.jsp"%>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>

<head>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>ErrorPage</title>
</head>

<body>
<span id="red"><center>Connection Failed Error Page</center></span>
<%
  if (exception instanceof MEMEException &&
    ((MEMEException)exception).getDetails().containsKey("failed_to_connect")) {
      Map details = ((MEMEException)exception).getDetails();
%>
<br>

<TABLE width="95%" border="0">
  <FORM name="connection" method="post" action="controller?state=<%=request.getParameter("state")%>">
    <TR>
      <TH colspan="2">&nbsp;</TH>
    </TR>
    <tr>
      <td colspan="2">Cannot connect to the server using the following value.
Change settings and try again.
</td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>
    </tr>
    <TR>
      <TD width="30%">Change host</TD>
      <TD width="70%">
        <meme:hostList name="host" submit="false" initialValue="<%= (String)details.get("host") %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change port</TD>
      <TD width="70%">
        <INPUT type="text" name="port" size="12"
               value="<%= (String)details.get("port") %>">
        <INPUT type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
    <tr>
      <td colspan="2">&nbsp;</td>
    </tr>
  </FORM>
</TABLE>
<p>
Click <a href="/webapps-meme/meme/controller?state=ServerLifeCycle"> here </a> to start a server.
</p>
<%
  } else {
	throw exception;
  }

%>

</body>
</html>
