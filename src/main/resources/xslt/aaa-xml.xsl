<?xml version="1.0" encoding="utf-8"?>
<!-- 
(c) 2005-2020 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:nas="http://www.adv-online.de/namespaces/adv/gid/6.0" xmlns="http://www.adv-online.de/namespaces/adv/gid/fc/6.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"  xmlns:xlink="http://www.w3.org/1999/xlink">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:include href="aaa-labels.xsl"/>
	<xsl:variable name="advMA">
		<xsl:text> DLKM DKKM500 DKKM1000 DKKM2000 DKKM5000 Basis-DLM DLM50 DLM250 DLM1000 DTK10 DTK25 DTK50 DTK100 DTK250 DTK1000 DFGM DGM2 DGM5 DGM25 DGM50 DHM LoD1 LoD2 LoD3 GeoBasis-DE GVM BRM </xsl:text>
	</xsl:variable>
	<xsl:decimal-format name="code" NaN="999999"/>
	<xsl:template match="/">
		<!-- namespaces http://www.adv-online.de/namespaces/adv/gid/fc/6.0 und http://www.adv-online.de/namespaces/adv/gid/6.0; schemaLocation  -->
		<xsl:variable name="version" select="/FC_FeatureCatalogue/versionNumber"/>
		<AC_FeatureCatalogue xmlns="http://www.adv-online.de/namespaces/adv/gid/fc/6.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:nas="http://www.adv-online.de/namespaces/adv/gid/6.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" gml:id="_Objektartenkatalog" xsi:schemaLocation="http://www.adv-online.de/namespaces/adv/gid/fc/6.0 Tools/AAA-Katalogtool/AAA-Katalog.xsd">
			<gml:description><xsl:value-of select="$fc.Objektartenkatalog" /> '<xsl:value-of select="FC_FeatureCatalogue/name"/>'</gml:description>
			<gml:identifier>
				<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
				<xsl:value-of select="FC_FeatureCatalogue/name"/>
			</gml:identifier>
			<xsl:for-each select="FC_FeatureCatalogue/AC_Objektbereich">
				<xsl:sort select="format-number(number(code), '000000', 'code')"/>
				<xsl:sort select="code" />
				<xsl:apply-templates select="." mode="dictionaryEntry"/>
			</xsl:for-each>
			<xsl:for-each select="FC_FeatureCatalogue/AC_Objektartengruppe">
				<xsl:sort select="format-number(number(code), '000000', 'code')"/>
				<xsl:sort select="code" />
				<xsl:variable name="obid" select="Objektbereichzugehoerigkeit/@idref"/>
				<xsl:if test="count(//AC_Objektbereich[@id=$obid])=0">
					<xsl:apply-templates select="." mode="dictionaryEntry"/>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="FC_FeatureCatalogue/FC_FeatureRelationship">
				<xsl:apply-templates select="." mode="dictionaryEntry"/>
			</xsl:for-each>
			<xsl:for-each select="FC_FeatureCatalogue/modellart">
				<scope>
					<xsl:value-of select="."/>
				</scope>
			</xsl:for-each>
			<versionNumber>
				<xsl:value-of select="FC_FeatureCatalogue/versionNumber"/>
			</versionNumber>
			<versionDate>
				<xsl:variable name="vdate" select="FC_FeatureCatalogue/versionDate"/>
				<xsl:variable name="dd" select="substring-before($vdate,'.')"/>
				<xsl:variable name="mmyy" select="substring-after($vdate,'.')"/>
				<xsl:variable name="mm" select="substring-before($mmyy,'.')"/>
				<xsl:variable name="yy" select="substring-after($mmyy,'.')"/>
				<xsl:value-of select="$yy"/>-<xsl:value-of select="$mm"/>-<xsl:value-of select="$dd"/>
			</versionDate>
			<producer>
				<gmd:CI_ResponsibleParty>
					<gmd:organisationName>
						<gco:CharacterString><xsl:value-of select="FC_FeatureCatalogue/producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName"/></gco:CharacterString>
					</gmd:organisationName>
					<gmd:role>
					        <gmd:CI_RoleCode codeList="http://www.isotc211.org/2005/gmd#CI_RoleCode" codeListValue="publisher">Publisher</gmd:CI_RoleCode>
					</gmd:role>
				</gmd:CI_ResponsibleParty>
			</producer>
			<xsl:for-each select="FC_FeatureCatalogue/profil">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</AC_FeatureCatalogue>
	</xsl:template>
	<xsl:template name="AA_Modellart">
		<xsl:param name="mart"/>
		<nas:AA_Modellart>
			<xsl:choose>
				<xsl:when test="contains($advMA,concat(' ',$mart,' '))">
					<nas:advStandardModell>
						<xsl:value-of select="$mart"/>
					</nas:advStandardModell>
				</xsl:when>
				<xsl:otherwise>
					<nas:sonstigesModell>
						<xsl:value-of select="$mart"/>
					</nas:sonstigesModell>
				</xsl:otherwise>
			</xsl:choose>
		</nas:AA_Modellart>
	</xsl:template>
	<xsl:template match="modellart">
		<modellart>
			<xsl:call-template name="AA_Modellart">
				<xsl:with-param name="mart" select="."/>
			</xsl:call-template>
		</modellart>
	</xsl:template>
	<xsl:template match="grunddatenbestand">
		<grunddatenbestand>
			<xsl:call-template name="AA_Modellart">
				<xsl:with-param name="mart" select="."/>
			</xsl:call-template>
		</grunddatenbestand>
	</xsl:template>
	<xsl:template match="profil">
		<profil>
			<AC_Profil>
				<modellart>
					<xsl:call-template name="AA_Modellart">
						<xsl:with-param name="mart" select="substring-before(.,'_')"/>
					</xsl:call-template>
				</modellart>
				<profilname>
					<xsl:value-of select="."/>
				</profilname>
			</AC_Profil>
		</profil>
	</xsl:template>
	<xsl:template name="letzteAenderung">
		<xsl:param name="version"/>
		<xsl:param name="nummer"/>
		<letzteAenderung>
			<AC_LetzteAenderung>
				<letzteAenderungVersion>
					<xsl:value-of select="$version"/>
				</letzteAenderungVersion>
				<xsl:if test="$nummer">
					<letzteAenderungRevisionsnummer>
						<xsl:value-of select="$nummer"/>
					</letzteAenderungRevisionsnummer>
				</xsl:if>
			</AC_LetzteAenderung>
		</letzteAenderung>
	</xsl:template>
	<xsl:template name="Konsistenzbedingung">
		<xsl:param name="list"/>
		<xsl:param name="mart"/>
		<konsistenzbedingung>
			<AC_Konsistenzbedingung>
				<bedingung>
					<xsl:value-of select="$list" separator="&#xa;"/>
				</bedingung>
				<xsl:if test="$mart!='*'">
					<modellart>
						<xsl:call-template name="AA_Modellart">
							<xsl:with-param name="mart" select="$mart"/>				
						</xsl:call-template>
					</modellart>
				</xsl:if>		
			</AC_Konsistenzbedingung>
		</konsistenzbedingung>
	</xsl:template>
	<xsl:template name="Auswerteregel">
		<xsl:param name="list"/>
		<xsl:param name="mart"/>
		<auswerteregel>
			<AC_Auswerteregel>
				<regel>
					<xsl:value-of select="$list" separator="&#xa;"/>
				</regel>
				<xsl:if test="$mart!='*'">
					<modellart>
						<xsl:call-template name="AA_Modellart">
							<xsl:with-param name="mart" select="$mart"/>				
						</xsl:call-template>
					</modellart>
				</xsl:if>		
			</AC_Auswerteregel>
		</auswerteregel>
	</xsl:template>
	<xsl:template name="Bildungsregel">
		<xsl:param name="list"/>
		<xsl:param name="mart"/>
		<bildungsregel>
			<AC_Bildungsregel>
				<regel>
					<xsl:value-of select="$list" separator="&#xa;"/>
				</regel>
				<xsl:if test="$mart!='*'">
					<modellart>
						<xsl:call-template name="AA_Modellart">
							<xsl:with-param name="mart" select="$mart"/>				
						</xsl:call-template>
					</modellart>
				</xsl:if>		
			</AC_Bildungsregel>
		</bildungsregel>
	</xsl:template>
	<xsl:template name="Erfassungskriterium">
		<xsl:param name="list"/>
		<xsl:param name="mart"/>
		<erfassungskriterium>
			<AC_Erfassungskriterium>
				<kriterium>
					<xsl:value-of select="$list" separator="&#xa;"/>
				</kriterium>
				<xsl:if test="$mart!='*'">
					<modellart>
						<xsl:call-template name="AA_Modellart">
							<xsl:with-param name="mart" select="$mart"/>				
						</xsl:call-template>
					</modellart>
				</xsl:if>		
			</AC_Erfassungskriterium>
		</erfassungskriterium>
	</xsl:template>
	<xsl:template match="diagram">
		<diagramm>
			<xsl:attribute name="xlink:href"><xsl:value-of select="."/></xsl:attribute>
		</diagramm>
	</xsl:template>
	<xsl:template match="AC_Objektbereich" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<AC_Objektartenbereich>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="definition">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:if test="count(nutzungsartkennung)=1">
					<xsl:for-each select="tokenize(nutzungsartkennung, ',')">
						<nutzungsartkennung>				
							<xsl:value-of select="normalize-space(.)"/>
						</nutzungsartkennung>
            	</xsl:for-each>
				</xsl:if>
				<xsl:variable name="bid" select="@id"/>
				<xsl:for-each select="/FC_FeatureCatalogue/AC_Objektartengruppe[Objektbereichzugehoerigkeit/@idref=$bid]">
					<xsl:sort select="format-number(number(code), '000000', 'code')"/>
					<xsl:sort select="code" />
					<xsl:apply-templates select="." mode="dictionaryEntry"/>
				</xsl:for-each>
				<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[(count(bedeutung)=0 or bedeutung='Objektart') and Objektartengruppenzugehoerigkeit/@idref=$bid]">
					<xsl:sort select="format-number(number(code), '000000', 'code')"/>
					<xsl:sort select="code" />
					<xsl:apply-templates select="." mode="dictionaryEntryFT"/>
				</xsl:for-each>
				<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[bedeutung!='Objektart' and Objektartengruppenzugehoerigkeit/@idref=$bid]">
					<xsl:sort select="format-number(number(code), '000000', 'code')"/>
					<xsl:sort select="code" />
					<xsl:apply-templates select="." mode="dictionaryEntryDT"/>
				</xsl:for-each>
				<xsl:apply-templates select="diagram"/>
			</AC_Objektartenbereich>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="AC_Objektartengruppe" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<AC_Objektartengruppe>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:if test="count(nutzungsartkennung)=1">
					<xsl:for-each select="tokenize(nutzungsartkennung, ',')">
						<nutzungsartkennung>				
							<xsl:value-of select="normalize-space(.)"/>
						</nutzungsartkennung>
            	</xsl:for-each>
				</xsl:if>
				<xsl:variable name="gid" select="@id"/>
				<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[(count(bedeutung)=0 or bedeutung='Objektart') and Objektartengruppenzugehoerigkeit/@idref=$gid]">
					<xsl:sort select="format-number(number(code), '000000', 'code')"/>
					<xsl:sort select="code" />
					<xsl:apply-templates select="." mode="dictionaryEntryFT"/>
				</xsl:for-each>
				<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[bedeutung!='Objektart' and Objektartengruppenzugehoerigkeit/@idref=$gid]">
					<xsl:sort select="format-number(number(code), '000000', 'code')"/>
					<xsl:sort select="code" />
					<xsl:apply-templates select="." mode="dictionaryEntryDT"/>
				</xsl:for-each>
				<xsl:apply-templates select="diagram"/>
			</AC_Objektartengruppe>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="AC_FeatureType" mode="dictionaryEntryFT">
		<gml:dictionaryEntry>
			<AC_FeatureType>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:for-each select="subtypeOf">
					<xsl:apply-templates select="." mode="dictionaryEntry"/>
				</xsl:for-each>
				<!-- XXX
				<xsl:for-each select="characterizedBy">
					<xsl:variable name="pid" select="@idref"/>
					<xsl:apply-templates select="/FC_FeatureCatalogue/FC_FeatureAttribute[@id=$pid]" mode="dictionaryEntry"/>
				</xsl:for-each>
				-->
				<xsl:variable name="id" select="@id"/>
				<xsl:for-each select="/FC_FeatureCatalogue/*[inType/@idref=$id]">
					<xsl:apply-templates select="." mode="dictionaryEntry"/>
				</xsl:for-each>
				<xsl:choose>
					<xsl:when test="abstrakt">
						<isAbstract><xsl:value-of select="abstrakt" /></isAbstract>
					</xsl:when>
					<xsl:otherwise>
						<isAbstract>false</isAbstract>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:for-each select="note">
					<constrainedBy>
						<xsl:value-of select="."/>
					</constrainedBy>
				</xsl:for-each>
				<xsl:apply-templates select="modellart"/>
				<xsl:apply-templates select="grunddatenbestand"/>
				<xsl:apply-templates select="profil"/>
				<!-- letzteAenderungVersion wird nie gesetzt ?! -->
				<xsl:if test="letzteAenderungVersion">
					<xsl:call-template name="letzteAenderung">
						<xsl:with-param name="version" select="letzteAenderungVersion"/>
						<xsl:with-param name="nummer" select="letzteAenderungRevisionsnummer"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:Landnutzung'])=1">
					<landnutzung>				
						<xsl:value-of select="replace(taggedValue[@tag='AAA:Landnutzung'], 'True', 'true')"/>
					</landnutzung>
				</xsl:if>
				<xsl:if test="count(nutzungsart)=1">
					<nutzungsart>				
						<xsl:value-of select="nutzungsart"/>
					</nutzungsart>
				</xsl:if>
				<xsl:if test="count(nutzungsartkennung)=1">
					<xsl:for-each select="tokenize(nutzungsartkennung, ',')">
						<nutzungsartkennung>				
							<xsl:value-of select="normalize-space(.)"/>
						</nutzungsartkennung>
            	</xsl:for-each>
				</xsl:if>
				<xsl:if test="wirdTypisiertDurch">
					<wirdTypisiertDurch>
						<xsl:value-of select="wirdTypisiertDurch[1]"/>
					</wirdTypisiertDurch>
				</xsl:if>
				<xsl:if test="erfassungskriterium">
					<xsl:for-each-group select="erfassungskriterium" group-by="@modellart">
						<xsl:sort select="current-grouping-key()" /> 
						<xsl:call-template name="Erfassungskriterium">
							<xsl:with-param name="list" select="current-group()"/>
							<xsl:with-param name="mart" select="current-grouping-key()" />
						</xsl:call-template>
					</xsl:for-each-group> 
				</xsl:if>
				<xsl:if test="bildungsregel">
					<xsl:for-each-group select="bildungsregel" group-by="@modellart">
						<xsl:sort select="current-grouping-key()" /> 
						<xsl:call-template name="Bildungsregel">
							<xsl:with-param name="list" select="current-group()"/>
							<xsl:with-param name="mart" select="current-grouping-key()" />
						</xsl:call-template>
					</xsl:for-each-group> 
				</xsl:if>
				<xsl:if test="konsistenzbedingung">
					<xsl:for-each-group select="konsistenzbedingung" group-by="@modellart">
						<xsl:sort select="current-grouping-key()" /> 
						<xsl:call-template name="Konsistenzbedingung">
							<xsl:with-param name="list" select="current-group()"/>
							<xsl:with-param name="mart" select="current-grouping-key()" />
						</xsl:call-template>
					</xsl:for-each-group> 
				</xsl:if>
				<xsl:apply-templates select="diagram"/>
			</AC_FeatureType>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="AC_FeatureType" mode="dictionaryEntryDT">
		<gml:dictionaryEntry>
			<AC_DataType>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:for-each select="subtypeOf">
					<xsl:apply-templates select="." mode="dictionaryEntry"/>
				</xsl:for-each>
				<xsl:for-each select="characterizedBy">
					<xsl:variable name="pid" select="@idref"/>
					<xsl:apply-templates select="/FC_FeatureCatalogue/*[@id=$pid]" mode="dictionaryEntry"/>
				</xsl:for-each>
				<xsl:choose>
					<xsl:when test="count(definition[.='Es handelt sich um eine abstrakte Objektart.'])>0">
						<isAbstract>true</isAbstract>
					</xsl:when>
					<xsl:otherwise>
						<isAbstract>false</isAbstract>
					</xsl:otherwise>
				</xsl:choose>
				<kategorie>
					<xsl:choose>
						<xsl:when test="bedeutung='Datentyp'">DataType</xsl:when>
						<xsl:when test="bedeutung='Auswahldatentyp'">Union</xsl:when>
						<xsl:when test="bedeutung='NAS-Auftrag'">Request</xsl:when>
						<xsl:when test="bedeutung='NAS-Ergebnis'">Response</xsl:when>
					</xsl:choose>
				</kategorie>
				<xsl:for-each select="note">
					<constrainedBy>
						<xsl:value-of select="."/>
					</constrainedBy>
				</xsl:for-each>
				<xsl:apply-templates select="modellart"/>
				<xsl:apply-templates select="grunddatenbestand"/>
				<xsl:apply-templates select="profil"/>
				<xsl:if test="count(letzteAenderungVersion)=1">
					<xsl:call-template name="letzteAenderung">
						<xsl:with-param name="version" select="letzteAenderungVersion"/>
						<xsl:with-param name="nummer" select="letzteAenderungRevisionsnummer"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:if test="konsistenzbedingung">
					<xsl:for-each-group select="konsistenzbedingung" group-by="@modellart">
						<xsl:sort select="current-grouping-key()" /> 
						<xsl:call-template name="Konsistenzbedingung">
							<xsl:with-param name="list" select="current-group()"/>
							<xsl:with-param name="mart" select="current-grouping-key()" />
						</xsl:call-template>
					</xsl:for-each-group> 
				</xsl:if>
				<xsl:apply-templates select="diagram"/>
			</AC_DataType>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="subtypeOf" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<FC_InheritanceRelation>
				<xsl:attribute name="gml:id">__<xsl:value-of select="generate-id(.)"/></xsl:attribute>
				<gml:identifier><xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>Abgeleitet aus <xsl:value-of select="."/>
				</gml:identifier>
				<xsl:variable name="supertype" select="."/>
				<xsl:choose>
					<xsl:when test="//AC_FeatureType[name=$supertype]">
						<supertype>
							<xsl:attribute name="xlink:href">#<xsl:value-of select="//AC_FeatureType[name=$supertype]/@id"/></xsl:attribute>
						</supertype>
					</xsl:when>
					<xsl:otherwise>
						<supertypeName>
							<xsl:value-of select="$supertype"/>
						</supertypeName>
					</xsl:otherwise>
				</xsl:choose>
			</FC_InheritanceRelation>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="FC_FeatureAttribute" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<AC_FeatureAttribute>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:if test="count(ValueDomainType)=1 and ValueDomainType='1'">
					<xsl:variable name="aid" select="@id"/>
					<xsl:for-each select="enumeratedBy">
						<xsl:variable name="lvid" select="@idref"/>
						<xsl:apply-templates select="//FC_Value[@id=$lvid]" mode="dictionaryEntry">
							<xsl:with-param name="aid" select="$aid"/>
						</xsl:apply-templates>
					</xsl:for-each>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="count(cardinality)=1">
						<cardinality>
							<xsl:value-of select="cardinality"/>
						</cardinality>
					</xsl:when>
					<xsl:otherwise>
						<cardinality>1</cardinality>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="ValueDataType">
					<xsl:variable name="ft" select="ValueDataType"/>
					<xsl:choose>
						<xsl:when test="$ft/@idref">
							<valueType>
								<xsl:attribute name="xlink:href">#<xsl:value-of select="$ft/@idref"/></xsl:attribute>
								<xsl:if test="contains($ft,'Set&lt;')">
									<xsl:attribute name="collectionType">set</xsl:attribute>
								</xsl:if>
								<xsl:if test="contains($ft,'Sequence&lt;')">
									<xsl:attribute name="collectionType">sequence</xsl:attribute>
								</xsl:if>
							</valueType>
						</xsl:when>
						<xsl:otherwise>
							<valueTypeName>
								<xsl:if test="contains($ft,'Set&lt;')">
									<xsl:attribute name="collectionType">set</xsl:attribute>
								</xsl:if>
								<xsl:if test="contains($ft,'Sequence&lt;')">
									<xsl:attribute name="collectionType">sequence</xsl:attribute>
								</xsl:if>
								<xsl:choose>
									<xsl:when test="contains($ft,'&lt;')">
										<xsl:value-of select="substring-after(substring-before($ft,'&gt;'),'&lt;')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$ft"/>
									</xsl:otherwise>
								</xsl:choose>
							</valueTypeName>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:apply-templates select="modellart"/>
				<xsl:apply-templates select="grunddatenbestand"/>
				<xsl:apply-templates select="profil"/>
				<xsl:if test="count(letzteAenderungVersion)=1">
					<xsl:call-template name="letzteAenderung">
						<xsl:with-param name="version" select="letzteAenderungVersion"/>
						<xsl:with-param name="nummer" select="letzteAenderungRevisionsnummer"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:Landnutzung'])=1">
					<landnutzung>				
						<xsl:value-of select="replace(taggedValue[@tag='AAA:Landnutzung'], 'True', 'true')"/>
					</landnutzung>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="count(objektbildend)=1 and (objektbildend='true' or objektbildend='-1')">
						<objektbildend>true</objektbildend>
					</xsl:when>
					<xsl:otherwise>
						<objektbildend>false</objektbildend>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="auswerteregel">
					<xsl:call-template name="Auswerteregel">
						<xsl:with-param name="list" select="auswerteregel"/>
						<xsl:with-param name="mart" select="'*'" />
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="bildungsregel">
					<xsl:call-template name="Bildungsregel">
						<xsl:with-param name="list" select="bildungsregel"/>
						<xsl:with-param name="mart" select="'*'" />
					</xsl:call-template>
				</xsl:if>
			</AC_FeatureAttribute>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="FC_Value" mode="dictionaryEntry">
		<xsl:param name="aid"/>
		<gml:dictionaryEntry>
			<AC_ListedValue>
				<xsl:attribute name="gml:id"><xsl:value-of select="$aid"/><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="label"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:apply-templates select="modellart"/>
				<xsl:apply-templates select="grunddatenbestand"/>
				<xsl:apply-templates select="profil"/>
				<xsl:if test="count(letzteAenderungVersion)=1">
					<xsl:call-template name="letzteAenderung">
						<xsl:with-param name="version" select="letzteAenderungVersion"/>
						<xsl:with-param name="nummer" select="letzteAenderungRevisionsnummer"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:Landnutzung'])=1">
					<landnutzung>				
						<xsl:value-of select="replace(taggedValue[@tag='AAA:Landnutzung'], 'True', 'true')"/>
					</landnutzung>
				</xsl:if>
				<xsl:if test="count(nutzungsartkennung)=1">
					<xsl:for-each select="tokenize(nutzungsartkennung, ',')">
						<nutzungsartkennung>				
							<xsl:value-of select="normalize-space(.)"/>
						</nutzungsartkennung>
            	</xsl:for-each>
				</xsl:if>
			</AC_ListedValue>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="FC_RelationshipRole" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<AC_AssociationRole>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="count(cardinality)=1 and cardinality!='Unbestimmt'">
						<cardinality>
							<xsl:value-of select="cardinality"/>
						</cardinality>
					</xsl:when>
					<xsl:otherwise>
						<cardinality>0..*</cardinality>
					</xsl:otherwise>
				</xsl:choose>
				<type>ordinary</type>
				<xsl:choose>
					<xsl:when test="count(orderIndicator)=1 and orderIndicator='1'">
						<isOrdered>true</isOrdered>
					</xsl:when>
					<xsl:otherwise>
						<isOrdered>false</isOrdered>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="inverseRichtung='true'">
						<isNavigable>false</isNavigable>
					</xsl:when>
					<xsl:otherwise>
						<isNavigable>true</isNavigable>
					</xsl:otherwise>
				</xsl:choose>
				<relation>
					<xsl:attribute name="xlink:href">#<xsl:value-of select="relation/@idref"/></xsl:attribute>
				</relation>
				<xsl:variable name="ft" select="FeatureTypeIncluded"/>
				<xsl:choose>
					<xsl:when test="$ft/@idref">
						<valueType>
							<xsl:attribute name="xlink:href">#<xsl:value-of select="$ft/@idref"/></xsl:attribute>
						</valueType>
					</xsl:when>
					<xsl:otherwise>
						<valueTypeName>
							<xsl:value-of select="$ft/@name"/>
						</valueTypeName>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:apply-templates select="modellart"/>
				<xsl:apply-templates select="grunddatenbestand"/>
				<xsl:apply-templates select="profil"/>
				<xsl:if test="count(letzteAenderungVersion)=1">
					<xsl:call-template name="letzteAenderung">
						<xsl:with-param name="version" select="letzteAenderungVersion"/>
						<xsl:with-param name="nummer" select="letzteAenderungRevisionsnummer"/>
					</xsl:call-template>
				</xsl:if>
				<xsl:if test="count(retired)=1">
					<isRetired>				
						<xsl:value-of select="retired"/>
					</isRetired>
				</xsl:if>
				<xsl:if test="count(taggedValue[@tag='AAA:GueltigBis'])=1">
					<retiredSinceVersion>
						<xsl:value-of select="taggedValue[@tag='AAA:GueltigBis']"/>
					</retiredSinceVersion>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="count(objektbildend)=1 and (objektbildend='true' or objektbildend='-1')">
						<objektbildend>true</objektbildend>
					</xsl:when>
					<xsl:otherwise>
						<objektbildend>false</objektbildend>
					</xsl:otherwise>
				</xsl:choose>
			</AC_AssociationRole>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="FC_FeatureRelationship" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<FC_FeatureAssociation>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="name"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<xsl:for-each select="roles">
					<xsl:variable name="fid" select="@idref"/>
					<xsl:choose>
						<xsl:when test="count(//@id[.=$fid])>0">
							<gml:dictionaryEntry>
								<xsl:attribute name="xlink:href">#<xsl:value-of select="@idref"/></xsl:attribute>
							</gml:dictionaryEntry>
						</xsl:when>
						<xsl:otherwise>
							<xsl:comment>Verweis auf Rolle in Objekt auޥrhalb des Katalogs mit ID <xsl:value-of select="$fid"/>
							</xsl:comment>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
				<isAbstract>false</isAbstract>
			</FC_FeatureAssociation>
		</gml:dictionaryEntry>
	</xsl:template>
	<xsl:template match="FC_FeatureOperation" mode="dictionaryEntry">
		<gml:dictionaryEntry>
			<AC_FeatureOperation>
				<xsl:attribute name="gml:id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:if test="count(definition)>0">
					<gml:description>
						<xsl:for-each select="definition">
							<xsl:value-of select="."/>
							<xsl:text>&#xa;</xsl:text>
						</xsl:for-each>
					</gml:description>
				</xsl:if>
				<gml:identifier>
					<xsl:attribute name="codeSpace">urn:adv:name</xsl:attribute>
					<xsl:value-of select="substring-after(name,'::')"/>
				</gml:identifier>
				<xsl:if test="count(code)=1">
					<gml:name>
						<xsl:attribute name="codeSpace">urn:adv:kennung</xsl:attribute>
						<xsl:value-of select="code"/>
					</gml:name>
				</xsl:if>
				<cardinality>1</cardinality>
				<xsl:apply-templates select="modellart"/>
				<xsl:apply-templates select="grunddatenbestand"/>
				<xsl:apply-templates select="profil"/>
				<xsl:if test="count(letzteAenderungVersion)=1">
					<xsl:call-template name="letzteAenderung">
						<xsl:with-param name="version" select="letzteAenderungVersion"/>
						<xsl:with-param name="nummer" select="letzteAenderungRevisionsnummer"/>
					</xsl:call-template>
				</xsl:if>
			</AC_FeatureOperation>
		</gml:dictionaryEntry>
	</xsl:template>
</xsl:stylesheet>
