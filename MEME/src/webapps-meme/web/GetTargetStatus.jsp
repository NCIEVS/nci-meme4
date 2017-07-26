<%@ page contentType="text/html;charset=utf-8" errorPage= "IFrameErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<html>
<head>
<meta http-equiv="REFRESH" content="200">
</head>
<%
  //
  // Compute status of the target
  //
  ReleaseClient rc = release_bean.getReleaseClient();
  ReleaseTarget target = rc.getTargetStatus(request.getParameter("release"),request.getParameter("target"));
  StageStatus [] stagestatus = target.getStageStatus();
  StringBuffer setStatus = new StringBuffer();
  for(int i=0; i<stagestatus.length; i++) {
  	String status = "&nbsp;";
  	String color = "";
	int code = stagestatus[i].getCode();
	if((code & StageStatus.RUNNING) == StageStatus.RUNNING) {
		status = "Running";
		color = "purple";
	}
	if((code & StageStatus.FINISHED) == StageStatus.FINISHED) {
		status = "Finished";
		color = "green";
	}
	if((code & StageStatus.NEEDSREVIEW) == StageStatus.NEEDSREVIEW) {
		status = "NeedsReview";
		color = "maroon";
	}
	if((code & StageStatus.ERROR) == StageStatus.ERROR) {
		status = "Error";
		color = "red";
	}
	if((code & StageStatus.QUEUED) == StageStatus.QUEUED) {
		status = "Queued";
		color = "Fuchsia";
	}
  	setStatus.append("parent.setStatus('" + target.getName() + "_" + stagestatus[i].getName() +"','"+ request.getParameter("release") + "','" + status + "','" + color +"','" + (stagestatus[i].getEndTime() == null ? "" : stagestatus[i].getEndTime().toString())+ "'," + target.isActive() +");");
  }
%>
<body onLoad="<%= setStatus.toString() %>">
</body>
</html>
