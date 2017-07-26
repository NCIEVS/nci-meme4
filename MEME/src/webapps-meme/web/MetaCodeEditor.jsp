<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="gov.nih.nlm.meme.client.AuxiliaryDataClient" %>
<%@ page import="gov.nih.nlm.meme.common.MetaCode" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="metacode_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="metacode_bean" property="*" />

<title>Code Map Editor</title>
</head>

<body>
<span id="blue"><center>Code Map Editor</center></span>
<HR width="100%">
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%"  >
<tr >
  <form name="connection" method="GET" action="controller">
  <input name="state" type="hidden" value="MetaCodeEditor">
  <td width="25%">Change Database:</td><td>
        <meme:midServiceList name="midService" submit="true" initialValue="<%= metacode_bean.getMidService() %>" />
</td>
</tr>
</form>
<tr >
<td width="25%">Select a category to edit:</td><td>
<form  method="GET" action="controller">
  <input name="state" type="hidden" value="ListMetaCode">
 <select name="category" onChange="this.form.submit(); return true;">
    <option>-- SELECT CATEGORY --</option>
    <% AuxiliaryDataClient aux_client = metacode_bean.getAuxiliaryDataClient();
       MetaCode [] codes = aux_client.getMetaCodes();
       Arrays.sort(codes);
       Hashtable seen = new Hashtable();
       for(int i=0; i<codes.length; i++) {
	if(!seen.containsKey(codes[i].getType())) {
		seen.put(codes[i].getType(),codes[i].getType());
    %>
    <option value="<%= codes[i].getType()%>"><%= codes[i].getType()%></option>
    <%
	}
       }
    %>
  </select>
</form>
</td>
</tr>
<tr >
<td valign="top" width="25%">New category:</td><td align="left">
         <form action="controller">
           <input type="hidden" name="state" value="EditMetaCode">
           <input type="hidden" name="command" value="Insert">

            <table >
            <tr >
            <td valign="top">
            Category:
            </td>
            <td align="left">

            <input type="text" name="category" > <br>
            </td>
            </tr>

            <tr >
            <td valign="top">
            Description:
            </td>
            <td align="left">
            <input type="text" name="description" >
            </td>
            <td align="left">
            <input type="submit" value="GO" >

            </td>
            </tr>
            </table>
        </form>
    </td>
</tr></table></center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
