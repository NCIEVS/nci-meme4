<%@ page contentType="text/html;charset=utf-8" errorPage= "ErrorPage.jsp"%>
<%@ page import="gov.nih.nlm.mrd.client.ReleaseClient" %>
<%@ page import="gov.nih.nlm.mrd.client.FullMRFilesReleaseClient" %>
<%@ page import="gov.nih.nlm.meme.common.StageStatus" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseInfo" %>
<%@ page import="gov.nih.nlm.mrd.common.ReleaseTarget" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Calendar" %>
<%@ taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme" %>

<jsp:useBean id="release_bean" scope="session" class="gov.nih.nlm.mrd.beans.ReleaseBean" />
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../stylesheets.css" rel="stylesheet" type="text/css">
<title>Release Summary</title>

<!-- Load IFrame error handler code -->
<script src="../IFrameErrorHandler.js">
  alert("Included JS file not found");
</script>

<script language="javascript">
<!--

  //
  // Verify delete action
  //
  function verifyDelete() {
    if(confirm("Are you sure you want to delete " + document.form2.release.value + " release?")) {
      document.form2.submit();
    }
  }

  //
  // Verify finish action
  //
  function verifyFinish() {
    if(confirm("Are you sure you want to finish " + document.form4.release.value + " release?")) {
      document.form4.submit();
    }
  }
  //
  // Toggle highlighting
  //
  function toggle(e) {
    if (e.checked) {
      highlight(e);
    } else {
      unhighlight(e);
    }
  }

  //
  // Programatically select a row
  //
  function check(e) {
    e.checked = true;
    highlight(e);
  }

  //
  // Programatically unselect a row
  //
  function clear(e) {
    e.checked = false;
    unhighlight(e);
  }

  //
  // Highlight a row
  //
  function highlight(e) {
    var r = null;
    if (e.parentNode && e.parentNode.parentNode)
      r = e.parentNode.parentNode;
    else if (e.parentElement && e.parentElement.parentElement)
      r = e.parentElement.parentElement;

    if (r) {
      if (r.className == "unchecked")
          r.className = "checked";
    }
  }

  //
  // Unhighlight a row
  //
  function unhighlight(e) {
    var r = null;
    if (e.parentNode && e.parentNode.parentNode)
      r = e.parentNode.parentNode;
    else if (e.parentElement && e.parentElement.parentElement)
      r = e.parentElement.parentElement;

    if (r) {
      if (r.className == "checked")
          r.className = "unchecked";
    }
  }

  //
  // Check all
  //
  function checkAll() {
    var ml = document.targets;
    var len = ml.elements.length;
    for (var i = 0; i < len; i++) {
      var e = ml.elements[i];
      if (e.name == "handlers") {
        check(e);
      }
    }
  }

  //
  // Clear all
  //
  function clearAll() {
    var ml = document.targets;
    var len = ml.elements.length;
    for (var i = 0; i < len; i++) {
      var e = ml.elements[i];
      if (e.name == "handlers") {
        clear(e);
      }
    }
  }

  function setIsActive(target) {
    for(i=0; i<document.targets.elements.length; i++) {
      if(document.targets.elements[i].name == "handlers" &&
	target.indexOf(document.targets.elements[i].value) == 0 &&
	!document.targets.elements[i].checked)
          check(document.targets.elements[i]);
    }
  }

  function setTarget(target, value) {
    var allInputs = document.targets.getElementsByTagName('INPUT');
    var inputObj;
    for (i=0; i<allInputs.length; i++) {
        if ( (allInputs[i].type == 'hidden') && (allInputs[i].name == target) ) {
		allInputs[i].value = value;
        }
    }
  }

  function setStatus(target, release, status, color, title, isActive) {
    var e = document.getElementById(target+"_status");
    e.innerHTML = "<font color=" + color + ">"+ status + "</font>";
    e.innerText = status;
    e.title = title;
    if(isActive) {
      setIsActive(target);
    }
  }

  function getStatus(target,release) {
    frames[target+"_frame"].location.href="controller?state=GetTargetStatus&target="+target+"&release="+release;
  }

  function activate(action, confirmSelection, checkDependencies) {
    var str = "action=" + action + "&release=" + document.targets.release.value;
    var message = "Are you sure you want to " + action + " these targets?";
    var targets = "";
    for(i=0; i<document.targets.elements.length; i++) {
      if(document.targets.elements[i].name == "handlers" && document.targets.elements[i].checked) {
	if(checkDependencies) {
	var target = eval('document.targets.' + document.targets.elements[i].value);
	if( target.value != "") {
	    var dep_array = target.value.split(",");
            for(j=0; j<dep_array.length; j++) {
		if(targets.indexOf(dep_array[j]) == -1 && document.getElementById(dep_array[j] + "_" + action + "_status").innerText != "Finished") {
			alert("You must " + action + " " + dep_array[j] + " before " + document.targets.elements[i].value);
			return false;
		}
            }
	}
	}
	targets = targets + " " + document.targets.elements[i].value;
        str = str + "&" + document.targets.elements[i].name + "=" + document.targets.elements[i].value;
	if(confirmSelection) {
        message = message + "\n" + (document.getElementById(document.targets.elements[i].value + "_" + action + "_status").innerText == "Finished" ? "*" : "") +  document.targets.elements[i].value;
	}
      }
    }

    if(confirmSelection && !confirm(message))
	return;
    frames["activate_frame"].location.href="controller?state=ManageTargets&"+str;
  }
