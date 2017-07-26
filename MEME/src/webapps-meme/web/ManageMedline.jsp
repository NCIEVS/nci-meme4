<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<%  ReleaseClient rc = release_bean.getReleaseClient();
	if("clearLog".equals(request.getParameter("action"))) {
		rc.clearMedlineStatus(request.getParameter("stage"));
	}
	else if("start".equals(request.getParameter("action"))) {
        	if("download".equals(request.getParameter("stage"))) {
			rc.downloadMedlineBaseline();
        	} if("parse".equals(request.getParameter("stage"))) {
			rc.parseMedlineBaseline(request.getParameter("release"));
		} if("process".equals(request.getParameter("stage"))) {
			rc.processMedlineBaseline();
        	} if("update".equals(request.getParameter("stage"))) {
			rc.updateMedline(request.getParameter("release"));
        	}
	}
	else if ("delete".equals(request.getParameter("action"))) {
		rc.deleteUpdateMedlineXML(request.getParameter("file"));
	}
%>
