<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-layout.tld" prefix="layout"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<html>
<head>
<link href="<html:rewrite page="/config/skin2.css" />" rel="stylesheet" type="text/css">
</head>
<title>ViewReleaseReport</title>
</head><body bgcolor="#ffffff">
<logic:equal value="elapsedTime" property="type" name="viewReportActionForm">
  <layout:collection name="targets" styleClass="FORM">
    <layout:collectionItem title="Name" property="name"/>
    <layout:collectionItem title="PrevQA" property="prevQA"/>
    <layout:collectionItem title="Gold" property="gold"/>
    <layout:collectionItem title="Build" property="build"/>
    <layout:collectionItem title="Validate" property="validate"/>
    <layout:collectionItem title="Publish" property="publish"/>
  </layout:collection>
</logic:equal>
<logic:equal value="daily" name="viewReportActionForm" property="type">
  <table border="0" cellpadding="1" cellspacing="1" width="100%" class="FORM">
    <tbody>
      <tr valign="middle">
        <th class="FORM" rowspan="3">Hour</th>
      </tr>
      <tr valign="top">
        <th class="FORM" colspan="<bean:write name="prevQASize"/>">PrevQA</th>
        <th class="FORM" colspan="<bean:write name="goldSize" />">Gold</th>
        <th class="FORM" colspan="<bean:write name="buildSize" />">Build</th>
        <th class="FORM" colspan="<bean:write name="validateSize" />">Validate</th>
        <th class="FORM" colspan="<bean:write name="publishSize" />">Publish</th>
      </tr>
      <tr valign="top">
        <logic:iterate id="pid" name="processIds">
          <th class="FORM">
            <bean:write name="pid" filter="false"/>
          </th>
        </logic:iterate>
      </tr>
      <logic:iterate id="row" name="rows">
        <tr>
          <logic:iterate id="column" name="row">
            <td class="FORM">
              <bean:write name="column" filter="false"/>
            </td>
          </logic:iterate>
        </tr>
      </logic:iterate>
    </tbody>
  </table>
  <logic:present name="queue">
  <layout:collection name="queue" styleClass="FORM" title="Queued Targets">
    <layout:collectionItem title="Parallel ID" property="parallelId"/>
    <layout:collectionItem title="Targets" property="targets"/>
  </layout:collection>
  </logic:present>
</logic:equal>
</body>
</html>
