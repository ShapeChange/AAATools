<?xml version="1.0" encoding="utf-8"?>
<!-- 
(c) 2001-2020 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->
<xsl:stylesheet version="2.0"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
 xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
 xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
 xmlns:sc="http://shapechange.net/functions" exclude-result-prefixes="sc xs xsl">

 <!-- =============== -->
 <!-- Output settings -->
 <!-- =============== -->
 <xsl:output indent="yes" method="xml"/>

 <!-- ================= -->
 <!-- Catalogue content -->
 <!-- ================= -->
 <!-- The path to the catalogue tmp xml is set automatically by ShapeChange. -->
 <xsl:param name="catalogXmlPath">Ausgaben/Kataloge/aaa.tmp.xml</xsl:param>
 <!-- When executed with ShapeChange, the absolute URI to the catalog XML is automatically determined via a custom URI resolver. -->
 <xsl:variable name="catalog" select="document($catalogXmlPath)"/>
 <xsl:key match="/*/*[@id]" name="modelElement" use="@id"/>

 <!-- ============== -->
 <!-- Docx style XML -->
 <!-- ============== -->
 <!-- The path to the docx internal document.xml is set automatically by ShapeChange. -->
 <xsl:param name="styleXmlPath">_template/word/styles.xml</xsl:param>
 <!-- When executed with ShapeChange, the absolute URI to the style XML is automatically determined via a custom URI resolver. -->
 <xsl:variable name="heading1Id"
  select="document($styleXmlPath)/w:styles/w:style[w:name/@w:val = 'heading 1']/@w:styleId"/>
 <xsl:variable name="heading2Id"
  select="document($styleXmlPath)/w:styles/w:style[w:name/@w:val = 'heading 2']/@w:styleId"/>
 <xsl:variable name="heading3Id"
  select="document($styleXmlPath)/w:styles/w:style[w:name/@w:val = 'heading 3']/@w:styleId"/>
 <xsl:variable name="smallId"
  select="document($styleXmlPath)/w:styles/w:style[w:name/@w:val = 'klein geschrieben']/@w:styleId"/>
 <xsl:variable name="captionId"
  select="document($styleXmlPath)/w:styles/w:style[w:name/@w:val = 'caption']/@w:styleId"/>

 <!-- ============================================ -->
 <!-- Docx transformation parameters and variables -->
 <!-- ============================================ -->
 <xsl:param name="DOCX_PLACEHOLDER">ShapeChangeFeatureCatalogue</xsl:param>
 <xsl:variable name="fcRefId">_RefFeatureCatalogue</xsl:variable>

 <!-- Define the width (in percent [not adding the '%' sign, thus the value is multiplied by 50]) for general table properties. -->
 <xsl:variable name="tableWidth">5000</xsl:variable>

 <!-- Define the width (in percent [not adding the '%' sign, thus the value is multiplied by 50]) for each of the columns in a feature type table. -->
 <xsl:variable name="tableColumn1Width">300</xsl:variable>
 <xsl:variable name="tableColumn2Width">1000</xsl:variable>
 <xsl:variable name="tableColumn12Width">1300</xsl:variable>
 <xsl:variable name="tableColumn3Width">2700</xsl:variable>
 <xsl:variable name="tableColumn4Width">1000</xsl:variable>
 <xsl:variable name="tableColumn34Width">3700</xsl:variable>
 <xsl:variable name="tableColumn234Width">4700</xsl:variable>

 <!-- ====== -->
 <!-- Labels -->
 <!-- ====== -->
 <xsl:include href="aaa-labels.xsl"/>

 <!-- =================================== -->
 <!-- Sort Kennungen as six digit numbers -->
 <!-- =================================== -->
 <xsl:decimal-format name="code" NaN="999999" />

 <!-- ======================== -->
 <!-- Transformation templates -->
 <!-- ======================== -->
 <xsl:template match="@* | node()">
  <xsl:copy>
   <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
 </xsl:template>

 <xsl:template
  match="/w:document/w:body/w:p[w:r/w:t[contains(text(), $DOCX_PLACEHOLDER)]] | /w:document/w:body/w:p[w:ins/w:r/w:t[contains(text(), $DOCX_PLACEHOLDER)]]">
  <xsl:apply-templates select="$catalog/FC_FeatureCatalogue"/>
 </xsl:template>

 <xsl:template match="FC_FeatureCatalogue">
  <w:p>
   <w:pPr>
    <w:pStyle>
     <xsl:attribute name="w:val">
      <xsl:value-of disable-output-escaping="no" select="$heading1Id"/>
     </xsl:attribute>
    </w:pStyle>
   </w:pPr>
   <w:r>
    <w:t>
     <xsl:value-of select="$fc.Objektartenkatalog" />
     <xsl:text> </xsl:text>
     <xsl:value-of disable-output-escaping="no" select="name"/>
    </w:t>
   </w:r>
  </w:p>
  <w:p>
   <w:pPr>
    <w:pStyle>
     <xsl:attribute name="w:val">
      <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
     </xsl:attribute>
    </w:pStyle>
   </w:pPr>
   <w:r>
    <w:t>
     <xsl:value-of select="$fc.Version" />
    </w:t>
   </w:r>
  </w:p>
  <w:p>
   <w:r>
    <w:t>
     <xsl:value-of disable-output-escaping="no" select="versionNumber"/>
    </w:t>
   </w:r>
  </w:p>
  <w:p>
   <w:pPr>
    <w:pStyle>
     <xsl:attribute name="w:val">
      <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
     </xsl:attribute>
    </w:pStyle>
   </w:pPr>
   <w:r>
    <w:t>
     <xsl:value-of select="$fc.Veröffentlichung" />
    </w:t>
   </w:r>
  </w:p>
  <w:p>
   <w:r>
    <w:t>
     <xsl:value-of disable-output-escaping="no" select="versionDate"/>
    </w:t>
   </w:r>
  </w:p>
  <w:p>
   <w:pPr>
    <w:pStyle>
     <xsl:attribute name="w:val">
      <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
     </xsl:attribute>
    </w:pStyle>
   </w:pPr>
   <w:r>
    <w:t>
     <xsl:value-of select="$fc.Anwendungsgebiet" />
    </w:t>
   </w:r>
  </w:p>
  <w:p>
   <w:r>
    <w:t>
     <xsl:value-of select="$fc.Modellarten" /><xsl:text>:</xsl:text>
    </w:t>
   </w:r>
  </w:p>
  <xsl:for-each select="modellart">
   <w:p>
    <w:r>
     <w:t>
      <xsl:text> - </xsl:text>
      <xsl:value-of disable-output-escaping="no" select="text()"/>
      <xsl:if test="@name">: <xsl:value-of select="@name" /></xsl:if>
     </w:t>
    </w:r>
   </w:p>
  </xsl:for-each>
  <xsl:if test="profil">
   <w:p>
      <w:r>
      <w:t>
      <xsl:value-of select="$fc.Profile" /><xsl:text>:</xsl:text>
      </w:t>
      </w:r>
   </w:p>
   <xsl:for-each select="profil">
      <w:p>
      <w:r>
      <w:t>
         <xsl:text> - </xsl:text>
         <xsl:value-of disable-output-escaping="no" select="text()"/>
      </w:t>
      </w:r>
      </w:p>
   </xsl:for-each>
  </xsl:if>
  <xsl:if test="aaaVersionNumber">
   <w:p>
	<w:pPr>
	 <w:pStyle>
	  <xsl:attribute name="w:val">
	   <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
	  </xsl:attribute>
	 </w:pStyle>
	</w:pPr>
	<w:r>
	 <w:t>
	  <xsl:value-of select="$fc.AAA-Version" />
	 </w:t>
	</w:r>
   </w:p>
   <w:p>
	<w:r>
	 <w:t>
	  <xsl:value-of disable-output-escaping="no" select="aaaVersionNumber"/>
	 </w:t>
	</w:r>
   </w:p>
  </xsl:if>
  <xsl:if test="producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName">
   <w:p>
	<w:pPr>
	 <w:pStyle>
	  <xsl:attribute name="w:val">
	   <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
	  </xsl:attribute>
	 </w:pStyle>
	</w:pPr>
	<w:r>
	 <w:t>
	  <xsl:value-of select="$fc.VerantwortlicheInstitution" />
	 </w:t>
	</w:r>
   </w:p>
   <w:p>
	<w:r>
	 <w:t>
	  <xsl:value-of disable-output-escaping="no" select="producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName"/>
	 </w:t>
	</w:r>
   </w:p>
  </xsl:if>
  <xsl:for-each select="AC_Objektartengruppe|AC_Objektbereich">
   <xsl:sort select="format-number(number(code), '000000', 'code')" />
   <xsl:sort select="code" />
   <xsl:apply-templates mode="detail" select="."/>
  </xsl:for-each>
 </xsl:template>

 <xsl:template match="AC_Objektartengruppe|AC_Objektbereich" mode="detail">
  <xsl:variable name="package" select="."/>
  <!-- Test if there are any (feature) types or packages that belong to this package. -->
  <xsl:if
   test="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/@idref=$package/@id]|/FC_FeatureCatalogue/AC_Objektartengruppe[Objektbereichzugehoerigkeit/@idref=$package/@id]">
   <w:p>
    <w:pPr>
     <w:pStyle>
      <xsl:attribute name="w:val">
       <xsl:value-of disable-output-escaping="no" select="$heading1Id"/>
      </xsl:attribute>
     </w:pStyle>
    </w:pPr>
    <w:r>
     <w:t>
      <xsl:call-template name="paket-name">
         <xsl:with-param name="paket" select="."/>
         <xsl:with-param name="name"><xsl:value-of disable-output-escaping="no" select="name"/></xsl:with-param>
       </xsl:call-template>
     </w:t>
    </w:r>
   </w:p>
   <xsl:if test="definition">
    <w:p>
     <w:pPr>
      <w:pStyle>
       <xsl:attribute name="w:val">
        <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
       </xsl:attribute>
      </w:pStyle>
     </w:pPr>
     <w:r>
      <w:t>
       <xsl:value-of select="$fc.Definition" />
      </w:t>
     </w:r>
    </w:p>
    <xsl:for-each select="definition">
     <w:p>
      <w:r>
       <w:t>
        <xsl:value-of disable-output-escaping="no" select="."/>
       </w:t>
      </w:r>
     </w:p>
    </xsl:for-each>
   </xsl:if>
   <xsl:if test="retired">
      <w:p>
         <w:pPr>
          <w:pStyle>
           <xsl:attribute name="w:val">
            <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
           </xsl:attribute>
          </w:pStyle>
         </w:pPr>
         <w:r>
          <w:t>
           <xsl:value-of select="$fc.Stillgelegt" />
          </w:t>
         </w:r>
        </w:p>
        <w:p>
         <w:r>
          <w:t>
            <xsl:choose>
               <xsl:when test="taggedValue[@tag='AAA:GueltigBis']">
                 <xsl:text>Gültig bis GeoInfoDok </xsl:text>
                 <xsl:value-of select="taggedValue[@tag='AAA:GueltigBis'][1]" />
               </xsl:when>
               <xsl:otherwise><xsl:text>Ja</xsl:text></xsl:otherwise>
             </xsl:choose>
          </w:t>
         </w:r>
      </w:p>
   </xsl:if>
   <xsl:if test="nutzungsartkennung">
      <w:p>
         <w:pPr>
            <w:pStyle>
            <xsl:attribute name="w:val">
            <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
            </xsl:attribute>
            </w:pStyle>
         </w:pPr>
         <w:r>
            <w:t>
            <xsl:value-of select="$fc.Nutzungsartkennung" />
            </w:t>
         </w:r>
         </w:p>
         <w:p>
         <w:r>
            <w:t>
            <xsl:value-of disable-output-escaping="no" select="nutzungsartkennung"/>
            </w:t>
         </w:r>
      </w:p>
   </xsl:if>
   <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/attribute::idref=$package/@id]">
    <xsl:sort select="format-number(number(code), '000000', 'code')" />
    <xsl:sort select="code" />
    <xsl:sort select="name"/>
    <xsl:apply-templates mode="detail" select="."/>
   </xsl:for-each>
  </xsl:if>
 </xsl:template>

 <xsl:template match="AC_FeatureType" mode="detail">
  <xsl:variable name="featuretype" select="."/>
  <xsl:variable name="package" select="key('modelElement', $featuretype/Objektartengruppenzugehoerigkeit/@idref)"/>
  <w:p>
   <w:pPr>
    <w:pStyle>
     <xsl:attribute name="w:val">
      <xsl:value-of disable-output-escaping="no" select="$heading2Id"/>
     </xsl:attribute>
    </w:pStyle>
    <w:pageBreakBefore/>
   </w:pPr>
   <w:r>
    <xsl:element name="w:t">
     <xsl:value-of disable-output-escaping="no" select="$featuretype/name"/>
    </xsl:element>
   </w:r>
  </w:p>
  <w:tbl>
   <w:tblPr>
    <w:tblW w:type="pct">
     <xsl:attribute name="w:w">
      <xsl:value-of disable-output-escaping="no" select="$tableWidth"/>
     </xsl:attribute>
    </w:tblW>
    <w:tblBorders>
     <w:top w:color="auto" w:space="0" w:sz="4" w:val="single"/>
     <w:left w:color="auto" w:space="0" w:sz="4" w:val="single"/>
     <w:bottom w:color="auto" w:space="0" w:sz="4" w:val="single"/>
     <w:right w:color="auto" w:space="0" w:sz="4" w:val="single"/>
     <w:insideH w:color="auto" w:space="0" w:sz="0" w:val="none"/>
     <w:insideV w:color="auto" w:space="0" w:sz="0" w:val="none"/>
    </w:tblBorders>
   </w:tblPr>
   <w:tblGrid>
     <w:gridCol w:w="{$tableColumn1Width}"/>
     <w:gridCol w:w="{$tableColumn2Width}"/>
     <w:gridCol w:w="{$tableColumn3Width}"/>
     <w:gridCol w:w="{$tableColumn4Width}"/>
   </w:tblGrid>
   <w:tr>
    <w:trPr>
     <w:tblHeader/>
    </w:trPr>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="4"/>
      <w:shd w:val="pct10" w:color="auto" w:fill="auto"/>
	  <w:tcBorders>
	   <w:bottom w:color="auto" w:space="0" w:sz="4" w:val="single"/>
	  </w:tcBorders>
     </w:tcPr>
     <w:p>
       <w:pPr>
         <w:tabs>
           <w:tab w:val="right" w:pos="8813"/>
         </w:tabs>
      </w:pPr>
      <w:r>
       <w:t>
         <xsl:call-template name="klasse-name">
            <xsl:with-param name="klasse" select="."/>
            <xsl:with-param name="name"><xsl:value-of disable-output-escaping="no" select="$featuretype/name"/></xsl:with-param>
          </xsl:call-template>
       </w:t>
      </w:r>
      <w:r>
        <w:tab/>
      </w:r>
      <w:r>
       <w:t>
         <xsl:value-of select="$fc.Kennung" /><xsl:text>: </xsl:text>
        <xsl:value-of disable-output-escaping="no" select="$featuretype/code"/>
       </w:t>
      </w:r>
     </w:p>
    </w:tc>
   </w:tr>
   <xsl:call-template name="entry">
	<xsl:with-param name="title"><xsl:value-of select="$fc.Definition" />:</xsl:with-param>
	<xsl:with-param name="lines" select="$featuretype/definition"/>
   </xsl:call-template>
   <xsl:if test="$featuretype/retired">
    <xsl:choose>
     <xsl:when test="$featuretype/taggedValue[@tag='AAA:GueltigBis']">
      <xsl:call-template name="entry">
	   <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt" />:</xsl:with-param>
	   <xsl:with-param name="lines">Gültig bis GeoInfoDok <xsl:value-of select="$featuretype/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:with-param>
      </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
      <xsl:call-template name="entry">
	   <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt" />:</xsl:with-param>
	   <xsl:with-param name="lines">Ja</xsl:with-param>
      </xsl:call-template>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:if>
   <!-- nicht in HTML und DOCX: letzteAenderungRevisionsnummer  -->
   <xsl:if test="$featuretype/abstrakt">
      <xsl:call-template name="entry">
         <xsl:with-param name="title"><xsl:value-of select="$fc.Abstrakt" />:</xsl:with-param>
         <xsl:with-param name="lines">Ja</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
   <xsl:call-template name="entry">
	<xsl:with-param name="title"><xsl:value-of select="$fc.AbgeleitetAus" />:</xsl:with-param>
	<xsl:with-param name="lines" select="$featuretype/subtypeOf"/>
   </xsl:call-template>
   <xsl:call-template name="entry">
	<xsl:with-param name="title"><xsl:value-of select="$fc.Objekttyp" />:</xsl:with-param>
	<xsl:with-param name="lines" select="$featuretype/wirdTypisiertDurch"/>
   </xsl:call-template>
   <xsl:call-template name="entry">
	<xsl:with-param name="title"><xsl:value-of select="$fc.Modellarten" />:</xsl:with-param>
	<xsl:with-param name="lines" select="$featuretype/modellart"/>
   </xsl:call-template>
   <xsl:call-template name="entry">
	<xsl:with-param name="title"><xsl:value-of select="$fc.Grunddatenbestand" />:</xsl:with-param>
	<xsl:with-param name="lines" select="$featuretype/grunddatenbestand"/>
   </xsl:call-template>
   <xsl:if test="$featuretype/taggedValue[@tag='AAA:Landnutzung']">
      <xsl:call-template name="entry">
         <xsl:with-param name="title"><xsl:value-of select="$fc.Landnutzung" />:</xsl:with-param>
         <xsl:with-param name="lines">Ja</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
   <!-- nicht in HTML und DOCX: nutzungsart  -->
   <xsl:call-template name="entry">
	<xsl:with-param name="title"><xsl:value-of select="$fc.Nutzungsartkennung" />:</xsl:with-param>
	<xsl:with-param name="lines" select="$featuretype/nutzungsartkennung"/>
   </xsl:call-template>
   <!-- nicht in HTML und DOCX: profil  -->
   <xsl:if test="$featuretype/bildungsregel">
      <xsl:for-each-group select="$featuretype/bildungsregel" group-by="@modellart">
        <xsl:sort select="current-grouping-key()" /> 
        <xsl:call-template name="entry">
         <xsl:with-param name="title">
            <xsl:value-of select="$fc.Bildungsregeln" />
            <xsl:if test="current-grouping-key()!='*'">
            <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()" />                
            </xsl:if>:
         </xsl:with-param>
         <xsl:with-param name="lines" select="current-group()"/>
         </xsl:call-template>
      </xsl:for-each-group> 
    </xsl:if>
    <xsl:if test="$featuretype/erfassungskriterium">
    <xsl:for-each-group select="$featuretype/erfassungskriterium" group-by="@modellart">
      <xsl:sort select="current-grouping-key()" /> 
      <xsl:call-template name="entry">
       <xsl:with-param name="title">
          <xsl:value-of select="$fc.Erfassungskriterien" />
          <xsl:if test="current-grouping-key()!='*'">
          <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()" />                
          </xsl:if>:
       </xsl:with-param>
       <xsl:with-param name="lines" select="current-group()"/>
       </xsl:call-template>
    </xsl:for-each-group> 
  </xsl:if>
  <xsl:if test="$featuretype/konsistenzbedingung">
  <xsl:for-each-group select="$featuretype/konsistenzbedingung" group-by="@modellart">
    <xsl:sort select="current-grouping-key()" /> 
    <xsl:call-template name="entry">
     <xsl:with-param name="title">
        <xsl:value-of select="$fc.Konsistenzbedingungen" />
        <xsl:if test="current-grouping-key()!='*'">
        <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()" />                
        </xsl:if>:
     </xsl:with-param>
     <xsl:with-param name="lines" select="current-group()"/>
     </xsl:call-template>
  </xsl:for-each-group> 
  </xsl:if>
  <xsl:for-each select="key('modelElement', $featuretype/characterizedBy/@idref|/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$featuretype/@id]/@id)">
	<!-- apply an alphabetical sort of feature type characteristics (attributes, relationships etc) -->
   <xsl:sort select="@sequenceNumber" data-type="number"/>
	<xsl:sort select="name"/>
	<xsl:apply-templates mode="detail" select="."/>
   </xsl:for-each>
  </w:tbl>
  <w:p/>
 </xsl:template>

 <xsl:template match="FC_FeatureAttribute|FC_RelationshipRole" mode="detail">
  <xsl:variable name="featureProp" select="."/>
  <w:tr>
   <w:tc>
	<w:tcPr>
	 <w:gridSpan w:val="4"/>
	 <w:tcBorders>
	  <w:top w:color="auto" w:space="0" w:sz="4" w:val="single"/>
	 </w:tcBorders>
	</w:tcPr>
	<w:p>
	 <w:r>
	  <w:rPr>
	   <w:b/>
	  </w:rPr>
	  <w:t>
      <xsl:call-template name="eigenschaft-name">
         <xsl:with-param name="eigenschaft" select="."/>
       </xsl:call-template>
     </w:t>
	 </w:r>
	</w:p>
   </w:tc>
  </w:tr>  
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Bezeichnung" />:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/name"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Kennung" />:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/code"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Definition:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/definition"/>
  </xsl:call-template>
  <xsl:if test="$featureProp/auswerteregel">
   <xsl:call-template name="propentry">
      <xsl:with-param name="title">
         <xsl:value-of select="$fc.Auswerteregel" />:
      </xsl:with-param>
      <xsl:with-param name="lines" select="$featureProp/auswerteregel"/>
   </xsl:call-template>
 </xsl:if>
  <xsl:if test="$featureProp/bildungsregel">
   <xsl:call-template name="propentry">
      <xsl:with-param name="title">
         <xsl:value-of select="$fc.Bildungsregel" />:
      </xsl:with-param>
      <xsl:with-param name="lines" select="$featureProp/bildungsregel"/>
   </xsl:call-template>
 </xsl:if>

  <!-- nicht in HTML und DOCX: objektbildend -->
  <xsl:if test="$featureProp/retired">
   <xsl:choose>
    <xsl:when test="$featureProp/taggedValue[@tag='AAA:GueltigBis']">
     <xsl:call-template name="propentry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt" />:</xsl:with-param>
      <xsl:with-param name="lines">Gültig bis GeoInfoDok <xsl:value-of select="$featureProp/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:with-param>
     </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
     <xsl:call-template name="propentry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt" />:</xsl:with-param>
      <xsl:with-param name="lines">Ja</xsl:with-param>
     </xsl:call-template>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:if>
  <!-- nicht in HTML und DOCX: letzteAenderungRevisionsnummer -->
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Modellarten" />:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/modellart"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Grunddatenbestand" />:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/grunddatenbestand"/>
  </xsl:call-template>
  <xsl:if test="$featureProp/taggedValue[@tag='AAA:Landnutzung']">
  <xsl:call-template name="propentry">
     <xsl:with-param name="title"><xsl:value-of select="$fc.Landnutzung" />:</xsl:with-param>
     <xsl:with-param name="lines">Ja</xsl:with-param>
  </xsl:call-template>
