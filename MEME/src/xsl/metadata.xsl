<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/metadataCollection">
    <a name="sb6_0" id="sb6_0">    </a>
    <h3>
      <strong>B.6 Release Metadata</strong>
    </h3>
    <p>
      <strong>Official Counts:</strong>
      <br/>
      Release version:
      &#160;&#160;
      <xsl:value-of select="release"/>
      <br/>
      Release format:
      &#160;&#160;
      RRF
      <br/>
      Concepts:
      &#160;&#160;
      <xsl:number value="concepts" grouping-separator="," grouping-size="3"/>
      <br/>
      Number of concept names (AUIs):
      &#160;&#160;
      <xsl:number value="auis" grouping-separator="," grouping-size="3"/>
      <br/>
      Number of distinct concept names (SUIs):
      &#160;&#160;
      <xsl:number value="suis" grouping-separator="," grouping-size="3"/>
      <br/>
      Number of distinct normalized concept names (LUIs):
      &#160;&#160;
      <xsl:number value="luis" grouping-separator="," grouping-size="3"/>
      <br/>
      Number of sources (distinct source families by language):
      &#160;&#160;
      <xsl:value-of select="sources_by_language"/>
      <br/>
      Number of sources contributing concept names:
      &#160;&#160;
      <xsl:value-of select="sources"/>
      <br/>
      Number of languages contributing concept names:
      &#160;&#160;
      <xsl:value-of select="languages"/>
    </p>
    <xsl:variable name="auis" select="auis"/>
    <xsl:variable name="sources" select="sources"/>
    <p>
      <strong>Name Count by Language:</strong>
    </p>
    <table border="1" cellpadding="1" cellspacing="1" width="330" summary="This table charts lists the language, name count and percentage of Metathesaurus">
      <thead>
        <tr valign="middle" align="center">
          <th id="a1" width="150">Language</th>
          <th id="a2" width="150">Name Count</th>
          <th id="a3" width="150">% of Metathesaurus</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="//name_by_language">
          <xsl:sort select="./count" order="descending" data-type="number"/>
          <tr>
            <td headers="a1" width="110" valign="middle" align="center">
              <xsl:value-of select="language"/>
            </td>
            <td headers="a2" width="110" valign="middle" align="right">
              <xsl:value-of select="count"/>
            </td>
            <td headers="a3" width="110" valign="middle" align="right">
              <xsl:value-of select='format-number(count div $auis,"#.##%")'/>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
    <p>
      <strong>Name Count by Source Restriction Level (SRL):</strong>
    </p>
    <table border="1" cellpadding="1" cellspacing="1" width="330" summary="This table charts lists the Source Restriction Level, concept count and percentage of Metathesaurus">
      <thead>
        <tr valign="middle" align="center">
          <th id="b1" width="150">SRL</th>
          <th id="b2" width="150">Source Count</th>
          <th id="b3" width="150">% of Sources</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="//name_by_srl">
          <xsl:sort select="./srl" order="ascending" data-type="number"/>
          <tr>
            <td headers="b1" width="110" valign="middle" align="center">
              <xsl:value-of select="srl"/>
            </td>
            <td headers="b2" width="110" valign="middle" align="right">
              <xsl:value-of select="count"/>
            </td>
            <td headers="b3" width="110" valign="middle" align="right">
              <xsl:value-of select='format-number(count div $auis,"#.##%")'/>
            </td>
          </tr>
        </xsl:for-each>
          <tr>
            <td headers="c1" width="110" valign="middle" align="center">
              0+4
            </td>
            <td headers="c2" width="110" valign="middle" align="right">
              <xsl:value-of select="sum(//name_by_srl[srl = 0 or  srl = 4]/count)"/>
            </td>
            <td headers="c3" width="110" valign="middle" align="right">
              <xsl:value-of select='format-number(sum(//name_by_srl[srl = 0 or  srl = 4]/count) div $auis,"#.##%")'/>
            </td>
          </tr>
      </tbody>
    </table>
    <p>
      <strong>Count of Atoms by Suppressibility:</strong>
    </p>
    <table border="1" cellpadding="1" cellspacing="1" width="330" summary="This table charts lists the Source Restriction Level, concept count and percentage of Metathesaurus">
      <thead>
        <tr valign="middle" align="center">
          <th id="c1" width="150">Suppressibility Status</th>
          <th id="c2" width="150">Name Count</th>
          <th id="c3" width="150">% of Metathesaurus</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="//atom_by_suppress">
          <tr>
            <td headers="c1" width="110" valign="middle" align="center">
              <xsl:value-of select="suppress"/>
            </td>
            <td headers="c2" width="110" valign="middle" align="right">
              <xsl:value-of select="count"/>
            </td>
            <td headers="c3" width="110" valign="middle" align="right">
              <xsl:value-of select='format-number(count div $auis,"#.##%")'/>
            </td>
          </tr>
        </xsl:for-each>
        </tbody>
    </table>
    <p>
      <strong>Source Counts by Language (from MRSAB):</strong>
    </p>
    <table border="1" cellpadding="1" cellspacing="1" width="330" summary="This table charts lists the language, name count and percentage of Metathesaurus">
      <thead>
        <tr valign="middle" align="center">
          <th id="d1" width="110">Language</th>
          <th id="d2" width="110">Name count</th>
          <th id="d3" width="110">% of Metathesaurus</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="//source_by_language">
          <xsl:sort select="./count" order="descending" data-type="number"/>
          <tr>
            <xsl:if test="./source = 'null'">
            <td headers="d1" width="110" valign="middle" align="center">
              *
            </td>
            </xsl:if>
            <xsl:if test="./source != 'null'">
            <td headers="d1" width="110" valign="middle" align="center">
              <xsl:value-of select="source"/>
            </td>
            </xsl:if>
            <td headers="d2" width="110" valign="middle" align="right">
              <xsl:value-of select="count"/>
            </td>
            <td headers="d3" width="110" valign="middle" align="right">
              <xsl:value-of select='format-number(count div $sources,"#.##%")'/>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
        <xsl:for-each select="//source_by_language">
            <xsl:if test="./source = 'null'">
<p>*Note: <xsl:value-of select="count"/> sources contribute relationships which have no associated language values.</p>
            </xsl:if>
        </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
