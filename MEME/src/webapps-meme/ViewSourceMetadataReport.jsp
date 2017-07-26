<%@taglib uri="/WEB-INF/meme.tld" prefix="meme"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-layout.tld" prefix="layout"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<html:html>
<head>
<link href="<html:rewrite page="/stylesheets.css" />" rel="stylesheet" type="text/css">
<title>SourceMetadataReport</title>
</head>
<body bgcolor="#ffffff">
  <h1>
    <center>SourceMetadata Report</center>
  </h1>
  <TABLE WIDTH=90% border="1" align="center">
    <TR>
      <TH colspan="4">Source Differences</TH>
    </TR>
    <tr>
      <th>Differences</th>
      <th>Count</th>
      <th>Old Value</th>
      <th>New Value</th>
    </tr>
    <logic:iterate id="diff" name="report" property="sourceDifferences">
      <tr>
        <td valign="top">
          <bean:write name="diff" property="name"/>
        </td>
        <td valign="top">
          <bean:write name="diff" property="count"/>
        </td>
        <td valign="top">
          <logic:empty name="diff" property="oldValues">
            &nbsp;
          </logic:empty>
          <logic:iterate id="value" name="diff" property="oldValues">
            <bean:write name="value" property="value"/>
            <br/>
          </logic:iterate>
        </td>
        <td valign="top">
          <logic:empty name="diff" property="newValues">
            &nbsp;
          </logic:empty>
          <logic:iterate id="value" name="diff" property="newValues">
            <bean:write name="value" property="value"/>
            <br/>
          </logic:iterate>
        </td>
      </tr>
    </logic:iterate>
    <TR>
      <TH colspan="4">Attribute Name Differences</TH>
    </TR>
    <tr>
      <th>Differences</th>
      <th>Count</th>
      <th>Old Value</th>
      <th>New Value</th>
    </tr>
    <logic:iterate id="diff" name="attributeNameDifferences">
      <tr>
        <td valign="top">
          <bean:write name="diff" property="name"/>
        </td>
        <td valign="top">
          <bean:write name="diff" property="count"/>
        </td>
        <td valign="top">
          <table border="1" width="100%">
            <logic:iterate id="value" name="diff" property="oldValues">
              <logic:iterate id="source" name="value" property="sourceAbbreviations" indexId="i">
                <tr>
                  <logic:equal value="0" name="i">
                    <bean:size id="size" name="value" property="sourceAbbreviations" />
                    <td rowspan="<%=size%>" valign="top">
                      <logic:empty name="value" property="value">
                        &nbsp;
                      </logic:empty>
                      <bean:write name="value" property="value"/>
                    </td>
                  </logic:equal>
                  <td>
                    <bean:write name="source"/>
                  </td>
                  <td>
                    <bean:write name="value" property='<%="count(" + source + ")"%>'/>
                  </td>
                </tr>
              </logic:iterate>
            </logic:iterate>
          </table>
        </td>
        <td valign="top">
          <table border="1" width="100%">
            <logic:iterate id="value" name="diff" property="newValues">
              <logic:iterate id="source" name="value" property="sourceAbbreviations" indexId="i">
                <tr>
                  <logic:equal value="0" name="i">
                    <bean:size id="size" name="value" property="sourceAbbreviations" />
                    <td rowspan="<%=size%>" valign="top">
                      <logic:empty name="value" property="value">
                        &nbsp;
                      </logic:empty>
                      <bean:write name="value" property="value"/>
                    </td>
                  </logic:equal>
                  <td>
                    <bean:write name="source"/>
                  </td>
                  <td>
                    <bean:write name="value" property='<%="count(" + source + ")"%>'/>
                  </td>
                </tr>
              </logic:iterate>
            </logic:iterate>
          </table>
        </td>
      </tr>
    </logic:iterate>
    <TR>
      <TH colspan="4">Relationship Attribute Differences</TH>
    </TR>
    <tr>
      <th>Differences</th>
      <th>Count</th>
      <th>Old Value</th>
      <th>New Value</th>
    </tr>
    <logic:iterate id="diff" name="relationshipAttributeDifferences">
      <tr>
        <td valign="top">
          <bean:write name="diff" property="name"/>
        </td>
        <td valign="top">
          <bean:write name="diff" property="count"/>
        </td>
        <td valign="top">
          <table border="1" width="100%">
            <logic:iterate id="value" name="diff" property="oldValues">
              <logic:iterate id="source" name="value" property="sourceAbbreviations" indexId="i">
                <tr>
                  <logic:equal value="0" name="i">
                    <bean:size id="size" name="value" property="sourceAbbreviations" />
                    <td rowspan="<%=size%>" valign="top">
                      <logic:empty name="value" property="value">
                        &nbsp;
                      </logic:empty>
                      <bean:write name="value" property="value"/>
                    </td>
                  </logic:equal>
                  <td>
                    <bean:write name="source"/>
                  </td>
                  <td>
                    <bean:write name="value" property='<%="count(" + source + ")"%>'/>
                  </td>
                </tr>
              </logic:iterate>
            </logic:iterate>
          </table>
        </td>
        <td valign="top">
          <table border="1" width="100%">
            <logic:iterate id="value" name="diff" property="newValues">
              <logic:iterate id="source" name="value" property="sourceAbbreviations" indexId="i">
                <tr>
                  <logic:equal value="0" name="i">
                    <bean:size id="size" name="value" property="sourceAbbreviations" />
                    <td rowspan="<%=size%>" valign="top">
                      <logic:empty name="value" property="value">
                        &nbsp;
                      </logic:empty>
                      <bean:write name="value" property="value"/>
                    </td>
                  </logic:equal>
                  <td>
                    <bean:write name="source"/>
                  </td>
                  <td>
                    <bean:write name="value" property='<%="count(" + source + ")"%>'/>
                  </td>
                </tr>
              </logic:iterate>
            </logic:iterate>
          </table>
        </td>
      </tr>
    </logic:iterate>
    <TR>
      <TH colspan="4">Term Group Differences</TH>
    </TR>
    <tr>
      <th>Differences</th>
      <th>Count</th>
      <th>Old Value</th>
      <th>New Value</th>
    </tr>
    <logic:iterate id="diff" name="termgroupDifferences">
      <tr>
        <td valign="top">
          <bean:write name="diff" property="name"/>
        </td>
        <td valign="top">
          <bean:write name="diff" property="count"/>
        </td>
        <td valign="top">
          <table border="1" width="100%">
            <logic:iterate id="value" name="diff" property="oldValues">
              <logic:iterate id="source" name="value" property="sourceAbbreviations" indexId="i">
                <tr>
                  <logic:equal value="0" name="i">
                    <bean:size id="size" name="value" property="sourceAbbreviations" />
                    <td rowspan="<%=size%>" valign="top">
                      <logic:empty name="value" property="value">
                        &nbsp;
                      </logic:empty>
                      <bean:write name="value" property="value"/>
                    </td>
                  </logic:equal>
                  <td>
                    <bean:write name="source"/>
                  </td>
                  <td>
                    <bean:write name="value" property='<%="count(" + source + ")"%>'/>
                  </td>
                </tr>
              </logic:iterate>
            </logic:iterate>
          </table>
        </td>
        <td valign="top">
          <table border="1" width="100%">
            <logic:iterate id="value" name="diff" property="newValues">
              <logic:iterate id="source" name="value" property="sourceAbbreviations" indexId="i">
                <tr>
                  <logic:equal value="0" name="i">
                    <bean:size id="size" name="value" property="sourceAbbreviations" />
                    <td rowspan="<%=size%>" valign="top">
                      <logic:empty name="value" property="value">
                        &nbsp;
                      </logic:empty>
                      <bean:write name="value" property="value"/>
                    </td>
                  </logic:equal>
                  <td>
                    <bean:write name="source"/>
                  </td>
                  <td>
                    <bean:write name="value" property='<%="count(" + source + ")"%>'/>
                  </td>
                </tr>
              </logic:iterate>
            </logic:iterate>
          </table>
        </td>
      </tr>
    </logic:iterate>
  </TABLE>
  <meme:footer name="Brian Carlsen" email="bcarlsen@apelon.com" url="/" text="Meta News Home"/>
</body>
</html:html>
