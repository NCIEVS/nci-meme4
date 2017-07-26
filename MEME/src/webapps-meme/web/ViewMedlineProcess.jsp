<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<html:html>
<HEAD>

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
  <LINK href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<TITLE>Process Medline</TITLE>
  <!-- Load IFrame error handler code -->
<script src="<html:rewrite page="/IFrameErrorHandler.js" />" type="">
  alert("Included JS file not found");
</script>
</HEAD>
<BODY>
  <span id=blue>
    <center>Process Medline</center>
  </span>
  <hr WIDTH=100%>
  <i>    Click <html:link href="mrd/controller?state=ListMedlineProperty" name="linkParams">here</html:link> to edit medline date patterns.</i>
  <br>
  <center>
    <html:form action="/processMedline">
      <table cellpadding=4 cellspacing=0 WIDTH=70% BORDER=1>
        <tr>
          <th>&nbsp;
          <th>Stage
          <th>Status
          <th>View Log
          <th>TimeStamp
        </tr>
        <bean:define id="options" name="options">
        </bean:define>
        <logic:iterate id="status" name="medlineBean" property="stageStatus" indexId="i">
          <tr>
            <td>
              <html:radio property="stage" value="name" idName="status" disabled="<%=(((String)options).indexOf(status.toString()) == -1 ? true : false)%>">
              </html:radio>
            </td>
            <td>
                <bean:write name="status" property="name"/>
            </td>
            <logic:equal value="0" name="status" property="code">
              <td>&nbsp;</td>
              <td>&nbsp;</td>
            </logic:equal>
            <logic:equal value="4" name="status" property="code">
              <td>Running</td>
              <td>
                <font size=-1>
                  <html:link target="_blank" action="/viewMedlineDetails?details=View Log" paramName="status" paramProperty="name" paramId="stage">View Log
                  </html:link>
                </font>
              </td>
            </logic:equal>
            <logic:equal value="20" name="status" property="code">
              <td>Finished
                <html:link target="_self" action="processMedline.do?action=clearLog" paramName="status" paramProperty="name" paramId="stage">
                  <html:img styleClass="clearrow" border="0" page="/img/clearrow.gif" alt="clear" title="clear log" />
                </html:link>
              </td>
              <td>
                <font size=-1>
                  <html:link target="_blank" action="/viewMedlineDetails?details=View Log" paramName="status" paramProperty="name" paramId="stage">View Log
                  </html:link>
                </font>
              </td>
            </logic:equal>
            <logic:equal value="36" name="status" property="code">
              <td>Error</td>
              <td>
                <font size=-1>
                  <html:link target="_blank" action="/viewMedlineDetails?details=View Log" paramName="status" paramProperty="name" paramId="stage">View Log
                  </html:link>
                </font>
              </td>
            </logic:equal>
            <td>
              <bean:write name="status"  property="endTime" format="dd-MMM-yyyy hh:mm:ss"/>
              &nbsp;
             </td>
          </tr>
        </logic:iterate>
      </table>
      <TABLE WIDTH=90%>
        <TR>
          <TD align="center">
            <html:submit property="action">Start </html:submit>
            <html:button value="Refresh" property="" onclick="window.location.reload();"/>
          </TD>
        </TR>
      </TABLE>
    </html:form>
  </center>
  <meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home" docurl="/MRD/ReleaseManager" doctext="Release Manager User Manual"/>
</BODY>
</html:html>
