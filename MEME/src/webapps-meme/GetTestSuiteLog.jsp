<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.net.*, java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.client.*, gov.nih.nlm.meme.qa.TestSuiteSet, gov.nih.nlm.meme.qa.ITestSuite"
         errorPage= "IFrameErrorPage.jsp" %>

<jsp:useBean id="testsuite_bean" scope="session"
             class="gov.nih.nlm.meme.beans.TestSuiteSetBean" />
<jsp:setProperty name="testsuite_bean" property="*" />

<HTML>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
<BODY>

<%
	String name = null;
	String log = "Log not found: Please rerun this test";
	if (request.getParameter("name") != null) {
          name = request.getParameter("name");
	  log = testsuite_bean.getTestSuite(name).getLog();
	}
%>
<PRE><%=log%></PRE>
</BODY>

<FORM>
  <TABLE WIDTH=90% >
    <TD align="center">
      <input type="button" value="Close" onClick="window.close(); return true">
    </TD>
  </TABLE>
</FORM>
</HTML>
