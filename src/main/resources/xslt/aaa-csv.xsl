<?xml version="1.0" encoding="utf-8"?>
<!-- 
(c) 2001-2020 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" encoding="utf-8"/>
	<xsl:include href="aaa-labels.xsl"/>
	<xsl:variable name="separator"><xsl:text>,</xsl:text></xsl:variable>
	<xsl:variable name="newline"><xsl:text>&#xa;</xsl:text></xsl:variable>
	<xsl:decimal-format name="code" NaN="999999" />
	 <xsl:template match="/">
	 	<!-- ** Kopfzeile ** -->
		<!-- Klasse -->
		<xsl:value-of select="$fc.Objektart" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Kennung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Kategorie" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Objekttyp" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Modellarten" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Grunddatenbestand" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Landnutzung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Nutzungsart" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Nutzungsartkennung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Profile" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Stillgelegt" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Revisionsnummer" /><xsl:value-of select="$separator" />
		<!-- Eigenschaft -->
		<xsl:value-of select="$fc.Eigenschaft" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Kennung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Kategorie" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Modellarten" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Grunddatenbestand" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Landnutzung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Profile" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Multiplizität" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Datentyp" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Stillgelegt" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Revisionsnummer" /><xsl:value-of select="$separator" />
		<!-- Werteart -->
		<xsl:value-of select="$fc.Werteart" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Kennung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Modellarten" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Grunddatenbestand" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Landnutzung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Nutzungsart" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Nutzungsartkennung" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Profile" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Stillgelegt" /><xsl:value-of select="$separator" />
		<xsl:value-of select="$fc.Revisionsnummer" /><xsl:value-of select="$newline" />
	 	<!-- ** Daten ** -->
		<!-- Klasse -->
		<xsl:for-each select="FC_FeatureCatalogue/AC_FeatureType">
			<xsl:sort select="format-number(number(code), '000000', 'code')" />
			<xsl:sort select="code" />
			<xsl:variable name="objektart" select="."/>
			<xsl:choose>
				<xsl:when test="/FC_FeatureCatalogue/FC_FeatureAttribute[@id=$objektart/characterizedBy/@idref]|/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$objektart/@id]">
					<xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[@id=$objektart/characterizedBy/@idref]|/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$objektart/@id]">
						<xsl:sort select="@sequenceNumber" data-type="number"/>
						<xsl:sort select="name" />
						<xsl:variable name="eigenschaft" select="."/>
						<xsl:choose>
							<xsl:when test="ValueDomainType = 1">
								<xsl:for-each select="/FC_FeatureCatalogue/FC_Value[@id=$eigenschaft/enumeratedBy/@idref]">
									<xsl:sort select="code" />
									<xsl:variable name="werteart" select="."/>
									<xsl:apply-templates select="$objektart"/>
									<xsl:value-of select="$separator" />
									<xsl:apply-templates select="$eigenschaft"/>
									<xsl:value-of select="$separator" />
									<xsl:apply-templates select="$werteart"/>
									<xsl:value-of select="$newline" />
								</xsl:for-each>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="$objektart"/>
								<xsl:value-of select="$separator" />
								<xsl:apply-templates select="$eigenschaft"/>
								<xsl:value-of select="$newline" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="."/>
				<xsl:value-of select="$newline" />
			</xsl:otherwise>
		</xsl:choose>
