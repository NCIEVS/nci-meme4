<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.mrd.common.StageStatus" %>

<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
  <script language="javascript">
function launchCenter(url, name, height, width)
{
	var str = "height=" + height + ",innerHeight=" + height;
	str += ",width=" + width + ",innerWidth=" + width;
	if (window.screen)
	{
		var ah = screen.availHeight - 30;
		var aw = screen.availWidth - 10;

		var xc = (aw - width) / 2;
		var yc = (ah - height) / 2;

		str += ",left=" + xc + ",screenX=" + xc;
		str += ",top=" + yc + ",screenY=" + yc;
	}
	str += ", menubar=no, scrollbars=yes, status=0, location=0, directories=0, resizable=1";
	window.open(url, name, str);
}
function confirmDelete(file, href) {
	if (!confirm("Are you sure you want to delete "+ file +"?")) {
      		return false;
    	}
	frames["activate_frame"].location.href=href;
}
  </script>
<TITLE>
Medline Details
</TITLE>
</HEAD>
<BODY>
<iframe name="activate_frame" style="visibility:hidden" width=0 height=0></iframe>
<span id=blue><center><%="Medline " + request.getParameter("stage") + " Log"%></center></span>
<hr WIDTH=100%>
<%  ReleaseClient rc = release_bean.getReleaseClient();
    if(request.getParameter("detail").equals("View Log") ) {
    	StageStatus stagestatus = rc.getMedlineStageStatus(request.getParameter("stage"));
	int code = stagestatus.getCode();
        String log = stagestatus.getLog();
	if((code & StageStatus.ERROR) == StageStatus.ERROR) {
		log = log.replaceAll("(ERROR parsing )(medline.*xml)","$1<A href=\"controller?state=ManageMedline&action=delete&file=$2\" onclick=\"confirmDelete('$2',this.href);return false;\">$2</A>");
	 	log = log + "<blockquote><i> Click <a href=\"controller?state=EditMedlineProperty&command=Insert\">here</a> to add a new pattern.</i></blockquote>";
	}
        out.println("<PRE STYLE=\"margin-left:  1.0cm\">" + log + "</PRE>" );
    }
%>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>

</HTML>

