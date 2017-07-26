<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.integrity.*, gov.nih.nlm.meme.client.*"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
EditIntegrityCheck
</title>
</head>
<jsp:useBean id="integrity_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<%
    IntegrityCheck check = new IntegrityCheck.Default(request.getParameter("name"));
   if(!"Insert".equals(request.getParameter("command")))
    check = integrity_bean.getAuxiliaryDataClient().getIntegrityCheck(request.getParameter("check"));
%>
<body>
<SPAN id="blue"><CENTER>Integrity Check Editor</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="90%" border="0">
  <FORM method="GET" action="controller">
  <INPUT name="state" type="hidden" value="EditCheckComplete">
  <INPUT name="command" type="hidden" value="">
    <TR>
      <TD width="25%">Check Name</TD>
      <TD width="25%">
        <input type="text" name="name" value="<%= check.getName() %>">
      </TD>
    </TR>
    <TR>
      <TD width="25%">Status</TD>
      <TD width="25%">
        <input type="checkbox" name="isActive" value="true" <%= check.isActive()?"checked":"" %>> Active
      </TD>
    </TR>
    <TR>
      <TD width="25%">Type</TD>
      <TD width="25%">
        <input type="checkbox" name="isFatal" value="true" <%= check.isFatal()?"checked":"" %>> Fatal
      </TD>
    </TR>
    <TR>
      <TD width="25%">Short Desc</TD>
      <TD width="25%">
        <input type="text" name="shortDescription" value="<%= check.getShortDescription() %>">
      </TD>
    </TR>
    <TR>
      <TD width="25%">Long Desc</TD>
      <TD width="25%">
        <textarea name="description" wrap="soft" cols="60" rows="4"><%= check.getDescription() %></textarea>
      </TD>
    </TR>
      <tr >
        <td colspan=2 width="50%" align="center">
          <font size="-1">
          <% if("Insert".equals(request.getParameter("command"))) { %>
	  <input type="button" value="Insert"
	      onMouseOver='window.status="Insert New Check"; return true;'
              onClick=' this.form.command.value="Insert"; this.form.submit(); return true;'>

          &nbsp;&nbsp;&nbsp;
          <% } else { %>
	  <input type="button" value="Update"
	      onMouseOver='window.status="Update selected Check"; return true;'
              onClick='this.form.command.value="Update"; this.form.submit(); return true;'>
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Delete"
	      onMouseOver='window.status="Delete selected Check"; return true;'
              onClick='this.form.command.value="Delete"; this.form.submit(); return true;'>

          &nbsp;&nbsp;&nbsp;
          <%}%>
           <input type="button" value="Cancel" onClick="history.go(-1)"></b>
          </font></b>
	</td>
      </tr>
</form>
</TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</body>
</html>
