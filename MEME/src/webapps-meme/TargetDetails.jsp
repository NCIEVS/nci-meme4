<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.mrd.common.QAReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAResultReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAComparisonReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAReport" %>
<%@ page import="gov.nih.nlm.mrd.common.QAComparison" %>
<%@ page import="gov.nih.nlm.mrd.common.QAResult" %>
<%@ page import="gov.nih.nlm.mrd.common.StageStatus" %>
<%@ page import="gov.nih.nlm.mrd.common.ByReasonByNameComparator" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.io.PrintWriter" %>
<%@ taglib uri="/WEB-INF/meme.tld" prefix="meme" %>
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />

<%  ReleaseClient rc = release_bean.getReleaseClient();
    ReleaseInfo release_info = rc.getReleaseInfo(request.getParameter("release"));
    ReleaseTarget target;
    if(request.getParameter("detail").equals("View Log") ||
	request.getParameter("detail").equals("Preview Target")) {
	target = rc.getTargetStatus(request.getParameter("release"),request.getParameter("target"));
    }
    else if("View Needs Review Only".equals(request.getParameter("view"))) {
	target = rc.getTargetQAReport(request.getParameter("release"),request.getParameter("target"));
    } else {
    	target = rc.getTarget(request.getParameter("release"),request.getParameter("target"));
    }
    if(request.getParameter("detail").equals("View QAReport") && "xml".equals(request.getParameter("type"))) {
	out.println("<QAReport target=\"" + target.getName() + "\" release=\"" + release_info.getName()
	+ "\" preivous=\"" + release_info.getPreviousReleaseInfo().getName() + "\" previousMajor=\""
	+ release_info.getPreviousMajorReleaseInfo().getName() + "\" database=\"" + release_bean.getMidService()
	+ "\" directory=\"" + release_info.getReleaseUri() + "\">");
	QAReport report = target.getQAReport();
        String[] checks = report.getChecks();
        for (int i=0; i< checks.length; i++) {
        if(report.getErrorForCheck(checks[i]) == null && report.getWarningForCheck(checks[i]) == null ) {
                out.println("<Check name=\"" + checks[i] + "\" />");
        } else {
                out.println("<Check name=\"" + checks[i] + "\" >");
                if(report.getErrorForCheck(checks[i]) != null ) {
                        StringTokenizer st = new StringTokenizer(report.getErrorForCheck(checks[i]),"\n");
                        out.println("<Error name=\"" + st.nextToken() + "\">");
                        while(st.hasMoreElements()) {
                                out.println("<Value>" + st.nextToken() + "</Value>");
                        }
                        out.println("</Error>");
                } else {
                        out.println("<Warning value=\"" + report.getWarningForCheck(checks[i]) + "\" />");

                }
                out.println("</Check>");
          }

        }
	QAComparison[] qacompare = report.getGoldQAComparisons();
        out.println("<CompareTo name=\"Gold\">");
        out.println("<Differences>");
        out.println("<QAReason>");
        Arrays.sort(qacompare,new ByReasonByNameComparator());
        String reason = null;
        for(int i = 0; i< qacompare.length; i++) {
        QAReason[] reasons = qacompare[i].getReasons();
        if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAComparison name=\"" + qacompare[i].getName() + "\" value=\"" +  qacompare[i].getValue()
		+ "\" count=\"" +
                qacompare[i].getCount()  + "\" comparisoncount=\"" + qacompare[i].getComparisonCount()    + "\" />");
        reason = qacompare[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</Differences>");
        out.println("<NotInOther>");
        out.println("<QAReason>");
        QAResult[] qaresult = report.getMetaNotGold();
        Arrays.sort(qaresult,new ByReasonByNameComparator());
        reason = null;
        for(int i = 0; i< qaresult.length; i++) {
        QAReason[] reasons = qaresult[i].getReasons();
        if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAResult name=\"" + qaresult[i].getName() + "\" value=\"" + qaresult[i].getValue()
		+ "\" count=\"" +
                qaresult[i].getCount()  + "\" />");
        reason = qaresult[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</NotInOther>");
        out.println("<Missing>");
        out.println("<QAReason>");
        qaresult = report.getGoldNotMeta();
        for(int i = 0; i< qaresult.length; i++) {
        QAReason[] reasons = qaresult[i].getReasons();
        if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAResult name=\"" + qaresult[i].getName() + "\" value=\"" + qaresult[i].getValue()
		+ "\" count=\"" +
                qaresult[i].getCount()  + "\" />");
        reason = qaresult[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</Missing>");
        out.println("</CompareTo>");

	qacompare = report.getMinorPreviousQAComparisons();
        out.println("<CompareTo name=\"Previous\">");
        out.println("<Differences>");
        out.println("<QAReason>");
        Arrays.sort(qacompare,new ByReasonByNameComparator());
        reason = null;
        for(int i = 0; i< qacompare.length; i++) {
        QAReason[] reasons = qacompare[i].getReasons();
        if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAComparison name=\"" + qacompare[i].getName() + "\" value=\"" +  qacompare[i].getValue()
		+ "\" count=\"" +
                qacompare[i].getCount()  + "\" comparisoncount=\"" + qacompare[i].getComparisonCount()    + "\" />");
        reason = qacompare[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</Differences>");
        out.println("<NotInOther>");
        out.println("<QAReason>");
        qaresult = report.getMetaNotMinorPrevious();
        Arrays.sort(qaresult,new ByReasonByNameComparator());
        reason = null;
        for(int i = 0; i< qaresult.length; i++) {
        QAReason[] reasons = qaresult[i].getReasons();
        if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAResult name=\"" + qaresult[i].getName() + "\" value=\"" + qaresult[i].getValue()
		+ "\" count=\"" +
                qaresult[i].getCount()  + "\" />");
        reason = qaresult[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</NotInOther>");
        out.println("<Missing>");
        out.println("<QAReason>");
        qaresult = report.getMinorPreviousNotMeta();
        for(int i = 0; i< qaresult.length; i++) {
        QAReason[] reasons = qaresult[i].getReasons();
        if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAResult name=\"" + qaresult[i].getName() + "\" value=\"" + qaresult[i].getValue()
		+ "\" count=\"" +
                qaresult[i].getCount()  + "\" />");
        reason = qaresult[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</Missing>");
        out.println("</CompareTo>");

	qacompare = report.getMajorPreviousQAComparisons();
        out.println("<CompareTo name=\"Previous\">");
        out.println("<Differences>");
        out.println("<QAReason>");
        Arrays.sort(qacompare,new ByReasonByNameComparator());
        reason = null;
        for(int i = 0; i< qacompare.length; i++) {
        QAReason[] reasons = qacompare[i].getReasons();
        if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAComparison name=\"" + qacompare[i].getName() + "\" value=\""  + qacompare[i].getValue()
		+ "\" count=\"" +
                qacompare[i].getCount()  + "\" comparisoncount=\"" + qacompare[i].getComparisonCount()    + "\" />");
        reason = qacompare[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</Differences>");
        out.println("<NotInOther>");
        out.println("<QAReason>");
        qaresult = report.getMetaNotMajorPrevious();
        Arrays.sort(qaresult,new ByReasonByNameComparator());
        reason = null;
        for(int i = 0; i< qaresult.length; i++) {
        QAReason[] reasons = qaresult[i].getReasons();
        if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAResult name=\"" + qaresult[i].getName() + "\" value=\"" + qaresult[i].getValue()
		+ "\" count=\"" +
                qaresult[i].getCount()  + "\" />");
        reason = qaresult[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</NotInOther>");
        out.println("<Missing>");
        out.println("<QAReason>");
        qaresult = report.getMajorPreviousNotMeta();
        for(int i = 0; i< qaresult.length; i++) {
        QAReason[] reasons = qaresult[i].getReasons();
        if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
		if(reason != null) {
			out.println("</QAReason>");
			out.println("<QAReason>");
		}
                for(int j=0; j < reasons.length; j++){
                        out.println("<Reason>" + reasons[j].getReason() + "</Reason>");
                }
        }
        out.println("<QAResult name=\"" + qaresult[i].getName() + "\" value=\"" + qaresult[i].getValue()
		+ "\" count=\"" +
                qaresult[i].getCount()  + "\" />");
        reason = qaresult[i].getReasonsAsString();
        }
	out.println("</QAReason>");
        out.println("</Missing>");
        out.println("</CompareTo>");
	out.println("</QAReport>");
    } else {
%>

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
  </script>
<TITLE>
Target Details
</TITLE>
</HEAD>
<BODY>
<span id=blue><center><%=target.getName()%></center></span>
<hr WIDTH=100%>
<FORM name="form1" method="get" action="controller">
<%  if(request.getParameter("detail").equals("View Errors")) { %>
    <PRE STYLE="margin-left:  1.0cm">
    <% String[] errors = target.getErrors();
        for(int i=0; i<errors.length; i++)
          out.println(errors[i]);
    %>
    </PRE>
<% } else if(request.getParameter("detail").equals("Preview Target")) { %>


    <%= "<PRE STYLE=\"margin-left:  1.0cm\">" + rc.previewTarget(release_info.getName(),target.getName(),100) + "</PRE>" %>

<% } else if(request.getParameter("detail").equals("View Log")) {
    StageStatus[] stagestatus = rc.getTargetStatus(release_info.getName(),target.getName()).getStageStatus();
    for(int j=0; j < stagestatus.length; j++) {
	if(stagestatus[j].getName().equals(request.getParameter("stage"))) {
     	out.println("<PRE STYLE=\"margin-left:  1.0cm\">" + stagestatus[j].getLog() + "</PRE>" );
	}
    }
 } else if(request.getParameter("detail").equals("View QAResult")) { %>
	<CENTER>
	<TABLE WIDTH=90%>
      	<TR><TH colspan = "3"> QA Checks </TH></TR>
	<% QAResult[] qaresult = target.getQAResults();
        	out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
		for (int i=0; i< qaresult.length; i++) {
                    out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + (qaresult[i].getValue() == null ? "" : qaresult[i].getValue())  + "</TD><TD>" +
                            qaresult[i].getCount()  + "</TD><TD></TR>");
		}
	%>
	</TABLE>
	</CENTER>
<% } else if(request.getParameter("detail").equals("View QAReport") && "html".equals(request.getParameter("type"))){
	%>
	<CENTER>
	<TABLE WIDTH=90%>
      	<TR><TH colspan = "3"> QA Checks </TH></TR>
	<% QAReport report = target.getQAReport();
		String[] checks = report.getChecks();
		for (int i=0; i< checks.length; i++) {
                out.println("<TR> <TD width=5% >Verify </TD><TD colspan=\"2\">" + checks[i] + " </TD></TR>");
                if(report.getErrorForCheck(checks[i]) != null) {
			StringTokenizer st = new StringTokenizer(report.getErrorForCheck(checks[i]),"\n");
			out.println("<TR><TD></TD><TD id=error width=5% >Error</TD> <TD>" + st.nextToken() + "</TD></TR>");
			while(st.hasMoreElements()) {
				out.println("<TR><TD></TD><TD></TD><TD>" + st.nextToken() + "</TD></TR>");
			}
                  }

                if(report.getWarningForCheck(checks[i]) != null) {
			out.println("<TR><TD></TD><TD id=error width=5% >Warning</TD> <TD>" + report.getWarningForCheck(checks[i]) + "</TD></TR>");
                  }
		}
	%>
	</TABLE>
	<TABLE WIDTH=90%>
      	<TR><TH colspan = "6"> Report <%=target.getName() %> real-gold differences (versionless) </TH></TR>
	<TR><TD>&nbsp;</TD></TR>
        <% QAComparison[] qacompare = report.getGoldQAComparisons();
		if(qacompare.length == 0 ) {
			out.println("<TR><TD colspan=5 id=nodiff> There are no diffs between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getName() + "_gold where keys match </TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=5 id=nodiff> Differences between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getName() + "_gold " + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>"+ release_info.getName() + " Count</STRONG></TD><TD><STRONG>Gold Count</STRONG></TD><TD><STRONG>Diff</STRONG></TD><TD><STRONG>%</STRONG></TD></TR>");
			Arrays.sort(qacompare,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qacompare.length; i++) {
                        QAReason[] reasons = qacompare[i].getReasons();
			if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=5><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qacompare[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qacompare[i].getName() + "</A></TD><TD>" + qacompare[i].getValue()  + "</TD><TD>" +
				qacompare[i].getCount()  + "</TD><TD>" + qacompare[i].getComparisonCount()    + "</TD><TD>" +
				String.valueOf(qacompare[i].getCount() - qacompare[i].getComparisonCount())  + "</TD><TD>" +
                                (qacompare[i].getCount() == 0 ? "&#8734" : String.valueOf(((qacompare[i].getCount() - qacompare[i].getComparisonCount()) * 100 ) / qacompare[i].getCount())) + "</TD></TR>");
			reason = qacompare[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
            QAResult[] qaresult = report.getMetaNotGold();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getName() + "_gold</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not in qa_" + target.getName() + "_" + release_info.getName() + "_gold</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=4><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" +
				qaresult[i].getCount()  + "</TD><TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
		qaresult = report.getGoldNotMeta();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				"_gold not found in qa_" + target.getName() + "_" + release_info.getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				"_gold not in qa_" + target.getName() + "_" + release_info.getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=4><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" +
				qaresult[i].getCount()  + "</TD><TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
	%>
	</TABLE>

	<TABLE WIDTH=90%>
	<TR><TD>&nbsp;</TD></TR>
      	<TR><TH colspan = "6"> Report <%=target.getName() %> real-minor differences (versionless) </TH></TR>
        <% qacompare = report.getMinorPreviousQAComparisons();
		if(qacompare.length == 0 ) {
			out.println("<TR><TD colspan=5 id=nodiff> There are no diffs between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + " where keys match </TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=5 id=nodiff> Differences between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>"+ release_info.getName() + " Count</STRONG></TD><TD><STRONG>" + release_info.getPreviousReleaseInfo().getName()+ " Count</STRONG></TD><TD><STRONG>Diff</STRONG></TD><TD><STRONG>%</STRONG></TD></TR>");
			Arrays.sort(qacompare,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qacompare.length; i++) {
                        QAReason[] reasons = qacompare[i].getReasons();
			if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=5><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qacompare[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qacompare[i].getName() + "</A></TD><TD>" + qacompare[i].getValue()  + "</TD><TD>" +
				qacompare[i].getCount()  + "</TD><TD>" + qacompare[i].getComparisonCount()    + "</TD><TD>" +
				String.valueOf(qacompare[i].getCount() - qacompare[i].getComparisonCount())  + "</TD><TD>" +
                                (qacompare[i].getCount() == 0 ? "&#8734" : String.valueOf(((qacompare[i].getCount() - qacompare[i].getComparisonCount()) * 100 ) / qacompare[i].getCount())) + "</TD></TR>");
			reason = qacompare[i].getReasonsAsString();
			}
		}
            qaresult = report.getMetaNotMinorPrevious();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=4><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" +
				qaresult[i].getCount()  + "</TD><TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
		qaresult = report.getMinorPreviousNotMeta();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() +
				" not in qa_" + target.getName() + "_" + release_info.getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=4><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" +
				qaresult[i].getCount()  + "</TD><TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
	%>
	</TABLE>
	<TABLE WIDTH=90%>
	<TR><TD>&nbsp;</TD></TR>
      	<TR><TH colspan = "6"> Report <%=target.getName() %> real-major differences (versionless) </TH></TR>
        <% if( !release_info.getPreviousReleaseInfo().getName().equals(release_info.getPreviousMajorReleaseInfo().getName())) {
		qacompare = report.getMajorPreviousQAComparisons();
		if(qacompare.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no diffs between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + " where keys match </TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=5 id=nodiff> Differences between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>"+ release_info.getName() + " Count</STRONG></TD><TD><STRONG>" + release_info.getPreviousMajorReleaseInfo().getName()+ " Count</STRONG></TD><TD><STRONG>Diff</STRONG></TD><TD><STRONG>%</STRONG></TD></TR>");
			Arrays.sort(qacompare,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qacompare.length; i++) {
                        QAReason[] reasons = qacompare[i].getReasons();
			if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=5><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qacompare[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qacompare[i].getName() + "</A></TD><TD>" + qacompare[i].getValue()  + "</TD><TD>" +
				qacompare[i].getCount()  + "</TD><TD>" + qacompare[i].getComparisonCount()    + "</TD><TD>" +
				String.valueOf(qacompare[i].getCount() - qacompare[i].getComparisonCount())  + "</TD></TR>");
			reason = qacompare[i].getReasonsAsString();
			}
		}
            qaresult = report.getMetaNotMajorPrevious();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in " + target.getName() + "_" + release_info.getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=4><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" +
				qaresult[i].getCount()  + "</TD><TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
		qaresult = report.getMajorPreviousNotMeta();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() +
				" not in qa_" + target.getName() + "_" + release_info.getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
                        	for(int j=0; j < reasons.length; j++){
                        		out.println("<TR><TD colspan=4><STRONG>" + reasons[j].getReason() + "</STRONG></TD></TR>");
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
			out.println("<TR align=\"right\"><TD><A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" > " + qaresult[i].getName() + "</A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" +
				qaresult[i].getCount()  + "</TD><TD></TR>");
			}
		}
		out.println("<TR></TR>");
	%>
	</TABLE>
	</CENTER>
<% }
} else if(request.getParameter("detail").equals("Edit QAReport")) {
	String type = "";
		if(request.getParameter("view") == null || "View All".equals(request.getParameter("view"))) {
			type = "Needs Review Only";
		} else if("View Needs Review Only".equals(request.getParameter("view"))) {
			type = "All";
		}
%>
<input type="hidden" name="state" value="TargetDetails">
<input type="hidden" name="release" value="<%= release_info.getName() %>">
<input type="hidden" name="target" value="<%= target.getName() %>">
<input type="hidden" name="detail" value="Edit QAReport">
        <center><input type="submit" name="view" value="<%= "View " + type %>"></center>
	<TABLE WIDTH=90%>
      	<TR><TH colspan = "5"> QA Checks </TH></TR>
	<TR><TD>&nbsp;</TD></TR>
        <% QAReport report = target.getQAReport();;
           if("View Needs Review Only".equals(request.getParameter("view"))) {
		report = report.getNeedsReviewReport();
           }
		String[] checks = report.getChecks();
		for (int i=0; i< checks.length; i++) {
                out.println("<TR> <TD width=5% >Verify </TD><TD colspan=\"2\">" + checks[i] + " </TD></TR>");
                if(report.getErrorForCheck(checks[i]) != null) {
			StringTokenizer st = new StringTokenizer(report.getErrorForCheck(checks[i]),"\n");
			out.println("<TR><TD></TD><TD id=error width=5% >Error</TD> <TD>" + st.nextToken() + "</TD></TR>");
			while(st.hasMoreElements()) {
				out.println("<TR><TD></TD><TD></TD><TD>" + st.nextToken() + "</TD></TR>");
			}
                  }

                if(report.getWarningForCheck(checks[i]) != null) {
			out.println("<TR><TD></TD><TD id=error width=5% >Warning</TD> <TD>" + report.getWarningForCheck(checks[i]) + "</TD></TR>");
                  }
		}
	%>
	</TABLE>
	<TABLE WIDTH=90%>
      	<TR><TH colspan = "6"> Report <%=target.getName() %> real-gold differences (versionless) </TH></TR>
	<TR><TD>&nbsp;</TD></TR>
	<%
	   QAComparison[] qacompare = report.getGoldQAComparisons();
		if(qacompare.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no diffs between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getName() + "_gold where keys match </TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=5 id=nodiff> Differences between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getName() + "_gold " + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>"+ release_info.getName() + " Count</STRONG></TD><TD><STRONG>Gold Count</STRONG></TD><TD><STRONG>Diff</STRONG></TD><TD><STRONG>%</STRONG></TD></TR>");
			Arrays.sort(qacompare,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qacompare.length; i++) {
                        QAReason[] reasons = qacompare[i].getReasons();
			if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=5><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
					QAComparisonReason qacompare_reason = (QAComparisonReason) reasons[j];
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=5><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qacompare_reason.getName(),"UTF-8"));
                                        sb.append("&test_value=" + (qacompare_reason.getValue() != null ? URLEncoder.encode(qacompare_reason.getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (qacompare_reason.getCountOperator() != null ? String.valueOf(qacompare_reason.getCount()) : ""))
					.append("&test_count_2=" + (qacompare_reason.getComparisonCountOperator() != null ? String.valueOf(qacompare_reason.getComparisonCount()) : ""))
					.append("&diff_count=" + (qacompare_reason.getDiffCountOperator() != null ? String.valueOf(qacompare_reason.getDiffCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(qacompare_reason.getReason(),"UTF-8"))
					.append("&qareason=compare")
					.append("&name_operator=" + (qacompare_reason.getNameOperator() != null ? URLEncoder.encode(qacompare_reason.getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (qacompare_reason.getValueOperator() != null ? URLEncoder.encode(qacompare_reason.getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (qacompare_reason.getCountOperator() != null ? URLEncoder.encode(qacompare_reason.getCountOperator(),"UTF-8") : ""))
					.append("&comparisoncount_operator=" + (qacompare_reason.getComparisonCountOperator() != null ? URLEncoder.encode(qacompare_reason.getComparisonCountOperator(),"UTF-8") : ""))
					.append("&diffcount_operator=" + (qacompare_reason.getDiffCountOperator() != null ? URLEncoder.encode(qacompare_reason.getDiffCountOperator(),"UTF-8") : ""))
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + qacompare_reason.getReleaseName())
					.append("&comparison_name=" + qacompare_reason.getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + qacompare_reason.getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qacompare[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qacompare[i].getValue() != null ? URLEncoder.encode(qacompare[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qacompare[i].getCount())
					.append("&test_count_2=" + qacompare[i].getComparisonCount())
					.append("&diff_count=" + (qacompare[i].getCount() - qacompare[i].getComparisonCount()))
					.append("&qareason=compare")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName())
					.append("&release_name=Current")
					.append("&comparison_name=" + release_info.getName() + "_gold")
					.append("&comparison_name=Gold")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qacompare[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qacompare[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qacompare[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qacompare[i].getCount()  + "</TD><TD>" + qacompare[i].getComparisonCount()    + "</TD><TD>" +
				String.valueOf(qacompare[i].getCount() - qacompare[i].getComparisonCount())  + "</TD><TD>" +
                                (qacompare[i].getCount() == 0 ? "&#8734" : String.valueOf(((qacompare[i].getCount() - qacompare[i].getComparisonCount()) * 100 ) / qacompare[i].getCount())) + "</TD></TR>");
			reason = qacompare[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
            QAResult[] qaresult = report.getMetaNotGold();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getName() + "_gold</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not in qa_" + target.getName() + "_" + release_info.getName() + "_gold</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=4><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                                        sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
					.append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
					.append("&qareason=result")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + reasons[j].getReleaseName())
					.append("&comparison_name=" + reasons[j].getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult[i].getValue() != null ? URLEncoder.encode(qaresult[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult[i].getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName() )
					.append("&release_name=Current")
					.append("&comparison_name=" + release_info.getName() + "_gold")
					.append("&comparison_name=Gold")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qaresult[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qaresult[i].getCount()  + "</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
		qaresult = report.getGoldNotMeta();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				"_gold not found in qa_" + target.getName() + "_" + release_info.getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				"_gold not in qa_" + target.getName() + "_" + release_info.getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=4><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                                        sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
					.append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
					.append("&qareason=result")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + reasons[j].getReleaseName())
					.append("&comparison_name=" + reasons[j].getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult[i].getValue() != null ? URLEncoder.encode(qaresult[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult[i].getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName() + "_gold")
					.append("&release_name=Gold")
					.append("&comparison_name=" + release_info.getName())
					.append("&comparison_name=Current")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qaresult[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qaresult[i].getCount()  + "</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
	%>
	</TABLE>

	<TABLE WIDTH=90%>
	<TR><TD>&nbsp;</TD></TR>
      	<TR><TH colspan = "6"> Report <%=target.getName() %> real-minor differences (versionless) </TH></TR>
        <% qacompare = report.getMinorPreviousQAComparisons();
		if(qacompare.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no diffs between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + " where keys match </TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=5 id=nodiff> Differences between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>"+ release_info.getName() + " Count</STRONG></TD><TD><STRONG>" + release_info.getPreviousReleaseInfo().getName()+ " Count</STRONG></TD><TD><STRONG>Diff</STRONG></TD><TD><STRONG>%</STRONG></TD></TR>");
			Arrays.sort(qacompare,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qacompare.length; i++) {
                        QAReason[] reasons = qacompare[i].getReasons();
			if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=5><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
					QAComparisonReason qacompare_reason = (QAComparisonReason) reasons[j];
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qacompare_reason.getName(),"UTF-8"));
                                        sb.append("&test_value=" + (qacompare_reason.getValue() != null ? URLEncoder.encode(qacompare_reason.getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (qacompare_reason.getCountOperator() != null ? String.valueOf(qacompare_reason.getCount()) : ""))
					.append("&test_count_2=" + (qacompare_reason.getComparisonCountOperator() != null ? String.valueOf(qacompare_reason.getComparisonCount()) : ""))
					.append("&diff_count=" + (qacompare_reason.getDiffCountOperator() != null ? String.valueOf(qacompare_reason.getDiffCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(qacompare_reason.getReason(),"UTF-8"))
					.append("&name_operator=" + (qacompare_reason.getNameOperator() != null ? URLEncoder.encode(qacompare_reason.getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (qacompare_reason.getValueOperator() != null ? URLEncoder.encode(qacompare_reason.getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (qacompare_reason.getCountOperator() != null ? URLEncoder.encode(qacompare_reason.getCountOperator(),"UTF-8") : ""))
					.append("&comparisoncount_operator=" + (qacompare_reason.getComparisonCountOperator() != null ? URLEncoder.encode(qacompare_reason.getComparisonCountOperator(),"UTF-8") : ""))
					.append("&diffcount_operator=" + (qacompare_reason.getDiffCountOperator() != null ? URLEncoder.encode(qacompare_reason.getDiffCountOperator(),"UTF-8") : ""))
					.append("&qareason=compare")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + qacompare_reason.getReleaseName())
					.append("&comparison_name=" + qacompare_reason.getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + qacompare_reason.getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qacompare[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qacompare[i].getValue() != null ? URLEncoder.encode(qacompare[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qacompare[i].getCount())
					.append("&test_count_2=" + qacompare[i].getComparisonCount())
					.append("&diff_count=" + (qacompare[i].getCount() - qacompare[i].getComparisonCount()))
					.append("&qareason=compare")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName())
					.append("&release_name=Current")
					.append("&comparison_name=" + release_info.getPreviousReleaseInfo().getName())
					.append("&comparison_name=Previous")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qacompare[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qacompare[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qacompare[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qacompare[i].getCount()  + "</TD><TD>" + qacompare[i].getComparisonCount()    + "</TD><TD>" +
				String.valueOf(qacompare[i].getCount() - qacompare[i].getComparisonCount())  + "</TD><TD>" +
                                (qacompare[i].getCount() == 0 ? "&#8734" : String.valueOf(((qacompare[i].getCount() - qacompare[i].getComparisonCount()) * 100 ) / qacompare[i].getCount())) + "</TD></TR>");
			reason = qacompare[i].getReasonsAsString();
			}
		}
            qaresult = report.getMetaNotMinorPrevious();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=4><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                                        sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
					.append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
					.append("&qareason=result")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + reasons[j].getReleaseName())
					.append("&comparison_name=" + reasons[j].getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult[i].getValue() != null ? URLEncoder.encode(qaresult[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult[i].getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName())
					.append("&release_name=Current")
					.append("&comparison_name=" + release_info.getPreviousReleaseInfo().getName())
					.append("&comparison_name=Previous")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qaresult[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qaresult[i].getCount()  + "</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
		qaresult = report.getMinorPreviousNotMeta();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getPreviousReleaseInfo().getName() +
				" not in qa_" + target.getName() + "_" + release_info.getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=4><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                                        sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
					.append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
					.append("&qareason=result")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + reasons[j].getReleaseName())
					.append("&comparison_name=" + reasons[j].getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult[i].getValue() != null ? URLEncoder.encode(qaresult[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult[i].getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getPreviousReleaseInfo().getName())
					.append("&release_name=Previous")
					.append("&comparison_name=" + release_info.getName())
					.append("&comparison_name=Current")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qaresult[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qaresult[i].getCount()  + "</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
	%>
	</TABLE>
	<TABLE WIDTH=90%>
	<TR><TD>&nbsp;</TD></TR>
      	<TR><TH colspan = "6"> Report <%=target.getName() %> real-major differences (versionless) </TH></TR>
        <% if( !release_info.getPreviousReleaseInfo().getName().equals(release_info.getPreviousMajorReleaseInfo().getName())) {
		qacompare = report.getMajorPreviousQAComparisons();
		if(qacompare.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no diffs between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + " where keys match </TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=5 id=nodiff> Differences between qa_" + target.getName() + "_" + release_info.getName() +
				" and qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>"+ release_info.getName() + " Count</STRONG></TD><TD><STRONG>" + release_info.getPreviousMajorReleaseInfo().getName()+ " Count</STRONG></TD><TD><STRONG>Diff</STRONG></TD><TD><STRONG>%</STRONG></TD></TR>");
			Arrays.sort(qacompare,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qacompare.length; i++) {
                        QAReason[] reasons = qacompare[i].getReasons();
			if(reason == null || !reason.equals(qacompare[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=5><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
					QAComparisonReason qacompare_reason = (QAComparisonReason) reasons[j];
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=5><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qacompare_reason.getName(),"UTF-8"));
                                        sb.append("&test_value=" + (qacompare_reason.getValue() != null ? URLEncoder.encode(qacompare_reason.getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (qacompare_reason.getCountOperator() != null ? String.valueOf(qacompare_reason.getCount()) : ""))
					.append("&test_count_2=" + (qacompare_reason.getComparisonCountOperator() != null ? String.valueOf(qacompare_reason.getComparisonCount()) : ""))
					.append("&diff_count=" + (qacompare_reason.getDiffCountOperator() != null ? String.valueOf(qacompare_reason.getDiffCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(qacompare_reason.getReason(),"UTF-8"))
					.append("&name_operator=" + (qacompare_reason.getNameOperator() != null ? URLEncoder.encode(qacompare_reason.getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (qacompare_reason.getValueOperator() != null ? URLEncoder.encode(qacompare_reason.getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (qacompare_reason.getCountOperator() != null ? URLEncoder.encode(qacompare_reason.getCountOperator(),"UTF-8") : ""))
					.append("&comparisoncount_operator=" + (qacompare_reason.getComparisonCountOperator() != null ? URLEncoder.encode(qacompare_reason.getComparisonCountOperator(),"UTF-8") : ""))
					.append("&diffcount_operator=" + (qacompare_reason.getDiffCountOperator() != null ? URLEncoder.encode(qacompare_reason.getDiffCountOperator(),"UTF-8") : ""))
					.append("&qareason=compare")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + qacompare_reason.getReleaseName())
					.append("&comparison_name=" + qacompare_reason.getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + qacompare_reason.getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qacompare[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qacompare[i].getValue() != null ? URLEncoder.encode(qacompare[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qacompare[i].getCount())
					.append("&test_count_2=" + qacompare[i].getComparisonCount())
					.append("&diff_count=" + (qacompare[i].getCount() - qacompare[i].getComparisonCount()))
					.append("&qareason=compare")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName())
					.append("&release_name=Current")
					.append("&comparison_name=" + release_info.getPreviousMajorReleaseInfo().getName())
					.append("&comparison_name=PreviousMajor")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qacompare[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qacompare[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qacompare[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qacompare[i].getCount()  + "</TD><TD>" + qacompare[i].getComparisonCount()    + "</TD><TD>" +
				String.valueOf(qacompare[i].getCount() - qacompare[i].getComparisonCount())  + "</TD><TD>" +
                                (qacompare[i].getCount() == 0 ? "&#8734" : String.valueOf(((qacompare[i].getCount() - qacompare[i].getComparisonCount()) * 100 ) / qacompare[i].getCount())) + "</TD></TR>");
			reason = qacompare[i].getReasonsAsString();
			}
		}
            qaresult = report.getMetaNotMajorPrevious();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getName() +
				" not in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=4><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                                        sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
					.append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
					.append("&qareason=result")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + reasons[j].getReleaseName())
					.append("&comparison_name=" + reasons[j].getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult[i].getValue() != null ? URLEncoder.encode(qaresult[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult[i].getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName())
					.append("&release_name=Current")
					.append("&comparison_name=" + release_info.getPreviousMajorReleaseInfo().getName())
					.append("&comparison_name=PreviousMajor")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qaresult[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qaresult[i].getCount()  + "</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
		}
		out.println("<TR></TR>");
		qaresult = report.getMajorPreviousNotMeta();
		if(qaresult.length == 0 ) {
			out.println("<TR><TD colspan=4 id=nodiff> There are no rows in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() +
				" not found in qa_" + target.getName() + "_" + release_info.getName() + "</TD> </TR>");
		}else {
                        out.println("<TR><TD>&nbsp;</TD></TR>");
			out.println("<TR><TD colspan=4 id=nodiff> Rows in qa_" + target.getName() + "_" + release_info.getPreviousMajorReleaseInfo().getName() +
				" not in qa_" + target.getName() + "_" + release_info.getName() + "</TD></TR>");
			out.println("<TR align=\"right\"><TD><STRONG>Test</STRONG></TD><TD><STRONG>Value</STRONG></TD><TD><STRONG>Count</STRONG></TD></TR>");
			Arrays.sort(qaresult,new ByReasonByNameComparator());
			String reason = null;
			for(int i = 0; i< qaresult.length; i++) {
                        QAReason[] reasons = qaresult[i].getReasons();
			if(reason == null || !reason.equals(qaresult[i].getReasonsAsString())) {
                        	out.println("<TR><TD>&nbsp;</TD></TR>");
				if(reasons.length == 0) {
                                	out.println("<TR><TD colspan=4><STRONG>no reason</STRONG></TD></TR>");
					reason = "no reason";
				}
                        	for(int j=0; j < reasons.length; j++){
                                        StringBuffer sb = new StringBuffer();
					sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                                        sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
					.append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
					.append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
					.append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
					.append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
					.append("&qareason=result")
					.append("&action=Update")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + reasons[j].getReleaseName())
					.append("&comparison_name=" + reasons[j].getComparisonName())
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
					out.println(sb.toString());
                        	}
                                out.println("<TR><TD>&nbsp;</TD></TR>");
			reason = qaresult[i].getReasonsAsString();
			}
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"right\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult[i].getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult[i].getValue() != null ? URLEncoder.encode(qaresult[i].getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult[i].getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getPreviousMajorReleaseInfo().getName())
					.append("&release_name=PreviousMajor")
					.append("&comparison_name=" + release_info.getName())
					.append("&comparison_name=Current")
                                        .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" > ")
					.append(qaresult[i].getName() + "</A> <A HREF=\"/webapps-meme/meme/controller?state=ViewCodeDescription&category=qa_test_name&code=" + qaresult[i].getName() + "\" onclick=\"launchCenter(this.href, 'TestNameDescription', 200,480);return false;\" ><img class=\"clearrow\" border=\"0\" src=\"../img/help.gif\" alt=\"description\" title=\"test name description\"></A></TD><TD>" + qaresult[i].getValue()  + "</TD><TD>" );
				out.print(sb.toString());
				out.println(qaresult[i].getCount()  + "</TD></TR>");
			}
		}
		out.println("<TR></TR>");
        }
	%>
	</TABLE>
	<TABLE WIDTH=90%>
      	<TR><TH colspan = "5"> Unattached QAReasons </TH></TR>
	<TR><TD>&nbsp;</TD></TR>
	<%
	QAReason[] reasons = report.getUnusedReasons();
        for(int j=0; j < reasons.length; j++){
                StringBuffer sb = new StringBuffer();
		String qareason = "result";
                sb.append("<TR><TD colspan=4><STRONG><A HREF=\"controller?state=QAReasonForm")
                .append("&test_name=" + URLEncoder.encode(reasons[j].getName(),"UTF-8"));
                sb.append("&test_value=" + (reasons[j].getValue() != null ? URLEncoder.encode(reasons[j].getValue(),"UTF-8") : ""));
                sb.append("&test_count=" + (reasons[j].getCountOperator() != null ? String.valueOf(reasons[j].getCount()) : ""))
                .append("&reason=" +  URLEncoder.encode(reasons[j].getReason(),"UTF-8"))
                .append("&name_operator=" + (reasons[j].getNameOperator() != null ? URLEncoder.encode(reasons[j].getNameOperator(),"UTF-8") : ""))
                .append("&value_operator=" + (reasons[j].getValueOperator() != null ? URLEncoder.encode(reasons[j].getValueOperator(),"UTF-8") : ""))
                .append("&count_operator=" + (reasons[j].getCountOperator() != null ? URLEncoder.encode(reasons[j].getCountOperator(),"UTF-8") : ""))
                .append("&action=Update")
                .append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
                .append("&release_name=" + reasons[j].getReleaseName())
                .append("&comparison_name=" + reasons[j].getComparisonName());
		if(reasons[j] instanceof QAComparisonReason){
                	QAComparisonReason qacompare_reason = (QAComparisonReason) reasons[j];
                	qareason="compare";
                	sb.append("&test_count_2=" + (qacompare_reason.getComparisonCountOperator() != null ? String.valueOf(qacompare_reason.getComparisonCount()) : ""))
                	.append("&diff_count=" + (qacompare_reason.getDiffCountOperator() != null ? String.valueOf(qacompare_reason.getDiffCount()) : ""))
                	.append("&comparisoncount_operator=" + (qacompare_reason.getComparisonCountOperator() != null ? URLEncoder.encode(qacompare_reason.getComparisonCountOperator(),"UTF-8") : ""))
                	.append("&diffcount_operator=" + (qacompare_reason.getDiffCountOperator() != null ? URLEncoder.encode(qacompare_reason.getDiffCountOperator(),"UTF-8") : ""));
		}
		sb.append("&qareason=" + qareason)
                .append("&target=" + target.getName() + "\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reasons[j].getReason() + "</A></STRONG></TD></TR>");
                out.println(sb.toString());
        }
        out.println("<TR><TD>&nbsp;</TD></TR>");
      %>
  </TABLE>

<% } %>
<TABLE WIDTH="90%" >
<TR>
    <TD align="center">
	<% String type = "";
           if(request.getParameter("detail").equals("Edit QAReport")) {
		if(request.getParameter("view") == null || "View All".equals(request.getParameter("view"))) {
			type = "Needs Review Only";
		} else if("View Needs Review Only".equals(request.getParameter("view"))) {
			type = "All";
		}
              %>
        <input type="submit" name="view" value="<%= "View " + type %>">
	<% } %>
      <input type="button" value="Close" onClick="window.close(); return true">
    </TD>
</TR>
  </TABLE>
</FORM>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>

</HTML>

<% } %>

