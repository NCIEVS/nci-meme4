<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="yes" method="text"/>
  <xsl:template match="/mrfilesCollection">
    New Files :
    <xsl:for-each select="//newfile">
      <xsl:value-of select="."/>
    </xsl:for-each>
    Old Files :
    <xsl:for-each select="//oldfile">
      <xsl:value-of select="."/>
    </xsl:for-each>
    Changed Files :
    <xsl:if test="//changefile/newfield">New Fields :</xsl:if>
    <xsl:for-each select="//changefile/newfield" xml:space="preserve">
      <xsl:value-of select="../filename"/><xsl:text> : </xsl:text><xsl:value-of select="./name"/>
        <xsl:value-of select="./detail"/>
    </xsl:for-each>
    <xsl:if test="//changefile/oldfield">Old Fields :</xsl:if>
    <xsl:for-each select="//changefile/oldfield" xml:space="preserve">
      <xsl:value-of select="../filename"/><xsl:text> : </xsl:text><xsl:value-of select="./name"/>
        <xsl:value-of select="./detail"/>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
