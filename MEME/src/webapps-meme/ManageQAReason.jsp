<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.PatternSyntaxException" %>
<%@ page import="gov.nih.nlm.util.FieldedStringTokenizer" %>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.mrd.common.QAReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAResultReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAComparisonReason" %>
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<%  ReleaseClient rc = release_bean.getReleaseClient();
    String pattern_error = null;
    if("in =~".equals(request.getParameter("name_operator")) ||
	"=~".equals(request.getParameter("name_operator"))) {
	try {
            String pattern = request.getParameter("test_name");
	    if("=~".equals(request.getParameter("name_operator"))) {
      		String[] names = FieldedStringTokenizer.split(pattern,",");
      		for(int i=0; i<names.length; i++) {
			Pattern.compile(names[i]);
      		}
	    }else {
		Pattern.compile(pattern);
	    }

	    } catch(PatternSyntaxException e) {
		pattern_error = "&name_pattern_error=Regex syntax error: " + e.getDescription() + " near index " + e.getIndex();
	    }
    }

    if("in =~".equals(request.getParameter("value_operator")) ||
	"=~".equals(request.getParameter("value_operator"))) {
	try {
            String pattern = request.getParameter("test_value");
	    if("=~".equals(request.getParameter("value_operator"))) {
      		String[] names = FieldedStringTokenizer.split(pattern,",");
      		for(int i=0; i<names.length; i++) {
			Pattern.compile(names[i]);
      		}
	    }else {
		Pattern.compile(pattern);
	    }

	    } catch(PatternSyntaxException e) {
		pattern_error = "&value_pattern_error=Regex syntax error: " + e.getDescription() + " near index " + e.getIndex();
	    }
    }
    if (pattern_error != null) {
	StringBuffer uri = new StringBuffer("/mrd/controller?state=QAReasonForm&release=" + request.getParameter("release_name") + "&qareason=" + request.getParameter("qareason")
		+ "&comparison_name=" + request.getParameter("comparison_name")
		+ "&test_name=" + request.getParameter("test_name")
		+ "&name_operator=" + request.getParameter("name_operator")
		+ "&test_value=" + request.getParameter("test_value")
		+ "&value_operator=" + request.getParameter("value_operator")
		+ "&test_count=" + request.getParameter("test_count")
		+ "&count_operator=" + request.getParameter("count_operator")
		+ "&test_count_2=" + request.getParameter("test_count_2")
		+ "&comparisoncount_operator=" + request.getParameter("comparisoncount_operator")
		+ "&diff_count=" + request.getParameter("diff_count")
		+ "&diffcount_operator=" + request.getParameter("diffcount_operator")
		+ "&reason=" + request.getParameter("reason")
		+ pattern_error );
	 if("Update".equals(request.getParameter("action"))) {
            uri.append("&old_test_name=" +   request.getParameter("old_test_name")
		+ "&old_name_operator=" +  request.getParameter("old_name_operator")
		+ "&old_test_value=" + request.getParameter("old_test_value")
		+ "&old_test_value_operator=" +  request.getParameter("old_test_value_operator")
		+ "&old_test_count=" +  request.getParameter("old_test_count")
		+ "&old_test_count_operator=" + request.getParameter("old_test_count_operator")
		+ "&old_reason=" +  request.getParameter("old_reason")) ;
	 if("compare".equals(request.getParameter("qareason"))) {
         	uri.append("&old_test_count_2=" +  request.getParameter("old_test_count_2")
	+ "&old_comparisoncount_operator=" +  request.getParameter("old_comparisoncount_operator")
	+ "&old_diff_count=" +  request.getParameter("old_diff_count")
	+ "&old_diffcount_operator=" +  request.getParameter("old_diffcount_operator") );
	 }
	 }
    	RequestDispatcher dispatcher =
  	  getServletContext().getRequestDispatcher(response.encodeURL(uri.toString()
		));
    	dispatcher.forward(request,response);
    } else {
    if("compare".equals(request.getParameter("qareason"))) {
            QAComparisonReason qareason = new QAComparisonReason();
            qareason.setReleaseName(request.getParameter("release_name"));
            qareason.setComparisonName(request.getParameter("comparison_name"));
            qareason.setName(request.getParameter("test_name"));
	    qareason.setNameOperator(request.getParameter("name_operator"));
            if(!(request.getParameter("test_value") == null || "".equals(request.getParameter("test_value")))) {
            	qareason.setValue(request.getParameter("test_value"));
	    	qareason.setValueOperator(request.getParameter("value_operator"));
            }
            if(!(request.getParameter("test_count") == null || "".equals(request.getParameter("test_count")))) {
                qareason.setCount(Long.parseLong(request.getParameter("test_count")));
	    	qareason.setCountOperator(request.getParameter("count_operator"));
            }
            if(!(request.getParameter("test_count_2") == null || "".equals(request.getParameter("test_count_2")))) {
            	qareason.setComparisonCount(Long.parseLong(request.getParameter("test_count_2")));
	    	qareason.setComparisonCountOperator(request.getParameter("comparisoncount_operator"));
            }
            if(!(request.getParameter("diff_count") == null || "".equals(request.getParameter("diff_count")))) {
            	qareason.setDiffCount(Long.parseLong(request.getParameter("diff_count")));
	    	qareason.setDiffCountOperator(request.getParameter("diffcount_operator"));
            }
            qareason.setReason(request.getParameter("reason"));
            if("Add".equals(request.getParameter("action"))) {
            	rc.addQAReason(qareason,request.getParameter("target"));
            } else if("Update".equals(request.getParameter("action"))) {
            	QAComparisonReason old_qareason = new QAComparisonReason();
            	old_qareason.setReleaseName(request.getParameter("release_name"));
            	old_qareason.setComparisonName(request.getParameter("comparison_name"));
            	old_qareason.setName(request.getParameter("old_test_name"));
	    	old_qareason.setNameOperator(request.getParameter("old_name_operator"));
            	if(!(request.getParameter("old_test_value") == null || "".equals(request.getParameter("old_test_value")))) {
            		old_qareason.setValue(request.getParameter("old_test_value"));
	    		old_qareason.setValueOperator(request.getParameter("old_test_value_operator"));
            	}
            	if(!(request.getParameter("old_test_count") == null || "".equals(request.getParameter("old_test_count")))) {
            		old_qareason.setCount(Long.parseLong(request.getParameter("old_test_count")));
	    		old_qareason.setCountOperator(request.getParameter("old_test_count_operator"));
            	}
            	if(!(request.getParameter("old_test_count_2") == null || "".equals(request.getParameter("old_test_count_2")))) {
            		old_qareason.setComparisonCount(Long.parseLong(request.getParameter("old_test_count_2")));
	    		old_qareason.setComparisonCountOperator(request.getParameter("old_comparisoncount_operator"));
            	}
            	if(!(request.getParameter("old_diff_count") == null || "".equals(request.getParameter("old_diff_count")))) {
            		old_qareason.setDiffCount(Long.parseLong(request.getParameter("old_diff_count")));
	    		old_qareason.setDiffCountOperator(request.getParameter("old_diffcount_operator"));
            	}
            	old_qareason.setReason(request.getParameter("old_reason"));
		rc.removeQAReason(old_qareason, request.getParameter("target"));
		rc.addQAReason(qareason, request.getParameter("target"));
            }else if("Delete".equals(request.getParameter("action"))) {
            	rc.removeQAReason(qareason,request.getParameter("target"));
            }
    } else if("result".equals(request.getParameter("qareason"))){
            QAResultReason qareason = new QAResultReason();
            qareason.setReleaseName(request.getParameter("release_name"));
            qareason.setComparisonName(request.getParameter("comparison_name"));
            qareason.setName(request.getParameter("test_name"));
	    qareason.setNameOperator(request.getParameter("name_operator"));
            if(!(request.getParameter("test_value") == null || "".equals(request.getParameter("test_value")))) {
            	qareason.setValue(request.getParameter("test_value"));
	    	qareason.setValueOperator(request.getParameter("value_operator"));
            }
            if(!(request.getParameter("test_count") == null || "".equals(request.getParameter("test_count")))) {
            	qareason.setCount(Long.parseLong(request.getParameter("test_count")));
	    	qareason.setCountOperator(request.getParameter("count_operator"));
            }
            qareason.setReason(request.getParameter("reason"));
            if("Add".equals(request.getParameter("action"))) {
            	rc.addQAReason(qareason,request.getParameter("target"));
            } else if("Update".equals(request.getParameter("action"))) {
            	QAResultReason old_qareason = new QAResultReason();
            	old_qareason.setReleaseName(request.getParameter("release_name"));
            	old_qareason.setComparisonName(request.getParameter("comparison_name"));
            	old_qareason.setName(request.getParameter("old_test_name"));
	    	old_qareason.setNameOperator(request.getParameter("old_name_operator"));
            	if(!(request.getParameter("old_test_value") == null || "".equals(request.getParameter("old_test_value")))) {
            		old_qareason.setValue(request.getParameter("old_test_value"));
	    		old_qareason.setValueOperator(request.getParameter("old_test_value_operator"));
            	}
            	if(!(request.getParameter("old_test_count") == null || "".equals(request.getParameter("old_test_count")))) {
            		old_qareason.setCount(Long.parseLong(request.getParameter("old_test_count")));
	    		old_qareason.setCountOperator(request.getParameter("old_test_count_operator"));
            	}
            	old_qareason.setReason(request.getParameter("old_reason"));
		rc.removeQAReason(old_qareason, request.getParameter("target"));
		rc.addQAReason(qareason, request.getParameter("target"));
            }else if("Delete".equals(request.getParameter("action"))) {
            	rc.removeQAReason(qareason,request.getParameter("target"));
            }
    }
    }
%>
<HTML>

<HEAD>
<TITLE>ManageQAReason</TITLE>
</HEAD>
  <BODY onLoad="window.close();window.opener.location.reload();"></BODY>
</HTML>