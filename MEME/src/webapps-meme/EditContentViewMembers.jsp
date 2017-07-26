<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.common.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="cv_bean" scope="session" class="gov.nih.nlm.meme.beans.ContentViewBean" />
<TITLE>EditContentViewMembers</TITLE>
</HEAD>
<%
   ContentView cv = new ContentView.Default();
   Identifier identifier = null;
   if (!request.getParameter("cv").equals(""))
     identifier = new Identifier.Default(request.getParameter("cv"));
   if (identifier != null)
     cv = cv_bean.getContentViewClient().getContentView(identifier);
%>
<BODY>
<SPAN id="blue"><CENTER>Edit Content View Members</CENTER></SPAN>
<HR width="100%">
<CENTER>
  <TABLE width="40%" border="0">
    <TR><TD align="left">&nbsp;Content View: <%= cv.getName() %></TD></TR>
    <TR><TD>&nbsp;</TD></TR>
    <FORM method="POST" enctype="multipart/form-data" action="../EditContentViewMembersComplete.jsp">
     <INPUT name="state" type="hidden" value="EditContentViewMembersComplete">
     <INPUT name="command" type="hidden" value="">
     <INPUT name="cv" type="hidden" value="<%= request.getParameter("cv") %>">
     <TR><TD align="left"><INPUT type="radio" name="action" value="generate"> Generate Members</TD></TR>
     <TR><TD align="left"><INPUT type="radio" name="action" value="load"> Load Members</TD></TR>
     <TR><TD>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type="file" name="upload"></TD></TR>
     <TR><TD>&nbsp;</TD></TR>
     <TR >
       <TD align="center">
        <FONT size="-1">
        <INPUT type="button" value="Done"
               onMouseOver='window.status="Finish Edit Content View Member";'
               onMouseOut='window.status="";'
               onClick='this.form.command.value="Done"; this.form.submit(); return true;'>
        &nbsp;&nbsp;&nbsp;
        <INPUT type="button" value="Cancel"
               onMouseOver='window.status="Cancel Edit Content View Member";'
               onMouseOut='window.status="";'
               onClick="history.go(-1)">
        </FONT>
       </TD>
     </TR>
    </FORM>
    <FORM method="POST" action="../ViewContentViewMembers.jsp">
     <INPUT name="state" type="hidden" value="ViewContentViewMembers">
     <INPUT name="command" type="hidden" value="">
     <INPUT name="cv" type="hidden" value="<%= request.getParameter("cv") %>">
     <TR><TD>&nbsp;</TD></TR>
     <TR >
       <TD align="center">
        <FONT size="-1">
        <INPUT type="button" value="View Members"
               onMouseOver='window.status="View Content View Member";'
               onMouseOut='window.status="";'
               onClick='this.form.command.value="View"; this.form.submit(); return true;'>
        </FONT>
       </TD>
     </TR>
    </FORM>
  </TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
