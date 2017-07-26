<%@page contentType="text/html;charset=utf-8" errorPage="ErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@page import="gov.nih.nlm.mrd.server.ServerConstants"%>
<%@page import="java.util.Hashtable"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Arrays"%>
<%@page import="gov.nih.nlm.meme.MEMEToolkit"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Calendar"%>
<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean"/>
<TITLE>New Release Form</TITLE>
</HEAD>
<BODY>
<span id=blue>
  <center>New Release</center>
</span>
<hr WIDTH=100%>
<CENTER>
<form action="controller" name="form1" method="GET">
  <input type="hidden" name="state" value="ManageRelease">
  <input type="hidden" name="action" value="prepare">
<%
  SimpleDateFormat dateformat = release_bean.getDateFormat();
  Calendar now = Calendar.getInstance();
  String newrelease = now.get(Calendar.YEAR) + "AA";
  String release = request.getParameter("release");
  Hashtable season = new Hashtable();
  season.put("AA", "");
  season.put("AB", " Spring");
  season.put("AC", " Summer");
  season.put("AD", " Fall");
  if (release != null && !release.equals("")) {
    if (release.endsWith("AD"))
      newrelease = (Integer.valueOf(release.substring(0, 4)).intValue() + 1) + "AA";
    else
      newrelease = (release.substring(0, release.length() - 1) + (char) (release.charAt(release.length() - 1) + 1));
  }
  String med = "01/01/" + (Integer.parseInt(newrelease.substring(0, 4)) - 5);
  String mbd = "01/01/" + (Integer.parseInt(newrelease.substring(0, 4)) - 10);
%>
  <table WIDTH=95% BORDER=0>
    <tr>
      <td width="30%">Release Name:</td>
      <td width="70%">
        <input type="text" name="release" value="<%=newrelease%>" size=7>
      </td>
    </tr>
    <tr>
      <td width="30%">Description:</td>
      <td width="70%">
        <input type="text" size=30 name="description" value="<%= "Base Release for" + season.get(newrelease.substring(4,6)) + " " + newrelease.substring(0,4)%>">
      </td>
    </tr>
    <tr>
    <tr>
      <td width="30%">Previous Release:</td>
      <td width="70%">
        <select name="previous_release">
        <%
          ReleaseClient rc = release_bean.getReleaseClient();
          ReleaseInfo release_info[] = rc.getReleaseHistory();
          Arrays.sort(release_info, new Comparator() {
            public int compare(Object o1, Object o2) {
              return ((ReleaseInfo) o2).getName().compareTo(((ReleaseInfo) o1).getName());
            }
          });
          if (release_info.length > 0) {
            for (int i = 0; i < release_info.length; i++) {
        %>
          <option <%=(release_info[i].getName().equals(request.getParameter("release")) ? "SELECTED" : "") %> value="<%=release_info[i].getName()%>"><%= release_info[i].getName()%>
        <%
            }
                }
        %>
        </select>
      </td>
    </tr>
    <tr>
      <td width="30%">Previous Major Release:</td>
      <td width="70%">
        <select name="previous_major_release">
        <%
          if (release_info.length > 0) {
            String prev_major = now.get(Calendar.YEAR) + "AA";
            for (int i = 0; i < release_info.length; i++) {
        %>
          <option <%=(release_info[i].getName().equals(prev_major) ? "SELECTED" : "") %> value="<%=release_info[i].getName()%>"><%= release_info[i].getName()%>
        <%
            }
                }
        %>
        </select>
      </td>
    </tr>
    <tr>
      <td width="30%">Authority:</td>
      <td width="70%">
        <input type="text" name="authority">
      </td>
    </tr>
    <tr>
      <td width="30%">Generator Class:</td>
      <td width="70%">
        <select name="generator">
          <option value="FullMRFilesReleaseClient">FullMRFilesReleaseClient
        </select>
      </td>
    </tr>
</tr>    <tr>
      <td width="30%">Administrator:</td>
      <td width="70%">
        <input type="text" name="administrator">
      </td>
    </tr>
    <tr>
      <td width="30%">Build Host:</td>
      <td width="70%">
        <input type="text" name="build_host" value="<%=release_bean.getHost()%>">
      </td>
    </tr>
    <tr>
      <td width="30%">Build URI:</td>
      <td width="70%">
        <input type="text" name="build_uri" value="<%=MEMEToolkit.getProperty(ServerConstants.MRD_HOME) + "/" + newrelease%>">
      </td>
    </tr>
    <tr>
      <td width="30%">Release Host:</td>
      <td width="70%">
        <input type="text" name="release_host" value="umls-source.nlm.nih.gov">
      </td>
    </tr>
    <tr>
      <td width="30%">Release URI:</td>
      <td width="70%">
        <input type="text" name="release_uri" value="<%= "/umls/Releases/" + newrelease %>">
      </td>
    </tr>
    <tr>
      <td width="30%">Documentation Host:</td>
      <td width="70%">
        <input type="text" name="documentation_host" value="smis.nlm.nih.gov">
      </td>
    </tr>
    <tr>
      <td width="30%">Documentation URI:</td>
      <td width="70%">
        <input type="text" name="documentation_uri" value="<%= "/" + newrelease %>">
      </td>
    </tr>
    <tr>
      <td width="30%">Start Date:</td>
      <td width="70%">
        <meme:calendar name="start_date" initialValue="<%= dateformat.format(now.getTime()) %>" first="true"/>
      </td>
    </tr>
    <tr>
      <td width="30%">MED Start Date:</td>
      <td width="70%">
        <meme:calendar name="med_start_date" initialValue="<%= med %>"/>
      </td>
    </tr>
    <tr>
      <td width="30%">MBD Start Date:</td>
      <td width="70%">
        <meme:calendar name="mbd_start_date" initialValue="<%= mbd %>"/>
      </td>
    </tr>
    <tr>
      <td width="30%">Release Date:</td>
      <td width="70%">
        <meme:calendar name="release_date" initialValue="<%= dateformat.format(now.getTime()) %>"/>
      </td>
    </tr>
  </table>
  <br>
  <input accesskey="r" class="SPECIAL" type="submit" value="Prepare Release" name="Button">
</form>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual"/>
</BODY>
</HTML>
