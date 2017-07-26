<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="gov.nih.nlm.meme.MEMEToolkit" %>
<%@ page import="gov.nih.nlm.meme.common.Authority" %>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<jsp:useBean id="bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<%  ReleaseClient rc = bean.getReleaseClient();
    ReleaseInfo releaseinfo = rc.getReleaseInfo(request.getParameter("release"));
    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");
    releaseinfo.setDescription(request.getParameter("description"));
    releaseinfo.setAuthority(new Authority.Default(request.getParameter("authority")));
    releaseinfo.setBuildHost(request.getParameter("build_host"));
    releaseinfo.setBuildUri(request.getParameter("build_uri"));
    releaseinfo.setReleaseHost(request.getParameter("release_host"));
    releaseinfo.setReleaseUri(request.getParameter("release_uri"));
    releaseinfo.setMEDStartDate(dateformat.parse(request.getParameter("med_start_date")));
    releaseinfo.setMBDStartDate(dateformat.parse(request.getParameter("mbd_start_date")));
    releaseinfo.setReleaseDate(dateformat.parse(request.getParameter("release_date")));
    rc.setReleaseInfo(releaseinfo);
%>
    <jsp:forward page="mrd/controller">
      <jsp:param name="state" value="ReleaseSummary" />
      <jsp:param name="release" value="<%= request.getParameter(\"release\")%>" />
    </jsp:forward>
