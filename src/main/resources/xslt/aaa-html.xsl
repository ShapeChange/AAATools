<?xml version="1.0" encoding="utf-8"?>
<!-- 
(c) 2001-2020 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" />
  <xsl:include href="aaa-labels.xsl"/>
  <xsl:include href="aaa-diff.xsl"/>
  <xsl:decimal-format name="code" NaN="999999" />

  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="$fc.Objektartenkatalog" /> '<xsl:value-of select="FC_FeatureCatalogue/name" />'</title>
        <style>
          h1 {
            font-size: 32px;
          }
          h2 {
            font-size: 24px;
          }
          h3 {
            font-size: 22px;
          }
          h4 {
            font-size: 20px;
          }
          h5 {
            font-size: 18px;
          }
          h2.highlight, h3.highlight, h4.highlight, h5.highlight {
            background-color: #e0e0e0;
            padding: 5px;
          }
          del, p.DELETE, div.DELETE, span.DELETE, tr.DELETE, td.DELETE {
            background-color: #ffe6e6;
          }
          ins, p.INSERT, div.INSERT, span.INSERT, tr.INSERT, td.INSERT {
            background-color: #e6ffe6;
          }
          p.indent, li.indent {
            margin-left: 20px;
          }
          p.smaller {
            font-size: smaller;
            margin-top: 0.25em;
            margin-bottom: 0em;
          }
          p.backlink {
            font-size: smaller;
            text-align: right;
          }
          span.kennung {
            font-size: smaller;
            float: right;
            position: relative;
            top: 4px;
          }
          table.overview {
            border: 0px;
            border-spacing: 10px;
            border-collapse: separate;
          }
          table.enum {
            margin-left: 20px;
            border: 0px;
          }
          th.overview {
            background-color: #404040;
            color: #f0f0f0;
            border-bottom: 1px solid #ddd;
          }
          th, td {
            padding: 5px;
            border-bottom: 1px solid #ddd;
          }
          th.enum {
            font-weight: bold;
            text-align: left;
            border-bottom: 1px solid #ddd;
          }
          tr.enum, td.enum {
            vertical-align: top;
          }
          th.paket, tr.paket, td.paket {
            background-color: #e0e0e0;
            font-weight: bold;
          }
          th.kennung, td.kennung {
            text-align: right;
          }
          th.objektart, td.objektart {
            text-align: left;
          }
          th.objekttyp, td.objekttyp {
            text-align: left;
          }
          th.modellarten, td.modellarten {
            text-align: left;
          }
          th.name, td.name {
            text-align: left;
          }
          th.wert, td.wert {
            width: 10%;
            text-align: left;
          }
          tr:hover {
            background-color: #e6e6ff;
          }
        </style>
      </head>
      <body>
        <h1>
          <xsl:value-of select="$fc.Objektartenkatalog" /> '<xsl:value-of select="FC_FeatureCatalogue/name"/>'
        </h1>
        <xsl:apply-templates select="FC_FeatureCatalogue" mode="overview" />
        <xsl:apply-templates select="FC_FeatureCatalogue" mode="detail" />
      </body>
    </html>
  </xsl:template>  

  <xsl:template match="FC_FeatureCatalogue" mode="overview">
    <h2><xsl:value-of select="$fc.Version" /></h2>
    <p class="indent"><xsl:value-of select="versionNumber" /></p>
    
    <h2><xsl:value-of select="$fc.Veröffentlichung" /></h2>
    <p class="indent"><xsl:value-of select="versionDate" /></p>
    
    <xsl:if test="referenceModelVersionNumber">
      <h2><xsl:value-of select="$fc.Referenzversion" /></h2>
      <p class="indent"><xsl:value-of select="referenceModelVersionNumber" /></p>
    </xsl:if>
    
    <h2><xsl:value-of select="$fc.Anwendungsgebiet" /></h2>
    <p class="indent"><xsl:value-of select="$fc.Modellarten" />:</p>
    <ul>
      <xsl:apply-templates select="modellart" mode="scope" />
    </ul>
    <xsl:if test="profil">
      <p class="indent"><xsl:value-of select="$fc.Profile" />:</p>
      <ul>
        <xsl:apply-templates select="profil" mode="scope" />
      </ul>
    </xsl:if>
    
    <xsl:if test="aaaVersionNumber">
      <h2><xsl:value-of select="$fc.AAA-Version" /></h2>
      <p class="indent"><xsl:value-of select="aaaVersionNumber" /></p>
    </xsl:if>
    
    <h2><xsl:value-of select="$fc.VerantwortlicheInstitution" /></h2>
    <p class="indent"><xsl:value-of select="producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName" /></p>
    
    <h2 id="uebersicht"><xsl:value-of select="$fc.ÜbersichtObjektarten" /></h2>
    <table class="overview">
      <tr>
        <th class="overview kennung"><xsl:value-of select="$fc.Kennung" /></th>
        <th class="overview objektart"><xsl:value-of select="$fc.Objektart" /></th>
        <th class="overview objekttyp"><xsl:value-of select="$fc.Objekttyp" /></th>
        <th class="overview modellarten"><xsl:value-of select="$fc.Modellarten" /></th>
      </tr>
      <xsl:for-each select="AC_Objektartengruppe|AC_Objektbereich">
        <xsl:sort select="format-number(number(code), '000000', 'code')" />
        <xsl:sort select="code" />
        <xsl:apply-templates select="." mode="overview" />
      </xsl:for-each>
    </table>
    <hr/>
  </xsl:template>

  <xsl:template match="AC_Objektartengruppe|AC_Objektbereich" mode="overview">
    <xsl:variable name="paket" select="." />
    <xsl:variable name="inhalt" select="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/@idref=$paket/@id]|/FC_FeatureCatalogue/AC_Objektartengruppe[Objektbereichzugehoerigkeit/@idref=$paket/@id]" />
    <xsl:if test="$inhalt">
      <tr>
        <xsl:attribute name="class">paket<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
        <td class="kennung">
          <xsl:call-template name="diff">
            <xsl:with-param name="element" select="code"/>
          </xsl:call-template>
        </td>
        <td class="objektart" colspan="3">
          <xsl:call-template name="paket-name">
            <xsl:with-param name="paket" select="."/>
            <xsl:with-param name="name">
              <xsl:call-template name="diff">
                <xsl:with-param name="element" select="name"/>
              </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="href">#<xsl:value-of select="@id"/></xsl:with-param>
          </xsl:call-template>
        </td>
      </tr>
      <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/@idref=$paket/@id]">
        <xsl:sort select="format-number(number(code), '000000', 'code')" />
        <xsl:sort select="code" />
        <xsl:apply-templates select="." mode="overview" />
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template match="AC_FeatureType" mode="overview">
    <tr>
      <xsl:choose>
        <xsl:when test="@mode">
          <xsl:attribute name="class"><xsl:value-of select="@mode" /></xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="class">indent</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <td class="kennung">
        <xsl:call-template name="diff">
          <xsl:with-param name="element" select="code"/>
        </xsl:call-template>
      </td>
      <td class="objektart">
        <a>
          <xsl:attribute name="href">#<xsl:value-of select="@id" /></xsl:attribute>
          <xsl:call-template name="diff">
            <xsl:with-param name="element" select="name"/>
          </xsl:call-template>
        </a>
      </td>
      <td class="objekttyp">
        <xsl:choose>
          <xsl:when test="wirdTypisiertDurch">
            <xsl:value-of select="wirdTypisiertDurch" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="bedeutung" />
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td class="modellarten">
        <xsl:apply-templates select="modellart" mode="inline"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="FC_FeatureCatalogue" mode="detail">
    <xsl:for-each select="AC_Objektartengruppe|AC_Objektbereich">
      <xsl:sort select="format-number(number(code), '000000', 'code')" />
      <xsl:sort select="code" />
      <xsl:apply-templates select="." mode="detail" />
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="AC_Objektartengruppe|AC_Objektbereich" mode="detail">
    <xsl:variable name="paket" select="." />
    <xsl:variable name="inhalt" select="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/@idref=$paket/@id]|/FC_FeatureCatalogue/AC_Objektartengruppe[Objektbereichzugehoerigkeit/@idref=$paket/@id]" />
    <xsl:if test="$inhalt">
      <div>
        <xsl:attribute name="class">paket<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
        <h2 class="highlight">
          <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
          <xsl:call-template name="paket-name">
            <xsl:with-param name="paket" select="."/>
            <xsl:with-param name="name">
              <xsl:call-template name="diff">
                <xsl:with-param name="element" select="name"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:if test="code">
            <span class="kennung">
              <xsl:value-of select="$fc.Kennung" />: <xsl:call-template name="replace-ins"><xsl:with-param name="string" select="code"/></xsl:call-template>
            </span>
          </xsl:if>
        </h2>
        <xsl:if test="definition">
          <h3><xsl:value-of select="$fc.Definition" />:</h3>
          <xsl:apply-templates select="definition" mode="absatz" />
        </xsl:if>
        <xsl:if test="retired">
          <h3><xsl:value-of select="$fc.Stillgelegt" />:</h3>
          <xsl:apply-templates select="retired" mode="absatz" />
        </xsl:if>
        <xsl:if test="nutzungsartkennung">
          <h3><xsl:value-of select="$fc.Nutzungsartkennung" />:</h3>
          <xsl:apply-templates select="nutzungsartkennung" mode="absatz" />
        </xsl:if>
      </div>
      <xsl:call-template name="backlink" />
      <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[Objektartengruppenzugehoerigkeit/@idref=$paket/@id]">
        <xsl:sort select="format-number(number(code), '000000', 'code')" />
        <xsl:sort select="code" />
        <xsl:apply-templates select="." mode="detail" />
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template match="AC_FeatureType" mode="detail">
    <div>
      <xsl:attribute name="class">klasse<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
      <a><xsl:attribute name="name"><xsl:call-template name="current"><xsl:with-param name="element" select="name"/></xsl:call-template></xsl:attribute></a>
      <h3 class="highlight">
        <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
        <xsl:call-template name="klasse-name">
          <xsl:with-param name="klasse" select="."/>
          <xsl:with-param name="name">
            <xsl:call-template name="diff">
              <xsl:with-param name="element" select="name"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
        <span class="kennung">
          <xsl:value-of select="$fc.Kennung" />: <xsl:call-template name="replace-ins"><xsl:with-param name="string" select="code"/></xsl:call-template>
        </span>
      </h3>
      <xsl:if test="definition">
        <h4><xsl:value-of select="$fc.Definition" />:</h4>
        <xsl:apply-templates select="definition" mode="absatz" />
      </xsl:if>
      <xsl:if test="retired">
        <h4><xsl:value-of select="$fc.Stillgelegt" />:</h4>
        <xsl:apply-templates select="retired" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="letzteAenderungRevisionsnummer">
        <h4><xsl:value-of select="$fc.Revisionsnummer" />:</h4>
        <xsl:apply-templates select="letzteAenderungRevisionsnummer" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="abstrakt">
        <h4><xsl:value-of select="$fc.Abstrakt" />:</h4>
        <xsl:apply-templates select="abstrakt" mode="absatz" />
      </xsl:if>
      <xsl:if test="subtypeOf">
        <h4><xsl:value-of select="$fc.AbgeleitetAus" />:</h4>
        <xsl:apply-templates select="subtypeOf" mode="absatz" />
      </xsl:if>
      <xsl:if test="wirdTypisiertDurch">
        <h4><xsl:value-of select="$fc.Objekttyp" />:</h4>
        <xsl:apply-templates select="wirdTypisiertDurch" mode="absatz" />
      </xsl:if>
      <xsl:if test="modellart">
        <h4><xsl:value-of select="$fc.Modellarten" />:</h4>
        <xsl:apply-templates select="modellart" mode="absatz" />
      </xsl:if>
      <xsl:if test="grunddatenbestand">
        <h4><xsl:value-of select="$fc.Grunddatenbestand" />:</h4>
        <xsl:apply-templates select="grunddatenbestand" mode="absatz" />
      </xsl:if>
      <xsl:if test="taggedValue[@tag='AAA:Landnutzung']">
        <h4><xsl:value-of select="$fc.Landnutzung" />:</h4>
        <xsl:apply-templates select="taggedValue[@tag='AAA:Landnutzung']" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="nutzungsart">
        <h4><xsl:value-of select="$fc.Nutzungsart" />:</h4>
        <xsl:apply-templates select="nutzungsart" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="nutzungsartkennung">
        <h4><xsl:value-of select="$fc.Nutzungsartkennung" />:</h4>
        <xsl:apply-templates select="nutzungsartkennung" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="profil">
        <h4><xsl:value-of select="$fc.Profile" />:</h4>
        <xsl:apply-templates select="profil" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="bildungsregel">
        <xsl:for-each-group select="bildungsregel" group-by="@modellart">
          <xsl:sort select="current-grouping-key()" /> 
          <h4>
            <xsl:value-of select="$fc.Bildungsregeln" />
            <xsl:if test="current-grouping-key()!='*'">
              <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()" />                
            </xsl:if>:
          </h4>
          <xsl:apply-templates select="current-group()" mode="absatz" />
        </xsl:for-each-group> 
      </xsl:if>
      <xsl:if test="erfassungskriterium">
        <xsl:for-each-group select="erfassungskriterium" group-by="@modellart"> 
          <xsl:sort select="current-grouping-key()" /> 
          <h4>
            <xsl:value-of select="$fc.Erfassungskriterien" />
            <xsl:if test="current-grouping-key()!='*'">
              <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()" />                
            </xsl:if>:
          </h4>
          <xsl:apply-templates select="current-group()" mode="absatz" />
        </xsl:for-each-group> 
      </xsl:if>
      <xsl:if test="konsistenzbedingung">
        <xsl:for-each-group select="konsistenzbedingung" group-by="@modellart"> 
          <xsl:sort select="current-grouping-key()" /> 
          <h4>
            <xsl:value-of select="$fc.Konsistenzbedingungen" />
            <xsl:if test="current-grouping-key()!='*'">
              <xsl:text> </xsl:text><xsl:value-of select="current-grouping-key()" />                
            </xsl:if>:
          </h4>
          <xsl:apply-templates select="current-group()" mode="absatz" />
        </xsl:for-each-group> 
      </xsl:if>
      <xsl:if test="taggedValue[@tag!='AAA:Landnutzung' and @tag!='AAA:GueltigBis']">
        <h4><xsl:value-of select="$fc.WeitereAngaben" />:</h4>
        <xsl:apply-templates select="taggedValue[@tag!='AAA:Landnutzung' and @tag!='AAA:GueltigBis']" mode="absatz" />
      </xsl:if>

      <xsl:variable name="objektart" select="." />
      <h4><xsl:value-of select="$fc.Attributarten" />:</h4>
      <xsl:choose>
        <xsl:when test="/FC_FeatureCatalogue/FC_FeatureAttribute[@id=$objektart/characterizedBy/@idref]">
          <ul>
            <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[@id=$objektart/characterizedBy/@idref]">
              <xsl:sort select="@sequenceNumber" />
              <xsl:sort select="name" />
              <li>
                <a>
                  <xsl:attribute name="href">#<xsl:value-of select="$objektart/@id" />-<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:call-template name="diff"><xsl:with-param name="element" select="name"/></xsl:call-template>
                </a>
                <xsl:if test="grunddatenbestand"> (<xsl:value-of select="$fc.Grunddatenbestand" />)</xsl:if>
              </li>
            </xsl:for-each>
          </ul>
        </xsl:when>
        <xsl:otherwise>
          <p class="indent"><xsl:value-of select="$fc.keine" /></p>
        </xsl:otherwise>
      </xsl:choose>

      <h4><xsl:value-of select="$fc.Relationsarten" />:</h4>
      <xsl:choose>
        <xsl:when test="/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$objektart/@id]">
          <ul>
            <xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$objektart/@id]">
              <xsl:sort select="@sequenceNumber" />
              <xsl:sort select="name" />
              <li>
                <a>
                  <xsl:attribute name="href">#<xsl:value-of select="$objektart/@id" />-<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:call-template name="diff"><xsl:with-param name="element" select="name"/></xsl:call-template>
                </a>
                <xsl:if test="grunddatenbestand"> (<xsl:value-of select="$fc.Grunddatenbestand" />)</xsl:if>
              </li>
            </xsl:for-each>
          </ul>
        </xsl:when>
        <xsl:otherwise>
          <p class="indent"><xsl:value-of select="$fc.keine" /></p>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:call-template name="backlink" />

      <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[@id=$objektart/characterizedBy/@idref]|/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$objektart/@id]">
        <xsl:sort select="@sequenceNumber" />
        <xsl:sort select="name" />
        <xsl:apply-templates select="." mode="detail" />
      </xsl:for-each>

    </div>
  </xsl:template>

  <xsl:template match="FC_FeatureAttribute|FC_RelationshipRole" mode="detail">
    <div>
      <xsl:attribute name="class">attribut<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
      <a><xsl:attribute name="name"><xsl:value-of select="inType/@name" />__<xsl:call-template name="current"><xsl:with-param name="element" select="name"/></xsl:call-template></xsl:attribute></a>
      <h4 class="highlight">
        <xsl:attribute name="id"><xsl:value-of select="inType/@idref"/>-<xsl:value-of select="@id"/></xsl:attribute>
        <xsl:call-template name="eigenschaft-name">
          <xsl:with-param name="eigenschaft" select="."/>
          <xsl:with-param name="name">
            <xsl:call-template name="diff">
              <xsl:with-param name="element" select="name"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:if test="code">
          <span class="kennung">
            <xsl:value-of select="$fc.Kennung" />: <xsl:call-template name="replace-ins"><xsl:with-param name="string" select="code"/></xsl:call-template>
          </span>
        </xsl:if>
      </h4>
      <xsl:if test="definition">
        <h5><xsl:value-of select="$fc.Definition" />:</h5>
        <xsl:apply-templates select="definition" mode="absatz" />
      </xsl:if>
      <xsl:if test="auswerteregel">
        <h5><xsl:value-of select="$fc.Auswerteregel" />:</h5>
        <xsl:apply-templates select="auswerteregel" mode="absatz" />
      </xsl:if>
      <xsl:if test="bildungsregel">
        <h5><xsl:value-of select="$fc.Bildungsregel" />:</h5>
        <xsl:apply-templates select="bildungsregel" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="objektbildend">
        <h5><xsl:value-of select="$fc.Objektbildend" />:</h5>
        <xsl:apply-templates select="objektbildend" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="retired">
        <h5><xsl:value-of select="$fc.Stillgelegt" />:</h5>
        <xsl:apply-templates select="retired" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="letzteAenderungRevisionsnummer">
        <h5><xsl:value-of select="$fc.Revisionsnummer" />:</h5>
        <xsl:apply-templates select="letzteAenderungRevisionsnummer" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="modellart">
        <h5><xsl:value-of select="$fc.Modellarten" />:</h5>
        <xsl:apply-templates select="modellart" mode="absatz" />
      </xsl:if>
      <xsl:if test="grunddatenbestand">
        <h5><xsl:value-of select="$fc.Grunddatenbestand" />:</h5>
        <xsl:apply-templates select="grunddatenbestand" mode="absatz" />
      </xsl:if>
      <xsl:if test="taggedValue[@tag='AAA:Landnutzung']">
        <h5><xsl:value-of select="$fc.Landnutzung" />:</h5>
        <xsl:apply-templates select="taggedValue[@tag='AAA:Landnutzung']" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="nutzungsart">
        <h5><xsl:value-of select="$fc.Nutzungsart" />:</h5>
        <xsl:apply-templates select="nutzungsart" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="nutzungsartkennung">
        <h5><xsl:value-of select="$fc.Nutzungsartkennung" />:</h5>
        <xsl:apply-templates select="nutzungsartkennung" mode="absatz" />
      </xsl:if>
      <!-- nicht in HTML und DOCX 
      <xsl:if test="profil">
        <h5><xsl:value-of select="$fc.Profile" />:</h5>
        <xsl:apply-templates select="profil" mode="absatz" />
      </xsl:if>
      -->
      <xsl:if test="inverseRichtung">
        <h5><xsl:value-of select="$fc.InverseRichtung" />:</h5>
        <xsl:apply-templates select="inverseRichtung" mode="absatz" />
      </xsl:if>
      <xsl:if test="derived">
        <h5><xsl:value-of select="$fc.Abgeleitet" />:</h5>
        <xsl:apply-templates select="derived" mode="absatz" />
      </xsl:if>
      <xsl:if test="initialValue">
        <h5><xsl:value-of select="$fc.Defaultwert" />:</h5>
        <p class="indent">
          <xsl:apply-templates select="initialValue" mode="inline" />
          <xsl:if test="readOnly">
            <xsl:text> (</xsl:text><xsl:value-of select="$fc.NichtÄnderbar" /><xsl:text>)</xsl:text>
          </xsl:if>
        </p>  
      </xsl:if>
      <xsl:if test="cardinality">
        <h5><xsl:value-of select="$fc.Multiplizität" />:</h5>
        <xsl:apply-templates select="cardinality" mode="absatz" />
      </xsl:if>
      <xsl:if test="ValueDataType">
        <h5><xsl:value-of select="$fc.Datentyp" />:</h5>
        <xsl:apply-templates select="ValueDataType" mode="absatz" />
      </xsl:if>
      <xsl:if test="ValueDomainType=1">
        <h5><xsl:value-of select="$fc.Wertearten" />:</h5>
        <table class="enum">
          <tr class="enum">
            <th class="enum name">
             <xsl:value-of select="$fc.Name" />
            </th>
            <th class="enum wert">
              <xsl:value-of select="$fc.Wert" />
            </th>
          </tr>
          <xsl:variable name="attribute" select="." />
          <xsl:apply-templates select="/FC_FeatureCatalogue/FC_Value[@id=$attribute/enumeratedBy/@idref]" mode="detail" />
        </table>
      </xsl:if>
      <xsl:if test="FeatureTypeIncluded">
        <h5><xsl:value-of select="$fc.Zielobjektart" />:</h5>
        <xsl:apply-templates select="FeatureTypeIncluded" mode="link" />
        <xsl:if test="InverseRole">
          <h5><xsl:value-of select="$fc.InverseRelationsart" />:</h5>
          <xsl:apply-templates select="InverseRole" mode="link" />
        </xsl:if>
      </xsl:if>
      <xsl:if test="taggedValue[@tag!='AAA:Landnutzung' and @tag!='AAA:GueltigBis']">
        <h5><xsl:value-of select="$fc.WeitereAngaben" />:</h5>
        <xsl:apply-templates select="taggedValue[@tag!='AAA:Landnutzung' and @tag!='AAA:GueltigBis']" mode="absatz" />
      </xsl:if>
    </div>
    <xsl:call-template name="backlink">
      <xsl:with-param name="anker" select="inType/@idref"/>
    </xsl:call-template>  
  </xsl:template>

  <xsl:template match="FC_Value" mode="detail">
    <tr class="enum">
      <td class="enum name">
      <div>
        <xsl:attribute name="class">wert<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
        <xsl:apply-templates select="label" mode="inline" />
        <xsl:if test="definition">
          <p class="indent smaller">
            <xsl:apply-templates select="definition" mode="inline" />
          </p>  
        </xsl:if>  
        <xsl:if test="retired">
          <p class="indent smaller">
            <xsl:value-of select="$fc.Stillgelegt" />:<xsl:apply-templates select="retired" mode="inline" />
          </p>  
        </xsl:if>  
        <!-- nicht in HTML und DOCX 
        <xsl:if test="letzteAenderungRevisionsnummer">
          <p class="indent smaller">
            <xsl:value-of select="$fc.Revisionsnummer" />:<xsl:apply-templates select="letzteAenderungRevisionsnummer" mode="inline" />
          </p>  
        </xsl:if>
        -->
        <!-- nicht bei HTML und DOCX
        <xsl:if test="modellart">
          <p class="indent smaller">
            <xsl:value-of select="$fc.Modellarten" />:<xsl:apply-templates select="modellart" mode="inline" />
          </p>  
        </xsl:if>  
        -->
        <xsl:if test="grunddatenbestand">
          <p class="indent smaller">
            <xsl:value-of select="$fc.Grunddatenbestand" />:<xsl:apply-templates select="grunddatenbestand" mode="inline" />
          </p>  
        </xsl:if>  
        <xsl:if test="nutzungsartkennung">
          <p class="indent smaller">
            <xsl:value-of select="$fc.Nutzungsartkennung" />:<xsl:apply-templates select="nutzungsartkennung" mode="inline" />
          </p>  
        </xsl:if>
        <!-- nicht bei HTML und DOCX
        <xsl:if test="profil">
          <p class="indent smaller">
            <xsl:value-of select="$fc.Profile" />:<xsl:apply-templates select="profil" mode="inline" />
          </p>  
        </xsl:if>  
        -->
        <xsl:if test="taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']">
          <p class="indent smaller">
            <xsl:apply-templates select="taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']" mode="inline" />
          </p>  
        </xsl:if>  
      </div>  
      </td>
      <td class="enum wert">
      <div>
        <xsl:attribute name="class">wert<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
        <xsl:apply-templates select="code" mode="inline" />
        <xsl:if test="grunddatenbestand">
          <xsl:text> </xsl:text>
          <xsl:call-template name="diff-text">
            <xsl:with-param name="mode" select="grunddatenbestand[@mode][1]/@mode"/>
            <xsl:with-param name="text">(G)</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="taggedValue[@tag='AAA:Landnutzung']">
          <xsl:text> </xsl:text>
          <xsl:call-template name="diff-text">
            <xsl:with-param name="mode" select="taggedValue[@tag='AAA:Landnutzung' and @mode][1]/@mode"/>
            <xsl:with-param name="text">(LN)</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </div>  
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="modellart" mode="scope">
    <li>
      <xsl:value-of select="text()" />
      <xsl:if test="@name">: <xsl:value-of select="@name" /></xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="profil" mode="scope">
    <li>
      <xsl:value-of select="text()" />
    </li>
  </xsl:template>

  <xsl:template match="definition|code|wirdTypisiertDurch|modellart|grunddatenbestand|profil|konsistenzbedingung|bildungsregel|erfassungskriterium|auswerteregel|nutzungsart|nutzungsartkennung|taggedValue[@tag!='AAA:Landnutzung' and @tag!='AAA:GueltigBis']|ValueDataType|cardinality|initialValue|label" mode="absatz">
    <p class="indent">
      <xsl:call-template name="diff">
        <xsl:with-param name="element" select="." />
      </xsl:call-template>
    </p>
  </xsl:template>

  <xsl:template match="definition|code|wirdTypisiertDurch|konsistenzbedingung|bildungsregel|erfassungskriterium|auswerteregel|taggedValue[@tag!='AAA:Landnutzung' and @tag!='AAA:GueltigBis']|ValueDataType|cardinality|initialValue|label" mode="inline">
    <xsl:call-template name="diff">
      <xsl:with-param name="element" select="." />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="modellart|grunddatenbestand|profil|nutzungsart|nutzungsartkennung" mode="inline">
    <xsl:text> </xsl:text>
    <xsl:call-template name="diff">
      <xsl:with-param name="element" select="." />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="objektbildend|abstrakt|taggedValue[@tag='AAA:Landnutzung']|derived|inverseRichtung|readOnly" mode="absatz">
    <p class="indent">
      <xsl:call-template name="diff-text">
        <xsl:with-param name="mode" select="@mode" />
        <xsl:with-param name="text" select="replace(lower-case(.), 'true', $fc.Ja)" />
      </xsl:call-template>
    </p>
  </xsl:template>

  <xsl:template match="objektbildend|abstrakt|taggedValue[@tag='AAA:Landnutzung']|derived|inverseRichtung|readOnly" mode="inline">
    <xsl:text> </xsl:text>
    <xsl:call-template name="diff">
      <xsl:with-param name="element" select="replace(lower-case(.), 'true', $fc.Ja)" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="subtypeOf" mode="absatz">
    <xsl:variable name="subtype" select="." />
    <p class="indent">
      <xsl:choose>
        <xsl:when test="/FC_FeatureCatalogue/AC_FeatureType[name=$subtype]">
          <a>
            <xsl:attribute name="href">#<xsl:value-of select="/FC_FeatureCatalogue/AC_FeatureType[name=$subtype][1]/@id" /></xsl:attribute>
            <xsl:call-template name="replace-ins">
              <xsl:with-param name="string" select="$subtype" />
            </xsl:call-template>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="replace-ins">
            <xsl:with-param name="string" select="$subtype" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>

  <xsl:template match="retired" mode="absatz">
    <p>
      <xsl:attribute name="class">indent<xsl:if test="@mode"><xsl:text> </xsl:text><xsl:value-of select="@mode" /></xsl:if></xsl:attribute>
      <xsl:choose>
        <xsl:when test="../taggedValue[@tag='AAA:GueltigBis']">
          Gültig bis GeoInfoDok
          <xsl:call-template name="replace-ins">
            <xsl:with-param name="string" select="../taggedValue[@tag='AAA:GueltigBis'][1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>Ja</xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>

  <xsl:template match="retired" mode="inline">
    <span>
      <xsl:if test="@mode">
        <xsl:attribute name="class"><xsl:value-of select="@mode" /></xsl:attribute>
      </xsl:if>
      <xsl:text> </xsl:text>
      <xsl:choose>
        <xsl:when test="../taggedValue[@tag='AAA:GueltigBis']">
          Gültig bis GeoInfoDok
          <xsl:call-template name="replace-ins">
            <xsl:with-param name="string" select="../taggedValue[@tag='AAA:GueltigBis'][1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>Ja</xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

  <xsl:template match="FeatureTypeIncluded" mode="link">
    <p class="indent">
      <a>
        <xsl:attribute name="href">#<xsl:value-of select="@idref" /></xsl:attribute>
        <xsl:value-of select="@name" />
      </a>
    </p>  
  </xsl:template>

  <xsl:template match="InverseRole" mode="link">
    <xsl:variable name="roleId" select="@idref"/>
    <p class="indent">
      <a>
        <xsl:attribute name="href">#<xsl:value-of select="../FeatureTypeIncluded/@idref" />-<xsl:value-of select="@idref" /></xsl:attribute>
        <xsl:value-of select="/FC_FeatureCatalogue/FC_RelationshipRole[@id=$roleId]/name" />
      </a>
    </p>  
  </xsl:template>

  <xsl:template name="backlink">
    <xsl:param name="anker" />
    <p class="backlink">
      <xsl:choose>
        <xsl:when test="$anker">
          <a>
            <xsl:attribute name="href">#<xsl:value-of select="$anker"></xsl:value-of></xsl:attribute>
            <xsl:text>zurück zur Objektart</xsl:text>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <a href="#uebersicht">zurück zur Objektartenübersicht</a>
        </xsl:otherwise>
      </xsl:choose>
    </p>
    <hr/>
  </xsl:template>

</xsl:stylesheet>