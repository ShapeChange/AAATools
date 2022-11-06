<?xml version="1.0" encoding="UTF-8"?>
<!-- 
(c) 2001-2020 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->    
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:variable name="fc.AAA-Version">Version des AAA-Anwendungsschemas</xsl:variable>
  <xsl:variable name="fc.Abgeleitet">Abgeleiteter Wert</xsl:variable>
  <xsl:variable name="fc.AbgeleitetAus">Abgeleitet aus</xsl:variable>
  <xsl:variable name="fc.Abstrakt">Abstrakt</xsl:variable>
  <xsl:variable name="fc.Anwendungsgebiet">Anwendungsgebiet</xsl:variable>
  <xsl:variable name="fc.Art">Art</xsl:variable>
  <xsl:variable name="fc.Attributarten">Attributarten</xsl:variable>
  <xsl:variable name="fc.Auswerteregel">Auswerteregel</xsl:variable>
  <xsl:variable name="fc.Bezeichnung">Bezeichnung</xsl:variable>
  <xsl:variable name="fc.Bildungsregel">Bildungsregel</xsl:variable>
  <xsl:variable name="fc.Bildungsregeln">Bildungsregeln</xsl:variable>
  <xsl:variable name="fc.Datentyp">Datentyp</xsl:variable>
  <xsl:variable name="fc.Defaultwert">Initialer Wert bei Objekterzeugung</xsl:variable>
  <xsl:variable name="fc.Definition">Definition</xsl:variable>
  <xsl:variable name="fc.Eigenschaft">Attributart/Relationsart</xsl:variable>
  <xsl:variable name="fc.Erfassungskriterien">Erfassungskriterien</xsl:variable>
  <xsl:variable name="fc.Grunddatenbestand">Grunddatenbestand</xsl:variable>
  <xsl:variable name="fc.InverseRelationsart">Inverse Relationsart</xsl:variable>
  <xsl:variable name="fc.InverseRichtung">Inverse Relationsrichtung</xsl:variable>
  <xsl:variable name="fc.Ja">Ja</xsl:variable>
  <xsl:variable name="fc.Kategorie">Kategorie</xsl:variable>
  <xsl:variable name="fc.keine">keine</xsl:variable>
  <xsl:variable name="fc.Kennung">Kennung</xsl:variable>
  <xsl:variable name="fc.Konsistenzbedingungen">Konsistenzbedingungen</xsl:variable>
  <xsl:variable name="fc.Landnutzung">Landnutzung</xsl:variable>
  <xsl:variable name="fc.Lebenszeitintervall">Lebenszeitintervall</xsl:variable>
  <xsl:variable name="fc.Modellart">Modellart</xsl:variable>
  <xsl:variable name="fc.Modellarten">Modellarten</xsl:variable>
  <xsl:variable name="fc.Multiplizität">Multiplizität</xsl:variable>
  <xsl:variable name="fc.Name">Bezeichnung</xsl:variable>
  <xsl:variable name="fc.NichtÄnderbar">nicht änderbar</xsl:variable>
  <xsl:variable name="fc.Nutzungsart">Nutzungsart</xsl:variable>
  <xsl:variable name="fc.Nutzungsartkennung">Nutzungsartkennung</xsl:variable>
  <xsl:variable name="fc.Objektart">Objektart</xsl:variable>
  <xsl:variable name="fc.Objektartenkatalog">Objektartenkatalog</xsl:variable>
  <xsl:variable name="fc.Objektbildend">Objektbildend</xsl:variable>
  <xsl:variable name="fc.Objekttyp">Objekttyp</xsl:variable>
  <xsl:variable name="fc.Profile">Profile</xsl:variable>
  <xsl:variable name="fc.Referenzversion">Referenzversion</xsl:variable>
  <xsl:variable name="fc.Relationsarten">Relationsarten</xsl:variable>
  <xsl:variable name="fc.Revisionsnummer">Letzte Änderungen (Revisionsnummer)</xsl:variable>
  <xsl:variable name="fc.Stillgelegt">Stillgelegt</xsl:variable>
  <xsl:variable name="fc.ÜbersichtObjektarten">Objektartenübersicht</xsl:variable>
  <xsl:variable name="fc.VerantwortlicheInstitution">Verantwortliche Institution</xsl:variable>
  <xsl:variable name="fc.Veröffentlichung">Veröffentlichung</xsl:variable>
  <xsl:variable name="fc.Version">Version</xsl:variable>
  <xsl:variable name="fc.WeitereAngaben">Weitere Angaben</xsl:variable>
  <xsl:variable name="fc.Wert">Wert</xsl:variable>
  <xsl:variable name="fc.Werteart">Werteart</xsl:variable>
  <xsl:variable name="fc.Wertearten">Wertearten</xsl:variable>
  <xsl:variable name="fc.Zielobjektart">Zielobjektart</xsl:variable>

  <xsl:template name="paket-name">
   <xsl:param name="paket" />
   <xsl:param name="name" />
   <xsl:param name="href" />
   <xsl:choose>
     <xsl:when test="count($paket/Objektbereichzugehoerigkeit)=1">
       <xsl:text>Objektartengruppe: </xsl:text>
     </xsl:when>
     <xsl:otherwise>
       <xsl:text>Objektartenbereich: </xsl:text>
     </xsl:otherwise>
   </xsl:choose>
   <xsl:choose>
     <xsl:when test="$href">
       <a>
         <xsl:attribute name="href"><xsl:value-of select="$href"/></xsl:attribute>
         <xsl:value-of select="$name"/>
      </a>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$name"/>
     </xsl:otherwise>
   </xsl:choose>
 </xsl:template>

 <xsl:template name="klasse-name">
   <xsl:param name="klasse" />
   <xsl:param name="name" />
   <xsl:param name="href" />
   <xsl:choose>
     <xsl:when test="$klasse/bedeutung='Objektart'">
       <xsl:text>Objektart: </xsl:text>
     </xsl:when>
     <xsl:when test="$klasse/bedeutung='Datentyp'">
       <xsl:text>Datentyp: </xsl:text>
     </xsl:when>
   </xsl:choose>
   <xsl:choose>
     <xsl:when test="$href">
       <a>
         <xsl:attribute name="href"><xsl:value-of select="$href"/></xsl:attribute>
         <xsl:copy-of select="$name"/>
       </a>
     </xsl:when>
     <xsl:otherwise>
      <xsl:copy-of select="$name"/>
   </xsl:otherwise>
   </xsl:choose>
 </xsl:template>

 <xsl:template name="eigenschaft-name">
   <xsl:param name="eigenschaft" />
   <xsl:param name="name" />
   <xsl:param name="href" />
   <xsl:choose>
     <xsl:when test="$eigenschaft/local-name()='FC_FeatureAttribute'">
       <xsl:text>Attributart: </xsl:text>
     </xsl:when>
     <xsl:when test="$eigenschaft/local-name()='FC_RelationshipRole'">
       <xsl:text>Relationsart: </xsl:text>
     </xsl:when>
   </xsl:choose>
   <xsl:choose>
     <xsl:when test="$href">
       <a>
         <xsl:attribute name="href"><xsl:value-of select="$href"/></xsl:attribute>
         <xsl:copy-of select="$name"/>
       </a>
     </xsl:when>
     <xsl:otherwise>
      <xsl:copy-of select="$name"/>
   </xsl:otherwise>
   </xsl:choose>
 </xsl:template>  

</xsl:stylesheet>
