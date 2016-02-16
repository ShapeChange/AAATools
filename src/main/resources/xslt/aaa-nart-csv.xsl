<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" encoding="iso-8859-1"/>
	<xsl:variable name="root" select="/FC_FeatureCatalogue"/>
	<xsl:template match="/"><xsl:value-of select="FC_FeatureCatalogue/name"/>&#13;&#10;Version: <xsl:value-of select="FC_FeatureCatalogue/versionNumber"/>&#13;&#10;Stand: <xsl:value-of select="FC_FeatureCatalogue/versionDate"/>&#13;&#10;&#13;&#10;0-w;Nutzungsartengruppe;0-w;Nutzungsart;0-w;Untergliederung (1. Stufe);0-w;Untergliederung (2. Stufe);ALKIS-Objektartenkatalog&#13;&#10;<xsl:for-each select="//nutzungsartkennung[not(text()=preceding::nutzungsartkennung/text())]">
			<xsl:sort select="."/>
			<xsl:variable name="num" select="text()"/>
			<xsl:variable name="entries" select="$root/AC_FeatureType[nutzungsartkennung/text()=$num]|$root/FC_Value[nutzungsartkennung/text()=$num]"/>
			<xsl:variable name="block">
				<xsl:value-of select="$num"/>
				<xsl:if test="contains($entries/grunddatenbestand,'DLKM')"><xsl:text> (G)</xsl:text></xsl:if>
				<xsl:text>;</xsl:text>
				<xsl:choose>
					<xsl:when test="$entries[not(code='9999')]/nutzungsart"><xsl:value-of select="$entries[not(code='9999')]/nutzungsart[1]"/></xsl:when>
					<xsl:when test="$entries[not(code='9999')]/label"><xsl:value-of select="$entries[not(code='9999')]/label[1]"/></xsl:when>
					<xsl:otherwise>(Bezeichnung fehlt)</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="alkis">
				<xsl:if test="$entries[code='9999']">
					<xsl:choose>
						<xsl:when test="local-name($entries[not(code='9999')])='AC_FeatureType'">
							<xsl:value-of select="$entries[not(code='9999')]/code"/>
						</xsl:when>
						<xsl:when test="local-name($entries[not(code='9999')])='FC_Value'">
							<xsl:variable name="val" select="$entries[not(code='9999')]"/>
							<xsl:variable name="att" select="$root/FC_FeatureAttribute[enumeratedBy/@idref=$val/@id]"/>
							<xsl:variable name="fea" select="$root/AC_FeatureType[characterizedBy/@idref=$att/@id]"/>
							<xsl:value-of select="$fea/code"/><xsl:text>-</xsl:text><xsl:value-of select="$att/code"/><xsl:text>-</xsl:text><xsl:value-of select="$entries[not(code='9999')]/code"/>
						</xsl:when>
						<xsl:otherwise>(unbekannt)</xsl:otherwise>
					</xsl:choose>
					<xsl:text> und/oder </xsl:text>
				</xsl:if>
				<xsl:for-each select="$entries">
				<xsl:if test="position()>1 and not(local-name($entries[not(code='9999')])='AC_FeatureType')">
					<xsl:text>+</xsl:text>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="local-name(.)='AC_FeatureType'">
						<xsl:if test="count($entries)=1">
							<xsl:value-of select="./code"/>
						</xsl:if>
					</xsl:when>
					<xsl:when test="local-name(.)='FC_Value'">
						<xsl:variable name="val" select="."/>
						<xsl:variable name="att" select="$root/FC_FeatureAttribute[enumeratedBy/@idref=$val/@id]"/>
						<xsl:variable name="fea" select="$root/AC_FeatureType[characterizedBy/@idref=$att/@id]"/>
						<xsl:value-of select="$fea/code"/><xsl:text>-</xsl:text><xsl:value-of select="$att/code"/><xsl:text>-</xsl:text><xsl:value-of select="./code"/>
					</xsl:when>
					<xsl:otherwise>(unbekannt)</xsl:otherwise>
				</xsl:choose>
				</xsl:for-each>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$num mod 1000 = 0"><xsl:value-of select="$block"/><xsl:text>;;;;;;;</xsl:text><xsl:value-of select="$alkis"/><xsl:text>&#13;&#10;</xsl:text></xsl:when>
				<xsl:when test="$num mod 100 = 0"><xsl:text>;;</xsl:text><xsl:value-of select="$block"/><xsl:text>;;;;;</xsl:text><xsl:value-of select="$alkis"/><xsl:text>&#13;&#10;</xsl:text></xsl:when>
				<xsl:when test="$num mod 10 = 0"><xsl:text>;;;;</xsl:text><xsl:value-of select="$block"/><xsl:text>;;;</xsl:text><xsl:value-of select="$alkis"/><xsl:text>&#13;&#10;</xsl:text></xsl:when>
				<xsl:otherwise><xsl:text>;;;;;;</xsl:text><xsl:value-of select="$block"/><xsl:text>;</xsl:text><xsl:value-of select="$alkis"/><xsl:text>&#13;&#10;</xsl:text></xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
