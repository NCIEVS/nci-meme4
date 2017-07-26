<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="gov.nih.nlm.meme.client.AuxiliaryDataClient" %>
<%@ page import="gov.nih.nlm.meme.common.MetaProperty" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="metaproperty_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="metaproperty_bean" property="*" />

<title>MEME Properties Editor</title>
</head>

<body>
<span id="blue"><center>MEME Properties Editor</center></span>
<HR width="100%">
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%"  >
<tr >
  <form name="connection" method="GET" action="controller">
  <input name="state" type="hidden" value="MetaPropertyEditor">
  <td width="25%">Change Database:</td><td>
        <meme:midServiceList name="midService" submit="true" initialValue="<%= metaproperty_bean.getMidService() %>" />
</td>
</tr>
</form>
<tr >
<td width="25%">Select a category to edit:</td><td>
<form  method="GET" action="controller">
  <input name="state" type="hidden" value="ListMetaProperty">
 <select name="category" onChange="this.form.submit(); return true;">
    <option>-- SELECT CATEGORY --</option>
    <% AuxiliaryDataClient aux_client = metaproperty_bean.getAuxiliaryDataClient();
       MetaProperty [] properties = aux_client.getMetaProperties();
       Arrays.sort(properties);
       Hashtable seen = new Hashtable();
       for(int i=0; i<properties.length; i++) {
	if(!seen.containsKey(properties[i].getKeyQualifier())) {
		seen.put(properties[i].getKeyQualifier(),properties[i].getKeyQualifier());
    %>
    <option value="<%= properties[i].getKeyQualifier()%>"><%= properties[i].getKeyQualifier()%></option>
    <%
	}
       }
    %>
  </select>
</form>
</td>
</tr>
<tr >
<td width="25%">New category</td><td>
       <form action="controller">
         <input type="hidden" name="state" value="EditMetaProperty">
         <input type="hidden" name="command" value="Insert">
          <input type="text" name="category" >
          <input type="submit" value="GO" >
      </form>
    </td>
</tr>
</table></center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
