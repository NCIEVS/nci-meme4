<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.mrd.common.QAReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAResultReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAComparisonReason" %>
<%@ page import="gov.nih.nlm.mrd.common.QAReport" %>
<%@ page import="gov.nih.nlm.mrd.common.QAComparison" %>
<%@ page import="gov.nih.nlm.mrd.common.QAResult" %>
<%@ page import="gov.nih.nlm.mrd.common.ByNameByReasonComparator" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Arrays" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>

<HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="release_bean" scope="session"
             class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<TITLE>
QAReason
</TITLE>
  <script language="javascript">
   function runSubmit(e) {
	document.form1.action.value = e.value;
	if(document.form1.reason.value == "") {
	  alert("Please enter the value for reason");
	  return;
	}
	document.form1.submit();
   }
  </script>
</HEAD>
<body bgcolor="#ffffff">
<span id=blue><center>QA Reason</center></span>
<hr WIDTH=100%>
<CENTER>
<form method="get" action="controller" name="form1">
<input type="hidden" name="state" value="ManageQAReason">
<input type="hidden" name="release" value="<%=request.getParameter("release")%>">
<input type="hidden" name="target" value="<%=request.getParameter("target")%>">
<input type="hidden" name="qareason" value="<%=request.getParameter("qareason")%>">
<input type="hidden" name="action" value="<%=request.getParameter("action")%>">
<table WIDTH=95% BORDER=0 >
	<tr ><td width="30%">Release Name:</td>
	    <td width="70%">
	<% if("Update".equals(request.getParameter("action"))) { %>
		<input type="hidden" name="release_name" value="<%=request.getParameter("release_name")%>">
		<%=request.getParameter("release_name")%>
	<% } else { %>
	    <SELECT name="release_name">
	  	<% String[] names = request.getParameterValues("release_name");
			for(int i=0; i < names.length; i++) {
				out.println("<option value=\""+ names[i] + "\">" + names[i] + "</option>");
			}
		%>
		</SELECT>
	<% } %>
	    </td>
  </tr>
	<tr ><td width="30%">Comparison Name:</td>
	    <td width="70%">
	<% if("Update".equals(request.getParameter("action"))) { %>
		<input type="hidden" name="comparison_name" value="<%=request.getParameter("comparison_name")%>">
		<%=request.getParameter("comparison_name")%>
	<% } else { %>
	    <SELECT name="comparison_name">
	  	<% String[] names = request.getParameterValues("comparison_name");
			for(int i=0; i < names.length; i++) {
				out.println("<option value=\""+ names[i] + "\">" + names[i] + "</option>");
			}
		%>
		</SELECT>
	<% } %>
	    </td>

  </tr>
	<tr ><td width="30%">Test Name:</td>
	    <td width="70%"><input type="text" name="test_name" value="<%=request.getParameter("test_name")%>" size=10>
	    <SELECT name="name_operator">
		<option value= "=" <%= "=".equals(request.getParameter("name_operator")) ? "SELECTED" : "" %>>=</option>
		<option value= "in" <%= "in".equals(request.getParameter("name_operator")) ? "SELECTED" : "" %>>in</option>
		<option value= "=~" <%= "=~".equals(request.getParameter("name_operator")) ? "SELECTED" : "" %>>=~</option>
		<option value= "in =~" <%= "in =~".equals(request.getParameter("name_operator")) ? "SELECTED" : "" %>>in =~</option>
		</SELECT>
	    </td>
  </tr>
	<tr>
                <td></td><td> <font color="#ff0033">
                  <% String results;
                     results = request.getParameter("name_pattern_error");
                     if ( results != null )
                        out.print(results);
                  %></font>
                </td>
  </tr>
	<tr ><td width="30%">Test Value:</td>
	    <td width="70%"><input type="text" size=30 name="test_value" value="<%= request.getParameter("test_value")%>">
	    <SELECT name="value_operator">
		<option value= "=" <%= "=".equals(request.getParameter("value_operator")) ? "SELECTED" : "" %>>=</option>
		<option value= "in" <%= "in".equals(request.getParameter("value_operator")) ? "SELECTED" : "" %>>in</option>
		<option value= "=~" <%= "=~".equals(request.getParameter("value_operator")) ? "SELECTED" : "" %>>=~</option>
		<option value= "in =~" <%= "in =~".equals(request.getParameter("value_operator")) ? "SELECTED" : "" %>>in =~</option>
		</SELECT>
	    </td>
  </tr>
	<tr>
                <td></td><td> <font color="#ff0033">
                  <% results = request.getParameter("value_pattern_error");
                     if ( results != null )
                        out.print(results);
                  %></font>
                </td>
  </tr>
	<tr ><td width="30%">Test Count:</td>
	    <td width="70%"><input type="text" name="test_count" value="<%= request.getParameter("test_count")%>">
	    <SELECT name="count_operator">
		<option value= "=" <%= "=".equals(request.getParameter("count_operator")) ? "SELECTED" : "" %>>=</option>
		<option value= "<" <%= "<".equals(request.getParameter("count_operator")) ? "SELECTED" : "" %>>&lt;</option>
		<option value= ">" <%= ">".equals(request.getParameter("count_operator")) ? "SELECTED" : "" %>>&gt;</option>
		</SELECT>
	    </td>
  </tr>
