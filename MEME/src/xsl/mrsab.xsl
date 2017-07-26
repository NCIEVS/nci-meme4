<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/mrsabCollection">

	<a name="sb_4" id="sb_4"></a><h3><strong>B.4 Source
  		Vocabularies</strong></h3><br/>

  	<p>All sources that contribute strings or relationships to
	the <xsl:value-of select="release"/>
	Metathesaurus are listed. Each entry includes the:
  		<ul>
    		<li>RSAB: &#160;&#160; &#160;&#160; &#160;&#160;&#160;&#160;
				Root Source
    		Abbreviation </li>
    		<li>VSAB: &#160;&#160; &#160;&#160; &#160;&#160;&#160;&#160;
				Versioned Source Abbreviation </li>
    		<li>Source Official Name </li>
    		<li>Citation:
				&#160;&#160; &#160;&#160; &#160;&#160;&#160;&#160;
				Publisher name, date and place of publication,
				and contact </li>
    		<li>Number of strings included from this source </li></ul>
	</p>
	<p>
	HIPAA or CHI standard vocabularies are identified.
	</p>

	<p>
	  <table>
  		<tbody>
		<tr><td> <ul> <li>HIPAA standard:</li> </ul></td>
		  <td><xsl:text>&#160;&#160;&#160;&#160;</xsl:text></td>
		  <td>national standard for electronic health care
				transactions established by the Department
    			of Health and Human Services under the
				Administrative Simplificationprovisions
    			of the Health Insurance Portability and
				Accountability Act of 1996 (HIPAA, Title II)
		</td></tr>
		<tr><td> <ul> <li>CHI standard:</li> </ul></td>
		  <td><xsl:text>&#160;&#160;&#160;&#160;</xsl:text></td>
    	  <td>standard for use in U.S. Federal Government
				systems for the electronic exchange of clinical
				health information
			</td></tr>
  		</tbody>
	  </table>
	</p>

  	<p>Additional information on the status or use of some
		vocabularies in the Metathesaurus is also noted</p>

  	<hr/>
  <table cellspacing="0" cellpadding="5">
  	<tbody>
    <tr>
  	<th align="left" valign="top">RSAB</th>
  	<th align="left" valign="top"></th>
  	<th align="left" valign="top">VSAB</th>
  	<th align="left" valign="top">Source Official Name</th>
    </tr>

    <xsl:for-each select="//entry[./curver = 'Y']">
	  <xsl:sort select="./rsab"/>
	<tr>
		<th align="left" valign="top" width="10%">
		  <xsl:variable name="t" select="rsab"/>
		  <a name="mrsab_{$t}" id="mrsab_{$t}"></a><xsl:value-of select="rsab"/>
                  <xsl:if test="string(vsabtype)">
			<br/>
                          (<xsl:value-of select="vsabtype"/>)
                  </xsl:if>
		</th>
		<th align="left" valign="top" width="5%"></th>
		<th align="left" valign="top" width="10%">
		  <xsl:value-of select="vsab"/>
		</th>
		<th align="left" valign="top" width="65%">
		  <xsl:value-of select="son"/>
		</th>
	</tr>

    <tr>
	  <td></td>
	  <td colspan="3" valign="top" align="left"><font size="-1">
		  <xsl:apply-templates select="scit"/></font></td>
	</tr>

	<tr><td></td>
	    <td></td>
		<td colspan="2" align="left"><font size="-1">
		  <xsl:if test="tfr">
		   <xsl:if test="string(tfr)">
			Number of Strings:
			  <xsl:value-of select="tfr"/><br/>
		   </xsl:if>
		  </xsl:if>
                    <xsl:if test="string(cxty)">
			Context:
			  <xsl:value-of select="cxty"/><br/>
			  <xsl:apply-templates select="."/>
		   </xsl:if>
		  </font></td>
		</tr>

	<tr><td colspan="4">
		<hr/>
	</td></tr>

  	</xsl:for-each>
	</tbody>
  </table>

	<center>
	  <a href="/research/umls/UMLSDOC.HTML">Return to Table of
			Contents</a>
	</center>

</xsl:template>

<xsl:template match="entry">
	<xsl:choose>
	  <xsl:when test="rsab='CDT' or rsab='CPT' or rsab='HCDT' or rsab='HCPCS' or rsab='HCPT' or rsab='ICD9CM' or rsab='MTHFDA' or rsab='NCI' or rsab='VANDF'">
		HIPAA standard<br/>
	  </xsl:when>
	</xsl:choose>

	<xsl:choose>
	  <xsl:when test="rsab='CDT' or rsab='CPT' or rsab='HCDT' or rsab='HCPCS' or rsab='HCPT' or rsab='HL7' or rsab='ICD9CM' or rsab='LNC' or rsab='MTHFDA' or rsab='RXNORM' or rsab='SNOMEDCT_US'">
		CHI standard<br/>
	  </xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template match="scit">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="LINK">
	<xsl:if test="@target">
<!--Target attribute specified.-->
	<xsl:call-template name="htmLink">
<xsl:with-param name="dest" select="@target"/>
<!--Destination = attribute value-->
</xsl:call-template>
</xsl:if>
	<xsl:if test="not(@target)">
<!--Target attribute not specified.-->
	<xsl:call-template name="htmLink">
	<xsl:with-param name="dest">
<xsl:apply-templates/>
<!--Destination value = text of node-->
</xsl:with-param>
</xsl:call-template>
</xsl:if>
</xsl:template>
<!-- A named template that constructs an HTML link -->
	<xsl:template name="htmLink">
<xsl:param name="dest" select="UNDEFINED"/>
<!--default value-->
	<xsl:element name="a">
	<xsl:attribute name="href">
<xsl:value-of select="$dest"/>
<!--link target-->
</xsl:attribute>
<xsl:apply-templates/>
<!--name of the link from text of node-->
</xsl:element>
</xsl:template>

</xsl:stylesheet>

