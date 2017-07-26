<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>
<%@ page import="gov.nih.nlm.mrd.web.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.io.*" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>


<HTML>
<HEAD>
<script language="javascript">
<!--

  //
  // Verify delete action
  //
  function clearLog(obj) {
      frames["clearlog_frame"].location.href=obj.href;
      window.location.reload();
	return false;
    }
// -->
  </script>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<TITLE>
Target Summary
</TITLE>
<!-- Load IFrame error handler code -->
<script src="../IFrameErrorHandler.js">
  alert("Included JS file not found");
</script>
</HEAD>
<BODY>
<%  ReleaseClient rc = release_bean.getReleaseClient();
    ReleaseInfo release_info = rc.getReleaseInfo(request.getParameter("release"));
    ReleaseTarget target = rc.getTargetStatus(request.getParameter("release"),request.getParameter("target"));
%>
<iframe name="activate_frame" style="visibility:hidden" width=0 height=0></iframe>
<iframe name="clearlog_frame" style="visibility:hidden" width=0 height=0></iframe>
<span id=blue><center><%=target.getName()%></center></span>
<hr WIDTH=100%>
<center>
<table WIDTH=60% BORDER=0 >
  <tr ><td>Builduri:</td>
    <td><tt><%=release_info.getBuildUri()%>
    </tt></td>
  </tr>
  <tr ><td>Buildhost:</td>
    <td><tt><%=release_info.getBuildHost()%>
    </tt></td>
  </tr>
  <tr ><td>Activated:</td>
    <td><tt>
      <% if(target.isActive()) {%>
          Yes
      <% } else {%>
          No
      <% } %>
    </tt></td>
  </tr>
</table>
<table cellpadding=4 cellspacing=0 WIDTH=70% BORDER=1 >
<tr> <th>Stage <th>Status <th>View Log <th>TimeStamp</tr>
<%
    StageStatus[] stagestatus = rc.getTargetStatus(release_info.getName(),target.getName()).getStageStatus();
    for(int j=0; j < stagestatus.length; j++) {
	int code = stagestatus[j].getCode();
        String status = "<td>&nbsp;</td><td>&nbsp;</td>";
	if((code & StageStatus.RUNNING) == StageStatus.RUNNING) {
		status = "<td>Running</td><td>&nbsp;</td>";
	}
	if((code & StageStatus.FINISHED) == StageStatus.FINISHED) {
	  if(stagestatus[j].getName().equals("validate")) {
    		status = "<td>Finished <a onclick=\"return clearLog(this);\" href=\"controller?state=ManageTargets&release=" + release_info.getName() + "&target=" +request.getParameter("target") + "&stage=" + stagestatus[j].getName() + "&action=clearLog\"><img class=\"clearrow\" border=\"0\" src=\"../img/clearrow.gif\" alt=\"clear\" title=\"clear log\"></a></td><td><font size=-1><A target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" + request.getParameter("target") + "&detail=View QAResult\" >View QAResult</A></font>&nbsp;|&nbsp;" +
    			  "<font size=-1><A target=\"_blank\" href=\"/MRD/Documentation/" + release_info.getName() + "/qa_" + request.getParameter("target") + ".rpt.html\" >View Generated QAReport</A></font><br/>" +
    			  "<font size=-1><A target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" + request.getParameter("target") + "&detail=View QAReport&type=html\">View QAReport</A></font>&nbsp;|&nbsp;" +
                          "<font size=-1><A target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" + request.getParameter("target") + "&detail=Edit QAReport&view=View Needs Review Only\" >Edit QAReport</A></font></td>";

	  } else {
    		status = "<td>Finished <a onclick=\"return clearLog(this);\" href=\"controller?state=ManageTargets&release=" + release_info.getName() + "&target=" +request.getParameter("target") + "&stage=" + stagestatus[j].getName() + "&action=clearLog\"><img class=\"clearrow\" border=\"0\" src=\"../img/clearrow.gif\" alt=\"clear\" title=\"clear log\"></a></td><td><font size=-1><a target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" +request.getParameter("target") + "&stage=" + stagestatus[j].getName() +"&detail=View Log\" >View Log</a></font></td>";
	  }
	}
	if((code & StageStatus.NEEDSREVIEW) == StageStatus.NEEDSREVIEW) {
    		status = "<td>NeedsReview</td><td><font size=-1><A target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" + request.getParameter("target") + "&detail=View QAResult\" >View QAResult</A></font>&nbsp;|&nbsp;" +
    			  "<font size=-1><A target=\"_blank\" href=\"/MRD/Documentation/" + release_info.getName() + "/qa_" + request.getParameter("target") + ".rpt.html\" >View Generated QAReport</A></font><br/>" +
    			  "<font size=-1><A target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" + request.getParameter("target") + "&detail=View QAReport&type=html\">View QAReport</A></font>&nbsp;|&nbsp;" +
                          "<font size=-1><A target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" + request.getParameter("target") + "&detail=Edit QAReport&view=View Needs Review Only\" >Edit QAReport</A></font></td>";
	}
	if((code & StageStatus.ERROR) == StageStatus.ERROR) {
    		status = "<td>Error</td><td><font size=-1><a target=\"_blank\" href=\"controller?state=TargetDetails&release=" + release_info.getName() + "&target=" +request.getParameter("target") + "&stage=" + stagestatus[j].getName() + "&detail=View Log\" >View Log</a></font></td>";
	}
	if((code & StageStatus.QUEUED) == StageStatus.QUEUED) {
		status = "<td>Queued</td><td>&nbsp;</td>";
	}
	out.println("<tr><td>" + stagestatus[j].getName() + "</td>" +  status + "<td>" + (stagestatus[j].getEndTime() == null ? "&nbsp;" : stagestatus[j].getEndTime().toString()) + "</td>" +"</tr>") ;
    }
%>

</table>
</center>


<FORM target="_blank" name="form1" method="get" action="controller">
  <input type="hidden" name="state" value="TargetDetails">
  <input type="hidden" name="release" value="<%=request.getParameter("release") %>">
  <input type="hidden" name="target" value="<%=request.getParameter("target") %>">
  <TABLE WIDTH=90% >
  <TR>
    <TD align="center">
  <%
    for(int j=0; j < stagestatus.length; j++) {
      if("build".equals(stagestatus[j].getName()) && ((stagestatus[j].getCode() & StageStatus.FINISHED) == StageStatus.FINISHED) ) {
    %>
        <input type="submit" name="detail" value="Preview Target">
    <% }
      if("validate".equals(stagestatus[j].getName()) && ((stagestatus[j].getCode() & StageStatus.FINISHED) == StageStatus.FINISHED) &&
	((stagestatus[j].getCode() & StageStatus.ERROR) != StageStatus.ERROR)) {
    %>
	<input type="button" onClick='frames["activate_frame"].location.href="controller?state=ManageTargets&action=QAReport&release=<%=request.getParameter("release") %>&handlers=<%=request.getParameter("target") %>"' value="Generate QA Report">
    <% }

    } %>
      <input type="button" value="Close" onClick="window.close(); return true">
    </TD>
  </TR>
  </TABLE>
</FORM>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>
</HTML>