// -->
  </script>
</head>

<body>

<%
  // Create iframe for each target
  ReleaseClient rc = release_bean.getReleaseClient();
  ReleaseInfo release_info = rc.getReleaseInfo(request.getParameter("release"));
  String[] targets = rc.getTargetNames(release_info.getName());
  for (int i=0; i < targets.length;i++) {
%>
  <iframe name="<%= targets[i] %>_frame" src="controller?state=GetTargetStatus&target=<%= targets[i] %>&release=<%=release_info.getName()%>" style="visibility:hidden" width=0 height=0></iframe>
<% } %>

<iframe name="activate_frame" style="visibility:hidden" width=0 height=0></iframe>

<span id=blue><center>Release Summary</center></span>
<hr WIDTH=100%>

<%
  ReleaseInfo release_infos[] = rc.getReleaseHistory();
  Arrays.sort(release_infos, new Comparator() {
    public int compare(Object o1, Object o2) {
      return ((ReleaseInfo)o2).getName().compareTo(((ReleaseInfo)o1).getName());
    }
  });
  SimpleDateFormat dateformat = release_bean.getDateFormat();
  StageStatus releaseStatus = rc.getReleaseStatus(release_info.getName());
%>
<form name="form4" method="get" action="controller">
	<input type="hidden" name="state" value="ManageRelease">
	<input type="hidden" name="action" value="finish">
	<input type="hidden" name="release" value="<%= release_info.getName() %>">
</form>
<form name ="form3" method="get" action="controller">
  <input type="hidden" name="state" value="ReleaseSummary">
  <center>
  <table width="60%" border="0">
    <tr><td><input accesskey="r" class="SPECIAL" type="button" onclick="form1.submit()" value="Edit Release"></td>
	   <td><input accesskey="r" class="SPECIAL" type="button" onclick="verifyFinish()" value="Finish Release"></td>
       <td><input accesskey="n" class="SPECIAL" type="button" value="Delete"
	              onClick="verifyDelete()"></td>
  <td width=30%>
Release :
<select name="release">
<%
  for(int i = 0 ; i < release_infos.length ; i++) {
%>
   <option value="<%=release_infos[i].getName()%>"><%= release_infos[i].getName()%>
<%
  }
%>
</select>

</td>
<td>
<input class="SPECIAL" type="submit" value="Manage">
</td>
</tr>
</table>
</center>
</form>
<FORM name="form1" method="get" action="controller">
<input type="hidden" name="state" value="EditReleaseForm">
<input type="hidden" name="release" value="<%= release_info.getName() %>">
<center>
<table WIDTH=60% BORDER=0 >
	<tr ><td>Release Name:</td>
	    <td><tt><%=release_info.getName() %>
	    </tt></td>
  </tr>
	<tr ><td>Description:</td>
	    <td><tt><%=release_info.getDescription() %>
	    </tt></td>
  </tr>
	<tr ><td>Previous Release:</td>
	    <td><tt><%=release_info.getPreviousReleaseInfo().getName() %>
	    </tt></td>
  </tr>
	<tr ><td>Previous Major Release:</td>
	    <td><tt><%=release_info.getPreviousMajorReleaseInfo().getName() %>
	    </tt></td>
  </tr>
	<tr ><td>Authority:</td>
	    <td><tt><%=(release_info.getAuthority() != null ? release_info.getAuthority().toString() : "") %>
	    </tt></td>
  </tr>
	<tr ><td>Generator Class:</td>
	    <td><tt><%=release_info.getGeneratorClass() %>
	    </tt></td>
  </tr>
	<tr ><td>Administrator:</td>
	    <td><tt><%=(release_info.getAdministrator() != null ? release_info.getAdministrator().toString() : "") %>
	    </tt></td>
  </tr>
	<tr ><td>Build Host:</td>
	    <td><tt><%=release_info.getBuildHost() %>
	    </tt></td>
  </tr>
	<tr ><td>Build URI:</td>
	    <td><tt><%=release_info.getBuildUri() %>
	    </tt></td>
  </tr>
	<tr ><td>Release Host:</td>
	    <td><tt><%=release_info.getReleaseHost() %></tt></td>
  </tr>
	<tr ><td>Release URI:</td>
	    <td><tt><%=release_info.getReleaseUri() %></tt></td>
  </tr>
	<tr ><td>Documentation Host:</td>
	    <td><tt><%=release_info.getDocumentationHost() %></tt></td>
  </tr>
	<tr ><td>Documentation URI:</td>
	    <td><tt><%=release_info.getDocumentationUri() %></tt></td>
  </tr>
	<tr ><td>Start Date:</td>
	    <td><tt><%=dateformat.format(release_info.getStartDate()) %></tt></td>
  </tr>
	<tr ><td>End Date:</td>
