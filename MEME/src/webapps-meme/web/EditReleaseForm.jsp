<%@ page contentType="text/html;charset=utf-8" errorPage= "ErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>Edit Release Form</title>
</head>

<body>
<span id=blue><center>Edit Release</center></span>
<hr WIDTH=100%>
<center>
<form method="get" action="controller" name="form1">
<input type="hidden" name="state" value="ManageRelease">
<input type="hidden" name="action" value="update">
<input type="hidden" name="release" value="<%= request.getParameter("release") %>">
<%
  ReleaseClient rc = release_bean.getReleaseClient();
  ReleaseInfo release_info = rc.getReleaseInfo(request.getParameter("release"));
  SimpleDateFormat dateformat = release_bean.getDateFormat();
%>
<table WIDTH=95% BORDER=0 >
	<tr ><td>Release Name:</td>
	    <td><%=release_info.getName()%>
	    </td>
  </tr>
	<tr ><td width="30%">Description:</td>
	    <td><input type="text" size=28 name="description" value="<%=release_info.getDescription()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Authority:</td>
	    <td width="70%"><input type="text" name="authority" value="<%=(release_info.getAuthority() !=null ? release_info.getAuthority().toString() : "") %>">
	    </td>
  </tr>
	<tr ><td width="30%">GeneratorClass:</td>
	    <td width="70%"><%=release_info.getGeneratorClass()%>
	    </td>
  </tr>
  </tr>
	<tr ><td width="30%">Administrator:</td>
	    <td width="70%"><input type="text" name="administrator" value="<%=(release_info.getAdministrator() != null ? release_info.getAdministrator().toString() : "")%>">
	    </td>
  </tr>
	<tr ><td width="30%">Build Host:</td>
	    <td width="70%"><input type="text" name="build_host" value="<%=release_info.getBuildHost()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Build URI:</td>
	    <td><input type="text" name="build_uri" value="<%=release_info.getBuildUri()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Release Host:</td>
	    <td width="70%"><input type="text" name="release_host" value="<%=release_info.getReleaseHost()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Release URI:</td>
	    <td width="70%"><input type="text" name="release_uri" value="<%=release_info.getReleaseUri()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Documentation Host:</td>
	    <td width="70%"><input type="text" name="documentation_host" value="<%=release_info.getDocumentationHost()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Documentation URI:</td>
	    <td width="70%"><input type="text" name="documentation_uri" value="<%=release_info.getDocumentationUri()%>">
	    </td>
  </tr>
	<tr ><td width="30%">Start Date:</td>
	    <td width="70%"><%= dateformat.format(release_info.getStartDate()) %></td>
  </tr>
	<tr ><td width="30%">MED Start Date:</td>
	    <td width="70%"><meme:calendar name="med_start_date" initialValue="<%= dateformat.format(release_info.getMEDStartDate()) %>" first="true" /></td>
  </tr>
	<tr ><td width="30%">MBD Start Date:</td>
	    <td width="70%"><meme:calendar name="mbd_start_date" initialValue="<%= dateformat.format(release_info.getMBDStartDate()) %>" first="false" /></td>
  </tr>
	<tr ><td width="30%">Release Date:</td>
	    <td width="70%"><meme:calendar name="release_date" initialValue="<%= dateformat.format(release_info.getReleaseDate()) %>" first="false" /></td>
  </tr>
        <td colspan=2 width="50%" align="center">
            <input accesskey="r" class="SPECIAL" type="submit" value="Update">
                            &nbsp;&nbsp;&nbsp;
            <input accesskey="r" class="SPECIAL" type="button" value="Cancel" onClick="history.go(-1)">

        </td>
  </tr>
    </table>
</form>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</CENTER>
</BODY>
</HTML>
