<%@ page contentType="text/html;charset=utf-8" errorPage= "ErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<jsp:useBean id="bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<%  ReleaseClient rc = bean.getReleaseClient();
    ReleaseInfo releaseinfo = rc.getReleaseInfo(request.getParameter("release"));
    rc.removeReleaseInfo(releaseinfo);
%>
    <jsp:forward page="mrd/controller">
      <jsp:param name="state" value="ReleaseManager" />
    </jsp:forward>