</xsl:if>
<!-- nicht in HTML und DOCX: nutzungsart  -->
<xsl:call-template name="propentry">
<xsl:with-param name="title"><xsl:value-of select="$fc.Nutzungsartkennung" />:</xsl:with-param>
<xsl:with-param name="lines" select="$featureProp/nutzungsartkennung"/>
</xsl:call-template>
<!-- nicht in HTML und DOCX: profil  -->
<xsl:if test="$featureProp/inverseRichtung">
<xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.InverseRichtung" />:</xsl:with-param>
   <xsl:with-param name="lines">Ja</xsl:with-param>
</xsl:call-template>
</xsl:if>
<xsl:if test="$featureProp/derived">
<xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Abgeleitet" />:</xsl:with-param>
   <xsl:with-param name="lines">Ja</xsl:with-param>
</xsl:call-template>
</xsl:if>
<xsl:if test="$featureProp/initialValue">
<xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Defaultwert" />:</xsl:with-param>
   <xsl:with-param name="lines">
      <xsl:value-of select="$featureProp/initialValue" />
      <xsl:if test="readOnly">
         <xsl:text> (</xsl:text><xsl:value-of select="$fc.NichtÄnderbar" /><xsl:text>)</xsl:text>
       </xsl:if>
   </xsl:with-param>
