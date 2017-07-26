<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<title>ViewMapSet:
<bean:write name="mapset" property="name"/>
</title>
</head>
<body bgcolor="#ffffff">
<center>
  <h1>    View MapSet:
    <bean:write name="mapset" property="name"/>
  </h1>
</center>
<hr width="100%"/>
<table align="center" width="90%" border="0">
  <tr>
    <td width="25%">Name (MAPSETNAME):</td>
    <td>
      <bean:write name="mapset" property="name"/>
    </td>
  </tr>
  <tr>
    <td>Description (SOS):</td>
    <td>
      <bean:write name="mapset" property="description"/>
    </td>
  </tr>
  <tr>
    <td>Identifier (MAPSETID):</td>
    <td>
      <bean:write name="mapset" property="mapSetIdentifier"/>
    </td>
  </tr>
  <tr>
    <td>Source (MAPSETVSAB):</td>
    <td>
      <bean:write name="mapset" property="mapSetSource"/>
    </td>
  </tr>
  <tr>
    <td>Type (MAPSETTYPE):</td>
    <td>
      <bean:write name="mapset" property="type"/>
    </td>
  </tr>
  <tr>
    <td>From Source (FROMVSAB):</td>
    <td>
      <bean:write name="mapset" property="fromSource"/>
    </td>
  </tr>
  <tr>
    <td>To Source (TOVSAB):</td>
    <td>
      <bean:write name="mapset" property="toSource"/>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td colspan="2">Optional Attributes</td>
  </tr>
  <tr>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>MTH_MAPSETCOMPLEXITY:</td>
    <td>
      <bean:write name="mapset" property="mapSetComplexity"/>
    </td>
  </tr>
  <tr>
    <td>MTH_MAPFROMCOMPLEXITY:</td>
    <td>
      <bean:write name="mapset" property="fromComplexity"/>
    </td>
  </tr>
  <tr>
    <td>MTH_MAPFROMEXHAUSTIVE:</td>
    <td>
      <logic:equal name="mapset" property="isFromExhaustive" value="true">
        Y
      </logic:equal>
      <logic:equal name="mapset" property="isFromExhaustive" value="false">
        N
      </logic:equal>
    </td>
  </tr>
  <tr>
    <td>MTH_MAPTOCOMPLEXITY:</td>
    <td>
      <bean:write name="mapset" property="toComplexity"/>
    </td>
  </tr>
  <tr>
    <td>MTH_MAPTOEXHAUSTIVE:</td>
    <td>
      <logic:equal name="mapset" property="isToExhaustive" value="true">
        Y
      </logic:equal>
      <logic:equal name="mapset" property="isToExhaustive" value="false">
        N
      </logic:equal>
    </td>
  </tr>
  <tr>
    <td>MAPSETSEPARATORCODE:</td>
    <td>
      <logic:present name="MAPSETSEPARATORCODE">
        <bean:write name="MAPSETSEPARATORCODE"/>
      </logic:present>
    </td>
  </tr>
  <tr>
    <td>MTH_UMLSMAPSETSEPARATOR:</td>
    <td>
      <logic:present name="UMLSMAPSETSEPARATOR">
        <bean:write name="UMLSMAPSETSEPARATOR"/>
      </logic:present>
    </td>
  </tr>
  <tr>
    <td>MAPSETXRTARGETID:</td>
    <td>
      <logic:present name="MAPSETXRTARGETID">
        <bean:write name="MAPSETXRTARGETID"/>
      </logic:present>
    </td>
  </tr>
</table>
<table align="center" width="50%" border="0">
  <tr>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>
      <html:form action="/editMapSetform">
        <html:submit>Edit MapSet</html:submit>
      </html:form>
    </td>
    <html:form action="/viewMapping">
      <td>
        <html:submit property="mrmap">View Mappings (MRMAP)</html:submit>
      </td>
      <td>
        <html:submit property="mrsmap">View Mappings (MRSMAP)</html:submit>
      </td>
    </html:form>
  </tr>
</table>
<meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>
</body>
</html>