<!--


					<xsl:if test="count(/FC_FeatureCatalogue/FC_RelationshipRole[attribute::id=$featuretype/characterizedBy/@idref]) + count(/FC_FeatureCatalogue/FC_FeatureAttribute[attribute::id=$featuretype/characterizedBy/@idref]) = 0"><xsl:value-of select="$featuretype/name"/>;<xsl:value-of select="$featuretype/code"/>;<xsl:for-each select="$featuretype/modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="$featuretype/retired"><xsl:choose><xsl:when test="$featuretype/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$featuretype/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="$featuretype/bedeutung"/>;<xsl:value-of select="$featuretype/wirdTypisiertDurch"/>;<xsl:value-of select="$featuretype/nutzungsart"/>;<xsl:value-of select="$featuretype/nutzungsartkennung"/>;<xsl:value-of select="$featuretype/letzteAenderungRevisionsnummer"/>;&#13;&#10;</xsl:if>					
					<xsl:if test="count(/FC_FeatureCatalogue/FC_FeatureAttribute[attribute::id=$featuretype/characterizedBy/@idref]) >= 1">
						<xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[attribute::id=$featuretype/characterizedBy/@idref]">
							<xsl:variable name="featureAtt" select="."/>
							<xsl:choose>
								<xsl:when test="$featureAtt/ValueDomainType = 1">
									<xsl:for-each select="/FC_FeatureCatalogue/FC_Value[attribute::id=$featureAtt/enumeratedBy/@idref]"><xsl:value-of select="$featuretype/name"/>;<xsl:value-of select="$featuretype/code"/>;<xsl:for-each select="$featuretype/modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="$featuretype/retired"><xsl:choose><xsl:when test="$featuretype/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$featuretype/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="$featuretype/bedeutung"/>;<xsl:value-of select="$featuretype/wirdTypisiertDurch"/>;<xsl:value-of select="$featuretype/nutzungsart"/>;<xsl:value-of select="$featuretype/nutzungsartkennung"/>;<xsl:value-of select="$featuretype/letzteAenderungRevisionsnummer"/>;<xsl:value-of select="$featureAtt/name"/>;<xsl:value-of select="$featureAtt/code"/>;<xsl:for-each select="$featureAtt/modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featureAtt/grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featureAtt/profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="$featureAtt/retired"><xsl:choose><xsl:when test="$featureAtt/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$featureAtt/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="$featureAtt/cardinality"/>;<xsl:value-of select="$featureAtt/ValueDataType"/>;<xsl:value-of select="$featureAtt/letzteAenderungRevisionsnummer"/>;<xsl:value-of select="./label"/>;<xsl:value-of select="./code"/>;<xsl:for-each select="./modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="./grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="./profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="./retired"><xsl:choose><xsl:when test="./taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="./taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="./nutzungsartkennung"/>;<xsl:value-of select="./letzteAenderungRevisionsnummer"/>;&#13;&#10;</xsl:for-each>
								</xsl:when>
								<xsl:otherwise><xsl:value-of select="$featuretype/name"/>;<xsl:value-of select="$featuretype/code"/>;<xsl:for-each select="$featuretype/modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="$featuretype/retired"><xsl:choose><xsl:when test="$featuretype/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$featuretype/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="$featuretype/bedeutung"/>;<xsl:value-of select="$featuretype/wirdTypisiertDurch"/>;<xsl:value-of select="$featuretype/nutzungsart"/>;<xsl:value-of select="$featuretype/nutzungsartkennung"/>;<xsl:value-of select="$featuretype/letzteAenderungRevisionsnummer"/>;<xsl:value-of select="$featureAtt/name"/>;<xsl:value-of select="$featureAtt/code"/>;<xsl:for-each select="$featureAtt/modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featureAtt/grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featureAtt/profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="$featureAtt/retired"><xsl:choose><xsl:when test="$featureAtt/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$featureAtt/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="$featureAtt/cardinality"/>;<xsl:value-of select="$featureAtt/ValueDataType"/>;<xsl:value-of select="$featureAtt/letzteAenderungRevisionsnummer"/>;&#13;&#10;</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="count(/FC_FeatureCatalogue/FC_RelationshipRole[attribute::id=$featuretype/characterizedBy/@idref]) >= 1">
						<xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole[attribute::id=$featuretype/characterizedBy/@idref]"><xsl:value-of select="$featuretype/name"/>;<xsl:value-of select="$featuretype/code"/>;<xsl:for-each select="$featuretype/modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="$featuretype/profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="$featuretype/retired"><xsl:choose><xsl:when test="$featuretype/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$featuretype/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="$featuretype/bedeutung"/>;<xsl:value-of select="$featuretype/wirdTypisiertDurch"/>;<xsl:value-of select="$featuretype/nutzungsart"/>;<xsl:value-of select="$featuretype/nutzungsartkennung"/>;<xsl:value-of select="$featuretype/letzteAenderungRevisionsnummer"/>;;;;;;;;;;;;;;;;;;;<xsl:value-of select="./name"/>;<xsl:value-of select="./code"/>;<xsl:for-each select="./modellart"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="./grunddatenbestand"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:for-each select="./profil"><xsl:value-of select="."/><xsl:text> </xsl:text></xsl:for-each>;<xsl:if test="./retired"><xsl:choose><xsl:when test="./taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="./taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when><xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise></xsl:choose></xsl:if>;<xsl:value-of select="./cardinality"/>;<xsl:value-of select="./FeatureTypeIncluded/@name"/>;<xsl:value-of select="./letzteAenderungRevisionsnummer"/>;&#13;&#10;</xsl:for-each>
					</xsl:if>
				</xsl:for-each>
			</xsl:if>
			-->
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="AC_FeatureType">
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="name"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="code"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="bedeutung"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="wirdTypisiertDurch"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="modellart"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="grunddatenbestand"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="taggedValue[@tag='AAA:Landnutzung']"><xsl:text>Ja</xsl:text></xsl:when>
				<xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="nutzungsart"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="nutzungsartkennung"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="profil"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="taggedValue[@tag='AAA:GueltigBis']">
					<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']" />
				</xsl:when>
				<xsl:when test="retired">Ja</xsl:when>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="letzteAenderungRevisionsnummer"/></xsl:call-template>		
	</xsl:template>

	<xsl:template match="FC_FeatureAttribute|FC_RelationshipRole">
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="name"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="code"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="local-name()='FC_FeatureAttribute'">Attributart</xsl:when>
				<xsl:otherwise>Relationsart</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="modellart"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="grunddatenbestand"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="taggedValue[@tag='AAA:Landnutzung']"><xsl:text>Ja</xsl:text></xsl:when>
				<xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="profil"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="cardinality"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="ValueDataType|FeatureTypeIncluded/@name"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="taggedValue[@tag='AAA:GueltigBis']">
					<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']" />
				</xsl:when>
				<xsl:when test="retired">Ja</xsl:when>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="letzteAenderungRevisionsnummer"/></xsl:call-template>		
	</xsl:template>

	<xsl:template match="FC_Value">
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="label"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="code"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="modellart"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="grunddatenbestand"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="taggedValue[@tag='AAA:Landnutzung']"><xsl:text>Ja</xsl:text></xsl:when>
				<xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="nutzungsart"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="nutzungsartkennung"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="profil"/></xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry">
			<xsl:with-param name="wert">
				<xsl:choose>
				<xsl:when test="taggedValue[@tag='AAA:GueltigBis']">
					<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']" />
				</xsl:when>
				<xsl:when test="retired">Ja</xsl:when>
				</xsl:choose>
			</xsl:with-param> 
		</xsl:call-template>
		<xsl:value-of select="$separator" />
		<xsl:call-template name="entry"><xsl:with-param name="wert" select="letzteAenderungRevisionsnummer"/></xsl:call-template>		
	</xsl:template>

	<xsl:template name="entry">
		<xsl:param name="wert" />
		<xsl:choose>
			<xsl:when test="$wert[contains(.,$separator)]"><xsl:text>"</xsl:text><xsl:value-of select="$wert" separator=" "/><xsl:text>"</xsl:text></xsl:when>
			<xsl:otherwise><xsl:value-of select="$wert" separator=" "/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>