<%
  if(release_info.getEndDate() != null)
      out.println("<td><tt> " + dateformat.format(release_info.getEndDate()) + " </tt></td>");
%>
  </tr>
	<tr ><td>MED Start Date:</td>
	    <td><tt><%=dateformat.format(release_info.getMEDStartDate()) %></tt></td>
  </tr>
	<tr ><td>MBD Start Date:</td>
	    <td><tt><%=dateformat.format(release_info.getMBDStartDate()) %></tt></td>
  </tr>
	<tr ><td>Release Date:</td>
	    <td><tt><%=dateformat.format(release_info.getReleaseDate()) %></tt></td>
  </tr>
	<tr ><td>Release Finished:</td>
	    <td><tt><%=(releaseStatus.getCode() == StageStatus.FINISHED ? "" : "No" )%></tt>
	    <% if (releaseStatus.getCode() == StageStatus.FINISHED) { %>
	    	<a href="controller?state=ReleaseStatus&detail=ViewLog&release=<%= release_info.getName() %>">View Log</a>
	    <% } %>
	    </td>
  </tr>
  	<tr ><td>&nbsp;</td>
</table>
<table WIDTH=60% BORDER=0 >
	<tr ><td><A href="controller?state=CuiReport&release=<%= release_info.getName() %>">CUI Comparison Report</A>
	</td>
        <td>
          <a href="../viewReleaseReport.do?type=elapsedTime&release=<%= release_info.getName() %>">Elapsed Time Report</a>
        </td>
        <td>
          <a href="../dailyStatusReport.do?release=<%= release_info.getName() %>&date=<%= dateformat.format(Calendar.getInstance().getTime()) %>">Daily Status Report</a>
        </td>
        <td><A href="/MRD/Documentation/<%= release_info.getName() %>/index.html">Release Documentation</A></td><td>
	<A href=controller?state=ProcessMedline&release=<%= release_info.getName() %>>Process Medline </A></td>
  </tr>
</table>
</center>
</form>
<FORM name ="form2" method="get" action="controller">
<input type="hidden" name="state" value="ManageRelease">
<input type="hidden" name="action" value="delete">
<input type="hidden" name="release" value="<%= release_info.getName() %>">
</form>
<FORM name ="targets" method="get" action="controller">
<input type="hidden" name="state" value="ManageTargets">
<input type="hidden" name="release" value="<%= release_info.getName() %>">
<span id=blue><center>Targets</center></span>
<center>
<table cellpadding=4 cellspacing=0 WIDTH=60% BORDER=1 >
  <tr ><th rowspan="2" width="10%">&nbsp;
<th rowspan="2">NAME<th colspan="5">Status</tr>
<TR><TH>PrevQA<TH>Gold<TH>Build<TH>Validate<TH>Publish

<%
  for(int i=0; i< targets.length; i++) {
%>
  <tr class="unchecked">
    <td width="10%" align="center" valign="middle">
      <input type="checkbox" name="handlers" value="<%= targets[i] %>" onClick="toggle(this)">
      <input type="hidden" name="<%= targets[i] %>" value="">
    </td>
    <td width="40%"><a href="controller?state=TargetSummary&midService=<%= request.getParameter("midService") %>&release=<%= release_info.getName() %>&generator=<%= release_info.getGeneratorClass() %>&target=<%= targets[i] %>" target="_blank">
        <%= targets[i] %></a>
    </td>
    <td><div id="<%= targets[i] + "_prevQA_status" %>" ></div></td>
    <td><div id="<%= targets[i] + "_gold_status" %>" ></div></td>
    <td><div id="<%= targets[i] + "_build_status" %>" ></div></td>
    <td><div id="<%= targets[i] + "_validate_status" %>" ></div></td>
    <td><div id="<%= targets[i] + "_publish_status" %>" ></div></td>
  </tr>
<%
  }
%>

<tr>
       <tr ><td colspan= "2" WIDTH=15% align="right" ><input class="SPECIAL" type="button" onClick='activate("QAReport")' value="QA Report"></td>
             <td WIDTH=15% ><input class="SPECIAL" type="button" onClick='activate("prevQA")' value="PrevQA"></td>
             <td WIDTH=15%><input class="SPECIAL" type="button" onClick='activate("gold")' value="Gold"></td>
             <td WIDTH=15%><input class="SPECIAL" type="button" onClick='activate("build",true,true)' value="Build"></td>
             <td WIDTH=15%><input class="SPECIAL" type="button" onClick='activate("validate",true,true)' value="Validate"></td>
             <td WIDTH=15%><input class="SPECIAL" type="button" onClick='activate("publish",true)' value="Publish"></td>
       </tr>
<td colspan="7"><a href="javascript:checkAll();">Check All</a> - <a href="javascript:clearAll();">Clear All</a></td>
</tr>
</table>
</center>

</FORM>
<iframe name="target_frame" src="controller?state=GetTargets&release=<%=release_info.getName()%>" style="visibility:hidden" width=0 height=0></iframe>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual" />
</BODY>
</HTML>
