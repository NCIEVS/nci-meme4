<%@ page contentType="text/html;charset=utf-8" errorPage= "ErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.*" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<jsp:setProperty name="release_bean" property="*" />

<HTML>

<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<TITLE>Cui Comparison Report</TITLE>
<script language="javascript">
<!--

  //
  // Verify delete action
  //
  function check() {
    if(document.form1.release.value <= document.form1.compareTo.value) {
	alert("Old Release must be earlier than New Release");
    } else {
        document.form1.submit();
    }
  }
// -->
  </script>
</HEAD>

<BODY>
<SPAN id="blue"><CENTER>Cui Comparison Report</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="95%" border="0">
<form name="form1" method="get" action="controller">
<input type="hidden" name="state" value="ManageCuiReport">
<input type="hidden" name="action" value="build">
       <tr> <td width="30%">Cui : </td>
	   <td width="70%"><input type="text" name="cui" >
           <input accesskey="r" class="SPECIAL" type="button" onclick="check()" value="Go"></td>
       </tr>

    <TR>
      <TD width="30%">New Release : </TD>
      <TD width="70%">

<select name="release">
<%
  ReleaseClient rc = release_bean.getReleaseClient();
  ReleaseInfo release_info[] = rc.getReleaseHistory();

  Arrays.sort(release_info, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((ReleaseInfo)o2).getName().compareTo(((ReleaseInfo)o1).getName());
      }
    });
  for(int i = 0 ; i < release_info.length ; i++) {
%>
  <option <%=(release_info[i].getName().equals(request.getParameter("release")) ? "SELECTED" : "") %> value="<%=release_info[i].getName()%>"><%= release_info[i].getName()%>
<%
  }
%>
</select>
</TD>
</TR>
    <TR>

      <TD width="30%">Old Release : </TD>
      <TD width="70%">

<select name="compareTo">
<%
  for(int i = 0 ; i < release_info.length ; i++) {
%>
  <option value="<%=release_info[i].getName()%>"><%= release_info[i].getName()%>
<%
  }
%>
</select>
</TD>
</TR>
</form>
   </table>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</HTML>
