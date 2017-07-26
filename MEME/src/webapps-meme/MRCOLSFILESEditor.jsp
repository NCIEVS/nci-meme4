<%@ page session="true" contentType="text/html; charset=UTF-8" %>
<%@ page import="gov.nih.nlm.meme.client.AuxiliaryDataClient" %>
<%@ page errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="mrcolsfiles_bean" scope="session"
             class="gov.nih.nlm.meme.beans.AuxiliaryDataClientBean" />
<jsp:setProperty name="mrcolsfiles_bean" property="*" />
<title>MRCOLS/MRFILES Editor</title>
</head>
<%
   AuxiliaryDataClient aux_client = mrcolsfiles_bean.getAuxiliaryDataClient();
   aux_client.getMetaPropertiesByKeyQualifier("MRFILES");
%>
<body>
<span id="blue"><center>MRCOLS/MRFILES Editor</center></span>
<hr width="100%">
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%"  >
<tr >
  <form name="connection" method="GET" action="controller">
  <input name="state" type="hidden" value="MRCOLSFILESEditor">
  <td width="25%">Change Database:</td><td>
        <meme:midServiceList name="midService" submit="true" initialValue="<%= mrcolsfiles_bean.getMidService() %>" />
</td>
</tr>
</form>
<tr >
<td width="25%">Select a category to edit:</td><td>
<form  method="GET" action="controller">
  <input name="state" type="hidden" value="ListMRCOLSFILES">
 <select name="category" onChange="this.form.submit(); return true;">
    <option>-- SELECT CATEGORY --</option>
    <option value="MRCOLS">MRCOLS</option>
    <option value="MRFILES">MRFILES</option>
  </select>
</form>
</td>
</tr>

</table></center>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" />
</body>
</html>
