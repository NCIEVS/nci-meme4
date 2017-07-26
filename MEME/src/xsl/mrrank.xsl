<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/mrrankCollection">

	<h3><strong><a name="sb_5" id="sb_5"></a>B.5  Source and Term Type
		Default Order of Precedence and Suppressibility</strong></h3>

	<br/><br/>

	<p>This Appendix displays the default order of Source|Term
		Types and suppressibility as set by NLM and distributed
		in the Metathesaurus&#174; in MRRANK.RRF or MRRANK in ORF.
		</p>

	<p>Effective with the <xsl:value-of select="release"/> release,
		MTH|MM is no longer assigned to
		Metathesaurus strings with multiple meanings and has been deleted
		from this list.  Ambiguous strings are identified in the
		AMBIGLUI.RRF (AMBIG.LUI in ORF) and AMBIGSUI.RRF
		(AMBIG.SUI in ORF) files.</p>

	<p>Users are encouraged to change the order of
		Source|Term Type  precedence and suppressibility to
		suit their requirements.  The default settings will
		not be suitable for all applications. The highest ranking
		Source|Term Type within a concept determines the preferred
		name for that concept.  Use MetamorphoSys
		(Section 6) to change the selection of
		preferred names or to alter suppressibility. </p>


  <table cellspacing="1" cellpadding="1">
	<tr>
		<th bgcolor="#DDDDDD" width="200" align="left" valign="top">
			Source Abbreviation
		  </th>
		<th bgcolor="#DDDDDD" width="140" align="left" valign="top">
			Term Type
		  </th>
		<th bgcolor="#DDDDDD" width="140" align="center" valign="top">
			Suppressible
		  </th>
	</tr>

  	<xsl:for-each select="//entry">
	  <xsl:sort select="./rank" order="descending"/>

	    <tr>
		  <td valign="top"><xsl:value-of select="./sab"/></td>
	      <td valign="top"><xsl:value-of select="./tty"/></td>
		  <td valign="top">
			<xsl:if test="./suppress[contains(., 'N')]">No</xsl:if>
			<xsl:if test="./suppress[contains(., 'Y')]">
				<font color="#00cc00">Yes</font></xsl:if>
		</td></tr>

	</xsl:for-each>

  </table>


	<center>
	  <hr width="550"/>
	  <a href="/research/umls/UMLSDOC.HTML">Return to Table of
			Contents</a>
	</center>

</xsl:template>

</xsl:stylesheet>

