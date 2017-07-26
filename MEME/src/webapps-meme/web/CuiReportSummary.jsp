<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>


<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<TITLE>
CuiReport Summary
</TITLE>
<!-- Load IFrame error handler code -->
<script src="../IFrameErrorHandler.js">
  alert("Included JS file not found");
</script>
</HEAD>
<BODY>
<iframe name="activate_frame" style="visibility:hidden" width=0 height=0></iframe>
<span id=blue><center><%=request.getParameter("cui")%></center></span>
<hr WIDTH=100%>

<FORM target="_blank" name="form1" method="get" action="controller">
  <input type="hidden" name="state" value="CuiReportDetails">
  <input type="hidden" name="release" value="<%=request.getParameter("release") %>">
  <input type="hidden" name="cui" value="<%=request.getParameter("cui") %>">
  <input type="hidden" name="compareTo" value="<%=request.getParameter("compareTo") %>">
  <TABLE WIDTH=90% >
   <TR><TD width="20%">New Release :</TD><TD><%= request.getParameter("release") %> </TD> </TR>
   <TR><TD width="20%">Old Release :</TD><TD><%= request.getParameter("compareTo") %> </TD> </TR>
  </TABLE>
  <TABLE WIDTH=90% >
  <TR>
    <TD colspan="2" align="center">
        <input type="button" onclick="window.open('<%= "/MRD/Documentation/" + request.getParameter("release")  + "/" + request.getParameter("cui") + ".rpt.html"%>')" name="detail" value="View Report">
        <input type="submit" name="detail" value="Edit Report">
	<input type="button" onClick='frames["activate_frame"].location.href="controller?state=ManageCuiReport&action=html&release=<%=request.getParameter("release") %>&cui=<%=request.getParameter("cui") %>"' value="Generate Cui Comparison Report">
        <input type="button" value="Close" onClick="window.close(); return true">
    </TD>
  </TR>
  </TABLE>
</FORM>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>
</HTML>
