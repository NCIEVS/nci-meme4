<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.client.*, gov.nih.nlm.meme.qa.ITestSuite"
         errorPage= "IFrameErrorPage.jsp" %>

<jsp:useBean id="testsuite_bean" scope="session"
             class="gov.nih.nlm.meme.beans.TestSuiteSetBean" />
<jsp:setProperty name="testsuite_bean" property="*" />
<HTML>
<BODY>
<%
	String checked = null;
	try{
            checked = request.getParameter("run");
	}catch(Exception e) {
        	throw new Exception("run is null");
	}
	ITestSuite test = null;
	if (checked != null) {
          test = (ITestSuite) testsuite_bean.getTestSuite(checked);
          test.clearLog();
          test.configureClient(testsuite_bean.getHost(),Integer.parseInt(testsuite_bean.getPort()), testsuite_bean.getMidService());
          test.run();

%>
<script>parent.UpdateStatus("<%=test.getName()%>", "<%=test.isPassed()%>")</script>
<%
	}
%>
</BODY>
</HTML>