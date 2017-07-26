<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/mrdocCollection">

    <center>
	  <a name="sb_3" id="sb_3"></a><h3>B3 Abbreviations Used in Data Elements</h3>
    </center>
  <br/>

  <table cellspacing="0" cellpadding="0" width="100%"
		summary="This table charts the attribute names and definitions">

  <tr><td valign="top">
  	<xsl:for-each select="//entry/dockey[not(. = following::entry/dockey)]">
		<xsl:sort select="." order="ascending"/>

		<xsl:if test=".!='ATN' and .!='COA' and not(contains(.,'CVF_')) ">
		<table cellspacing="0" cellpadding="0" width="100%" border="1">

		    <tr><td colspan="2" bgcolor="#DDDDDD" align="center" valign="top">
				<xsl:variable name="t" select="."/>
				<a name="mrdoc_{$t}" id="mrdoc_{$t}"></a>
		  	    <strong><xsl:value-of select="."/></strong>
				<xsl:if test=".='COT'">
				  <xsl:text> &#160; (Type of Co-Occurrence)</xsl:text>
				</xsl:if>
				<xsl:if test=".='FROMTYPE'"><br/>
				  <xsl:text> (Type of Expression from Which a
						Mapping is Mapped)</xsl:text>
				</xsl:if>
				<xsl:if test=".='LAT'">
				  <xsl:text> &#160; (Language of Terms)</xsl:text>
				</xsl:if>
				<xsl:if test=".='REL'">
				  <xsl:text> &#160; (Relationship)</xsl:text>
				</xsl:if>
				<xsl:if test=".='RELA'">
				  <xsl:text> &#160; (Relationship Attribute)</xsl:text>
				</xsl:if>
				<xsl:if test=".='STT'">
				  <xsl:text> &#160; (String Type)</xsl:text>
				</xsl:if>
				<xsl:if test=".='STYPE'"><br/>
				  <xsl:text> &#160; (Column name in MRCONSO.RRF or MRREL.RRF
					with identifier to which attribute is attached)</xsl:text>
				</xsl:if>
				<xsl:if test=".='STYPE1'"><br/>
				  <xsl:text> &#160; (Column name in MRCONSO.RRF
					with first identifier to which relationship
					is attached)</xsl:text>
				</xsl:if>
				<xsl:if test=".='STYPE2'"><br/>
				  <xsl:text> &#160; (Column name in MRCONSO.RRF
					with second identifier to which relationship
					is attached)</xsl:text>
				</xsl:if>
				<xsl:if test=".='TOTYPE'"><br/>
				  <xsl:text> (Type of Expression to Which a
						Mapping is Mapped)</xsl:text>
				</xsl:if>
				<xsl:if test=".='TS'">
				  <xsl:text> &#160; (Term Status)</xsl:text>
				</xsl:if>
				<xsl:if test=".='TTY'">
				  <xsl:text> &#160; (Term Type in Source)</xsl:text>
				</xsl:if>
			  </td></tr>


		  <xsl:for-each select="//entry[dockey=current()]">
		    <xsl:if test="type[contains(., 'expanded_form')]">
		      <tr><td valign="top" width="20%">
				  <xsl:variable name="t" select="dockey"/>
				  <xsl:variable name="mk" select="value"/>
				  <xsl:variable name="type" select="type"/>
				  <xsl:variable name="value" select="expl"/>
		  	      <a name="mrdoc_{$t}_{$mk}_{$type}" id="mrdoc_{$t}_{$mk}_{$type}"></a>
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

		  <xsl:for-each select="//entry[dockey=current()]">
		    <xsl:if test="type[contains(., 'content_view')]">
		      <tr><td valign="top" width="20%">
				  <xsl:variable name="t" select="dockey"/>
				  <xsl:variable name="mk" select="value"/>
				  <xsl:variable name="value" select="expl"/>
		  	      <a name="mrdoc_{$t}_{$mk}_{$value}" id="mrdoc_{$t}_{$mk}_{$value}"></a>
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


	<!-- now put in the table containing the tty_class grouping -->
  <table width="100%" cellspacing="0" cellpadding="0" border="1">
  	<xsl:for-each select="//entry[contains(./dockey, 'TTY') and contains(./type, 'tty_class')]">
	  <xsl:sort select="." order="ascending"/>

		  <xsl:variable name="type" select="type"/>
		  <xsl:if test="position() = 1">
		    <tr><th colspan="2" bgcolor="#DDDDDD" align="center" valign="top">
				<xsl:variable name="t" select="./dockey"/>
				<a name="{$t}" id="{$t}"></a>
		  	    <xsl:value-of select="./dockey"/> (tty_class)
			  </th></tr>
		  </xsl:if>
		    <tr><td valign="top" width="20%" >
				<xsl:variable name="t" select="./dockey"/>
				<xsl:variable name="mk" select="./value"/>
                                <xsl:if test="not(following::dockey = $t and following::value = $mk and following::type = $type)">
                                  <a name="{$t}.{$mk}.{$type}" id="{$t}.{$mk}.{$type}"></a>
                                </xsl:if>
				<xsl:value-of select="./value"/>
		      </td>
			  <td valign="top" width="80%">
		  	    <xsl:value-of select="./expl"/>
			  </td>
		    </tr>
	</xsl:for-each>
  </table>

	<center>
	  <hr width="550"/>
	  <a href="/research/umls/UMLSDOC.HTML">Return to Table of
			Contents</a>
	</center>

</xsl:template>

</xsl:stylesheet>

