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
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>
<%@ page import="gov.nih.nlm.mrd.common.ByReasonByNameComparator" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.io.PrintWriter" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>


<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
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
Cui Report Details
</TITLE>
</HEAD>
<BODY>
<%  ReleaseClient rc = release_bean.getReleaseClient();
    ReleaseInfo release_info = rc.getReleaseInfo(request.getParameter("release"));
    QAReport report = rc.getCuiComparisonReport(request.getParameter("cui"),release_info.getName());
%>
<span id=blue><center><%=request.getParameter("cui")%></center></span>
<hr WIDTH=100%>
<FORM name="form1" method="get" action="controller">
<% if(request.getParameter("detail").equals("View Report")) { %>
	<TABLE WIDTH=90%>
	<TR><TD>&nbsp;</TD></TR>
      	<TH> Report <%=report.getName() %> <%=report.getReleaseName() + "-" + report.getPreviousReleaseName()%> differences</TH>
        <%
		QAResult[] qaresults = report.getMetaNotMinorPrevious();
		Hashtable notInOther = new Hashtable();
                for(int i = 0; i< qaresults.length; i++) {
                   QAReason[] reasons = qaresults[i].getReasons();
                   if(reasons.length == 0) {
			QAReason reason = new QAResultReason();
			reason.setReason("no reason");
			ArrayList list = new ArrayList();
                        if(notInOther.containsKey(reason)) {
                            list = (ArrayList)notInOther.get(reason);
                        }
                        list.add(qaresults[i]);
			notInOther.put(reason,list);

                   } else {
                     for(int j=0; j < reasons.length; j++) {
			ArrayList list = new ArrayList();
                        if(notInOther.containsKey(reasons[j])) {
                            list = (ArrayList)notInOther.get(reasons[j]);
                        }
                        list.add(qaresults[i]);
			notInOther.put(reasons[j],list);
                     }
                   }
                }
		qaresults = report.getMinorPreviousNotMeta();
		Hashtable missing = new Hashtable();
                for(int i = 0; i< qaresults.length; i++) {
                   QAReason[] reasons = qaresults[i].getReasons();
                   if(reasons.length == 0) {
			QAReason reason = new QAResultReason();
			reason.setReason("no reason");
			ArrayList list = new ArrayList();
                        if(missing.containsKey(reason)) {
                            list = (ArrayList)missing.get(reason);
                        }
                        list.add(qaresults[i]);
			missing.put(reason,list);

                   } else {
                     for(int j=0; j < reasons.length; j++) {
			ArrayList list = new ArrayList();
                        if(missing.containsKey(reasons[j])) {
                            list = (ArrayList)missing.get(reasons[j]);
                        }
                        list.add(qaresults[i]);
			missing.put(reasons[j],list);
                     }
                   }
                }
		for(Enumeration e = notInOther.keys(); e.hasMoreElements();) {
                    QAReason reason = (QAReason)e.nextElement();
                    out.println("<TR><TD>&nbsp;</TD></TR>");
                    out.println("<TR><TD><STRONG>" + reason.getReason() + "</STRONG></TD></TR>");
                    ArrayList list = (ArrayList) notInOther.get(reason);
                    for(int i=0; i < list.size(); i++) {
			out.println("<TR align=\"left\"><TD>&lt;" + ((QAResult)list.get(i)).getValue()  + "</TD></TR>");
                    }
                    if(missing.containsKey(reason)) {
                    	out.println("<TR><TD>---</TD></TR>");
                    	list = (ArrayList) missing.remove(reason);
                    	for(int i=0; i < list.size(); i++) {
				out.println("<TR align=\"left\"><TD>&gt;" + ((QAResult)list.get(i)).getValue()  + "</TD></TR>");
                    	}
                    }
		}
		for(Enumeration e = missing.keys(); e.hasMoreElements();) {
                    QAReason reason = (QAReason)e.nextElement();
                    out.println("<TR><TD>&nbsp;</TD></TR>");
                    out.println("<TR><TD><STRONG>" + reason.getReason() + "</STRONG></TD></TR>");
                    ArrayList list = (ArrayList) missing.get(reason);
                    for(int i=0; i < list.size(); i++) {
			out.println("<TR align=\"left\"><TD>&gt;" + ((QAResult)list.get(i)).getValue()  + "</TD></TR>");
                    }
		}
		out.println("<TR><TD>&nbsp;</TD></TR>");
	%>
	</TABLE>
<% } else if(request.getParameter("detail").equals("Edit Report")) {
	String type = "";
		if(request.getParameter("view") == null || "View All".equals(request.getParameter("view"))) {
			type = "Needs Review Only";
		} else if("View Needs Review Only".equals(request.getParameter("view"))) {
			type = "All";
		}
%>
<input type="hidden" name="state" value="CuiReportDetails">
<input type="hidden" name="release" value="<%= release_info.getName() %>">
<input type="hidden" name="cui" value="<%= report.getName() %>">
<input type="hidden" name="detail" value="Edit Report">
        <center><input type="submit" name="view" value="<%= "View " + type %>"></center>
	<TABLE WIDTH=90%>
	<TR><TD>&nbsp;</TD></TR>
        <%
           	if("View Needs Review Only".equals(request.getParameter("view"))) {
			report = report.getNeedsReviewReport();
           	}
    		String compareTo = report.getPreviousReleaseName();
		QAResult[] qaresults = report.getMetaNotMinorPrevious();
		Hashtable notInOther = new Hashtable();
                for(int i = 0; i< qaresults.length; i++) {
                   QAReason[] reasons = qaresults[i].getReasons();
                   if(reasons.length == 0) {
			QAReason reason = new QAResultReason();
			reason.setReason("no reason");
			ArrayList list = new ArrayList();
                        if(notInOther.containsKey(reason)) {
                            list = (ArrayList)notInOther.get(reason);
                        }
                        list.add(qaresults[i]);
			notInOther.put(reason,list);

                   } else {
                     for(int j=0; j < reasons.length; j++) {
			ArrayList list = new ArrayList();
                        if(notInOther.containsKey(reasons[j])) {
                            list = (ArrayList)notInOther.get(reasons[j]);
                        }
                        list.add(qaresults[i]);
			notInOther.put(reasons[j],list);
                     }
                   }
                }
		qaresults = report.getMinorPreviousNotMeta();
		Hashtable missing = new Hashtable();
                for(int i = 0; i< qaresults.length; i++) {
                   QAReason[] reasons = qaresults[i].getReasons();
                   if(reasons.length == 0) {
			QAReason reason = new QAResultReason();
			reason.setReason("no reason");
			ArrayList list = new ArrayList();
                        if(missing.containsKey(reason)) {
                            list = (ArrayList)missing.get(reason);
                        }
                        list.add(qaresults[i]);
			missing.put(reason,list);

                   } else {
                     for(int j=0; j < reasons.length; j++) {
			ArrayList list = new ArrayList();
                        if(missing.containsKey(reasons[j])) {
                            list = (ArrayList)missing.get(reasons[j]);
                        }
                        list.add(qaresults[i]);
			missing.put(reasons[j],list);
                     }
                   }
                }
		for(Enumeration e = notInOther.keys(); e.hasMoreElements();) {
                    QAReason reason = (QAReason)e.nextElement();
                    out.println("<TR><TD>&nbsp;</TD></TR>");
		    if(reason.getReason().equals("no reason")) {
                    	out.println("<TR><TD><STRONG>" + reason.getReason() + "</STRONG></TD></TR>");
		    } else {
                  	StringBuffer sb = new StringBuffer();
                  	sb.append("<TR><TD><STRONG><A HREF=\"controller?state=QAReasonForm")
                  	.append("&test_name=" + URLEncoder.encode(reason.getName(),"UTF-8"));
                  	sb.append("&test_value=" + (reason.getValue() != null ? URLEncoder.encode(reason.getValue(),"UTF-8") : ""));
                        sb.append("&test_count=" + (reason.getCountOperator() != null ? String.valueOf(reason.getCount()) : ""))
                  	.append("&reason=" +  URLEncoder.encode(reason.getReason(),"UTF-8"))
                  	.append("&name_operator=" + (reason.getNameOperator() != null ? URLEncoder.encode(reason.getNameOperator(),"UTF-8") : ""))
                  	.append("&value_operator=" + (reason.getValueOperator() != null ? URLEncoder.encode(reason.getValueOperator(),"UTF-8") : ""))
                  	.append("&count_operator=" + (reason.getCountOperator() != null ? URLEncoder.encode(reason.getCountOperator(),"UTF-8") : ""))
                  	.append("&qareason=result")
                  	.append("&action=Update")
                  	.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
                  	.append("&release_name=" + reason.getReleaseName())
                  	.append("&comparison_name=" + reason.getComparisonName())
                  	.append("&target=CUI\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reason.getReason() + "</A></STRONG></TD></TR>");
                  	out.println(sb.toString());
		    }
                    ArrayList list = (ArrayList) notInOther.get(reason);
                    for(int i=0; i < list.size(); i++) {
			QAResult qaresult = (QAResult)list.get(i);
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"left\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult.getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult.getValue() != null ? URLEncoder.encode(qaresult.getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult.getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + release_info.getName())
					.append("&release_name=Current")
					.append("&comparison_name=" + compareTo)
					.append("&comparison_name=Previous")
                                        .append("&target=CUI\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" >&lt;")
					.append(qaresult.getValue()  + "</A></TD></TR>" );
                    	out.print(sb.toString());
                    }
                    if(missing.containsKey(reason)) {
                    	out.println("<TR><TD>---</TD></TR>");
                    	list = (ArrayList) missing.remove(reason);
                    	for(int i=0; i < list.size(); i++) {
				QAResult qaresult = (QAResult)list.get(i);
                        	StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"left\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult.getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult.getValue() != null ? URLEncoder.encode(qaresult.getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult.getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + compareTo)
					.append("&release_name=Previous")
					.append("&comparison_name=" + release_info.getName())
					.append("&comparison_name=Current")
                                        .append("&target=CUI\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" >&gt;")
					.append(qaresult.getValue()  + "</A></TD></TR>" );
                    		out.print(sb.toString());
                    	}
                    }
		}
		for(Enumeration e = missing.keys(); e.hasMoreElements();) {
                    QAReason reason = (QAReason)e.nextElement();
                    out.println("<TR><TD>&nbsp;</TD></TR>");
		    if(reason.getReason().equals("no reason")) {
                    	out.println("<TR><TD><STRONG>" + reason.getReason() + "</STRONG></TD></TR>");
		    } else {
                  	StringBuffer sb = new StringBuffer();
                  	sb.append("<TR><TD><STRONG><A HREF=\"controller?state=QAReasonForm")
                  	.append("&test_name=" + URLEncoder.encode(reason.getName(),"UTF-8"));
                  	sb.append("&test_value=" + (reason.getValue() != null ? URLEncoder.encode(reason.getValue(),"UTF-8") : ""));
                        sb.append("&test_count=" + (reason.getCountOperator() != null ? String.valueOf(reason.getCount()) : ""))
                  	.append("&reason=" +  URLEncoder.encode(reason.getReason(),"UTF-8"))
                  	.append("&name_operator=" + (reason.getNameOperator() != null ? URLEncoder.encode(reason.getNameOperator(),"UTF-8") : ""))
                  	.append("&value_operator=" + (reason.getValueOperator() != null ? URLEncoder.encode(reason.getValueOperator(),"UTF-8") : ""))
                  	.append("&count_operator=" + (reason.getCountOperator() != null ? URLEncoder.encode(reason.getCountOperator(),"UTF-8") : ""))
                  	.append("&qareason=result")
                  	.append("&action=Update")
                  	.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
                  	.append("&release_name=" + reason.getReleaseName())
                  	.append("&comparison_name=" + reason.getComparisonName())
                  	.append("&target=CUI\" onclick=\"launchCenter(this.href, 'EditQAReason', 500,480);return false;\" > " + reason.getReason() + "</A></STRONG></TD></TR>");
                  	out.println(sb.toString());
		    }
                    ArrayList list = (ArrayList) missing.get(reason);
                    for(int i=0; i < list.size(); i++) {
			QAResult qaresult = (QAResult)list.get(i);
                        StringBuffer sb = new StringBuffer();
			sb.append("<TR align=\"left\"><TD><A HREF=\"controller?state=QAReasonForm")
					.append("&test_name=" + URLEncoder.encode(qaresult.getName(), "UTF-8"));
                                        sb.append("&test_value=" + (qaresult.getValue() != null ? URLEncoder.encode(qaresult.getValue(),"UTF-8") : ""));
					sb.append("&test_count=" + qaresult.getCount())
					.append("&qareason=result")
					.append("&action=Add")
        				.append("&midservice=" + request.getParameter("midService") + "&release=" + release_info.getName())
					.append("&release_name=" + compareTo)
					.append("&release_name=Previous")
					.append("&comparison_name=" + release_info.getName())
					.append("&comparison_name=Current")
                                        .append("&target=CUI\" onclick=\"launchCenter(this.href, 'AddQAReason', 500,480);return false;\" >&gt;")
					.append(qaresult.getValue()  + "</A></TD></TR>" );
                    	out.print(sb.toString());
                    }
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
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</HTML>

