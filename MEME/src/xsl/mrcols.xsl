<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/mrcolsCollection">

	<center>
		<p><a name="sb_0" id="sb_0"></a><strong>APPENDIX B</strong>
		<br/>
		<br/>
		<strong>METATHESAURUS<sup>&#174;</sup> METADATA</strong></p>
	</center>

	<p><strong>B.0 Introduction</strong></p>
	<p>Appendix B provides details on Metathesaurus data described
		in Section 2 of the documentation, which contains many
		references to this Appendix.  This appendix
		contains descriptions of:</p>

	<dl>
		<dt>B.1 <strong>&#160;&#160;Columns and Data Elements</strong></dt>
		<dd>Lists column and data element abbreviations, names
			and descriptions in Metathesaurus files in alphabetical
			order by abbreviation; includes length of characters and
			SQL92 datatype information</dd>
	</dl>

	<dl>
		<dd>B.1.1  Columns and Data Elements in Rich
			Release Format (RRF)</dd>
		<dd>B.1.2  Columns and Data Elements in Original
			Release Format (ORF)</dd>
	</dl>

	<dl>
		<dt>B.2 <strong>&#160;&#160; Attribute Names</strong></dt>
		<dd>Lists attribute names and definitions in
			alphabetical order by abbreviation</dd>
	</dl>

	<dl>
		<dt>B.3 <strong>&#160;&#160; Abbreviations Used in
				Data Elements </strong></dt>
		<dd>Lists abbreviations and definitions of
			abbreviations used in data elements alphabetically
			by attribute type; includes relationship attributes</dd>
	</dl>

	<dl>
		<dt>B.4 <strong>&#160;&#160; Source Vocabularies</strong></dt>
		<dd>Lists vocabularies and classifications that are the
			sources of the concepts, terms and relationships in
			the Metathesaurus alphabetically by source
			abbreviation; includes the number of strings each
			contributes.  HIPAA and CHI vocabularies are noted.</dd>
	</dl>

	<dl>
		<dt>B.5 <strong>&#160;&#160; Source and Term Types:  Default
			Order of Precedence and Suppressibility </strong></dt>
		<dd>Lists sources and term types in default order of rank,
			or precedence, used to determine referred names in
			the Metathesaurus, and notes the default
			suppressibility status (yes or no) assigned to each
			Source|Term Type in the Metathesaurus.</dd>
	</dl>


	<hr/>

<!-- Begin second Table -->

	<h2>B.1  Columns and Data Elements</h2>

	<p>All data elements in the Metathesaurus are described in
		this section. The data elements have been divided into
		Column Descriptions and Attribute Descriptions. The
		descriptions are arranged alphabetically by data element
		abbreviation.</p>

	<p>Columns are described for Rich Release Format (RRF) in
		B.1.1, and for Original Release Format (ORF) in B.1.2.</p>

	<a name="sb_1.1" id="sb_1.1"></a>
	<h3>B1.1 Columns and Data Elements in Rich Release Format (RRF)</h3>


	<table border="1" width="100%" cellpadding="0" cellspacing="0"
		summary="This table charts the abbreviation, description, file, length of value in characters, average length of value in characters, and SQL92 datatype for all data elements in Metathesaurus files in the Rich Release Format">

	<thead>
	  <tr>
		<th id="a1" width="10%" align="center" valign="middle">
			<font size="-1">Abbreviation</font></th>
		<th id="a2" width="50%" align="center" valign="middle">
			<font size="-1">Description</font></th>
		<th id="a3" width="10%" align="center" valign="middle">
			<font size="-1">File</font></th>
		<th id="a4" width="5%" align="center" valign="middle">
			<font size="-1">Length of Value in characters</font></th>
		<th id="a5" width="5%" align="center" valign="top">
			<font size="-1">Average Length of Value in characters</font></th>
		<th id="a6" width="20%" align="center" valign="middle">
			<font size="-1">SQL92 Datatype</font></th>
	  </tr>
	</thead>

	<tbody>


  	<xsl:for-each select="//entry/col[not(. = following::entry/col)]">
	  <xsl:sort select="." order="ascending"/>

	    <tr>
	  	  <td headers="a1" width="10%" align="left" valign="top">
		    <xsl:variable name="t" select="//entry[col=current()]/col"/>
		    <a name="mrcols_{$t}" id="mrcols_{$t}"></a>
			<strong>
	  	     <xsl:value-of select="//entry[col=current()]/col"/>
			</strong>
		  </td>

		  <td headers="a2" width="50%" align="left" valign="top">
	  	     <xsl:value-of select="//entry[col=current()]/des"/>
		  </td>

		  <td headers="a3" width="10%" valign="top" align="center">
			<font size="-1">
	        <xsl:for-each select="//entry[col=current()]">
			    <xsl:value-of select="fil"/>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		    </xsl:for-each>
		  </font></td>

		  <td headers="a4" width="5%" valign="top" align="center">
			  <font size="-1">
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

		  <td headers="a5" width="5%" valign="top" align="center">
			  <font size="-1">
	          <xsl:for-each select="//entry[col=current()]">
			    <xsl:value-of select="av"/>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		      </xsl:for-each>
		  </font></td>

		  <td headers="a6" width="20%" valign="top" align="center">
			  <font size="-1">
	          <xsl:for-each select="//entry[col=current()]">
			    <xsl:value-of select="dty"/>
				<xsl:if test="position()!=last()"><br/></xsl:if>
		      </xsl:for-each>
		  </font></td>

	      </tr>
	    </xsl:for-each>

	  </tbody>
    </table>

</xsl:template>

</xsl:stylesheet>
