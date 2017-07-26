<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
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
  // activate the process
  //
  function activate(stage) {
  	for(var i=0; i <stage.length; i++) {
      		if(stage[i].checked) {
      			frames["activate_frame"].location.href="controller?state=ManageMedline&action=start&release=<%=request.getParameter("release")%>&stage=" + stage[i].value;
      		}
  	}
  }
// -->
  </script>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<TITLE>
Process Medline
</TITLE>
<!-- Load IFrame error handler code -->
<script src="../IFrameErrorHandler.js">
  alert("Included JS file not found");
</script>
</HEAD>
<BODY>
<%  ReleaseClient rc = release_bean.getReleaseClient();
%>
<iframe name="activate_frame" style="visibility:hidden" width=0 height=0></iframe>
<iframe name="clearlog_frame" style="visibility:hidden" width=0 height=0></iframe>
<span id=blue><center>Process Medline</center></span>
<hr WIDTH=100%>
 <i> Click <a href="controller?state=ListMedlineProperty&host=<%=release_bean.getHost()%>&port=<%=release_bean.getPort()%>&midService=<%=release_bean.getMidService()%>">here</a> to edit medline date patterns.</i>
<br>
<center>
<FORM target="_blank" name="form1" method="get" action="controller">
<table cellpadding=4 cellspacing=0 WIDTH=70% BORDER=1 >
<tr><th>&nbsp; <th>Stage <th>Status <th>View Log <th>TimeStamp</tr>
<%
    StageStatus[] stageStatus = rc.getMedlineStatus();
    boolean enable = true;
    boolean prev;
    for(int j=0; j < stageStatus.length; j++) {
	int code = stageStatus[j].getCode();
        String status = "<td>&nbsp;</td><td>&nbsp;</td>";
	String input = "<td>&nbsp;</td>";
	if(enable) {
          	input = "<td><input type=\"radio\" name=\"stage\" value=\"" + stageStatus[j].getName() + "\"></td>";
	}
	prev = enable;
	enable = false;
	if((code & StageStatus.RUNNING) == StageStatus.RUNNING) {
		status = "<td>Running</td><td><font size=-1><a target=\"_blank\" href=\"controller?state=MedlineDetails&detail=View Log&stage=" + stageStatus[j].getName() + "\" >View Log</a></font></td>";
	}
	if((code & StageStatus.FINISHED) == StageStatus.FINISHED) {
    		status = "<td>Finished <a onclick=\"return clearLog(this);\" href=\"controller?state=ManageMedline&stage=" + stageStatus[j].getName() + "&action=clearLog\"><img class=\"clearrow\" border=\"0\" src=\"../img/clearrow.gif\" alt=\"clear\" title=\"clear log\"></a></td><td><font size=-1><a target=\"_blank\" href=\"controller?state=MedlineDetails&detail=View Log&stage=" + stageStatus[j].getName() + "\" >View Log</a></font></td>";
		enable = prev && true;
	}
	if((code & StageStatus.ERROR) == StageStatus.ERROR) {
    		status = "<td>Error</td><td><font size=-1><a target=\"_blank\" href=\"controller?state=MedlineDetails&detail=View Log&stage=" + stageStatus[j].getName() + "\" >View Log</a></font></td>";
	}

	out.println("<tr>" + input + "<td>" + stageStatus[j].getName() + "</td>" +  status + "<td>" + (stageStatus[j].getEndTime() == null ? "&nbsp;" : stageStatus[j].getEndTime().toString()) + "</td>"
	+ "</tr>") ;
    }
%>
</table>
</FORM>
  <TABLE WIDTH=90% >
  <TR>
    <TD align="center">
	<input type="button" value="Start" onClick="activate(document.form1.elements);"> <input type="button" onClick="window.location.reload();" value="Refresh">
    </TD>
  </TR>
  </TABLE>

</center>


<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>
</HTML>
