<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<html>
<head>
</head>
<%
  //
  // Compute status of the target
  //
  ReleaseClient rc = release_bean.getReleaseClient();
  ReleaseTarget[] targets = rc.getTargets(request.getParameter("release"));
  StringBuffer setStatus = new StringBuffer();
  for(int i=0; i<targets.length; i++) {
	if(targets[i].getDependencies() != null)
  	setStatus.append("parent.setTarget('" + targets[i].getName() + "','"+ targets[i].getDependencies()+"');");
  }
%>
<body onLoad="<%= setStatus.toString() %>">
</body>
</html>
