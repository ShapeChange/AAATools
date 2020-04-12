<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns="http://www.adv-online.de/namespaces/adv/gid/fc/6.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<!-- 

	Version 0.1 - 09.05.2013
		erste Version
		
	Version 0.2 - 11.04.2020
		- Unterstützung für neue Standardmodellarten in GeoInfoDok NEU

     (c) 2013-2020 interactive instruments GmbH, Bonn
     im Auftrag der AdV, Arbeitsgemeinschaft der Vermessungsverwaltungen der
     Länder der Bundesrepublik Deutschland

	  http://www.adv-online.de/

	  -->
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:variable name="advMA">
		<xsl:text> DLKM DKKM500 DKKM1000 DKKM2000 DKKM5000 Basis-DLM DLM50 DLM250 DLM1000 DTK10 DTK25 DTK50 DTK100 DTK250 DTK1000 DFGM DGM2 DGM5 DGM25 DGM50 DHM LoD1 LoD2 LoD3 GeoBasis-DE GVM BORIS </xsl:text>
	</xsl:variable>
	<xsl:decimal-format name="code" NaN="999999"/>
	<xsl:template match="/">
		<xsl:variable name="version" select="/FC_FeatureCatalogue/versionNumber"/>
		<gfc:FC_FeatureCatalogue id="_Objektartenkatalog" xsi:schemaLocation="http://www.isotc211.org/2005/gfc http://www.isotc211.org/2005/gfc/gfc.xsd">
			<gmx:name>
				<gco:CharacterString><xsl:value-of select="FC_FeatureCatalogue/name"/></gco:CharacterString>
			</gmx:name>
			<gmx:scope>
				<gco:CharacterString><xsl:value-of select="FC_FeatureCatalogue/scope"/></gco:CharacterString>
			</gmx:scope>
			<xsl:for-each select="//modellart">
				<xsl:variable name="mart" select="."/>
				<xsl:if test="generate-id(//modellart[.=$mart][1])=generate-id($mart)">
					<gmx:fieldOfApplication>
						<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
					</gmx:fieldOfApplication>
				</xsl:if>
			</xsl:for-each>
			<gmx:versionNumber>
				<gco:CharacterString><xsl:value-of select="$version"/></gco:CharacterString>
			</gmx:versionNumber>
			<gmx:versionDate>
				<gco:Date>
					<xsl:variable name="vdate" select="FC_FeatureCatalogue/versionDate"/>
					<xsl:variable name="dd" select="substring-before($vdate,'.')"/>
					<xsl:variable name="mmyy" select="substring-after($vdate,'.')"/>
					<xsl:variable name="mm" select="substring-before($mmyy,'.')"/>
					<xsl:variable name="yy" select="substring-after($mmyy,'.')"/>
					<xsl:value-of select="$yy"/>-<xsl:value-of select="$mm"/>-<xsl:value-of select="$dd"/>
				</gco:Date>
			</gmx:versionDate>
			<gmx:language>
				<gco:CharacterString>deu</gco:CharacterString>
			</gmx:language>
			<gmx:characterSet>
				<gmd:MD_CharacterSetCode codeList="resources/Codelist/gmxcodelists.xml#MD_CharacterSetCode" codeListValue="utf8">UTF-8</gmd:MD_CharacterSetCode>
			</gmx:characterSet>
			<gfc:producer>
				<gmd:CI_ResponsibleParty>
					<gmd:organisationName>
						<gco:CharacterString><xsl:value-of select="FC_FeatureCatalogue/producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName"/></gco:CharacterString>
					</gmd:organisationName>
					<gmd:role>
						<gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode" codeListValue="publisher">Publisher</gmd:CI_RoleCode>
					</gmd:role>
				</gmd:CI_ResponsibleParty>
			</gfc:producer>
			<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[(count(bedeutung)=0 or bedeutung='Objektart')]">
				<xsl:sort select="format-number(code,'000000','code')"/>
				<gfc:featureType>
					<xsl:apply-templates select="."/>
				</gfc:featureType>
			</xsl:for-each>
			<xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureRelationship">
				<gfc:featureType>
					<xsl:apply-templates select="."/>
				</gfc:featureType>
			</xsl:for-each>
			<xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[(count(bedeutung)=0 or bedeutung='Objektart')]/subtypeOf">
				<gfc:inheritanceRelation>
					<xsl:apply-templates select="."/>
				</gfc:inheritanceRelation>
			</xsl:for-each>
		</gfc:FC_FeatureCatalogue>
	</xsl:template>
	<xsl:template name="definition">
		<xsl:param name="item"/>
		<gfc:definition>
			<gco:CharacterString>
				<xsl:for-each select="$item/definition"><xsl:value-of select="."/><xsl:text>&#xd;</xsl:text></xsl:for-each>
				<xsl:if test="count($item/modellart)>0">
					<xsl:text>Gültig für die Modellarten: </xsl:text>
					<xsl:for-each select="modellart">
						<xsl:value-of select="."/><xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="count($item/grunddatenbestand)>0">
					<xsl:text>Grunddatenbestand für die Modellarten: </xsl:text>
					<xsl:for-each select="grunddatenbestand">
						<xsl:value-of select="."/><xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="count($item/Erfassungskriterium)>0">
					<xsl:text>Erfassungskriterien:&#xd;</xsl:text>
					<xsl:for-each select="Erfassungskriterium">
						<xsl:value-of select="."/><xsl:text>&#xd;</xsl:text>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="count($item/Bildungsregel)>0">
					<xsl:text>Bildungsregeln:&#xd;</xsl:text>
					<xsl:for-each select="Bildungsregel">
						<xsl:value-of select="."/><xsl:text>&#xd;</xsl:text>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="count($item/Lebenszeitintervall)>0">
					<xsl:text>Lebenszeitintervallbeschreibung:&#xd;</xsl:text>
					<xsl:for-each select="Lebenszeitintervall">
						<xsl:value-of select="."/><xsl:text>&#xd;</xsl:text>
					</xsl:for-each>
				</xsl:if>
			</gco:CharacterString>
		</gfc:definition>
	</xsl:template>
	<xsl:template match="AC_FeatureType">
		<xsl:variable name="nam" select="name"/>
		<xsl:variable name="id" select="@id"/>
		<gfc:FC_FeatureType>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<gfc:typeName>
				<gco:LocalName><xsl:value-of select="name"/></gco:LocalName>
			</gfc:typeName>
			<xsl:call-template name="definition">
				<xsl:with-param name="item" select="."/>
			</xsl:call-template>
			<xsl:if test="count(code)=1">
				<gfc:code>
					<gco:CharacterString><xsl:value-of select="code"/></gco:CharacterString>
				</gfc:code>
			</xsl:if>
			<gfc:isAbstract>
				<xsl:choose>
					<xsl:when test="count(definition[.='Es handelt sich um eine abstrakte Objektart.'])>0">
						<gco:Boolean>true</gco:Boolean>
					</xsl:when>
					<xsl:otherwise>
						<gco:Boolean>false</gco:Boolean>
					</xsl:otherwise>
				</xsl:choose>
			</gfc:isAbstract>
			<xsl:for-each select="subtypeOf">
				<gfc:inheritsFrom>
					<xsl:attribute name="xlink:href">#IR_<xsl:value-of select="$nam"/>_<xsl:value-of select="."/></xsl:attribute>
				</gfc:inheritsFrom>
			</xsl:for-each>
			<xsl:for-each select="//FeatureType[count(subtypeOf=$nam)>0]">
				<gfc:inheritsTo>
					<xsl:attribute name="xlink:href">#IR_<xsl:value-of select="name"/>_<xsl:value-of select="$nam"/></xsl:attribute>
				</gfc:inheritsTo>
			</xsl:for-each>
			<gfc:featureCatalogue xlink:href="#_Objektartenkatalog" />
			<xsl:if test="count(Konsistenzbedingung)>0">
				<xsl:call-template name="Konsistenzbedingung">
					<xsl:with-param name="eklist" select="Konsistenzbedingung"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:for-each select="/FC_FeatureCatalogue/*[inType/@idref=$id]">
				<gfc:carrierOfCharacteristics>
					<xsl:apply-templates select=".">
						<xsl:with-param name="fid" select="$id"/>
					</xsl:apply-templates>
				</gfc:carrierOfCharacteristics>
			</xsl:for-each>
		</gfc:FC_FeatureType>
	</xsl:template>
	<xsl:template match="subtypeOf">
		<gfc:FC_InheritanceRelation>
			<xsl:attribute name="id">IR_<xsl:value-of select="../name"/>_<xsl:value-of select="."/></xsl:attribute>
			<gfc:description>
				<gco:CharacterString/>
			</gfc:description>
			<gfc:uniqueInstance>
				<gco:Boolean>true</gco:Boolean>
			</gfc:uniqueInstance>
			<gfc:subtype>
				<xsl:attribute name="xlink:href">#<xsl:value-of select="../@id"/></xsl:attribute>
			</gfc:subtype>
			<gfc:supertype>
				<xsl:variable name="supertype" select="."/>
				<xsl:choose>
					<xsl:when test="//AC_FeatureType[name=$supertype]">
						<xsl:attribute name="xlink:href">#<xsl:value-of select="//AC_FeatureType[name=$supertype]/@id"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="xlink:href"><xsl:value-of select="$supertype"/></xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			</gfc:supertype>
		</gfc:FC_InheritanceRelation>
	</xsl:template>
	<xsl:template name="Konsistenzbedingung">
		<xsl:param name="eklist"/>
		<xsl:variable name="ek" select="$eklist[1]"/>
		<xsl:variable name="ekrest" select="$ek/following-sibling::Konsistenzbedingung"/>
		<xsl:apply-templates select="$ek"/>
		<xsl:if test="$ekrest">
			<xsl:call-template name="Konsistenzbedingung">
				<xsl:with-param name="eklist" select="$ekrest"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<xsl:template match="Konsistenzbedingung">
		<gfc:constrainedBy>
			<gfc:FC_Constraint>
				<gfc:description>
					<gco:CharacterString>
						<xsl:value-of select="."/>
					</gco:CharacterString>
				</gfc:description>
			</gfc:FC_Constraint>
		</gfc:constrainedBy>
	</xsl:template>
	<xsl:template name="cardinality">
		<xsl:param name="item"/>
		<xsl:param name="default_min"/>
		<xsl:param name="default_max"/>
		<gfc:cardinality>
			<gco:Multiplicity>
				<gco:range>
					<gco:MultiplicityRange>
						<xsl:choose>
							<xsl:when test="count($item/cardinality)=1 and contains($item/cardinality,'..')">
								<gco:lower>
									<gco:Integer><xsl:value-of select="substring-before($item/cardinality,'..')"/></gco:Integer>
								</gco:lower>
								<gco:upper>
									<xsl:choose>
										<xsl:when test="substring-after($item/cardinality,'..')='*'">
											<gco:UnlimitedInteger isInfinite="true" xsi:nil="true"/>
										</xsl:when>
										<xsl:otherwise>
											<gco:UnlimitedInteger><xsl:value-of select="substring-after($item/cardinality,'..')"/></gco:UnlimitedInteger>
										</xsl:otherwise>
									</xsl:choose>
								</gco:upper>		
							</xsl:when>
							<xsl:when test="count($item/cardinality)=1">
								<gco:lower>
									<xsl:choose>
										<xsl:when test="$item/cardinality='*'">
											<gco:Integer>0</gco:Integer>
										</xsl:when>
										<xsl:otherwise>
											<gco:Integer><xsl:value-of select="$item/cardinality"/></gco:Integer>
										</xsl:otherwise>
									</xsl:choose>
								</gco:lower>
								<gco:upper>
									<xsl:choose>
										<xsl:when test="$item/cardinality='*'">
											<gco:UnlimitedInteger isInfinite="true" xsi:nil="true"/>
										</xsl:when>
										<xsl:otherwise>
											<gco:UnlimitedInteger><xsl:value-of select="$item/cardinality"/></gco:UnlimitedInteger>
										</xsl:otherwise>
									</xsl:choose>
								</gco:upper>								
							</xsl:when>
							<xsl:otherwise>
								<gco:lower>
									<gco:Integer><xsl:value-of select="$default_min"/></gco:Integer>
								</gco:lower>
								<gco:upper>
									<xsl:choose>
										<xsl:when test="$default_max='*'">
											<gco:UnlimitedInteger isInfinite="true" xsi:nil="true"/>
										</xsl:when>
										<xsl:otherwise>
											<gco:UnlimitedInteger><xsl:value-of select="$default_max"/></gco:UnlimitedInteger>
										</xsl:otherwise>
									</xsl:choose>
								</gco:upper>								
							</xsl:otherwise>
						</xsl:choose>
					</gco:MultiplicityRange>
				</gco:range>
			</gco:Multiplicity>
		</gfc:cardinality>
	</xsl:template>
	<xsl:template match="FC_FeatureAttribute">
		<xsl:param name="fid"/>
		<gfc:FC_FeatureAttribute>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<gfc:featureType>
				<xsl:attribute name="xlink:href">#<xsl:value-of select="$fid"/></xsl:attribute>
			</gfc:featureType>
			<gfc:memberName>
				<gco:LocalName><xsl:value-of select="name"/></gco:LocalName>
			</gfc:memberName>
			<xsl:call-template name="definition">
				<xsl:with-param name="item" select="."/>
			</xsl:call-template>
			<xsl:call-template name="cardinality">
				<xsl:with-param name="item" select="."/>
				<xsl:with-param name="default_min" select="1"/>
				<xsl:with-param name="default_max" select="1"/>
			</xsl:call-template>
			<xsl:if test="count(code)=1">
				<gfc:code>
					<gco:CharacterString><xsl:value-of select="code"/></gco:CharacterString>
				</gfc:code>
			</xsl:if>
			<xsl:if test="ValueDataType">
				<gfc:valueType>
					<gco:TypeName>
						<gco:aName>
							<gco:CharacterString><xsl:value-of select="ValueDataType"/></gco:CharacterString>
						</gco:aName>
					</gco:TypeName>
				</gfc:valueType>
			</xsl:if>
			<xsl:if test="count(ValueDomainType)=1 and ValueDomainType='1'">
				<xsl:variable name="aid" select="@id"/>
				<xsl:for-each select="enumeratedBy">
					<xsl:variable name="lvid" select="@idref"/>
					<gfc:listedValue>
						<xsl:apply-templates select="//FC_Value[@id=$lvid]">
							<xsl:with-param name="aid" select="$aid"/>
						</xsl:apply-templates>
					</gfc:listedValue>
				</xsl:for-each>
			</xsl:if>
		</gfc:FC_FeatureAttribute>
	</xsl:template>
	<xsl:template match="FC_Value">
		<xsl:param name="aid"/>
		<gfc:FC_ListedValue>
			<gfc:label>
				<gco:CharacterString><xsl:value-of select="label"/></gco:CharacterString>
			</gfc:label>
			<xsl:if test="count(code)=1">
				<gfc:code>
					<gco:CharacterString><xsl:value-of select="code"/></gco:CharacterString>
				</gfc:code>
			</xsl:if>
			<xsl:call-template name="definition">
				<xsl:with-param name="item" select="."/>
			</xsl:call-template>
		</gfc:FC_ListedValue>
	</xsl:template>
	<xsl:template match="FC_RelationshipRole">
		<xsl:param name="fid"/>
		<gfc:FC_AssociationRole>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<gfc:featureType>
				<xsl:attribute name="xlink:href">#<xsl:value-of select="$fid"/></xsl:attribute>
			</gfc:featureType>
			<gfc:memberName>
				<gco:LocalName><xsl:value-of select="name"/></gco:LocalName>
			</gfc:memberName>
			<xsl:call-template name="definition">
				<xsl:with-param name="item" select="."/>
			</xsl:call-template>
			<xsl:call-template name="cardinality">
				<xsl:with-param name="item" select="."/>
				<xsl:with-param name="default_min" select="0"/>
				<xsl:with-param name="default_max" select="*"/>
			</xsl:call-template>
			<gfc:type>
				<gfc:FC_RoleType codeList="http://shapechange.net/resources/codelist/FC_RoleType.xml" codeListValue="ordinary" />
			</gfc:type>
			<gfc:isOrdered>
				<xsl:choose>
					<xsl:when test="count(orderIndicator)=1 and orderIndicator='1'">
						<gco:Boolean>true</gco:Boolean>
					</xsl:when>
					<xsl:otherwise>
						<gco:Boolean>false</gco:Boolean>
					</xsl:otherwise>
				</xsl:choose>
			</gfc:isOrdered>
			<gfc:isNavigable>
				<gco:Boolean>true</gco:Boolean>
			</gfc:isNavigable>
			<gfc:relation>
				<xsl:attribute name="xlink:href">#<xsl:value-of select="relation/@idref"/></xsl:attribute>
			</gfc:relation>
			<gfc:rolePlayer>
				<xsl:variable name="ft" select="FeatureTypeIncluded"/>
				<xsl:choose>
					<xsl:when test="//AC_FeatureType[name=$ft]">
						<xsl:attribute name="xlink:href">#<xsl:value-of select="//AC_FeatureType[name=$ft]/@id"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="xlink:href"><xsl:value-of select="$ft"/></xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			</gfc:rolePlayer>
		</gfc:FC_AssociationRole>
	</xsl:template>
	<xsl:template match="FC_FeatureRelationship">
		<gfc:FC_FeatureAssociation>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<gfc:typeName>
				<gco:LocalName><xsl:value-of select="name"/></gco:LocalName>
			</gfc:typeName>
			<xsl:call-template name="definition">
				<xsl:with-param name="item" select="."/>
			</xsl:call-template>
			<xsl:if test="count(code)=1">
				<gfc:code>
					<gco:CharacterString><xsl:value-of select="code"/></gco:CharacterString>
				</gfc:code>
			</xsl:if>
			<gfc:isAbstract>
				<gco:Boolean>false</gco:Boolean>
			</gfc:isAbstract>
			<gfc:featureCatalogue xlink:href="#_Objektartenkatalog" />
			<xsl:for-each select="roles">
				<xsl:variable name="fid" select="@idref"/>
				<gfc:roleName>
					<xsl:attribute name="xlink:href">#<xsl:value-of select="@idref"/></xsl:attribute>
				</gfc:roleName>
			</xsl:for-each>
			<xsl:if test="count(roles)=0">
				<gfc:roleName gco:nilReason="unknown"/>
				<gfc:roleName gco:nilReason="unknown"/>
			</xsl:if>
			<xsl:if test="count(roles)=1">
				<gfc:roleName gco:nilReason="unknown"/>
			</xsl:if>
		</gfc:FC_FeatureAssociation>
	</xsl:template>
	<xsl:template match="FC_FeatureOperation">
		<xsl:param name="fid"/>
		<gfc:FC_FeatureOperation>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<gfc:featureType>
				<xsl:attribute name="xlink:href">#<xsl:value-of select="$fid"/></xsl:attribute>
			</gfc:featureType>
			<gfc:memberName>
				<gco:LocalName><xsl:value-of select="substring-after(name,'::')"/></gco:LocalName>
			</gfc:memberName>
			<xsl:call-template name="definition">
				<xsl:with-param name="item" select="."/>
			</xsl:call-template>
			<gfc:cardinality>
				<gco:Multiplicity>
					<gco:range>
						<gco:MultiplicityRange>
							<gco:lower>
								<gco:Integer>1</gco:Integer>
							</gco:lower>
							<gco:upper>
								<gco:UnlimitedInteger>1</gco:UnlimitedInteger>
							</gco:upper>
						</gco:MultiplicityRange>
					</gco:range>
				</gco:Multiplicity>
			</gfc:cardinality>
			<gfc:signature>
				<gco:CharacterString>()</gco:CharacterString>
			</gfc:signature>
		</gfc:FC_FeatureOperation>
	</xsl:template>
</xsl:stylesheet>