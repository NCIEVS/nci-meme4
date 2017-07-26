<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="gov.nih.nlm.meme.*" %>
<%@ page import="gov.nih.nlm.meme.client.*" %>
<%@ page import="gov.nih.nlm.meme.common.*" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<jsp:useBean id="cv_bean" scope="session"
             class="gov.nih.nlm.meme.beans.ContentViewBean" />
<jsp:setProperty name="cv_bean" property="*" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<script language="JavaScript">
  function formSubmit(form, command) {
    if (form.cv.value == "")
      return false;
    form.command.value=command;
    form.submit();
  }
</script>
<title>ContentViewEditor</title>
</head>
<body>
<span id="blue"><center>Content View Editor</center></span>
<hr width="100%">
<center>
<table width="95%" border="0">
  <form name="connection" method="GET">
  <input name="state" type="hidden" value="ContentViewEditor">
    <tr>
      <th colspan="3">&nbsp;</th>
    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <td width="30%">Change database</td>
      <td width="70%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= cv_bean.getMidService() %>" />
      </td>
    </tr>
    <tr>
      <td width="30%">Change host</td>
      <td width="70%">
        <meme:hostList name="host" submit="false" initialValue="<%= cv_bean.getHost() %>" />
      </td>
    </tr>
    <tr>
      <td width="30%">Change port</td>
      <td width="70%">
        <input type="text" name="port" size="12"
               value="<jsp:getProperty name="cv_bean" property="port" />">
        <input type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
</table>
<%
   ContentViewClient client = cv_bean.getContentViewClient();
   ContentView[] cvs = client.getContentViews();
   Arrays.sort(cvs, new Comparator() {
	      public int compare(Object o1, Object o2) {
	          return ((ContentView)o2).getName().compareTo(((ContentView)o1).getName());
	        }
	      });
%>
<table width="95%" border="0">
  <tr><td colspan="3">&nbsp;</td></tr>
  <tr><th colspan="3">Edit Content View</th></tr>
  <tr><td colspan="3">&nbsp;</td></tr>
  <form name="ecv" method="GET" action="controller">
    <input name="state" type="hidden" value="EditContentView">
    <input name="command" type="hidden" value="">
    <tr>
      <td width="30%">Edit Content View</td>
      <td colspan="2" width="70%">
        <select name="cv" style="width:30%" onChange='formSubmit(this.form, "");'>
          <option value="" >select A CONTENT VIEW</option>
          <%
             for (int i=0; i < cvs.length; i++) {
          %>
          <option value="<%= cvs[i].getIdentifier() %>" ><%= cvs[i].getName() %></option>
          <% } %>
        </select>
      </td>
    </tr>
  </form>
  <form name="ecvm" method="GET" action="controller">
    <input name="state" type="hidden" value="EditContentViewMembers">
    <input name="command" type="hidden" value="">
    <tr>
      <td width="30%">Edit Content View Members</td>
      <td colspan="2" width="70%">
        <select name="cv" style="width:30%" onChange='formSubmit(this.form, "");'>
          <option value="" >select A CONTENT VIEW</option>
          <%
             for (int i=0; i < cvs.length; i++) {
          %>
          <option value="<%= cvs[i].getIdentifier() %>" ><%= cvs[i].getName() %></option>
          <% } %>
        </select>
      </td>
    </tr>
  </form>
  <form name="ecvi" method="GET" action="controller">
    <input name="state" type="hidden" value="EditContentView">
    <input name="command" type="hidden" value="">
    <tr>
      <td width="30%">New Content View</td>
      <td width="18%">
        <input type="text" name="cv" style="width:100%">
      </td>
      <td width="57%">
        <input type="button" style="width:25" value="Go" onClick='formSubmit(this.form, "Insert");'
               OnMouseOver="window.status='Click to add new content view.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </td>
    </tr>
  </form>
</table>
</center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
