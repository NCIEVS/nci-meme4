<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/mrdocCollection">


    <center>
	  <a name="sb_2" id="sb_2"></a><h3>B2 Attribute Names</h3>
    </center>
    <br/>

  <table cellspacing="0" cellpadding="0" width="100%"
		summary="This table charts the attribute names and definitions">

  <tr><td valign="top">
  	<xsl:for-each select="//entry/dockey[not(. = following::entry/dockey)]">
		<xsl:sort select="." order="ascending"/>

		<xsl:if test=".='ATN' or .='COA'">
		<table cellspacing="0" cellpadding="0" width="100%" border="1">

		    <tr><td colspan="2" bgcolor="#DDDDDD" align="center" valign="top">
				<xsl:variable name="t" select="."/>
				<strong><a name="mrdoc_{$t}" id="mrdoc_{$t}"></a></strong>
		  	    <xsl:value-of select="."/>
				<xsl:if test=".='ATN'">
				  <xsl:text> &#160; (Attribute Name)</xsl:text>
				</xsl:if>
				<xsl:if test=".='COA'">
				  <xsl:text> &#160; (Co-Occurrence Attribute)</xsl:text>
				</xsl:if>
			  </td></tr>


		  <xsl:for-each select="//entry[dockey=current()]">
		    <xsl:if test="type[contains(., 'expanded_form')]">
		      <tr><td valign="top" width="20%">
				  <xsl:variable name="t" select="dockey"/>
				  <xsl:variable name="mk" select="value"/>
		  	      <a name="mrdoc_{$t}_{$mk}" id="mrdoc_{$t}_{$mk}"></a>
				  <xsl:value-of select="value"/>
		        </td>
			    <td valign="top" width="80%">
		          <xsl:if test="string(expl)">
		  	       <xsl:value-of select="expl"/>
		          </xsl:if>
			    </td>
		      </tr>
		   </xsl:if>
		  </xsl:for-each>

	    </table>
	  	<br/>
	  	<br/>
		</xsl:if>
  		</xsl:for-each>
	 </td></tr>
    </table>

	<center>
	  <hr width="550"/>
	  <a href="/research/umls/UMLSDOC.HTML">Return to Table of
			Contents</a>
	</center>

</xsl:template>

</xsl:stylesheet>

