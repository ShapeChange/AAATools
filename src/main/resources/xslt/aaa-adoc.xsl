<?xml version="1.0" encoding="utf-8"?>
<!-- 
(c) 2001-2022 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:sc="http://shapechange.net/functions" xmlns:adoc="http://asciidoc.org/"
 exclude-result-prefixes="sc xs xsl">

 <!-- =============== -->
 <!-- Output settings -->
 <!-- =============== -->
 <xsl:output indent="no" method="text"/>

 <!-- ================= -->
 <!-- Catalogue content -->
 <!-- ================= -->
 <!-- The path to the catalogue tmp xml is set automatically by ShapeChange. -->
 <!-- <xsl:param name="catalogXmlPath">Ausgaben/Kataloge/aaa.tmp.xml</xsl:param>-->
 <!-- When executed with ShapeChange, the absolute URI to the catalog XML is automatically determined via a custom URI resolver. -->
 <!-- <xsl:variable name="catalog" select="document($catalogXmlPath)"/>-->
 <xsl:key match="/*/*[@id]" name="modelElement" use="@id"/>

 <!-- ============================================ -->
 <!-- Transformation parameters and variables -->
 <!-- ============================================ -->
 
 <xsl:param name="redmineUrl">https://services.interactive-instruments.de/qsm/issues/</xsl:param>

 <xsl:variable name="newline">
  <xsl:text>&#xa;</xsl:text>
 </xsl:variable>

 <xsl:variable name="doublenewline">
  <xsl:text>&#xa;&#xa;</xsl:text>
 </xsl:variable>

 <xsl:variable name="insopen">[[ins]]</xsl:variable>
 <xsl:variable name="insclose">[[/ins]]</xsl:variable>
 <xsl:variable name="delopen">[[del]]</xsl:variable>
 <xsl:variable name="delclose">[[/del]]</xsl:variable>

 <!-- ====== -->
 <!-- Labels -->
 <!-- ====== -->
 <xsl:include href="aaa-labels.xsl"/>

 <!-- =================================== -->
 <!-- Sort Kennungen as six digit numbers -->
 <!-- =================================== -->
 <xsl:decimal-format name="code" NaN="999999"/>

 <!-- ======================== -->
 <!-- Transformation templates -->
 <!-- ======================== -->

 <xsl:function name="adoc:id">
  <xsl:param name="idvalue"/>
  <xsl:value-of select="concat('[[', $idvalue, ']]')"/>
 </xsl:function>

 <xsl:function name="adoc:idref">
  <xsl:param name="idvalue"/>
  <xsl:param name="text" as="xs:string"/>
  <xsl:value-of select="concat('&lt;&lt;', $idvalue, ',', $text, '&gt;&gt;')"/>
 </xsl:function>

 <xsl:function name="adoc:heading">
  <xsl:param name="hPrefix" as="xs:string"/>
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:if test="$hid">
   <xsl:value-of select="adoc:id($hid)"/>
   <xsl:value-of select="$newline"/>
  </xsl:if>
  <xsl:value-of select="$hPrefix"/>
  <xsl:value-of select="$htitle"/>
 </xsl:function>
 
 <xsl:function name="adoc:discreteHeading">
  <xsl:param name="hPrefix" as="xs:string"/>
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:if test="$hid">
   <xsl:value-of select="adoc:id($hid)"/>
   <xsl:value-of select="$newline"/>
  </xsl:if>
  <xsl:text>[discrete]</xsl:text>
  <xsl:value-of select="$newline"/>
  <xsl:value-of select="$hPrefix"/>
  <xsl:value-of select="$htitle"/>
 </xsl:function>

 <xsl:function name="adoc:h2">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:heading('== ', $htitle, $hid)"/>
 </xsl:function>
 
 <xsl:function name="adoc:discreteH2">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:discreteHeading('== ', $htitle, $hid)"/>
 </xsl:function>

 <xsl:function name="adoc:h3">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:heading('=== ', $htitle, $hid)"/>
 </xsl:function>
 
 <xsl:function name="adoc:discreteH3">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:discreteHeading('=== ', $htitle, $hid)"/>
 </xsl:function>

 <xsl:function name="adoc:h4">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:heading('==== ', $htitle, $hid)"/>
 </xsl:function>
 
 <xsl:function name="adoc:discreteH4">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:discreteHeading('==== ', $htitle, $hid)"/>
 </xsl:function>

 <xsl:function name="adoc:h5">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:heading('===== ', $htitle, $hid)"/>
 </xsl:function>
 
 <xsl:function name="adoc:discreteH5">
  <xsl:param name="htitle" as="xs:string"/>
  <xsl:param name="hid"/>
  <xsl:value-of select="adoc:discreteHeading('===== ', $htitle, $hid)"/>
 </xsl:function>

 <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
  <xd:desc>
   <xd:p>Applies a couple of replacements on the given string $s: '[[ins]][[/ins]]' and '[[ins]][[/ins]]' to '', '[[ins]]' to '[.ins]##', '[[del]]' to '[.del]##', '[[/ins]]' and '[[/del]]' to
    '##'.</xd:p>
  </xd:desc>
  <xd:param name="s">The string that shall be converted.</xd:param>
 </xd:doc>
 <xsl:function name="adoc:text">
  <xsl:param name="s" as="xs:string?"/>
  <xsl:variable name="s_empty_ins" select="replace($s, '\[\[ins\]\]\[\[/ins\]\]', '')"/>
  <xsl:variable name="s_empty_del" select="replace($s_empty_ins, '\[\[del\]\]\[\[/del\]\]', '')"/>
  <xsl:variable name="s_ins_open" select="replace($s_empty_del, '\[\[ins\]\]', '[.ins]##')"/>
  <xsl:variable name="s_ins_close" select="replace($s_ins_open, '\[\[del\]\]', '[.del]##')"/>
  <xsl:variable name="s_del_open" select="replace($s_ins_close, '\[\[/ins\]\]', '##')"/>
  <xsl:variable name="s_del_close" select="replace($s_del_open, '\[\[/del\]\]', '##')"/>
  <!-- JE: I know that this simple renaming is not strictly necessary. However, it is useful for extending the replacement logic. -->
  <xsl:variable name="s_final" select="$s_del_close"/>
  <xsl:value-of select="$s_final"/>
 </xsl:function>

 <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
  <xd:desc>
   <xd:p>Checks @mode on the given $element. If @mode=INSERT then '[.ins]##' is prepended to the element text, '##' is appended. If @mode=DELETE then '[.del]##' is prepended, '##' is appended.
    Otherwise, nothing is prepended or appended. However, the element text will additionally be converted using function adoc:text(..).</xd:p>
  </xd:desc>
  <xd:param name="element">The element whose textual value shall be returned, altered as needed to take into account diff-information (via @mode as well as [[ins]] and [[del]] within the element
   text).</xd:param>
 </xd:doc>
 <xsl:function name="adoc:diff-element">
  <xsl:param name="element" as="element()"/>
  <xsl:choose>
   <xsl:when test="$element/@mode eq 'INSERT' and (string-length(normalize-space($element/text())) != 0)">
    <xsl:value-of select="concat('[.ins]##', adoc:text($element/text()), '##')"/>
   </xsl:when>
   <xsl:when test="$element/@mode eq 'DELETE' and (string-length(normalize-space($element/text())) != 0)">
    <xsl:value-of select="concat('[.del]##', adoc:text($element/text()), '##')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="adoc:text($element/text())"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:function>

 <xsl:function name="adoc:diff-text-with-element">
  <xsl:param name="text" as="xs:string"/>
  <xsl:param name="element" as="element()"/>
  <xsl:choose>
   <xsl:when test="$element/@mode eq 'INSERT' and (string-length(normalize-space($element/text())) != 0)">
    <xsl:value-of select="concat('[.ins]##', adoc:text($text), '##')"/>
   </xsl:when>
   <xsl:when test="$element/@mode eq 'DELETE' and (string-length(normalize-space($element/text())) != 0)">
    <xsl:value-of select="concat('[.del]##', adoc:text($text), '##')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="adoc:text($text)"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:function>
 
 <xsl:function name="adoc:indented">
  <xsl:param name="text" as="xs:string"/>
  <xsl:choose>
   <xsl:when test="string-length($text) = 0">
    <xsl:value-of select="$text"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="concat('[role=indented]',$newline,$text)"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:function>

 <xsl:function name="adoc:paket-name">
  <xsl:param name="paketElement" as="element()"/>
  <xsl:param name="pNameString" as="xs:string"/>
  <xsl:value-of select="
    concat((if (count($paketElement/Objektbereichzugehoerigkeit) = 1) then
     'Objektartengruppe: '
    else
     'Objektartenbereich: '), $pNameString)"/>
 </xsl:function>
 
 <xsl:function name="adoc:redmineLinks">
  <xsl:param name="revisionsnummern" as="xs:string"/>
  <xsl:variable name="revnumbers" select="tokenize(replace($revisionsnummern,'\.',','),'\s*,\s*')"/>
  <!-- Revisionsnummern ohne vorangestelltes "#" bleiben unberücksichtigt, da sie sich nicht auf Redmine-Tickets beziehen, sondern auf die Vorgängerverwaltung in Word-Dokumenten. -->
  <xsl:variable name="numbers" select="for $rn in $revnumbers[starts-with(.,'#')] return replace($rn,'#','')"/>
  <!-- https://docs.asciidoctor.org/asciidoc/latest/macros/link-macro-attribute-parsing/#blank-window-shorthand -->
  <xsl:value-of select="for $number in $numbers return concat($redmineUrl,$number,'[',$number,',window=\_blank]')"/>
 </xsl:function>
 
 <xsl:function name="adoc:hatRelevanteRevisionsnummern" as="xs:boolean">
  <xsl:param name="revisionsnummernElement" as="element()?"/>
  <xsl:choose>
   <xsl:when test="not(exists($revisionsnummernElement)) or string-length($revisionsnummernElement/text()) = 0"><xsl:value-of select="false()"/></xsl:when>
   <xsl:otherwise>
    <xsl:variable name="revnumbers" select="tokenize($revisionsnummernElement,'\s*,\s*')"/>
    <xsl:value-of select="some $rn in $revnumbers satisfies starts-with($rn,'#')"/>    
   </xsl:otherwise>
  </xsl:choose>  
 </xsl:function>

 <xsl:template match="@* | node()">
  <xsl:copy>
   <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="FC_FeatureCatalogue">

  <xsl:value-of select="adoc:h2(concat($fc.Objektartenkatalog, ' ', name), @id)"/>
  <xsl:value-of select="$doublenewline"/>

  <xsl:value-of select="adoc:h3($fc.Version, '')"/>
  <xsl:value-of select="$doublenewline"/>
  <xsl:value-of select="adoc:indented(adoc:text(versionNumber))"/>
  <xsl:value-of select="$doublenewline"/>

  <xsl:value-of select="adoc:h3($fc.Veröffentlichung, '')"/>
  <xsl:value-of select="$doublenewline"/>
  <xsl:value-of select="adoc:indented(adoc:text(versionDate))"/>
  <xsl:value-of select="$doublenewline"/>

  <xsl:value-of select="adoc:h3($fc.Anwendungsgebiet, '')"/>
  <xsl:value-of select="$doublenewline"/>
  <xsl:value-of select="adoc:indented(adoc:text(concat($fc.Modellarten, ':')))"/>
  <xsl:value-of select="$doublenewline"/>
  <xsl:for-each select="modellart">
   <xsl:value-of select="adoc:indented(
     adoc:text(concat('* ', text(), if (@name) then
      concat(': ', @name)
     else
      ()))
      )"/>
   <xsl:value-of select="$newline"/>
  </xsl:for-each>
  <xsl:value-of select="$newline"/>

  <xsl:if test="profil">
   <xsl:value-of select="adoc:text(concat($fc.Profile, ':'))"/>
   <xsl:value-of select="$doublenewline"/>
   <xsl:for-each select="profil">
    <xsl:value-of select="adoc:text(concat('* ', text()))"/>
    <xsl:value-of select="$newline"/>
   </xsl:for-each>
   <xsl:value-of select="$newline"/>
  </xsl:if>

  <xsl:if test="aaaVersionNumber">
   <xsl:value-of select="adoc:h3($fc.AAA-Version, '')"/>
   <xsl:value-of select="$doublenewline"/>

   <xsl:value-of select="adoc:indented(adoc:text(aaaVersionNumber))"/>
   <xsl:value-of select="$doublenewline"/>
  </xsl:if>

  <xsl:if test="producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName">
   <xsl:value-of select="adoc:h3($fc.VerantwortlicheInstitution, '')"/>
   <xsl:value-of select="$doublenewline"/>

   <xsl:value-of select="adoc:indented(adoc:text(producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName))"/>
   <xsl:value-of select="$doublenewline"/>
  </xsl:if>

  <xsl:for-each select="AC_Objektartengruppe | AC_Objektbereich">
   <xsl:sort select="format-number(number(code), '000000', 'code')"/>
   <xsl:sort select="code"/>
   <xsl:apply-templates mode="detail" select="."/>
  </xsl:for-each>

 </xsl:template>

 <xsl:template match="AC_Objektartengruppe | AC_Objektbereich" mode="detail">
  <xsl:variable name="package" select="."/>
  <!-- Test if there are any (feature) types or packages that belong to this package. -->
  <xsl:if
   test="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/@idref = $package/@id] | /FC_FeatureCatalogue/AC_Objektartengruppe[Objektbereichzugehoerigkeit/@idref = $package/@id]">

   <xsl:value-of select="adoc:h2(adoc:paket-name(., adoc:diff-element(name)), @id)"/>
   <xsl:value-of select="$doublenewline"/>

   <xsl:if test="definition">
    <xsl:value-of select="adoc:h3($fc.Definition, '')"/>
    <xsl:value-of select="$doublenewline"/>

    <xsl:for-each select="definition">
     <xsl:value-of select="adoc:indented(adoc:diff-element(.))"/>
     <xsl:value-of select="$newline"/>
    </xsl:for-each>
    <xsl:value-of select="$newline"/>
   </xsl:if>

   <xsl:if test="retired">
    <xsl:value-of select="adoc:h3($fc.Stillgelegt, '')"/>
    <xsl:value-of select="$doublenewline"/>

    <xsl:choose>
     <xsl:when test="taggedValue[@tag = 'AAA:GueltigBis']">
      <xsl:value-of select="adoc:indented(adoc:text(concat('Gültig bis GeoInfoDok ', taggedValue[@tag = 'AAA:GueltigBis'][1])))"/>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="adoc:indented('Ja')"/>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="$newline"/>
   </xsl:if>

   <xsl:if test="nutzungsartkennung">
    <xsl:value-of select="adoc:h3($fc.Nutzungsartkennung, '')"/>
    <xsl:value-of select="$doublenewline"/>

    <xsl:value-of select="adoc:indented(adoc:text(nutzungsartkennung))"/>
    <xsl:value-of select="$doublenewline"/>
   </xsl:if>

   <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/attribute::idref = $package/@id]">
    <xsl:sort select="format-number(number(code), '000000', 'code')"/>
    <xsl:sort select="code"/>
    <xsl:sort select="name"/>
    <xsl:apply-templates mode="detail" select="."/>
   </xsl:for-each>
  </xsl:if>
 </xsl:template>

 <xsl:template match="AC_FeatureType" mode="detail">
  <xsl:variable name="featuretype" select="."/>
  <xsl:variable name="package" select="key('modelElement', $featuretype/Objektartengruppenzugehoerigkeit/@idref)"/>

  <xsl:value-of select="adoc:h3($featuretype/name, @id)"/>
  <xsl:value-of select="$doublenewline"/>
  
  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Kennung"/>:</xsl:with-param>
   <xsl:with-param name="lines"><xsl:value-of select="$featuretype/code"/></xsl:with-param>
  </xsl:call-template>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Definition"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featuretype/definition"/>
  </xsl:call-template>

  <xsl:if test="$featuretype/retired">
   <xsl:choose>
    <xsl:when test="$featuretype/taggedValue[@tag = 'AAA:GueltigBis']">
     <xsl:call-template name="entry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt"/>:</xsl:with-param>
      <xsl:with-param name="lines">Gültig bis GeoInfoDok <xsl:value-of select="$featuretype/taggedValue[@tag = 'AAA:GueltigBis'][1]"/></xsl:with-param>
     </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
     <xsl:call-template name="entry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt"/>:</xsl:with-param>
      <xsl:with-param name="lines">Ja</xsl:with-param>
     </xsl:call-template>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:if>

  <!-- nicht in HTML und DOCX: letzteAenderungRevisionsnummer  -->
  <xsl:if test="adoc:hatRelevanteRevisionsnummern($featuretype/letzteAenderungRevisionsnummer)">
  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Revisionsnummer"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="adoc:redmineLinks($featuretype/letzteAenderungRevisionsnummer)"/>
  </xsl:call-template>
  </xsl:if>
  
  <xsl:if test="$featuretype/abstrakt">
   <xsl:call-template name="entry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Abstrakt"/>:</xsl:with-param>
    <xsl:with-param name="lines">
     <xsl:value-of select="adoc:diff-text-with-element('Ja', $featuretype/abstrakt)"/>
    </xsl:with-param>
   </xsl:call-template>
  </xsl:if>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.AbgeleitetAus"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="for $sto in $featuretype/subtypeOf return (if (/FC_FeatureCatalogue/AC_FeatureType[name=$sto]) then (for $st in /FC_FeatureCatalogue/AC_FeatureType[name=$sto][1] return adoc:diff-text-with-element(adoc:idref($st/@id,$st/name),$sto)) else ($sto))"/>
  </xsl:call-template>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Objekttyp"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featuretype/wirdTypisiertDurch"/>
  </xsl:call-template>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Modellarten"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featuretype/modellart"/>
  </xsl:call-template>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Grunddatenbestand"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featuretype/grunddatenbestand"/>
  </xsl:call-template>

  <xsl:if test="$featuretype/taggedValue[@tag = 'AAA:Landnutzung']">
   <xsl:call-template name="entry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Landnutzung"/>:</xsl:with-param>
    <xsl:with-param name="lines">
     <xsl:value-of select="adoc:diff-text-with-element('Ja', $featuretype/taggedValue[@tag = 'AAA:Landnutzung'])"/>
    </xsl:with-param>
   </xsl:call-template>
  </xsl:if>

  <!-- nicht in HTML und DOCX: nutzungsart  -->
  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Nutzungsartkennung"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featuretype/nutzungsartkennung"/>
  </xsl:call-template>

  <!-- nicht in HTML und DOCX: profil  -->
  <xsl:if test="$featuretype/bildungsregel">
   <xsl:for-each-group select="$featuretype/bildungsregel" group-by="@modellart">
    <xsl:sort select="current-grouping-key()"/>
    <xsl:call-template name="entry">
     <xsl:with-param name="title">
      <xsl:value-of select="$fc.Bildungsregeln"/>
      <xsl:if test="current-grouping-key() != '*'">
       <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()"/>
      </xsl:if>:</xsl:with-param>
     <xsl:with-param name="lines" select="current-group()"/>
    </xsl:call-template>
   </xsl:for-each-group>
  </xsl:if>

  <xsl:if test="$featuretype/erfassungskriterium">
   <xsl:for-each-group select="$featuretype/erfassungskriterium" group-by="@modellart">
    <xsl:sort select="current-grouping-key()"/>
    <xsl:call-template name="entry">
     <xsl:with-param name="title">
      <xsl:value-of select="$fc.Erfassungskriterien"/>
      <xsl:if test="current-grouping-key() != '*'">
       <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()"/>
      </xsl:if>:</xsl:with-param>
     <xsl:with-param name="lines" select="current-group()"/>
    </xsl:call-template>
   </xsl:for-each-group>
  </xsl:if>

  <xsl:if test="$featuretype/konsistenzbedingung">
   <xsl:for-each-group select="$featuretype/konsistenzbedingung" group-by="@modellart">
    <xsl:sort select="current-grouping-key()"/>
    <xsl:call-template name="entry">
     <xsl:with-param name="title">
      <xsl:value-of select="$fc.Konsistenzbedingungen"/>
      <xsl:if test="current-grouping-key() != '*'">
       <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()"/>
      </xsl:if>:</xsl:with-param>
     <xsl:with-param name="lines" select="current-group()"/>
    </xsl:call-template>
   </xsl:for-each-group>
  </xsl:if>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Attributarten"/>:</xsl:with-param>
   <xsl:with-param name="lines">
    <xsl:choose>
     <xsl:when test="/FC_FeatureCatalogue/FC_FeatureAttribute[@id = $featuretype/characterizedBy/@idref]">
      <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[@id = $featuretype/characterizedBy/@idref]">
       <xsl:sort select="@sequenceNumber"/>
       <xsl:sort select="name"/>
       <xsl:text>* </xsl:text>
       <xsl:value-of select="adoc:idref(@id, name/text())"/>
       <xsl:if test="grunddatenbestand"> (<xsl:value-of select="$fc.Grunddatenbestand"/>)</xsl:if>
       <xsl:value-of select="$newline"/>
      </xsl:for-each>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="$fc.keine"/>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:with-param>
  </xsl:call-template>

  <xsl:call-template name="entry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Relationsarten"/>:</xsl:with-param>
   <xsl:with-param name="lines">
    <xsl:choose>
     <xsl:when test="/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref = $featuretype/@id]">
      <xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref = $featuretype/@id]">
       <xsl:sort select="@sequenceNumber"/>
       <xsl:sort select="name"/>
       <xsl:text>* </xsl:text>
       <xsl:value-of select="adoc:idref(@id, name/text())"/>
       <xsl:if test="grunddatenbestand"> (<xsl:value-of select="$fc.Grunddatenbestand"/>)</xsl:if>
       <xsl:value-of select="$newline"/>
      </xsl:for-each>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="$fc.keine"/>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:with-param>
  </xsl:call-template>

  <xsl:for-each select="key('modelElement', $featuretype/characterizedBy/@idref | /FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref = $featuretype/@id]/@id)">
   <!-- apply an alphabetical sort of feature type characteristics (attributes, relationships etc) -->
   <xsl:sort select="@sequenceNumber"/>
   <xsl:sort select="name"/>
   <xsl:apply-templates mode="detail" select="."/>
  </xsl:for-each>

  <xsl:value-of select="$newline"/>
  <xsl:value-of select="$newline"/>

 </xsl:template>


 <xsl:template match="FC_FeatureAttribute | FC_RelationshipRole" mode="detail">
  <xsl:variable name="featureProp" select="."/>
  <xsl:variable name="propKindTitle">
   <xsl:choose>
    <xsl:when test="$featureProp/local-name() = 'FC_FeatureAttribute'">
     <xsl:text>Attributart:</xsl:text>
    </xsl:when>
    <xsl:otherwise>
     <xsl:text>Relationsart:</xsl:text>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>  
  
  <xsl:value-of select="adoc:discreteH4(concat($propKindTitle, ' ', $featureProp/*:name/text()), @id)"/>
  <xsl:value-of select="$doublenewline"/>
  
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Kennung"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/code"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Definition"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/definition"/>
  </xsl:call-template>
  <xsl:if test="$featureProp/auswerteregel">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Auswerteregel"/>:</xsl:with-param>
    <xsl:with-param name="lines" select="$featureProp/auswerteregel"/>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="$featureProp/bildungsregel">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Bildungsregel"/>:</xsl:with-param>
    <xsl:with-param name="lines" select="$featureProp/bildungsregel"/>
   </xsl:call-template>
  </xsl:if>
  <!-- nicht in HTML und DOCX: objektbildend -->
  <xsl:if test="$featureProp/retired">
   <xsl:choose>
    <xsl:when test="$featureProp/taggedValue[@tag = 'AAA:GueltigBis']">
     <xsl:call-template name="propentry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt"/>:</xsl:with-param>
      <xsl:with-param name="lines">Gültig bis GeoInfoDok <xsl:value-of select="$featureProp/taggedValue[@tag = 'AAA:GueltigBis'][1]"/></xsl:with-param>
     </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
     <xsl:call-template name="propentry">
      <xsl:with-param name="title"><xsl:value-of select="$fc.Stillgelegt"/>:</xsl:with-param>
      <xsl:with-param name="lines">
       <xsl:value-of select="adoc:diff-text-with-element('Ja', $featureProp/retired)"/>
      </xsl:with-param>
     </xsl:call-template>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:if>
  <!-- nicht in HTML und DOCX: letzteAenderungRevisionsnummer -->
  <xsl:if test="adoc:hatRelevanteRevisionsnummern($featureProp/letzteAenderungRevisionsnummer)">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Revisionsnummer"/>:</xsl:with-param>
    <xsl:with-param name="lines" select="adoc:redmineLinks($featureProp/letzteAenderungRevisionsnummer)"/>
   </xsl:call-template>
  </xsl:if>
  
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Modellarten"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/modellart"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Grunddatenbestand"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/grunddatenbestand"/>
  </xsl:call-template>
  <xsl:if test="$featureProp/taggedValue[@tag = 'AAA:Landnutzung']">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Landnutzung"/>:</xsl:with-param>
    <xsl:with-param name="lines">
     <xsl:value-of select="adoc:diff-text-with-element('Ja', $featureProp/taggedValue[@tag = 'AAA:Landnutzung'])"/>
    </xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <!-- nicht in HTML und DOCX: nutzungsart  -->
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Nutzungsartkennung"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/nutzungsartkennung"/>
  </xsl:call-template>
  <!-- nicht in HTML und DOCX: profil  -->
  <xsl:if test="$featureProp/inverseRichtung">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.InverseRichtung"/>:</xsl:with-param>
    <xsl:with-param name="lines">
     <xsl:value-of select="adoc:diff-text-with-element('Ja', $featureProp/inverseRichtung)"/>
    </xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="$featureProp/derived">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Abgeleitet"/>:</xsl:with-param>
    <xsl:with-param name="lines">
     <xsl:value-of select="adoc:diff-text-with-element('Ja', $featureProp/derived)"/>
    </xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="$featureProp/initialValue">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Defaultwert"/>:</xsl:with-param>
    <xsl:with-param name="lines">
     <xsl:value-of select="adoc:diff-element($featureProp/initialValue)"/>
     <xsl:if test="readOnly">
      <xsl:text> (</xsl:text>
      <xsl:value-of select="$fc.NichtÄnderbar"/>
      <xsl:text>)</xsl:text>
     </xsl:if>
    </xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Multiplizität"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="$featureProp/cardinality"/>
  </xsl:call-template>
  <xsl:call-template name="propentry">
   <xsl:with-param name="title"><xsl:value-of select="$fc.Datentyp"/>:</xsl:with-param>
   <xsl:with-param name="lines" select="if (exists($featureProp/ValueDataType)) then (if ($featureProp/ValueDataType/@idref) then adoc:diff-text-with-element(adoc:idref($featureProp/ValueDataType/@idref,$featureProp/ValueDataType/text()),$featureProp/ValueDataType) else $featureProp/ValueDataType) else ()"/>   
  </xsl:call-template>
  <xsl:if test="$featureProp/ValueDomainType = 1">
   <xsl:call-template name="clentry">
    <xsl:with-param name="values" select="key('modelElement', $featureProp/enumeratedBy/@idref)"/>
    <xsl:with-param name="valueDomainType" select="$featureProp/ValueDomainType"/>    
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="FeatureTypeIncluded">
   <xsl:call-template name="propentry">
    <xsl:with-param name="title"><xsl:value-of select="$fc.Zielobjektart"/>:</xsl:with-param>
    <xsl:with-param name="lines" select="adoc:diff-text-with-element(adoc:idref($featureProp/FeatureTypeIncluded/@idref,$featureProp/FeatureTypeIncluded/@name),$featureProp/FeatureTypeIncluded)"/>
   </xsl:call-template>
   <xsl:if test="$featureProp/InverseRole">
    <xsl:variable name="inverseRole" select="key('modelElement', $featureProp/InverseRole/@idref)"/>
    <xsl:call-template name="propentry">
     <xsl:with-param name="title"><xsl:value-of select="$fc.InverseRelationsart"/>:</xsl:with-param>
     <xsl:with-param name="lines" select="adoc:idref($inverseRole/@id,$inverseRole/name/text())"/>
    </xsl:call-template>
   </xsl:if>
  </xsl:if>
  <xsl:value-of select="$doublenewline"/>
 </xsl:template>

 <xsl:template name="entry">
  <xsl:param name="title"/>
  <xsl:param name="lines"/>
  <xsl:if test="$lines">
   <xsl:value-of select="adoc:text(concat('*', $title, '*'))"/>
   <xsl:value-of select="$doublenewline"/>
   
   <xsl:for-each select="$lines">
    <xsl:variable name="line" select="
      if (. instance of element()) then
        adoc:indented(adoc:diff-element(.))
      else
        adoc:indented(adoc:text(.))"/>
    <xsl:if test="string-length(normalize-space($line)) != 0">
     <xsl:value-of disable-output-escaping="no" select="$line"/>
     <xsl:value-of select="$newline"/>
    </xsl:if>
   </xsl:for-each>
   <xsl:value-of select="$doublenewline"/>
  </xsl:if>
 </xsl:template>

 <xsl:template name="propentry">
  <xsl:param name="title"/>
  <xsl:param name="lines"/>
  <xsl:if test="$lines">
   <xsl:value-of select="adoc:text(concat('*', $title, '*'))"/>
   <xsl:value-of select="$doublenewline"/>
   <xsl:for-each select="$lines">
    <xsl:variable name="line" select="
      if (. instance of element()) then       
        adoc:indented(adoc:diff-element(.))
      else
        adoc:indented(adoc:text(.))"/>
    <xsl:if test="string-length(normalize-space($line)) != 0">
     <xsl:value-of disable-output-escaping="no" select="$line"/>
     <xsl:value-of select="$newline"/>
    </xsl:if>
   </xsl:for-each>
   <xsl:value-of select="$newline"/>
  </xsl:if>
 </xsl:template>

 <xsl:template name="clentry">
  <xsl:param name="values" as="element()*"/>
  <xsl:param name="valueDomainType" as="element()"/>
  <xsl:if test="$values">   
   <xsl:value-of select="concat('*', $fc.Wertearten,':*')"/>
   <xsl:value-of select="$newline"/>
   <xsl:text>[width="99%",cols="3,1",options="header",grid="rows",role=indented]</xsl:text>
   <xsl:value-of select="$newline"/>
   <xsl:text>|===</xsl:text>
   <xsl:value-of select="$newline"/>
   <xsl:value-of select="adoc:text(concat('|', $fc.Name, ' |', $fc.Wert))"/>
   <xsl:value-of select="$newline"/>
   
   <xsl:for-each select="$values">
    <xsl:variable name="value" select="."/>
    
    <xsl:text>|</xsl:text>
    <xsl:value-of disable-output-escaping="no" select="adoc:diff-element($value/label)"/>
    <xsl:value-of select="$doublenewline"/>
    
    <xsl:if test="$value/definition">
     <xsl:for-each select="$value/definition">
      <xsl:variable name="def" select="adoc:diff-element(.)"/>
      <xsl:if test="string-length($def) > 0">
       <xsl:text>[.small]#</xsl:text>
       <xsl:value-of disable-output-escaping="no" select="$def"/>
       <xsl:text>#</xsl:text>
       <xsl:value-of select="$doublenewline"/>
      </xsl:if>
     </xsl:for-each>
    </xsl:if>
    
    <xsl:if test="$value/retired">
     <xsl:text>[.small]#: </xsl:text>
     <xsl:choose>
      <xsl:when test="$value/taggedValue[@tag = 'AAA:GueltigBis']"><xsl:value-of select="$fc.Stillgelegt"/>Gültig bis GeoInfoDok<xsl:value-of disable-output-escaping="no"
        select="adoc:diff-element($value/taggedValue[@tag = 'AAA:GueltigBis'][1])"/></xsl:when>
      <xsl:otherwise>
       <xsl:value-of select="$fc.Stillgelegt"/>
       <xsl:value-of select="adoc:diff-text-with-element('Ja', $value/retired)"/>
      </xsl:otherwise>
     </xsl:choose>
     <xsl:text>#</xsl:text>
     <xsl:value-of select="$doublenewline"/>
    </xsl:if>
    
    <xsl:if test="$value/grunddatenbestand">
     <xsl:text>[.small]#</xsl:text>
     <xsl:value-of select="$fc.Grunddatenbestand"/>
     <xsl:text>:</xsl:text>
     <xsl:for-each select="$value/grunddatenbestand">
      <xsl:text> </xsl:text>
      <xsl:value-of disable-output-escaping="no" select="adoc:diff-element(.)"/>
     </xsl:for-each>
     <xsl:text>#</xsl:text>
     <xsl:value-of select="$doublenewline"/>
    </xsl:if>
    
    <xsl:if test="$value/nutzungsartkennung">
     <xsl:text>[.small]#</xsl:text>
     <xsl:value-of select="$fc.Nutzungsartkennung"/>
     <xsl:text>: </xsl:text>
     <xsl:value-of disable-output-escaping="no" select="adoc:diff-element($value/nutzungsartkennung)"/>
     <xsl:text>#</xsl:text>
     <xsl:value-of select="$doublenewline"/>
    </xsl:if>
    
    <xsl:if test="adoc:hatRelevanteRevisionsnummern($value/letzteAenderungRevisionsnummer)">
     <xsl:text>[.small]#</xsl:text>
     <xsl:value-of select="$fc.Revisionsnummer"/>
     <xsl:text>: </xsl:text>
     <xsl:value-of disable-output-escaping="no" select="string-join(adoc:redmineLinks($value/letzteAenderungRevisionsnummer),', ')"/>
     <xsl:text>#</xsl:text>
     <xsl:value-of select="$doublenewline"/>     
    </xsl:if>
    
    <xsl:text>|</xsl:text>
    <xsl:value-of disable-output-escaping="no" select="adoc:diff-element($value/code)"/>
    <xsl:if test="$value/grunddatenbestand">
     <xsl:text> </xsl:text>
     <xsl:choose>
      <xsl:when test="$value/grunddatenbestand[@mode]">
       <!-- TBD: Should there be any rule for handling multiple values for grunddatenbestand with different @mode values? -->
       <xsl:value-of select="adoc:diff-text-with-element('(G)', $value/grunddatenbestand[@mode][1])"/>
      </xsl:when>
      <xsl:otherwise>
       <xsl:value-of select="adoc:text('(G)')"/>
      </xsl:otherwise>
     </xsl:choose>
          
    </xsl:if>
    <xsl:if test="$value/taggedValue[@tag = 'AAA:Landnutzung' and translate(., 'TRUE', 'true') = 'true']">
     <xsl:text> </xsl:text>
     <xsl:value-of select="adoc:diff-text-with-element('(LN)', $value/taggedValue[@tag = 'AAA:Landnutzung' and translate(., 'TRUE', 'true') = 'true'])"/>
    </xsl:if>
    <xsl:value-of select="$doublenewline"/>
   </xsl:for-each>
   
   <xsl:text>|===</xsl:text>
   <xsl:value-of select="$newline"/>
  </xsl:if>
 </xsl:template>

</xsl:stylesheet>
