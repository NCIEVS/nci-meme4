<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/mrcols_orfCollection">

  <br/><br/><hr/><br/><br/>

  <a name="sb_1.2" id="sb_1.2"></a>
  <h3>B.1.2 Columns and Data Elements in Original Release Format (ORF)</h3>

  <table summary="This table charts the abbreviation, description, file, length of value in characters, average length of value in characters, and SQL92 datatype for all data elements in Metathesaurus files in the Original Release Format"
		width="100%" cellspacing="0" cellpadding="0" border="1">

	<thead>
	  <tr>
	    <th valign="middle" align="center" width="10%" id="b1">
		  <font size="-1">
		    Abbreviation</font></th>
	    <th valign="middle" align="center" width="50%" id="b2">
		  <font size="-1">
		    Description</font></th>
	    <th valign="middle" align="center" width="10%" id="b3">
		  <font size="-1">
		    File</font></th>
	    <th valign="top" align="center" width="5%" id="b4">
		  <font size="-1">
		    Length of Value in characters</font></th>
	    <th valign="middle" align="center" width="5%" id="b5">
		  <font size="-1">
		    Average Length of Value in characters</font></th>
	    <th valign="middle" align="center" width="20%" id="b6">
		  <font size="-1">
		    SQL92 Datatype</font></th></tr>
	</thead>

	<tbody>

  	<xsl:for-each select="//entry/col[not(. = following::entry/col)]">
	  <xsl:sort select="." order="ascending"/>

	    <tr>
	  	  <td valign="top" align="left" headers="b1">
		    <xsl:variable name="t" select="//entry[col=current()]/col"/>
		    <a name="mrcols_orf_{$t}" id="mrcols_orf_{$t}"></a>
	  	     <xsl:value-of select="//entry[col=current()]/col"/></td>
		  <td valign="top" align="left" headers="b2">
	  	     <xsl:value-of select="//entry[col=current()]/des"/>
		  </td>

		  <td valign="top" align="center" headers="b3"><font size="-1">
	        <xsl:for-each select="//entry[col=current()]">
			    <xsl:value-of select="fil"/>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		    </xsl:for-each>
		  </font></td>

		  <td valign="top" align="center" headers="b4"><font size="-1">
	          <xsl:for-each select="//entry[col=current()]">
		        <xsl:if test="min != max">
			      <xsl:value-of select="min"/> -
			      <xsl:value-of select="max"/>
		        </xsl:if>
		        <xsl:if test="min = max">
			      <xsl:value-of select="min"/>
		        </xsl:if>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		      </xsl:for-each>
		    </font></td>

		  <td valign="top" align="center" headers="b5"><font size="-1">
	          <xsl:for-each select="//entry[col=current()]">
			    <xsl:value-of select="av"/>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		      </xsl:for-each>
		  </font></td>

		  <td valign="top" align="center" headers="b6"><font size="-1">
	          <xsl:for-each select="//entry[col=current()]">
			    <xsl:value-of select="dty"/>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		      </xsl:for-each>
		  </font></td>

	      </tr>
	    </xsl:for-each>

	  </tbody>
    </table>

	<center>
	  <hr width="550"/>
	  <a href="/research/umls/UMLSDOC.HTML">Return to Table of
			Contents</a>
	</center>

</xsl:template>

</xsl:stylesheet>

