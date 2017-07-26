<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ page import = "java.util.Arrays" %>
<%@ page import = "java.util.Hashtable" %>
<%@ page import = "gov.nih.nlm.meme.client.AuxiliaryDataClient" %>
<%@ page import = "gov.nih.nlm.meme.common.MetaProperty" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="mrdoc_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="mrdoc_bean" property="*" />

<title>MRDOC Editor</title>
</head>

<body>
<span id="blue"><CENTER>MRDOC Editor</CENTER></span>
<hr width="100%">
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%"  >
<tr >
  <form name="connection" method="GET" action="controller">
  <input name="state" type="hidden" value="MRDOCEditor">
  <td width="25%">Change Database:</td><td>
        <meme:midServiceList name="midService" submit="true" initialValue="<%= mrdoc_bean.getMidService() %>" />
</td>
</tr>
</form>
<tr >
<td width="25%">Select a DOCKEY, TYPE to edit:</td><td>
<form  method="GET" action="controller">
  <input name="state" type="hidden" value="ListMRDOC">
 <select name="entry" onChange="this.form.submit(); return true;">

    <option>-- SELECT DOCKEY, TYPE --</option>

    <% AuxiliaryDataClient aux_client = mrdoc_bean.getAuxiliaryDataClient();
       MetaProperty [] properties = aux_client.getMetaProperties();
       Arrays.sort(properties);
       String exclude = "MRFILES,MRCOLS,MRSAT,MEDLINE";
       Hashtable seen = new Hashtable();
       for(int i=0; i<properties.length; i++) {
	if(exclude.indexOf(properties[i].getKeyQualifier()) == -1 &&
           !seen.containsKey(properties[i].getKeyQualifier() + properties[i].getKey())) {
	  seen.put(properties[i].getKeyQualifier() + properties[i].getKey(),properties[i].getKeyQualifier());
    %>
    <option value="<%= properties[i].getKeyQualifier() + "~" +  properties[i].getKey()%>"><%= properties[i].getKeyQualifier() + " - " +  properties[i].getKey()%></option>
    <%
	}
       }
    %>
  </select>
</form>
</td>
</tr>

<tr >
<td width="25%">New DOCKEY, TYPE</td><td>
   <form action="controller">
     <input type="hidden" name="state" value="EditMRDOC">
     <input type="hidden" name="command" value="Insert">
      <input type="text" name="dockey" >
      <input type="text" name="type" >
      <input type="submit" value="GO" >
  </form>
    </td>
</tr>
</table></center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" />
</body>
</html>
