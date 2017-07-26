<%@taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/tlds/meme.tld" prefix="meme"%>
<html>
<head>
<title>View <bean:write name="contentView" property="name"/> Atom Members</title>
</head>
<body bgcolor="#ffffff">
<h1>
  <center>View <bean:write name="contentView" property="name"/> Atom Members</center>
</h1>
<hr width="100%"/>
<center>
  <logic:equal name="cvmList" property="previousPageAvailable" value="true" >
    <a href="viewContentViewMembers.do?success=&pageDirection=previous"><font color="green"><B>&lt;&lt; Prev</B></font></a>
  </logic:equal>
  <logic:equal name="cvmList" property="nextPageAvailable" value="true" >
    <a href="viewContentViewMembers.do?success=&pageDirection=next"><font color="green"><B>Next &gt;&gt;</B></font></a>
  </logic:equal>
</center>
<br />
<table width="100%" align="center" border="1" cellspacing="0" cellpadding="1">
  <tr>
    <th valign="bottom">CONCEPT_ID</th>
    <th valign="bottom">CUI</th>
    <th valign="bottom">LAT</th>
    <th valign="bottom">TS</th>
    <th valign="bottom">LUI</th>
    <th valign="bottom">STT</th>
    <th valign="bottom">SUI</th>
    <th valign="bottom">ISPREF</th>
    <th valign="bottom">AUI</th>
    <th valign="bottom">SAUI</th>
    <th valign="bottom">SCUI</th>
    <th valign="bottom">SDUI</th>
    <th valign="bottom">SAB</th>
    <th valign="bottom">TTY</th>
    <th valign="bottom">CODE</th>
    <th valign="bottom">STR</th>
    <th valign="bottom">SRL</th>
    <th valign="bottom">SUPPRESS</th>
    <th valign="bottom">CVF</th>
  </tr>
  <logic:iterate id="atom" name="cvmList">
  <tr>
    <td>
      <bean:write name="atom" property="concept.identifier"/>
    </td>
    <td>
      <bean:write name="atom" property="lastReleaseCUI"/>
      <logic:empty name="atom" property="lastReleaseCUI">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="language.abbreviation"/>
      <logic:empty name="atom" property="language.abbreviation">&nbsp;</logic:empty>
    </td>
    <td>
      X
    </td>
    <td>
      <bean:write name="atom" property="LUI"/>
      <logic:empty name="atom" property="LUI">&nbsp;</logic:empty>
    </td>
    <td>
      X
    </td>
    <td>
      <bean:write name="atom" property="SUI"/>
      <logic:empty name="atom" property="SUI">&nbsp;</logic:empty>
    </td>
    <td>
      X
    </td>
    <td>
      <bean:write name="atom" property="AUI"/>
      <logic:empty name="atom" property="AUI">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="sourceIdentifier"/>
      <logic:empty name="atom" property="sourceIdentifier">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="sourceConceptIdentifier"/>
      <logic:empty name="atom" property="sourceConceptIdentifier">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="sourceDescriptorIdentifier"/>
      <logic:empty name="atom" property="sourceDescriptorIdentifier">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="source"/>
      <logic:empty name="atom" property="source">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="termgroup.termType"/>
      <logic:empty name="atom" property="termgroup.termType">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="code"/>
      <logic:empty name="atom" property="code">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="string"/>
      <logic:empty name="atom" property="string">&nbsp;</logic:empty>
    </td>
    <td>
      <bean:write name="atom" property="source.restrictionLevel"/>
      <logic:empty name="atom" property="source.restrictionLevel">&nbsp;</logic:empty>
    </td>
    <td>
      <logic:equal name="atom" property="suppressible" value="true">Y</logic:equal>
      <logic:equal name="atom" property="suppressible" value="false">N</logic:equal>
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
  </logic:iterate>
</table>
<br />
<center>
  <logic:equal name="cvmList" property="previousPageAvailable" value="true" >
    <a href="viewContentViewMembers.do?success=&pageDirection=previous"><font color="green"><B>&lt;&lt; Prev</B></font></a>
  </logic:equal>
  <logic:equal name="cvmList" property="nextPageAvailable" value="true" >
    <a href="viewContentViewMembers.do?success=&pageDirection=next"><font color="green"><B>Next &gt;&gt;</B></font></a>
  </logic:equal>
</center>
<meme:footer name="Brian Carlsen" email="bcarlsen@msdinc.com" url="/" text="Meta News Home"/>
</body>
</html>
