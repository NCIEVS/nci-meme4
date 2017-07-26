<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="java.util.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>

<HTML>
<HEAD>

<TITLE>View Session Log</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="bean" scope="page"
             class="gov.nih.nlm.meme.beans.AdminClientBean" />
<jsp:setProperty name="bean" property="*" />

</HEAD>
<%
  final AdminClient client = new AdminClient();
  client.getRequestHandler().setHost(request.getParameter("host"));
  client.getRequestHandler().setPort(Integer.parseInt(request.getParameter("port")));
  String session_id = request.getParameter("sessionId");

%>
<BODY>
<SPAN id="blue"><CENTER>View Session Log</CENTER></SPAN>
<P><HR WIDTH="100%">

<BLOCKQUOTE>
  <TABLE border="0">
    <TR>
      <TD>Host</TD>
      <TD><b><%= request.getParameter("host") %></b></TD>
    </TR>
    <TR>
      <TD>Port</TD>
      <TD><b><%= request.getParameter("port") %></b></TD>
    </TR>
    <TR>
      <TD>Session ID</TD>
      <TD><b><%= session_id %></b></TD>
    </TR>
  </TABLE>
  <PRE>
<%= client.getSessionLog(session_id) %>
  </PRE>
</BLOCKQUOTE>

<FORM>
  <TABLE WIDTH=90% >
    <TD align="center">
      <input type="button" value="Close" onClick="window.close(); return true">
    </TD>
  </TABLE>
</FORM>

<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />

</BODY>
</HTML>
