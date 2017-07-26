<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.integrity.*, gov.nih.nlm.meme.client.*"
          errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>
EditVector
</title>
<SCRIPT language="JavaScript">

function addCheck(vector, action) {
  if(action[action.selectedIndex].value == 'N')
    return;
  for( var i = document.vectors.check_list.options.length - 1; i >= 0; i-- )
  {
    if ( document.vectors.check_list.options[i] != null && ( document.vectors.check_list.options[i].value == vector[vector.selectedIndex].text) )
    {
       // Erase Existing Check
       document.vectors.check_list.options[i] = null;
    }
  }
    document.vectors.check_list[document.vectors.check_list.length] = new Option( vector[vector.selectedIndex].text + "[" +vector[vector.selectedIndex].value +"] : "+action[action.selectedIndex].value , vector[vector.selectedIndex].text, false, true);
}
function removeCheck() {
       // Erase Existing Check
       document.vectors.check_list.options[document.vectors.check_list.options.selectedIndex] = null;
}
function clearChecks() {
  for( var i = document.vectors.check_list.options.length - 1; i >= 0; i-- )
    document.vectors.check_list.options[i] = null;
}
function submitChecks() {
  var checks = "";
  for( var i = document.vectors.check_list.options.length - 1; i >= 0; i-- )
    checks = checks + document.vectors.check_list.options[i].value + ':' + document.vectors.check_list.options[i].text.split(": ")[1] + ';';
  document.vectors.vector.value = checks;
  document.vectors.submit();
}
</SCRIPT>
</head>
<jsp:useBean id="integrity_bean" scope="session" class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<body>
<SPAN id="blue"><CENTER><%= request.getParameter("type") %> Vector Editor</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="40%" border="0">
  <FORM method="GET">
    <TR>
      <TD>Name </TD>
      <TD> <%=request.getParameter("name") %> </TD>
    </TR>
    <TR>
      <TD>Add Check </TD>
      <TD>
        <SELECT name="vector">
          <%  IntegrityCheck[] checks = integrity_bean.getAuxiliaryDataClient().getIntegrityChecks();
             for (int i=0; i < checks.length; i++) {
          %>
          <OPTION value="<%= checks[i].getShortDescription()  %>" ><%= checks[i].getName() %></OPTION>
          <% }%>
        </SELECT>
        <SELECT name="action">
          <OPTION value="E" SELECTED>Enforce</OPTION>
          <OPTION value="W" >Warn</OPTION>
          <OPTION value="N" >None</OPTION>
        </SELECT>
        <INPUT type="button" value="Add" onClick="addCheck(this.form.vector,this.form.action)"
               OnMouseOver="window.status='Click to add check.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
    <TR>
      <TD>Remove Check </TD>
      <TD>
	  <input type="button" value="Remove"
	      onMouseOver='window.status="Remove selected Check"; return true;'
              onClick='removeCheck();'>
      </TD>
    </TR>
    <TR>
      <TD>Clear Checks </TD>
      <TD>
	  <input type="button" value="Clear"
	      onMouseOver='window.status="Clear Checks"; return true;'
              onClick='clearChecks();'>
      </TD>
    </TR>
    </FORM>
</TABLE>
<TABLE width="95%" border="0">
  <FORM name="vectors" method="GET">
  <INPUT name="state" type="hidden" value="EditVectorComplete">
  <INPUT name="type" type="hidden" value="<%= request.getParameter("type") %>">
  <INPUT name="name" type="hidden" value="<%= request.getParameter("name") %>">
  <INPUT name="command" type="hidden" value="<%= request.getParameter("command") %>">
  <INPUT name="vector" type="hidden" value="">
    <TR>
      <tr>
	<td><center><font size="+0">

	  <select style="width:500" name="check_list" size="15">
          <%  if(!"Insert".equals(request.getParameter("command"))) {
              IntegrityVector vector = null;
                if("Application".equals(request.getParameter("type")))
                  vector = integrity_bean.getAuxiliaryDataClient().getApplicationVector(request.getParameter("name"));
                else if("Override".equals(request.getParameter("type")))
                  vector = integrity_bean.getAuxiliaryDataClient().getOverrideVector(new Integer(request.getParameter("name")).intValue());
              checks = vector.getChecks();
             for (int i=0; i < checks.length; i++) {
          %>
          <OPTION value="<%= checks[i].getName() %>" ><%= checks[i].getName() + "[" + checks[i].getShortDescription() + "] : " + vector.getCodeForCheck(checks[i]) %></OPTION>
          <% } } %>
        </td>
      </tr>
      <tr >
        <td colspan=2 width="50%" align="center">
          <font size="-1">
          <% if("Insert".equals(request.getParameter("command"))) { %>
	  <input type="button" value="Insert"
	      onMouseOver='window.status="Insert New Vector"; return true;'
              onClick='submitChecks()'>

          &nbsp;&nbsp;&nbsp;
          <% } else { %>
	  <input type="button" value="Update"
	      onMouseOver='window.status="Update Vector"; return true;'
              onClick='submitChecks()'>
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Delete"
	      onMouseOver='window.status="Delete Vector"; return true;'
              onClick='this.form.command.value = "Delete";this.form.submit();'>

          &nbsp;&nbsp;&nbsp;
          <%}%>
           <input type="button" value="Cancel" onClick="history.go(-1)"></b>
          </font></b>
	</td>
      </tr>
    </FORM>
  </TABLE>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</BODY>
</HTML>

