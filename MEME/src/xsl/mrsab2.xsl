<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/mrsabCollection">

	<p align="center"><a name="saa1" id="saa1"></a><strong>APPENDIX A.1
  		</strong></p>

	<p align="center"><strong>Appendix to the License Agreement for Use
			of the UMLS<sup>&#174;</sup> Metathesaurus
  		</strong></p>

	<p align="center">UMLS METATHESAURUS<sup>&#174;</sup>
			SOURCE VOCABULARIES - <xsl:value-of select="release"/>
					Edition</p>

  	<p>Sources are listed in order according to the abbreviations
		used in the UMLS Metathesaurus files. If additional restrictions
		and notices apply, the category of restrictions and the special
		notices appear under the name of the source. See the license
		agreement for an explanation of the categories of restrictions.
		Many sources publish printed editions and/or other explanatory
		information that may be essential to understanding the purpose
		and application of particular sources in data creation and
		retrieval. Contact information is provided for each source.
		Please address questions about permissions or license agreements
		for additional uses not covered by this Agreement, or other
		inquiries about individual sources, to the appropriate
		contacts.</p>
	<p>NLM is working toward inclusion in the UMLS Metathesaurus of
		the complete, current edition of most of these vocabulary
		sources.</p>

	<hr/>

  	<xsl:for-each select="//entry">
	  <xsl:sort select="./rsab"/>

	<p><strong>
		  <xsl:variable name="t" select="rsab"/>
		  <a name="mrsab_{$t}" id="mrsab_{$t}"></a><xsl:value-of select="vsab"/>
		</strong>
		<xsl:if test="string(vsabtype)">
			<xsl:text>&#160;(</xsl:text>
			<xsl:value-of select="vsabtype"/>
			<xsl:text>)</xsl:text>
		</xsl:if>
		<xsl:text> - </xsl:text>
		<xsl:apply-templates select="scit"/></p>

	<xsl:if test="./srl[contains(., '1')]">
		<p><a href="/research/umls/license.html&#35;category1">CATEGORY 1 RESTRICTIONS APPLY</a></p>
	</xsl:if>

	<xsl:if test="./srl[contains(., '2')]">
		<p><a href="/research/umls/license.html&#35;category2">CATEGORY 2 RESTRICTIONS APPLY</a></p>
	</xsl:if>

	<xsl:if test="./srl[contains(., '3')]">
		<p><a href="/research/umls/license.html&#35;category3">CATEGORY 3 RESTRICTIONS APPLY</a></p>
	</xsl:if>

	<xsl:if test="./srl[contains(., '4')]">
		<p><a href="/research/umls/license.html&#35;category4">CATEGORY 4 RESTRICTIONS APPLY</a> to U.S. UMLS USERS</p>
		<p><a href="/research/umls/license.html&#35;category3">CATEGORY 3 RESTRICTIONS APPLY</a> to Non-U.S. UMLS USERS</p>
	</xsl:if>
	
	<xsl:if test="./srl[contains(., '5')]">
		<p><a href="/research/umls/license.html&#35;category4">CATEGORY 5 RESTRICTIONS APPLY</a></p>
	</xsl:if>
	
	<xsl:if test="./srl[contains(., '6')]">
		<p><a href="/research/umls/license.html&#35;category4">CATEGORY 6 RESTRICTIONS APPLY</a></p>
	</xsl:if>
	
	<xsl:if test="./srl[contains(., '7')]">
		<p><a href="/research/umls/license.html&#35;category4">CATEGORY 7 RESTRICTIONS APPLY</a></p>
	</xsl:if>
	
	<xsl:if test="./srl[contains(., '8')]">
		<p><a href="/research/umls/license.html&#35;category4">CATEGORY 8 RESTRICTIONS APPLY</a></p>
	</xsl:if>
	
	<xsl:if test="./srl[contains(., '9')]">
		<p>TERMS IN APPENDIX 2 OF UMLS LICENSE APPLY.</p>
	</xsl:if>
	<xsl:apply-templates select="." mode="subinfo"/>


	<p>
		Contact: <xsl:apply-templates select="slc"/>
	</p>
	<p>
	 <xsl:variable name="t1" select="rsab"/>
		  <a href="http://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/{$t1}/">Read more information about this source</a>
	</p>
		<hr/>

  	</xsl:for-each>
</xsl:template>

