<%@ page contentType="text/html;charset=utf-8" errorPage= "ErrorPage.jsp"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="gov.nih.nlm.meme.MEMEToolkit" %>
<%@ page import="gov.nih.nlm.meme.common.Authority" %>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<jsp:useBean id="bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<% ReleaseInfo releaseinfo = new ReleaseInfo();
    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");
    releaseinfo.setName(request.getParameter("release"));
    releaseinfo.setDescription(request.getParameter("description"));
    releaseinfo.setAuthority(new Authority.Default(request.getParameter("authority")));
    releaseinfo.setGeneratorClass("gov.nih.nlm.mrd.client." + request.getParameter("generator"));
    releaseinfo.setBuildHost(request.getParameter("build_host"));
    releaseinfo.setBuildUri(request.getParameter("build_uri"));
    releaseinfo.setReleaseHost(request.getParameter("release_host"));
    releaseinfo.setReleaseUri(request.getParameter("release_uri"));
    releaseinfo.setStartDate(dateformat.parse(request.getParameter("start_date")));
    releaseinfo.setEndDate(new java.util.Date());
    releaseinfo.setMEDStartDate(dateformat.parse(request.getParameter("med_start_date")));
    releaseinfo.setMBDStartDate(dateformat.parse(request.getParameter("mbd_start_date")));
    releaseinfo.setReleaseDate(dateformat.parse(request.getParameter("release_date")));
    releaseinfo.setIsBuilt(false);
    releaseinfo.setIsPublished(false);
    bean.setReleaseGenerator("gov.nih.nlm.mrd.client."+ request.getParameter("generator"));
    ReleaseClient client = bean.getReleaseClient();
    client.prepareRelease(releaseinfo);
%>
    <jsp:forward page="mrd/controller">
      <jsp:param name="state" value="ReleaseSummary" />
      <jsp:param name="release" value="<%= request.getParameter(\"release\")%>" />
    </jsp:forward>
