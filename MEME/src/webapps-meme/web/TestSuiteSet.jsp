<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.client.*, gov.nih.nlm.meme.qa.TestSuiteSet"
         errorPage= "EntryPointErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<HTML>
<HEAD><TITLE>TestSuiteSet</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="testsuite_bean" scope="session"
             class="gov.nih.nlm.meme.beans.TestSuiteSetBean" />
<jsp:setProperty name="testsuite_bean" property="*" />

<script language="javascript" type="text/javascript">

function getParameterURL() {
   param = "host="
    + document.connection.host[document.connection.host.selectedIndex].value
    + "&port=" + document.connection.port.value + "&midService="
    + document.connection.midService[document.connection.midService.selectedIndex].value
    + "&run=";

    for(var j=0; j < document.TestSuiteSetForm.length -1; j++) {
	box = document.TestSuiteSetForm.elements[j];
	if (box.checked == true)
           param = param + box.name + ",";
    }
    return param;
}

function displayTestSuiteSet() {
  window.open("controller?state=DisplayTestSuiteSets&"+getParameterURL(), "_self");
}
</SCRIPT>

</HEAD>
<BODY>

<SPAN id="blue"><CENTER>MEME Server QA Center</CENTER></SPAN>
<HR width="100%">
Status: <b><%= testsuite_bean.getActionStatus() %></b>
<!--  TestSuiteSet form -->
<p>
Server Host: <tt><%= testsuite_bean.getHost() %></tt><br>
Server Port: <tt><%= testsuite_bean.getPort() %></tt><br>
Server Version: <tt><%= testsuite_bean.getAdminClient().getServerVersion() %></tt>
<P>
<CENTER>
<TABLE width="95%" border="0">
  <FORM name="connection" method="GET" action="">
  <INPUT name="state" type="hidden" value="TestSuiteSet">
    <TR>
      <TH colspan="3">&nbsp;</TH>
    </TR>
    <TR>
      <TD colspan="3">&nbsp;</TD>
    </TR>
    <TR>
      <TD width="50%">Change database</TD>
      <td width="25%">
        <meme:midServiceList name="midService" submit="false" initialValue="<%= testsuite_bean.getMidService() %>" />
      </td>
      <TD width="25%">
        <INPUT type="button" value="Go" onClick="connection.submit();"
               OnMouseOver="window.status='Click to change database.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
    <TR>
      <TD width="50%">Change host</TD>
      <TD width="25%">
        <meme:hostList name="host" submit="false" initialValue="<%= testsuite_bean.getHost() %>" />
      </TD>
      <TD width="25%">
        <INPUT type="button" value="Go" onClick="connection.submit();"
               OnMouseOver="window.status='Click to change host.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
    <TR>
      <TD width="50%">Change port</TD>
      <TD width="25%">
        <INPUT type="text" name="port" size="12"
               value="<jsp:getProperty name="testsuite_bean" property="port" />">
      </TD>
      <TD width="25%">
        <INPUT type="button" value="Go" onClick="connection.submit();"
  	       OnMouseOver="window.status='Click to change port.'; return true;"
               OnMouseOut="window.status=''; return true;">
      </TD>
    </TR>
  </FORM>
</TABLE>
</CENTER>
<BR>
<BR>
<BR>
<CENTER>
<FORM name="TestSuiteSetForm" action="">
<TABLE width="95%" border="0">
<TR><th width="5%">Enabled</TH><th width="25%">Test Suite Name</TH><th width="70%">Description</TH></TR>
<%
	TestSuiteSet[] test_suite_sets = testsuite_bean.getTestSuiteSets();
	for(int i = 0; i <  test_suite_sets.length; i++) {%>
	<TR><TD><INPUT type=checkbox name=<%= test_suite_sets[i].getName()%> checked></TD>
        	<TD><%= test_suite_sets[i].getName()%></TD>
		<TD><%= test_suite_sets[i].getDescription()%></TD></TR><%}%>
</TABLE>
       <INPUT type="button" value="Next Step" onClick="displayTestSuiteSet(); return true;"
               OnMouseOver="window.status='Click to configure the selected test suite sets.'; return true;"
               OnMouseOut="window.status=''; return true;">
</FORM>
</CENTER>
<meme:footer name="Tim Kao" email="tkao@msdinc.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