<xsl:template match="//entry" mode="subinfo">
    <xsl:choose>
      <xsl:when test="rsab='CCPSS'">
        <p>Permission will be freely given for any uses and applications
			containing CCPSS which are not for sale - i.e. those used
			internally or given to others without charge.</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='CDT'">
        <p>For CDT the following special notice must be displayed:
			<br/>
			"For CDT only, copyright 2002 American Dental Association,
			all rights reserved."</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='COSTAR'">
        <p>The UMLS Metathesaurus includes terms that were used
			frequently at 3 COSTAR sites in the years indicated and
			supplied to NLM by Massachusetts General Hospital.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='CPM'">
        <p>The UMLS Metathesaurus includes a relatively small number
			of terms created at Columbia Presbyterian Medical
			Center for the MED, which also includes terms
			obtained from the UMLS Metathesaurus and other sources.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='CPT'">
        <p>The following special notic must be displayed:
			</p>
        <p>"CPT&#8482; only Copyright 2004 American Medical
			Association. All rights Reserved. No fee schedules,
			basic unit, relative values or related listings
			are included in CPT&#8482;. AMA does not directly
			or indirectly practice medicine or dispense medical
			services. AMA assumes no liability for data contained
			herein.
			</p>
        <p>U.S. Government Rights
			</p>
        <p>This product includes CPT&#8482; which is commercial
			technical data and/or computer data bases and/or
			commercial computer software and/or commercial
			computer software documentation, as applicable
			which were developed exclusively at private expense
			by the American Medical Association, 515 North State
			Street, Chicago, Illinois, 60610. U.S. Government
			rights to use, modify, reproduce, release, perform,
			display, or disclose these technical data and/or
			computer data bases and/or computer software and/or
			computer software documentation are subject to the
			limited rights restrictions of DFARS 252.227-7015(b)(2)
			(June 1995) and/or subject to the restrictions of DFARS
			227.7202-1(a) (June 1995) and DFARS 227.7202-3(a)
			(June 1995), as applicable for U.S. Department of
			Defense procurements and the limited rights
			restrictions of FAR 52.227-14 (June 1987) and/or
			subject to the restricted rights provisions of FAR
			52.227-14 (June 1987) and FAR 52.227-19 (June 1987),
			as applicable, and any applicable agency FAR Supplements,
			for non-Department of Defense Federal procurements."
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='CST'">
        <p>COSTART has been superseded by the Medical Dictionary
			for Regulatory Activities (MedDRA) Terminology.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='DSM4'">
        <p>The APA usually charges small administrative fees for
			copyright permissions, but these may be waived for
			research purposes. All users should apply for
			permission in writing or by email to:
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='HCPCS'">
        <p>The American Medical Association's CPT&#8482; codes in
			HCPCS have a Source Abbreviation of HCPT04. The
			American Dental Association's CDT codes in HCPCS
			have a Source Abbreviation of HCDT4.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='ICD9CM'">
        <p>NLM has generated fully specified titles for ICD-9-CM
			codes in cases in which the official ICD- 9-CM titles
			consist of extensions to higher levels in the ICD-9-CM
			hierarchy. The fully specified names were produced with
			reasonable care, but have not yet been reviewed and
			approved by the producers of ICD-9-CM.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='ICPC2E_1998'">
        <p>See reference: Okkes, IM; Jamoulle, M; Lamberts, H;
			Bentzen, N. ICPC-2-E: the electronic version of
			ICPC-2. Differences from the printed version and
			the consequences. Family Practice; 2000; 17:101-107.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='ICPC2AE_1998'">
        <p>See reference: Okkes, IM; Jamoulle, M; Lamberts, H;
			Bentzen, N. ICPC-2-E: the electronic version of
			ICPC-2. Differences from the printed version and
			the consequences. Family Practice; 2000; 17:101-107.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='ICPC'">
        <p>This year, the Metathesaurus has also included translations of ICPC93 in the following languages:
			<ul><li>Basque (ICPCBAQ_1993),</li>
				<li>Danish (ICPCDAN_1993),</li>
				<li>Dutch (ICPCDUT_1993),</li>
				<li>Finnish (ICPCFIN_1993),</li>
				<li>French (ICPCFRE_1993),</li>
				<li>German (ICPCGER_1993),</li>
				<li>Hebrew (ICPCHEB_1993),</li>
				<li>Hungarian (ICPCHUN_1993),</li>
				<li>Italian (ICPCITA_1993),</li>
				<li>Norwegian (ICPCNOR_1993),</li>
				<li>Portuguese (ICPCPOR_1993),</li>
				<li>Spanish (ICPCSPA_1993), and</li>
				<li>Swedish (ICPCSWE_1993).</li></ul>
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='LCH'">
        <p>There are later editions of this source that are
			not reflected in the UMLS Metathesaurus. This
			source has considerable non-biomedical content and
			will never be included in the Metathesaurus in
			its entirety.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MIM'">
        <p>To date the UMLS Metathesaurus contains a relatively
			small amount of data from this source.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MSH'">
        <p>This source has been translated into many languages.
			To date, eight of the translations have been incorporated
			into the UMLS Metathesaurus.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTH'">
        <p>Concept names with this source abbreviation were
			created by NLM to facilitate creation of the UMLS
			Metathesaurus. There are relatively few of them.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHFDA'">
        <p>Concept names with this source abbreviation were
			created by NLM to provide contextual information
			for FDA NDC terms.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHHH'">
        <p>Concept names with this source abbreviation were
			created by NLM to provide contextual information
			for HCPCS.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHICPC2EAE'">
        <p>Concept names with this source abbreviation were
			created by NLM to provide contextual information
			for ICPC2E terms.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHMSTFRE'">
        <p>*<strong>NOTE:</strong> Now a CATEGORY 0.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHMSTITA'">
        <p>*<strong>NOTE:</strong> Now a CATEGORY 0.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHPDQ'">
        <p>These terms were created by NLM to provide contextual
			information for PDQ terms.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='MTHSCTSPA'">
        <p>These terms were created by NLM to provide contextual
			information for Spanish SNOMED Clinical Terms.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='NCI'">
        <p>Subset only.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='RXNORM'">
        <p>This release contains concepts created by the National
			Library of Medicine which express the meaning of a
			drug name in a normalized form. These concepts relate
			the names of orderable medications to a dose form and
			the components of those medications. For further
			discussion, see the article at:
			</p>
		<p>
			<a href="http://umlsinfo.nlm.nih.gov/RxNorm.html">
				http://umlsinfo.nlm.nih.gov/RxNorm.html</a>
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='SCTSPA'">
        <p>Users are required to display the SNOMED copyright and
			trademark notice below if SNOMED information is used
			in their product(s).<br/>
			"This publication includes SNOMED CT, a copyrighted
			work of the College of American Pathologists. &#169;2000,
			2002 College of American Pathologists. This work is
			also protected by patent, U.S. Patent No. 6,438,533.
			SNOMED CT is used by permission of, and under license
			from, the College. SNOMED CT has been created by
			combining SNOMED RT and a computer based nomenclature
			and classification known as Clinical Terms Version 3,
			formerly known as Read Codes, Version 3, which was
			created on behalf of the U.K. Department of Health
			and is a crown copyright. SNOMED is a registered
			trademark of the College of American Pathologists."
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='SNM'">
        <p>Users are required to display the SNOMED copyright and
			trademark notice below if SNOMED information is used
			in their product(s).</p>
		<p>"This publication includes SNOMEDII, a copyrighted work
			of the College of American Pathologists. &#169;1979, 1980
			College of American Pathologists. SNOMEDII is used
			by permission of, and under license from, the College.
			SNOMED is a registered trademark of the College of
			American Pathologists."
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='SNMI'">
        <p>Users are required to display the SNOMED copyright
			and trademark notice below if SNOMED information
			is used in their product(s).</p>
		<p>"This publication includes SNOMED V3.5, a copyrighted
			work of the College of American Pathologists. &#169;1998
			College of American Pathologists. SNOMED V3.5 is used
			by permission of, and under license from, the College.
			SNOMED is a registered trademark of the College of
			American Pathologists."
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='SNOMEDCT_US'">
        <p>Users are required to display the SNOMED copyright
			and trademark notice below if SNOMED information
			is used in their product(s).</p>
		<p>"This publication includes SNOMED CT, a copyrighted work
			of the College of American Pathologists. &#169;2000, 2002
			College of American Pathologists. This work is also
			protected by patent, U.S. Patent No. 6,438,533. SNOMED
			CT is used by permission of, and under license from,
			the College. SNOMED CT has been created by combining
			SNOMED RT and a computer based nomenclature and
			classification known as Clinical Terms Version 3,
			formerly known as Read Codes, Version 3, which was
			created on behalf of the U.K. Department of Health
			and is a crown copyright. SNOMED is a registered
			trademark of the College of American Pathologists."
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='VANDF'">
        <p>*<strong>NOTE:</strong> Now a CATEGORY 0.
			</p>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="rsab='WHO'">
        <p>The Metathesaurus includes translations of WHO97 in:
			<ul>
			<li>French (WHOFRE_1997), </li>
			<li>German (WHOGER_1997), </li>
			<li>Portuguese (WHOPOR_1997), and </li>
			<li>Spanish (WHOSPA_1997).</li>
			</ul>
			</p>
      </xsl:when>
    </xsl:choose>

</xsl:template>

<xsl:template match="scc">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="scit">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="slc">
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