<%  if("compare".equals(request.getParameter("qareason"))) { %>
	<tr ><td width="30%">Test Comparison Count:</td>
	    <td width="70%"><input type="text" name="test_count_2" value="<%= request.getParameter("test_count_2")%>">
	    <SELECT name="comparisoncount_operator">
		<option value= "=" <%= "=".equals(request.getParameter("comparisoncount_operator")) ? "SELECTED" : "" %>>=</option>
		<option value= "<" <%= "<".equals(request.getParameter("comparisoncount_operator")) ? "SELECTED" : "" %>>&lt;</option>
		<option value= ">" <%= ">".equals(request.getParameter("comparisoncount_operator")) ? "SELECTED" : "" %>>&gt;</option>
		</SELECT>
	    </td>
  </tr>
	<tr ><td width="30%">Test Diff Count:</td>
	    <td width="70%"><input type="text" name="diff_count" value="<%= request.getParameter("diff_count")%>">
	    <SELECT name="diffcount_operator">
		<option value= "=" <%= "=".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>=</option>
		<option value= "<" <%= "<".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>&lt;</option>
		<option value= ">" <%= ">".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>&gt;</option>
		<option value= "abs <" <%= "abs <".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>abs &lt;</option>
		<option value= "abs >" <%= "abs >".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>abs &gt;</option>
		<option value= "% <" <%= "% <".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>% &lt;</option>
		<option value= "% >" <%= "% >".equals(request.getParameter("diffcount_operator")) ? "SELECTED" : "" %>>% &gt;</option>
		</SELECT>
	    </td>
  </tr>
<% } %>
  </tr>
	<tr ><td width="30%">Reason:</td>
	    <td width="70%"><input type="text" name="reason" value="<%= ( request.getParameter("reason") == null ? "" : request.getParameter("reason") )%>">
	    </td>
  </tr>
        <td colspan=2 width="50%" align="center">
         <input accesskey="r" class="SPECIAL" type="button" value="<%=request.getParameter("action")%>" name="Button" onClick="runSubmit(this)">

<% if("Update".equals(request.getParameter("action"))) {%>
                            &nbsp;&nbsp;&nbsp;
	<input type="hidden" name="old_test_name" value="<%= (  request.getParameter("old_test_name") == null ? request.getParameter("test_name") :  request.getParameter("old_test_name"))%>">
	<input type="hidden" name="old_name_operator" value="<%= ( request.getParameter("old_name_operator") == null ? request.getParameter("name_operator") : request.getParameter("old_name_operator"))%>">
	<input type="hidden" name="old_test_value" value="<%=( request.getParameter("old_test_value") == null ? request.getParameter("test_value") : request.getParameter("old_test_value"))%>">
	<input type="hidden" name="old_test_value_operator" value="<%= ( request.getParameter("old_test_value_operator") == null ? request.getParameter("value_operator") : request.getParameter("old_test_value_operator"))%>">
	<input type="hidden" name="old_test_count" value="<%= ( request.getParameter("old_test_count") == null ? request.getParameter("test_count") : request.getParameter("old_test_count"))%>">
	<input type="hidden" name="old_test_count_operator" value="<%=(request.getParameter("old_test_count_operator") == null ? request.getParameter("count_operator") : request.getParameter("old_test_count_operator"))%>">
	<input type="hidden" name="old_reason" value="<%=(request.getParameter("old_reason") == null ? request.getParameter("reason") : request.getParameter("old_reason"))%>">
<% if("compare".equals(request.getParameter("qareason"))) { %>
	<input type="hidden" name="old_test_count_2" value="<%=(request.getParameter("old_test_count_2") == null ? request.getParameter("test_count_2") : request.getParameter("old_test_count_2"))%>">
	<input type="hidden" name="old_comparisoncount_operator" value="<%=(request.getParameter("old_comparisoncount_operator") == null ? request.getParameter("comparisoncount_operator") : request.getParameter("old_comparisoncount_operator"))%>">
	<input type="hidden" name="old_diff_count" value="<%=(request.getParameter("old_diff_count") == null ? request.getParameter("diff_count"): request.getParameter("old_diff_count"))%>">
	<input type="hidden" name="old_diffcount_operator" value="<%=(request.getParameter("old_diffcount_operator") == null ? request.getParameter("diffcount_operator") : request.getParameter("old_diffcount_operator"))%>">
<% } %>
	<input accesskey="r" class="SPECIAL" type="button" value="Delete" name="Button" onClick="runSubmit(this)">
<% } %>

        </td>
  </tr>
</table>

<br>
</form>
</CENTER>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</body>
</html>
