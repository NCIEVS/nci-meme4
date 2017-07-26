
<%@ page session="true" contentType="text/html; charset=UTF-8"
         import= "java.util.*, java.text.*, gov.nih.nlm.meme.*, gov.nih.nlm.meme.client.*, gov.nih.nlm.meme.qa.TestSuiteSet, gov.nih.nlm.meme.qa.ITestSuite"
         errorPage= "ErrorPage.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>
<HTML>
<HEAD><TITLE>Display Test Suite Sets</TITLE>
<LINK href="../stylesheets.css" rel="stylesheet" type="text/css">
<jsp:useBean id="testsuite_bean" scope="session"
             class="gov.nih.nlm.meme.beans.TestSuiteSetBean" />
<jsp:setProperty name="testsuite_bean" property="*" />
<!-- Import error handler -->
<script src="../IFrameErrorHandler.js">
  alert("Error handler javascript page not found");
</script>

<SCRIPT language="JavaScript">

var selected_tests = new Array();
var original_length = 0;

function getValueFromURL(url) {
    frames["get_value_frame"].location.href=url;
}

//call the controller, go to the hidden state
function ButtonClick(btn) {
  //1st state, change the button value and kick off the RunTestSuiteSet state
  if (btn.value == "START") {
      btn.value = "STOP";
      document.getElementById("status").innerHTML = "Running";
      <meme:clearpm name="pm" mode="html" />
      StartRunning();
  //2nd state, while running this will stop the action sequences
  }else if (btn.value == "STOP") {
      btn.value="CONTINUE";
      document.getElementById("status").innerHTML = "Stopped";
  }else if (btn.value == "CONTINUE") {
      btn.value = "STOP";
      document.getElementById("status").innerHTML = "Running";
      runNext();
  }
}

function StartRunning(){
  for(var j=0; j < document.DisplayTestSuitesForm.length -1; j++) {
    box = document.DisplayTestSuitesForm.elements[j];
    if (box.type == 'checkbox' && box.checked == true)
        selected_tests.push(box.name);
  }
  original_length = selected_tests.length;
  RunNext();
}

function RunNext() {
 if(document.DisplayTestSuitesForm.btn.value == 'STOP') {
  if (selected_tests.length > 0 )
    getValueFromURL("controller?state=RunTestSuiteSet&run=" + selected_tests.shift());
  else
    Complete();
 }
}

function Complete() {
  document.DisplayTestSuitesForm.btn.value="START";
  document.getElementById("status").innerHTML = "FINISHED";
}

function UpdateStatus(name, passed) {
<meme:updatepm name="pm" isexpression="true" progress="Math.floor((original_length - selected_tests.length)*100/original_length)" mode="html" />
  if (passed == 'true') {
  	document.getElementById("S"+name).innerHTML="PASSED";
	document.getElementById("S"+name).color="green";
  }else{
  	document.getElementById("S"+name).innerHTML="FAILED";
	document.getElementById("S"+name).color="red";
  }
  eval("document.DisplayTestSuitesForm.L"+name+".disabled = false;");
  RunNext();
}

//open a new window to display the log
function DisplayLog(logBtn) {
  name = logBtn.name.substring(1,logBtn.name.length);
  window.open("controller?state=GetTestSuiteLog&name=" + name, "Result",'resizable, scrollbars,width=600,height=600');
}

function SelectAll(){
  for(var i=0; i < document.DisplayTestSuitesForm.length -1; i++) {
    box = document.DisplayTestSuitesForm.elements[i];
      if (box.type == 'checkbox')
        box.checked = true;
  }
}

function UnselectAll(){
  for(var i=0; i < document.DisplayTestSuitesForm.length -1; i++) {
    box = document.DisplayTestSuitesForm.elements[i];
      if (box.type == 'checkbox')
        box.checked = false;
  }
}
</SCRIPT>
</HEAD>
<BODY>

<SPAN id="blue"><CENTER>MEME Server QA Center</CENTER></SPAN>
<HR width="100%">
Status: <b><%= testsuite_bean.getActionStatus() %></b>
<!--  DisplayTestSuiteSet form -->
<CENTER>

<iframe name="get_value_frame" style="visibility:hidden" width=0 height=0></iframe>
<FORM name="DisplayTestSuitesForm">
<TABLE width="95%" border="0">
<TR>
    <TD>Status</TD>
    <TD><b><DIV style="color:#0000A0" id="status" align"right">Not started yet</DIV></b></TD>
</TR>
<%
    //initialize test_suite_sets
    String[] valid_sets = request.getParameter("run").split("[,]");
    TestSuiteSet[] all_test_suite_sets = testsuite_bean.getTestSuiteSets();
    for(int i = 0; i <  all_test_suite_sets.length; i++) {
	for(int j = 0; j < valid_sets.length; j++) {
       	   if (all_test_suite_sets[i].getName().equals(valid_sets[j])) {
%>

<TR><TH COLSPAN="5"><%= all_test_suite_sets[i].getName() %></TH><TR>

<%
    ITestSuite[] current_set = all_test_suite_sets[i].getTestSuites();
    for(int k=0; k < current_set.length; k++){
    	ITestSuite current_suite = current_set[k];
        testsuite_bean.putTestSuite(current_suite);
%>

<TR><TD width=5><INPUT type=checkbox name="<%=current_suite.getName()%>" checked
		OnMouseOver="window.status='Check to enable this test suite'; return true;"
               	OnMouseOut="window.status=''; return true;"></TD>
    <TD width=15><%=current_suite.getName()%></TD>
    <TD><%=current_suite.getDescription()%></TD>
    <TD width=15><FONT color="#000000" id="S<%=current_suite.getName()%>">STATUS</FONT></TD>
    <TD width=5><INPUT type=button name="L<%=current_suite.getName()%>" value="LOG" disabled
		OnClick ="DisplayLog(this); return true;"
		OnMouseOver="window.status='Complete log of the run'; return true;"
               	OnMouseOut="window.status=''; return true;"></TD>
</TR>
<%
}}}}
%>
</TABLE>

<DIV ALIGN=LEFT><INPUT type="button" name="selectAllbtn" value="All" align=LEFT OnClick="SelectAll(); return true;">
<INPUT type="button" name="unselectAllbtn" value="None" align=LEFT OnClick="UnselectAll(); return true;"></DIV>


<INPUT type="button" name="btn" value="START" OnClick="ButtonClick(this); return true;"
	OnMouseOver="window.status='Click to run or stop selected test suites'; return true;"
	OnMouseOut="window.status=''; return true;">
<INPUT type="button" name="resetbtn" value="RESET" OnClick="location.reload(); return true;"
	OnMouseOver="window.status='Click to reset.'; return true;"
	OnMouseOut="window.status=''; return true;">
</FORM>
</CENTER>
<center>
<meme:progressmonitor name="pm" message="Progress Monitor" max="100" /></center>
<meme:footer name="Tim Kao" email="tkao@msdinc.com" url="/" text="Meta News Home" />
</BODY>
</HTML>
