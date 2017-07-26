<%@ page contentType="text/html;charset=utf-8" errorPage= "EntryPointErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.*" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>


<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<jsp:setProperty name="release_bean" property="*" />
<TITLE>
Release Manager View
</TITLE>
</HEAD>
<BODY>
<SPAN id="blue"><CENTER>Release Manager View</CENTER></SPAN>
<HR width="100%">
<CENTER>
<TABLE width="95%" border="0">
  <FORM name="connection" method="GET">
  <INPUT name="state" type="hidden" value="ReleaseManagerView">
    <TR>
      <TH colspan="3">&nbsp;</TH>
    </TR>
    <TR>
      <TD colspan="3">&nbsp;</TD>
    </TR>
    <TR>
      <TD width="30%">Change database</TD>
      <TD width="70%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= release_bean.getMidService() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change host</TD>
      <TD width="70%">
        <meme:hostList name="host" submit="false" initialValue="<%= release_bean.getHost() %>" />
      </TD>
    </TR>
    <TR>
      <TD width="30%">Change port</TD>
      <TD width="70%">
        <INPUT type="text" name="port" size="12"
               value="<jsp:getProperty name="release_bean" property="port" />">
        <INPUT type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
      <th colspan="3">Release Manager View</th>

    </tr>
    <tr>
      <td colspan="3">&nbsp;</td>
    </tr>
<form method="get" action="controller">
<input type="hidden" name="state" value="ReleaseSummaryView">
    <TR>
      <TD width="30%">View Release</TD>
      <TD width="70%">

<select name="release">
  <% ReleaseClient rc = release_bean.getReleaseClient();
    ReleaseInfo release_info[] = rc.getReleaseHistory();

    Arrays.sort(release_info, new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((ReleaseInfo)o2).getName().compareTo(((ReleaseInfo)o1).getName());
        }
      });

    for(int i = 0 ; i < release_info.length ; i++) {
  %>
     <option value="<%=release_info[i].getName()%>"><%= release_info[i].getName()%>
  <%
    }
  %>
</select>
<input accesskey="r" class="SPECIAL" type="submit" name="Manage" value="Go">
</TD>
</TR>
</form>
   </table>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>
</HTML>
