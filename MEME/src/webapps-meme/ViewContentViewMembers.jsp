<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="cv_bean" scope="session" class="gov.nih.nlm.meme.beans.ContentViewBean" />
<TITLE>ViewContentViewMembers</TITLE>
</HEAD>
<%
  ContentViewClient client = cv_bean.getContentViewClient();
  Identifier identifier = new Identifier.Default(request.getParameter("cv"));
  ContentView cv = client.getContentView(identifier);
  ContentViewMember[] members = client.getContentViewMembers(cv);
%>
<BODY>
<SPAN id="blue"><CENTER>View Content View Members</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="60%" border="0">
    <TR>
      <TD align="center">Name: <%= cv.getName() %></TD>
      <TD align="center">Identifier: <%= cv.getIdentifier() %></TD>
      <TD align="center">Contributor: <%= cv.getContributor() %></TD>
    </TR>
    <TR><TD colspan="3">&nbsp;</TD></TR>
    <TR><TD align="center" colspan="3">META UI</TD></TR>
    <TR><TD colspan="3">&nbsp;</TD></TR>
    <FORM method="POST">
     <INPUT name="cv" type="hidden" value="<%= request.getParameter("cv") %>">
     <%
        for (int i=0; i < members.length; i++) {
     %>
     <TR><TD align="center" colspan="3"><%= members[i].getIdentifier() %></TD></TR>
     <%
        }
     %>
     <TR><TD colspan="3">&nbsp;</TD></TR>
     <TR >
       <TD align="center" colspan="3">
        <FONT size="-1">
        <INPUT type="button" value="Back"
               onMouseOver='window.status="Go back";'
               onMouseOut='window.status="";'
               onClick="window.location.href='meme/controller?state=ContentViewEditor'; return true;">
        </FONT>
       </TD>
     </TR>
    </FORM>
</TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
