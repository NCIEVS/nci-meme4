
<%@ page session="false" contentType="text/html; charset=UTF-8"
         import="gov.nih.nlm.meme.exception.*, java.util.*, java.io.*"
         isErrorPage="true" %>
<html>

<head>
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>ErrorPage</title>
</head>

<body>
<form name=errorPageFrom action=controller>
<input type=hidden name=state value=ConceptReportView>

<SPAN id="blue">
<CENTER>Concept Reports from MRD</CENTER>
</SPAN>

<table border=1 class=topTable align=center> 
<tr><td>

	<TABLE width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr><td align=center colspan=2>
	<br>
	<span id="red"><center>Error Page</center></span> 
	</td></tr>

	<tr>
	<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
	<td align=left>
	<br><br>
	<span class=data>
	
<%
	
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	exception.printStackTrace(pw);
	out.print(sw);
	sw.close();
	pw.close();
	
%>	

	<b> ERROR Message: <%= exception.toString() %></b>
	
	</span> 
	</td></tr>
	
	<tr><td align=center colspan=2>
		<br><br>
		<input type="submit" value="Home" >
		<br><br>
	</td></tr>
	
	</TABLE>

</td></tr>
</table>

<form>
  <table width="90%">
    <td align="center">
      
    </td>
  </table>
</form>

</body>
</html>