</xsl:call-template>
</xsl:if>
<xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Multiplizität" />:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/cardinality"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Datentyp" />:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/ValueDataType"/>
  </xsl:call-template>
  <xsl:if test="$featureProp/ValueDomainType = 1">
   <xsl:call-template name="clentry">
	<xsl:with-param name="values" select="key('modelElement', $featureProp/enumeratedBy/@idref)"/>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="FeatureTypeIncluded">
   <xsl:call-template name="propentry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Zielobjektart" />:</xsl:with-param>
      <xsl:with-param name="lines" select="$featureProp/FeatureTypeIncluded/@name"/>
     </xsl:call-template>
     <xsl:if test="$featureProp/InverseRole">
      <xsl:call-template name="propentry">
         <xsl:with-param name="title"><xsl:value-of select="$fc.InverseRelationsart" />:</xsl:with-param>
         <xsl:with-param name="lines" select="key('modelElement', $featureProp/InverseRole/@idref)/name"/>
      </xsl:call-template>
    </xsl:if>
   </xsl:if>
</xsl:template>

<!--
 <xsl:template match="FC_RelationshipRole" mode="detail">
  <xsl:variable name="featureProp" select="."/>
  <w:tr>
   <w:tc>
	<w:tcPr>
	 <w:gridSpan w:val="4"/>
	 <w:tcBorders>
	  <w:top w:color="auto" w:space="0" w:sz="4" w:val="single"/>
	 </w:tcBorders>
	</w:tcPr>
	<w:p>
	 <w:r>
	  <w:rPr>
	   <w:b/>
	  </w:rPr>
	  <w:t>Relationsart:</w:t>
	 </w:r>
	</w:p>
   </w:tc>
  </w:tr>  
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Bezeichnung:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/name"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Kennung:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/code"/>
  </xsl:call-template>
  <xsl:if test="$featureProp/retired">
   <xsl:choose>
    <xsl:when test="$featureProp/taggedValue[@tag='AAA:GueltigBis']">
     <xsl:call-template name="propentry">
      <xsl:with-param name="title">Stillgelegt:</xsl:with-param>
      <xsl:with-param name="lines">Gültig bis GeoInfoDok <xsl:value-of select="$featureProp/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:with-param>
     </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
     <xsl:call-template name="propentry">
      <xsl:with-param name="title">Stillgelegt:</xsl:with-param>
      <xsl:with-param name="lines">Ja</xsl:with-param>
     </xsl:call-template>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:if>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Zielobjektart:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/FeatureTypeIncluded/@name"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Kardinalität:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/cardinality"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Modellart:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/modellart"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Grunddatenbestand:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/grunddatenbestand"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Inv. Relation:</xsl:with-param>
   <xsl:with-param name="lines" select="key('modelElement', $featureProp/InverseRole/@idref)/name"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title">Definition:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/definition"/>
  </xsl:call-template>
 </xsl:template>
 -->

 <xsl:template name="entry">
  <xsl:param name="title"/>
  <xsl:param name="lines"/>
  <xsl:if test="$lines">
   <w:tr>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="4"/>
	  <w:tcBorders>
	   <w:top w:color="auto" w:space="0" w:sz="4" w:val="single"/>
	  </w:tcBorders>
     </w:tcPr>
     <w:p>
      <w:r>
       <w:rPr>
        <w:b/>
       </w:rPr>
       <w:t><xsl:value-of disable-output-escaping="no" select="$title"/></w:t>
      </w:r>
     </w:p>
    </w:tc>
   </w:tr>  
   <w:tr>
	<w:tc>
	 <w:tcPr>
	  <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn1Width}" w:type="pct"/>
	 </w:tcPr>
	 <w:p/>
	</w:tc>
	<w:tc>
	 <w:tcPr>
	  <w:gridSpan w:val="3"/>
	  <w:tcW w:w="{$tableColumn234Width}" w:type="pct"/>
	 </w:tcPr>
     <xsl:for-each select="$lines">
      <xsl:variable name="line" select="."/>
      <xsl:if test="string-length(normalize-space($line)) != 0">
	   <w:p>
		<w:r>
		 <w:t><xsl:value-of disable-output-escaping="no" select="$line"/></w:t>
		</w:r>
	   </w:p>
	  </xsl:if> 
	 </xsl:for-each>  
	</w:tc>
   </w:tr>  
  </xsl:if> 
 </xsl:template>

 <xsl:template name="propentry">
  <xsl:param name="title"/>
  <xsl:param name="lines"/>
  <xsl:if test="$lines">
   <w:tr>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn1Width}" w:type="pct"/>
     </w:tcPr>
     <w:p/>
    </w:tc>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn2Width}" w:type="pct"/>
     </w:tcPr>
     <w:p>
      <w:r>
       <w:t><xsl:value-of disable-output-escaping="no" select="$title"/></w:t>
      </w:r>
     </w:p>
    </w:tc>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="2"/>
	  <w:tcW w:w="{$tableColumn34Width}" w:type="pct"/>
     </w:tcPr>
     <xsl:for-each select="$lines">
      <xsl:variable name="line" select="."/>
      <xsl:if test="string-length(normalize-space($line)) != 0">
	   <w:p>
		<w:r>
		 <w:t><xsl:value-of disable-output-escaping="no" select="$line"/></w:t>
		</w:r>
	   </w:p>
	  </xsl:if> 
	 </xsl:for-each>  
    </w:tc>
   </w:tr>  
  </xsl:if> 
 </xsl:template>

 <xsl:template name="clentry">
  <xsl:param name="values"/>
  <xsl:if test="$values">
   <w:tr>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn1Width}" w:type="pct"/>
     </w:tcPr>
     <w:p/>
    </w:tc>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn2Width}" w:type="pct"/>
     </w:tcPr>
     <w:p>
      <w:r>
       <w:t><xsl:value-of select="$fc.Wertearten" />:</w:t>
      </w:r>
     </w:p>
    </w:tc>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn3Width}" w:type="pct"/>
     </w:tcPr>
     <w:p>
      <w:r>
       <w:t><xsl:value-of select="$fc.Name" /></w:t>
      </w:r>
     </w:p>
    </w:tc>
    <w:tc>
     <w:tcPr>
      <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn4Width}" w:type="pct"/>
     </w:tcPr>
     <w:p>
      <w:r>
       <w:t><xsl:value-of select="$fc.Wert" /></w:t>
      </w:r>
     </w:p>
    </w:tc>
   </w:tr>
   <xsl:for-each select="$values">
    <xsl:variable name="value" select="."/>
    <w:tr>
     <w:tc>
      <w:tcPr>
       <w:gridSpan w:val="2"/>
	  <w:tcW w:w="{$tableColumn12Width}" w:type="pct"/>
      </w:tcPr>
      <w:p/>
     </w:tc>
     <w:tc>
      <w:tcPr>
       <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn3Width}" w:type="pct"/>
      </w:tcPr>
      <w:p>
       <w:r>
   		<w:t><xsl:value-of disable-output-escaping="no" select="$value/label"/></w:t>
       </w:r>
      </w:p>
      <xsl:if test="$value/definition">
      	<xsl:for-each select="$value/definition">
         <xsl:if test="string-length(.)>0">
          <w:p>
           <w:r>
            <w:rPr>
             <w:rStyle>
              <xsl:attribute name="w:val">
               <xsl:value-of disable-output-escaping="no" select="$smallId"/>
              </xsl:attribute>
             </w:rStyle>
            </w:rPr>
   		    <w:t><xsl:value-of disable-output-escaping="no" select="."/></w:t>
           </w:r>
          </w:p>
         </xsl:if> 
      	</xsl:for-each>
      </xsl:if>
      <xsl:if test="$value/retired">
       <w:p>
        <w:r>
         <w:rPr>
          <w:rStyle>
           <xsl:attribute name="w:val">
            <xsl:value-of disable-output-escaping="no" select="$smallId"/>
           </xsl:attribute>
          </w:rStyle>
         </w:rPr>
   		 <w:t>
          <xsl:choose>
           <xsl:when test="$value/taggedValue[@tag='AAA:GueltigBis']"><xsl:value-of select="$fc.Stillgelegt" />: Gültig bis GeoInfoDok<xsl:value-of disable-output-escaping="no" select="$value/taggedValue[@tag='AAA:GueltigBis'][1]"/></xsl:when>
           <xsl:otherwise><xsl:value-of select="$fc.Stillgelegt" />: Ja</xsl:otherwise>
          </xsl:choose>
   		 </w:t>
        </w:r>
       </w:p>
      </xsl:if>
      <xsl:if test="$value/grunddatenbestand">
          <w:p>
           <w:r>
            <w:rPr>
             <w:rStyle>
              <xsl:attribute name="w:val">
               <xsl:value-of disable-output-escaping="no" select="$smallId"/>
              </xsl:attribute>
             </w:rStyle>
            </w:rPr>
   		    <w:t><xsl:value-of select="$fc.Grunddatenbestand" /><xsl:text>:</xsl:text><xsl:for-each select="$value/grunddatenbestand"><xsl:text> </xsl:text><xsl:value-of disable-output-escaping="no" select="."/></xsl:for-each></w:t>
           </w:r>
          </w:p>
      </xsl:if>
      <xsl:if test="$value/nutzungsartkennung">
          <w:p>
           <w:r>
            <w:rPr>
             <w:rStyle>
              <xsl:attribute name="w:val">
               <xsl:value-of disable-output-escaping="no" select="$smallId"/>
              </xsl:attribute>
             </w:rStyle>
            </w:rPr>
   		    <w:t><xsl:value-of select="$fc.Nutzungsartkennung" /><xsl:text>: </xsl:text><xsl:value-of disable-output-escaping="no" select="$value/nutzungsartkennung"/></w:t>
           </w:r>
          </w:p>
      </xsl:if>
   </w:tc>
     <w:tc>
      <w:tcPr>
       <w:gridSpan w:val="1"/>
	  <w:tcW w:w="{$tableColumn4Width}" w:type="pct"/>
      </w:tcPr>
      <w:p>
       <w:r>
   		<w:t>
   		 <xsl:value-of disable-output-escaping="no" select="$value/code"/>
   		 <xsl:if test="$value/grunddatenbestand"><xsl:text> (G)</xsl:text></xsl:if>
   		 <xsl:if test="$value/taggedValue[@tag='AAA:Landnutzung' and translate(.,'TRUE','true')='true']"><xsl:text> (LN)</xsl:text></xsl:if>
   		</w:t>
       </w:r>
      </w:p>
     </w:tc>
    </w:tr>
   </xsl:for-each>  
  </xsl:if> 
 </xsl:template>
 
</xsl:stylesheet>
