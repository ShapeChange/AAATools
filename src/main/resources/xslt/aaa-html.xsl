<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- 

     Version 1.0 - 12.05.2001
     Version 1.2 - 28.07.2001
     Version 1.5 - 03.01.2002
     Version 1.6 - 30.12.2002
     Version 1.7 - 30.03.2003
     Version 1.8 - 01.08.2004
     Version 1.10 - 29.07.2005
     Version 1.11 - 12.08.2008
     Version 1.12 - 24.10.2011
     Version 1.13 - 25.10.2012
     Version 1.14 - 23.01.2013
     Version 1.15 - 11.08.2016
     Version 1.16 - 11.12.2017
     Version 1.17 - 05.07.2018
     Version 1.18 - 31.12.2018

     (c) 2001-2018 interactive instruments GmbH, Bonn
     im Auftrag der AdV, Arbeitsgemeinschaft der Vermessungsverwaltungen der
     L‰nder der Bundesrepublik Deutschland

     http://www.adv-online.de/

  -->
  <xsl:output method="html" />
  <xsl:decimal-format name="code" NaN="999999" />
  <xsl:template match="/">
    <xsl:variable name="version" select="FC_FeatureCatalogue/versionNumber" />
    <html>

    <head>
      <title>Objektartenkatalog:
        <xsl:value-of select="FC_FeatureCatalogue/name" />
      </title>
    </head>

    <body>
      <h1>
        <xsl:value-of select="FC_FeatureCatalogue/name"/>
      </h1>
      <P>
        <b>Versionsnummer:</b>
      </P>
      <P STYLE="margin-left:20px">
        <xsl:value-of select="$version" />
      </P>
      <P>
        <b>Veröffentlichung:</b>
      </P>
      <P STYLE="margin-left:20px">
        <xsl:value-of select="FC_FeatureCatalogue/versionDate" />
      </P>
      <xsl:if test="FC_FeatureCatalogue/referenceModelVersionNumber">
      <P>
        <b>Referenzversion:</b>
      </P>
      <P STYLE="margin-left:20px">
        <xsl:value-of select="FC_FeatureCatalogue/referenceModelVersionNumber" />
      </P>
      </xsl:if>
      <P>
        <b>Anwendungsgebiet:</b>
      </P>
      <P STYLE="margin-left:20px">
        <xsl:call-template name="replace_br">
          <xsl:with-param name="string" select="FC_FeatureCatalogue/scope" />
        </xsl:call-template>
      </P>
      <xsl:if test="FC_FeatureCatalogue/aaaVersionNumber">
      <P>
        <b>Referenziertes AAA-Anwendungsschema:</b>
      </P>
      <P STYLE="margin-left:20px">
        <xsl:value-of select="FC_FeatureCatalogue/aaaVersionNumber" />
      </P>
      </xsl:if>
      <P>
        <b>Verantwortliche Institution:</b>
      </P>
      <P STYLE="margin-left:20px">
        <xsl:value-of select="FC_FeatureCatalogue/producer/CI_ResponsibleParty/CI_MandatoryParty/organisationName" />
      </P>
      <a><xsl:attribute name="name">uebersicht</xsl:attribute>
        <h2>Liste der Objektartenbereiche und Objektartengruppen mit ihren Objektarten und Datentypen</h2>
      </a>
      <TABLE BORDER="0" cellpadding="3" cellspacing="5">
        <TR>
          <TH width="64%" bgcolor="#F0F0F0">
            <i>Bezeichnung</i>
          </TH>
          <TH width="18%" bgcolor="#F0F0F0">
            <i>Objekttyp</i>
          </TH>
          <TH width="18%" bgcolor="#F0F0F0">
            <i>Modellart</i>
          </TH>
        </TR>
        <xsl:for-each select="FC_FeatureCatalogue/AC_Objektartengruppe|FC_FeatureCatalogue/AC_Objektbereich">
          <xsl:sort select="format-number(./code,'000000', 'code')" />
          <xsl:variable name="objektart" select="." />
          <xsl:variable name="nftx" select="count(/FC_FeatureCatalogue/AC_FeatureType/Objektartengruppenzugehoerigkeit[attribute::idref=$objektart/@id])" />
          <xsl:if test="$nftx >= 0">
            <DIV>
              <TR>
                <TD>
                  <xsl:choose>
                    <xsl:when test="@mode='DELETE'">
                      <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="@mode='INSERT'">
                      <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
                  <b>
                    <xsl:choose>
                      <xsl:when test="count($objektart/Objektbereichzugehoerigkeit)=1">
                        <xsl:text>Objektartengruppe: </xsl:text>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:text>Objektartenbereich: </xsl:text>
                      </xsl:otherwise>
                    </xsl:choose>
                    <a><xsl:attribute name="href">#<xsl:value-of select="@id"/></xsl:attribute>
                    <xsl:choose>
                      <xsl:when test="@mode='DELETE'">
                        <del style="background:#ffe6e6;">
                          <xsl:value-of select="name"/>
                        </del>
                      </xsl:when>
                      <xsl:when test="@mode='INSERT'">
                        <ins style="background:#e6ffe6;">
                          <xsl:value-of select="name"/>
                        </ins>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="name"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <!--(<xsl:value-of select="format-number(code,'000000', 'code')"/>)-->
                  </a>
                </b>
                </TD>
              </TR>
              <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType/Objektartengruppenzugehoerigkeit[attribute::idref=$objektart/@id]">
                <xsl:sort select="format-number(../code,'000000', 'code')" />
                <xsl:variable name="featuretype" select=".." />
                <TR>
                  <TD>
                    <xsl:choose>
                      <xsl:when test="$featuretype/@mode='DELETE'">
                        <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="$featuretype/@mode='INSERT'">
                        <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
                      </xsl:otherwise>
                    </xsl:choose>
                    <a><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id" /></xsl:attribute>
                      <xsl:choose>
                        <xsl:when test="$featuretype/@mode='DELETE'">
                          <del style="background:#ffe6e6;">
                        <xsl:value-of select="$featuretype/name"/>
                      </del>
                        </xsl:when>
                        <xsl:when test="$featuretype/@mode='INSERT'">
                          <ins style="background:#e6ffe6;">
                        <xsl:value-of select="$featuretype/name"/>
                      </ins>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="$featuretype/name" />
                        </xsl:otherwise>
                      </xsl:choose>
                      <!-- (<xsl:value-of select="format-number(../code,'000000', 'code')"/>)-->
                    </a>
                  </TD>
                  <td>
                    <xsl:choose>
                      <xsl:when test="$featuretype/@mode='DELETE'">
                        <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="$featuretype/@mode='INSERT'">
                        <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
                      </xsl:otherwise>
                    </xsl:choose>
                    <xsl:variable name="nft" select="count($featuretype/wirdTypisiertDurch)" />
                    <xsl:choose>
                      <xsl:when test="$nft = 1">
                        <xsl:value-of select="$featuretype/wirdTypisiertDurch" />
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$featuretype/bedeutung" />
                      </xsl:otherwise>
                    </xsl:choose>
                  </td>
                  <td>
                    <xsl:choose>
                      <xsl:when test="$featuretype/@mode='DELETE'">
                        <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="$featuretype/@mode='INSERT'">
                        <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
                      </xsl:otherwise>
                    </xsl:choose>
                    <xsl:variable name="nft" select="count($featuretype/modellart)" />
                    <xsl:if test="$nft >= 1">
                      <xsl:for-each select="$featuretype/modellart">
                        <xsl:value-of select="." />
                        <br/>
                      </xsl:for-each>
                    </xsl:if>
                  </td>
                </TR>
              </xsl:for-each>
            </DIV>
          </xsl:if>
        </xsl:for-each>
      </TABLE>
      <xsl:for-each select="FC_FeatureCatalogue/AC_Objektartengruppe|FC_FeatureCatalogue/AC_Objektbereich">
        <xsl:sort select="format-number(./code,'000000', 'code')" />
        <xsl:variable name="objektart" select="." />
        <xsl:variable name="nftx" select="count(/FC_FeatureCatalogue/AC_FeatureType/Objektartengruppenzugehoerigkeit[attribute::idref=$objektart/@id])" />
        <div>
          <xsl:choose>
            <xsl:when test="@mode='DELETE'">
              <xsl:attribute name="style">background-color:#ffe6e6;</xsl:attribute>
            </xsl:when>
            <xsl:when test="@mode='INSERT'">
              <xsl:attribute name="style">background-color:#e6ffe6;</xsl:attribute>
            </xsl:when>
          </xsl:choose>
          <xsl:if test="$nftx >= 0">
            <HR/>
            <H2>
          <A>
            <xsl:attribute name="name"><xsl:value-of select="@id"/></xsl:attribute>
            <xsl:choose>
              <xsl:when test="count($objektart/Objektbereichzugehoerigkeit)=1">
                <xsl:text>Objektartengruppe: </xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>Objektartenbereich: </xsl:text>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="name"/>
          </A>
        </H2>
            <DIV>
              <xsl:variable name="nft" select="count(definition)" />
              <xsl:if test="$nft >= 1">
                <P>
                  <b>Definition:</b>
                </P>
                <xsl:for-each select="definition">
                  <P STYLE="margin-left:20px">
                    <xsl:call-template name="replace_ins">
                      <xsl:with-param name="string" select="." />
                    </xsl:call-template>
                  </P>
                </xsl:for-each>
              </xsl:if>
            </DIV>
            <DIV>
              <xsl:variable name="nft" select="count(code)" />
              <xsl:if test="$nft >= 1">
                <P>
                  <b>Kennung:</b>
                </P>
                <xsl:for-each select="code">
                  <P STYLE="margin-left:20px">
                    <xsl:call-template name="replace_ins">
                      <xsl:with-param name="string" select="." />
                    </xsl:call-template>
                  </P>
                </xsl:for-each>
              </xsl:if>
            </DIV>
            <DIV>
              <xsl:variable name="nft" select="count(retired)" />
              <xsl:if test="$nft >= 1">
                <xsl:if test="retired">
                 <P>
                   <b>Stillgelegt:</b>
                 </P>
                 <xsl:choose>
                 <xsl:when test="taggedValue[@tag='AAA:GueltigBis']">
                 <P STYLE="margin-left:20px">
                  Gültig bis GeoInfoDok
                  <xsl:call-template name="replace_ins">
                    <xsl:with-param name="string" select="taggedValue[@tag='AAA:GueltigBis'][1]"/>
                  </xsl:call-template>
                 </P>
                 </xsl:when>
                 <xsl:otherwise>
                  <P STYLE="margin-left:20px">Ja</P>
                 </xsl:otherwise>
                 </xsl:choose>
                </xsl:if>
              </xsl:if>
            </DIV>
            <table>
              <tr>
                <td width="100%">
                  <p align="right">
                    <small>
                  <a href="#uebersicht">zurück zur Übersicht über die Objektarten und Datentypen</a>
                </small>
                  </p>
                </td>
                <td>
                  <BR/>
                </td>
              </tr>
            </table>
            <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType/Objektartengruppenzugehoerigkeit[attribute::idref=$objektart/@id]">
              <xsl:sort select="format-number(../code,'000000', 'code')" />
              <xsl:variable name="featuretype" select=".." />
              <div>
                <xsl:choose>
                  <xsl:when test="$featuretype/@mode='DELETE'">
                    <xsl:attribute name="style">background-color:#ffe6e6;</xsl:attribute>
                  </xsl:when>
                  <xsl:when test="$featuretype/@mode='INSERT'">
                    <xsl:attribute name="style">background-color:#e6ffe6;</xsl:attribute>
                  </xsl:when>
                </xsl:choose>
                <BR/>
                <a>
                  <xsl:attribute name="name"><xsl:value-of select="$featuretype/@id" /></xsl:attribute>
                  <TABLE>
                    <TR>
                      <TD width="100%">
                        <xsl:choose>
                          <xsl:when test="$featuretype/@mode='DELETE'">
                            <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                          </xsl:when>
                          <xsl:when test="$featuretype/@mode='INSERT'">
                            <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:attribute name="bgcolor">#d0d0d0</xsl:attribute>
                          </xsl:otherwise>
                        </xsl:choose>
                        <big>
                      <b>
                        <xsl:variable name="nft" select="count($featuretype/bedeutung)" />
                        <xsl:if test="$nft = 1">
                          <xsl:value-of select="$featuretype/bedeutung"/>: 
                         </xsl:if>
                          <xsl:choose>
                            <xsl:when test="$featuretype/@mode='DELETE'">
                              <del style="background:#ffe6e6;">
                                <xsl:value-of select="$featuretype/name"/>
                              </del>
                            </xsl:when>
                            <xsl:when test="$featuretype/@mode='INSERT'">
                              <ins style="background:#e6ffe6;">
                                <xsl:value-of select="$featuretype/name"/>
                              </ins>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="$featuretype/name"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </b>
                      </big>
                      </TD>
                      <td>
                        <br/>
                      </td>
                    </TR>
                  </TABLE>
                </a>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/definition)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Definition:</b>
                    </P>
                    <xsl:for-each select="$featuretype/definition">
                      <P STYLE="margin-left:20px">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="." />
                        </xsl:call-template>
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/code)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Kennung:</b>
                    </P>
                    <xsl:for-each select="$featuretype/code">
                      <P STYLE="margin-left:20px">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="." />
                        </xsl:call-template>
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
            <DIV>
              <xsl:variable name="nft" select="count($featuretype/retired)" />
              <xsl:if test="$nft >= 1">
                <xsl:if test="$featuretype/retired">
                 <P>
                   <b>Stillgelegt:</b>
                 </P>
                 <xsl:choose>
                 <xsl:when test="$featuretype/taggedValue[@tag='AAA:GueltigBis']">
                 <P STYLE="margin-left:20px">
                  Gültig bis GeoInfoDok
                  <xsl:call-template name="replace_ins">
                    <xsl:with-param name="string" select="$featuretype/taggedValue[@tag='AAA:GueltigBis'][1]"/>
                  </xsl:call-template>
                 </P>
                 </xsl:when>
                 <xsl:otherwise>
                  <P STYLE="margin-left:20px">Ja</P>
                 </xsl:otherwise>
                 </xsl:choose>
                </xsl:if>
              </xsl:if>
            </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/subtypeOf)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Abgeleitet aus:</b>
                    </P>
                    <xsl:for-each select="$featuretype/subtypeOf">
                      <xsl:variable name="st" select="." />
                      <xsl:variable name="nft2" select="count(/child::FC_FeatureCatalogue/child::AC_FeatureType[child::name=$st])" />
                      <xsl:choose>
                        <xsl:when test="$nft2 = 0">
                          <P STYLE="margin-left:20px">
                            <xsl:call-template name="replace_ins">
                              <xsl:with-param name="string" select="." />
                            </xsl:call-template>
                          </P>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[child::name=$st]">
                            <P STYLE="margin-left:20px">
                              <a><xsl:attribute name="href">#<xsl:value-of select="./@id" /></xsl:attribute>
                                <xsl:call-template name="replace_ins">
                                  <xsl:with-param name="string" select="$st" />
                                </xsl:call-template>
                              </a>
                            </P>
                          </xsl:for-each>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/wirdTypisiertDurch)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Wird typisiert durch:</b>
                    </P>
                    <table>
                      <tr>
                        <td width="20">
                          <BR/>
                        </td>
                        <td>
                          <xsl:value-of select="$featuretype/wirdTypisiertDurch" />
                        </td>
                      </tr>
                    </table>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/modellart)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Modellart:</b>
                    </P>
                    <xsl:for-each select="$featuretype/modellart">
                      <P STYLE="margin-left:20px">
                        <xsl:value-of select="." />
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/grunddatenbestand)" />
                  <xsl:if test="$nft >= 1">
                    <div>
                      <xsl:choose>
                        <xsl:when test="$featuretype/grunddatenbestand/@mode='DELETE'">
                          <xsl:attribute name="style">background-color:#ffe6e6;</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$featuretype/grunddatenbestand/@mode='INSERT'">
                          <xsl:attribute name="style">background-color:#e6ffe6;</xsl:attribute>
                        </xsl:when>
                      </xsl:choose>
                      <P>
                        <b>Grunddatenbestand:</b>
                      </P>
                      <xsl:for-each select="$featuretype/grunddatenbestand">
                        <P STYLE="margin-left:20px">
                          <xsl:call-template name="replace_ins">
                            <xsl:with-param name="string" select="." />
                          </xsl:call-template>
                        </P>
                      </xsl:for-each>
                    </div>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/Konsistenzbedingung)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Konsistenzbedingung:</b>
                    </P>
                    <xsl:for-each select="$featuretype/Konsistenzbedingung">
                      <P STYLE="margin-left:20px">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="." />
                        </xsl:call-template>
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/Bildungsregel)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Bildungsregel:</b>
                    </P>
                    <xsl:for-each select="$featuretype/Bildungsregel">
                      <P STYLE="margin-left:20px">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="." />
                        </xsl:call-template>
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/Erfassungskriterium)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Erfassungskriterium:</b>
                    </P>
                    <xsl:for-each select="$featuretype/Erfassungskriterium">
                      <P STYLE="margin-left:20px">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="." />
                        </xsl:call-template>
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/Lebenszeitintervall)" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Lebenszeitintervall:</b>
                    </P>
                    <xsl:for-each select="$featuretype/Lebenszeitintervall">
                      <P STYLE="margin-left:20px">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="." />
                        </xsl:call-template>
                      </P>
                    </xsl:for-each>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count($featuretype/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung'])" />
                  <xsl:if test="$nft >= 1">
                    <P>
                      <b>Weitere Angaben:</b>
                    </P>
                    <TABLE>
                            <TR>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Bezeichner</TD>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Wert</TD>
                            </TR>
                      <xsl:for-each select="$featuretype/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']">
                        <xsl:variable name="tv" select="." />
                        <TR>
                          <td width="20">
                            <BR/>
                          </td>
                          <TD valign="top">
                            <xsl:choose>
                              <xsl:when test="$tv/@mode='DELETE'">
                                <del style="background:#ffe6e6;">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="$tv/@tag"/>
                              </xsl:call-template>
                            </del>
                              </xsl:when>
                              <xsl:when test="$tv/@mode='INSERT'">
                                <ins style="background:#e6ffe6;">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="$tv/@tag"/>
                              </xsl:call-template>
                            </ins>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:call-template name="replace_ins">
                                  <xsl:with-param name="string" select="$tv/@tag" />
                                </xsl:call-template>
                              </xsl:otherwise>
                            </xsl:choose>
                          </TD>
                              <td width="20">
                                <BR/>
                              </td>
                          <TD valign="top">
                            <xsl:choose>
                              <xsl:when test="$tv/@mode='DELETE'">
                                <del style="background:#ffe6e6;">
                              <xsl:value-of select="$tv/text()"/>
                            </del>
                              </xsl:when>
                              <xsl:when test="$tv/@mode='INSERT'">
                                <ins style="background:#e6ffe6;">
                              <xsl:value-of select="$tv/text()"/>
                            </ins>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="$tv/text()" />
                              </xsl:otherwise>
                            </xsl:choose>
                          </TD>
                        </TR>
                      </xsl:for-each>
                    </TABLE>
                  </xsl:if>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count(/FC_FeatureCatalogue/FC_FeatureAttribute[attribute::id=$featuretype/characterizedBy/@idref])" />
                  <P>
                    <b>Attributarten:</b>
                  </P>
                  <xsl:choose>
                    <xsl:when test="$nft >= 1">
                      <UL>
                        <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[attribute::id=$featuretype/characterizedBy/@idref]">
                          <xsl:variable name="featureAtt" select="." />
                          <LI>
                            <a><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id" />-<xsl:value-of select="$featureAtt/@id" /></xsl:attribute>
                              <xsl:choose>
                                <xsl:when test="$featureAtt/@mode='DELETE'">
                                  <del style="background:#ffe6e6;">
                              <xsl:value-of select="$featureAtt/name"/>
                            </del>
                                </xsl:when>
                                <xsl:when test="$featureAtt/@mode='INSERT'">
                                  <ins style="background:#e6ffe6;">
                              <xsl:value-of select="$featureAtt/name"/>
                            </ins>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="replace_ins">
                                    <xsl:with-param name="string" select="$featureAtt/name" />
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                              <xsl:if test="count($featureAtt/grunddatenbestand) >= 1">
                                <xsl:choose>
                                  <xsl:when test="$featureAtt/grunddatenbestand/@mode='DELETE'">
                                    <del style="background:#ffe6e6;">
                  (Grunddatenbestand)
                </del>
                                  </xsl:when>
                                  <xsl:when test="$featureAtt/grunddatenbestand/@mode='INSERT'">
                                    <ins style="background:#e6ffe6;">
                  (Grunddatenbestand)
                </ins>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    (Grunddatenbestand)
                                  </xsl:otherwise>
                                </xsl:choose>
                              </xsl:if>
                            </a>
                          </LI>
                        </xsl:for-each>
                      </UL>
                    </xsl:when>
                    <xsl:otherwise>
                      <P STYLE="margin-left:20px">keine</P>
                    </xsl:otherwise>
                  </xsl:choose>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count(/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$featuretype/@id])" />
                  <P>
                    <b>Relationsarten:</b>
                  </P>
                  <xsl:choose>
                    <xsl:when test="$nft >= 1">
                      <UL>
                        <xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$featuretype/@id]">
                          <xsl:variable name="featureRel" select="." />
                          <LI>
                            <a><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id" />-<xsl:value-of select="$featureRel/@id" /></xsl:attribute>
                              <xsl:choose>
                                <xsl:when test="$featureRel/@mode='DELETE'">
                                  <del style="background:#ffe6e6;">
                            <xsl:value-of select="$featureRel/name"/>
                          </del>
                                </xsl:when>
                                <xsl:when test="$featureRel/@mode='INSERT'">
                                  <ins style="background:#e6ffe6;">
                            <xsl:value-of select="$featureRel/name"/>
                          </ins>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="replace_ins">
                                    <xsl:with-param name="string" select="$featureRel/name" />
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                              <xsl:if test="count($featureRel/grunddatenbestand) >= 1">
                                <xsl:choose>
                                  <xsl:when test="$featureRel/grunddatenbestand/@mode='DELETE'">
                                    <del style="background:#ffe6e6;">
                  (Grunddatenbestand)
                </del>
                                  </xsl:when>
                                  <xsl:when test="$featureRel/grunddatenbestand/@mode='INSERT'">
                                    <ins style="background:#e6ffe6;">
                  (Grunddatenbestand)
                </ins>
                                  </xsl:when>
                                  <xsl:otherwise>
                                    (Grunddatenbestand)
                                  </xsl:otherwise>
                                </xsl:choose>
                              </xsl:if>
                            </a>
                          </LI>
                        </xsl:for-each>
                      </UL>
                    </xsl:when>
                    <xsl:otherwise>
                      <P STYLE="margin-left:20px">keine</P>
                    </xsl:otherwise>
                  </xsl:choose>
                </DIV>
                <DIV>
                  <xsl:variable name="nft" select="count(/FC_FeatureCatalogue/FC_FeatureOperation[attribute::id=$featuretype/characterizedBy/@idref])" />
                  <P>
                    <b>Operationen:</b>
                  </P>
                  <xsl:choose>
                    <xsl:when test="$nft >= 1">
                      <UL>
                        <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureOperation[attribute::id=$featuretype/characterizedBy/@idref]">
                          <xsl:variable name="featureOpr" select="." />
                          <LI>
                            <a><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id" />-<xsl:value-of select="$featureOpr/@id" /></xsl:attribute>
                              <xsl:choose>
                                <xsl:when test="$featureOpr/@mode='DELETE'">
                                  <del style="background:#ffe6e6;">
                          <xsl:value-of select="$featureOpr/name"/>
                        </del>
                                </xsl:when>
                                <xsl:when test="$featureOpr/@mode='INSERT'">
                                  <ins style="background:#e6ffe6;">
                          <xsl:value-of select="$featureOpr/name"/>
                        </ins>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="replace_ins">
                                    <xsl:with-param name="string" select="$featureOpr/name" />
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                            </a>
                          </LI>
                        </xsl:for-each>
                      </UL>
                    </xsl:when>
                    <xsl:otherwise>
                      <P STYLE="margin-left:20px">keine</P>
                    </xsl:otherwise>
                  </xsl:choose>
                </DIV>
                <table>
                  <tr>
                    <td width="100%">
                      <p align="right">
                        <small><A><xsl:attribute name="href">#<xsl:value-of select="$objektart/@id"/></xsl:attribute>zurück zur Objektartengruppe/zum Objektartenbereich: <xsl:value-of select="$objektart/name"/></A></small>
                      </p>
                    </td>
                    <td>
                      <BR/>
                    </td>
                  </tr>
                </table>
                <DIV>
                  <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureAttribute[attribute::id=$featuretype/characterizedBy/@idref]">
                    <xsl:variable name="featureAtt" select="." />
                    <div>
                      <xsl:choose>
                        <xsl:when test="$featureAtt/@mode='DELETE'">
                          <xsl:attribute name="style">background-color:#ffe6e6;</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$featureAtt/@mode='INSERT'">
                          <xsl:attribute name="style">background-color:#e6ffe6;</xsl:attribute>
                        </xsl:when>
                      </xsl:choose>
                      <A><xsl:attribute name="name"><xsl:value-of select="$featuretype/@id" />-<xsl:value-of select="$featureAtt/@id" /></xsl:attribute>
                        <table>
                          <tr>
                            <td width="100%">
                              <xsl:choose>
                                <xsl:when test="$featureAtt/@mode='DELETE'">
                                  <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="$featureAtt/@mode='INSERT'">
                                  <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
                                </xsl:otherwise>
                              </xsl:choose>
                              <b>
                                  Attributart: 
               <xsl:choose>
                    <xsl:when test="$featureAtt/@mode='DELETE'">
                      <del style="background:#ffe6e6;">
                        <xsl:value-of select="$featureAtt/name"/>
                      </del>
                    </xsl:when>
                    <xsl:when test="$featureAtt/@mode='INSERT'">
                      <ins style="background:#e6ffe6;">
                        <xsl:value-of select="$featureAtt/name"/>
                      </ins>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="replace_ins">
                        <xsl:with-param name="string" select="$featureAtt/name"/>
                      </xsl:call-template>
                    </xsl:otherwise>
                  </xsl:choose>
                </b>
                            </td>
                            <td>
                              <br/>
                            </td>
                          </tr>
                        </table>
                      </A>
                      <BR/>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureAtt/definition)" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Definition:</b>
                          </P>
                          <xsl:for-each select="$featureAtt/definition">
                            <P STYLE="margin-left:20px">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="." />
                              </xsl:call-template>
                            </P>
                          </xsl:for-each>
                          <xsl:if test="count($featureAtt/objektbildend) > 0">
                            <P STYLE="margin-left:20px">Diese Attributart ist objektbildend.</P>
                          </xsl:if>
                        </xsl:if>
                      </DIV>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureAtt/code)" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Kennung:</b>
                          </P>
                          <xsl:for-each select="$featureAtt/code">
                            <P STYLE="margin-left:20px">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="." />
                              </xsl:call-template>
                            </P>
                          </xsl:for-each>
                        </xsl:if>
                      </DIV>
            <DIV>
              <xsl:variable name="nft" select="count($featureAtt/retired)" />
              <xsl:if test="$nft >= 1">
                <xsl:if test="$featureAtt/retired">
                 <P>
                   <b>Stillgelegt:</b>
                 </P>
                 <xsl:choose>
                 <xsl:when test="$featureAtt/taggedValue[@tag='AAA:GueltigBis']">
                 <P STYLE="margin-left:20px">
                  Gültig bis GeoInfoDok
                  <xsl:call-template name="replace_ins">
                    <xsl:with-param name="string" select="$featureAtt/taggedValue[@tag='AAA:GueltigBis'][1]"/>
                  </xsl:call-template>
                 </P>
                 </xsl:when>
                 <xsl:otherwise>
                  <P STYLE="margin-left:20px">Ja</P>
                 </xsl:otherwise>
                 </xsl:choose>
                </xsl:if>
              </xsl:if>
            </DIV>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureAtt/cardinality)" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Kardinalitaet:</b>
                          </P>
                          <xsl:for-each select="$featureAtt/cardinality">
                            <P STYLE="margin-left:20px">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="." />
                              </xsl:call-template>
                            </P>
                          </xsl:for-each>
                        </xsl:if>
                      </DIV>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureAtt/ValueDataType)" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Datentyp:</b>
                          </P>
                          <xsl:for-each select="$featureAtt/ValueDataType">
                            <xsl:variable name="dt" select="." />
                            <xsl:variable name="nft2" select="count(/child::FC_FeatureCatalogue/child::AC_FeatureType[attribute::id=$dt/@idref])" />
                            <xsl:choose>
                              <xsl:when test="$nft2 = 0">
                                <P STYLE="margin-left:20px">
                                  <xsl:call-template name="replace_ins">
                                    <xsl:with-param name="string" select="." />
                                  </xsl:call-template>
                                </P>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType">
                                  <xsl:if test=" @id = $dt/@idref">
                                    <P STYLE="margin-left:20px">
                                      <a><xsl:attribute name="href">#<xsl:value-of select="$dt/@idref" /></xsl:attribute>
                                        <xsl:call-template name="replace_ins">
                                          <xsl:with-param name="string" select="$dt" />
                                        </xsl:call-template>
                                      </a>
                                    </P>
                                  </xsl:if>
                                </xsl:for-each>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:for-each>
                        </xsl:if>
                        <xsl:if test=" $featureAtt/ValueDomainType = 1">
                          <P>
                            <b>Wertearten:</b>
                          </P>
                          <TABLE>
                            <TR>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Bezeichner</TD>
                                <td width="20">
                                  <BR/>
                                </td>
                              <TD>Wert</TD>
                            </TR>
                            <xsl:for-each select="/FC_FeatureCatalogue/FC_Value[attribute::id=$featureAtt/enumeratedBy/@idref]">
                              <xsl:variable name="fcvalue" select="." />
                              <TR>
                                <td width="20">
                                  <BR/>
                                </td>
                                <TD valign="top">
                                  <xsl:choose>
                                    <xsl:when test="$fcvalue/@mode='DELETE'">
                                      <del style="background:#ffe6e6;">
                          <xsl:call-template name="replace_ins">
                            <xsl:with-param name="string" select="$fcvalue/label"/>
                          </xsl:call-template>
                        </del>
                                    </xsl:when>
                                    <xsl:when test="$fcvalue/@mode='INSERT'">
                                      <ins style="background:#e6ffe6;">
                          <xsl:call-template name="replace_ins">
                            <xsl:with-param name="string" select="$fcvalue/label"/>
                          </xsl:call-template>
                        </ins>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <xsl:call-template name="replace_ins">
                                        <xsl:with-param name="string" select="$fcvalue/label" />
                                      </xsl:call-template>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                  <xsl:if test="count($fcvalue/definition)=1 or count($fcvalue/retired)=1 or count($fcvalue/taggedValue)>=1">
                                    <table>
                                      <tr>
                                        <td width="20">&#160;</td>
                                        <td>
                                          <small>
                              <xsl:choose>
                                <xsl:when test="$fcvalue/@mode='DELETE'">
                                  <del style="background:#ffe6e6;">
                                    <xsl:call-template name="replace_ins">
                                      <xsl:with-param name="string" select="$fcvalue/definition"/>
                                    </xsl:call-template>
                                  </del>
                                </xsl:when>
                                <xsl:when test="$fcvalue/@mode='INSERT'">
                                  <ins style="background:#e6ffe6;">
                                    <xsl:call-template name="replace_ins">
                                      <xsl:with-param name="string" select="$fcvalue/definition"/>
                                    </xsl:call-template>
                                  </ins>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="replace_ins">
                                    <xsl:with-param name="string" select="$fcvalue/definition"/>
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                            </small>
              <xsl:variable name="nft" select="count($fcvalue/retired)" />
              <xsl:if test="$nft >= 1">
                <xsl:if test="$fcvalue/retired">
					<br/><small><em>Stillgelegt: 
                 <xsl:choose>
                 <xsl:when test="$fcvalue/taggedValue[@tag='AAA:GueltigBis']">
                  Gültig bis GeoInfoDok
                  <xsl:call-template name="replace_ins">
                    <xsl:with-param name="string" select="$fcvalue/taggedValue[@tag='AAA:GueltigBis'][1]"/>
                  </xsl:call-template>
                 </xsl:when>
                 <xsl:otherwise>Ja</xsl:otherwise>
                 </xsl:choose>
                 </em></small>
                </xsl:if>
              </xsl:if>
                                          <xsl:if test="count($fcvalue/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']) >= 1">
                                  <xsl:for-each select="$fcvalue/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']">
                                    <xsl:variable name="tv" select="." />
                                    <br/><small><em>
                                        <xsl:choose>
                                          <xsl:when test="$tv/@mode='DELETE'">
                                            <del style="background:#ffe6e6;">
                                              <xsl:call-template name="replace_ins">
                                                <xsl:with-param name="string" select="$tv/@tag"/>
                                              </xsl:call-template>
                                            </del>
                                          </xsl:when>
                                          <xsl:when test="$tv/@mode='INSERT'">
                                            <ins style="background:#e6ffe6;">
                                              <xsl:call-template name="replace_ins">
                                                <xsl:with-param name="string" select="$tv/@tag"/>
                                              </xsl:call-template>
                                            </ins>
                                          </xsl:when>
                                          <xsl:otherwise>
                                            <xsl:call-template name="replace_ins">
                                              <xsl:with-param name="string" select="$tv/@tag"/>
                                            </xsl:call-template>
                                          </xsl:otherwise>
                                        </xsl:choose>:
                                        <xsl:choose>
                                          <xsl:when test="$tv/@mode='DELETE'">
                                            <del style="background:#ffe6e6;">
                                              <xsl:value-of select="$tv/text()"/>
                                            </del>
                                          </xsl:when>
                                          <xsl:when test="$tv/@mode='INSERT'">
                                            <ins style="background:#e6ffe6;">
                                              <xsl:value-of select="$tv/text()"/>
                                            </ins>
                                          </xsl:when>
                                          <xsl:otherwise>
                                            <xsl:value-of select="$tv/text()"/>
                                          </xsl:otherwise>
                                        </xsl:choose>
                                      </em></small>
                                  </xsl:for-each>
                                          </xsl:if>
                                        </td>
                                      </tr>
                                    </table>
                                  </xsl:if>
                                </TD>
                                <td width="20">
                                  <BR/>
                                </td>
                                <TD valign="top">
                                  <xsl:choose>
                                    <xsl:when test="$fcvalue/@mode='DELETE'">
                                      <del style="background:#ffe6e6;">
                          <xsl:value-of select="$fcvalue/code"/>
                        </del>
                                    </xsl:when>
                                    <xsl:when test="$fcvalue/@mode='INSERT'">
                                      <ins style="background:#e6ffe6;">
                          <xsl:value-of select="$fcvalue/code"/>
                        </ins>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <xsl:value-of select="$fcvalue/code" />
                                    </xsl:otherwise>
                                  </xsl:choose>
                                  <xsl:if test="count($fcvalue/grunddatenbestand) >= 1">
                                    <xsl:choose>
                                      <xsl:when test="$fcvalue/grunddatenbestand/@mode='DELETE'">
                                        <del style="background:#ffe6e6;">
                  (G)
                </del>
                                      </xsl:when>
                                      <xsl:when test="$fcvalue/grunddatenbestand/@mode='INSERT'">
                                        <ins style="background:#e6ffe6;">
                  (G)
                </ins>
                                      </xsl:when>
                                      <xsl:otherwise>
                                        (G)
                                      </xsl:otherwise>
                                    </xsl:choose>
                                  </xsl:if>
                                  <xsl:if test="count($fcvalue/taggedValue[@tag='AAA:Landnutzung' and translate(.,'TRUE','true')='true']) >= 1">
                                    <xsl:choose>
                                      <xsl:when test="$fcvalue/taggedValue[@tag='AAA:Landnutzung' and translate(.,'TRUE','true')='true'][1]/@mode='DELETE'">
                                        <del style="background:#ffe6e6;">
                  (LN)
                </del>
                                      </xsl:when>
                                      <xsl:when test="$fcvalue/taggedValue[@tag='AAA:Landnutzung' and translate(.,'TRUE','true')='true'][1]/@mode='INSERT'">
                                        <ins style="background:#e6ffe6;">
                  (LN)
                </ins>
                                      </xsl:when>
                                      <xsl:otherwise>
                                        (LN)
                                      </xsl:otherwise>
                                    </xsl:choose>
                                  </xsl:if>
                                </TD>
                              </TR>
                            </xsl:for-each>
                          </TABLE>
                        </xsl:if>
                      </DIV>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureAtt/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung'])" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Weitere Angaben:</b>
                          </P>
                          <TABLE>
                            <TR>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Bezeichner</TD>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Wert</TD>
                            </TR>
                            <xsl:for-each select="$featureAtt/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']">
                              <xsl:variable name="tv" select="." />
                              <TR>
                                <td width="20">
                                  <BR/>
                                </td>
                                <TD valign="top">
                                  <xsl:choose>
                                    <xsl:when test="$tv/@mode='DELETE'">
                                      <del style="background:#ffe6e6;">
                          <xsl:call-template name="replace_ins">
                            <xsl:with-param name="string" select="$tv/@tag"/>
                          </xsl:call-template>
                        </del>
                                    </xsl:when>
                                    <xsl:when test="$tv/@mode='INSERT'">
                                      <ins style="background:#e6ffe6;">
                          <xsl:call-template name="replace_ins">
                            <xsl:with-param name="string" select="$tv/@tag"/>
                          </xsl:call-template>
                        </ins>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <xsl:call-template name="replace_ins">
                                        <xsl:with-param name="string" select="$tv/@tag" />
                                      </xsl:call-template>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </TD>
                              <td width="20">
                                <BR/>
                              </td>
                                <TD valign="top">
                                  <xsl:choose>
                                    <xsl:when test="$tv/@mode='DELETE'">
                                      <del style="background:#ffe6e6;">
                          <xsl:value-of select="$tv/text()"/>
                        </del>
                                    </xsl:when>
                                    <xsl:when test="$tv/@mode='INSERT'">
                                      <ins style="background:#e6ffe6;">
                          <xsl:value-of select="$tv/text()"/>
                        </ins>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <xsl:value-of select="$tv/text()" />
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </TD>
                              </TR>
                            </xsl:for-each>
                          </TABLE>
                        </xsl:if>
                      </DIV>
                      <table>
                        <tr>
                          <td width="100%">
                            <p align="right">
                              <small><A><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id"/></xsl:attribute>zurück zu: <xsl:value-of select="$featuretype/name"/></A></small>
                            </p>
                          </td>
                          <td>
                            <BR/>
                          </td>
                        </tr>
                      </table>
                    </div>
                  </xsl:for-each>
                </DIV>
                <DIV>
                  <xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole[inType/@idref=$featuretype/@id]">
                    <xsl:variable name="featureRel" select="." />
                    <div>
                      <xsl:choose>
                        <xsl:when test="$featureRel/@mode='DELETE'">
                          <xsl:attribute name="style">background-color:#ffe6e6;</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$featureRel/@mode='INSERT'">
                          <xsl:attribute name="style">background-color:#e6ffe6;</xsl:attribute>
                        </xsl:when>
                      </xsl:choose>
                      <A><xsl:attribute name="name"><xsl:value-of select="$featuretype/@id" />-<xsl:value-of select="$featureRel/@id" /></xsl:attribute>
                        <table>
                          <tr>
                            <td width="100%">
                              <xsl:choose>
                                <xsl:when test="$featureRel/@mode='DELETE'">
                                  <xsl:attribute name="bgcolor">#ffe6e6</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="$featureRel/@mode='INSERT'">
                                  <xsl:attribute name="bgcolor">#e6ffe6</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
                                </xsl:otherwise>
                              </xsl:choose>
                              <b>
                                  Relationsart: 
               <xsl:choose>
                <xsl:when test="$featureRel/@mode='DELETE'">
                  <del style="background:#ffe6e6;">
                    <xsl:value-of select="$featureRel/name"/>
                  </del>
                </xsl:when>
                <xsl:when test="$featureRel/@mode='INSERT'">
                  <ins style="background:#e6ffe6;">
                    <xsl:value-of select="$featureRel/name"/>
                  </ins>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="replace_ins">
                    <xsl:with-param name="string" select="$featureRel/name"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </b>
                            </td>
                            <td>
                              <br/>
                            </td>
                          </tr>
                        </table>
                      </A>
                      <BR/>
                      <DIV>
                        <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureRelationship[attribute::id=$featureRel/relation/@idref]">
                          <xsl:variable name="nft" select="count(definition)" />
                          <xsl:if test="$nft >= 1">
                            <P>
                              <b>Definition:</b>
                            </P>
                            <xsl:for-each select="definition">
                              <P STYLE="margin-left:20px">
                                <xsl:call-template name="replace_ins">
                                  <xsl:with-param name="string" select="." />
                                </xsl:call-template>
                              </P>
                            </xsl:for-each>
                          </xsl:if>
                        </xsl:for-each>
                      </DIV>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureRel/definition)" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Anmerkung:</b>
                          </P>
                          <xsl:for-each select="$featureRel/definition">
                            <P STYLE="margin-left:20px">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="." />
                              </xsl:call-template>
                            </P>
                          </xsl:for-each>
                        </xsl:if>
                        <xsl:if test="count($featureRel/objektbildend) > 0">
                          <P STYLE="margin-left:20px">Diese Relationsart ist objektbildend.</P>
                        </xsl:if>
                      </DIV>
                      <DIV>
                        <xsl:variable name="nft" select="count($featureRel/code)" />
                        <xsl:if test="$nft >= 1">
                          <P>
                            <b>Kennung:</b>
                          </P>
                          <xsl:for-each select="$featureRel/code">
                            <P STYLE="margin-left:20px">
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="." />
                              </xsl:call-template>
                            </P>
                          </xsl:for-each>
                        </xsl:if>
                      </DIV>
            <DIV>
              <xsl:variable name="nft" select="count($featureRel/retired)" />
              <xsl:if test="$nft >= 1">
                <xsl:if test="$featureRel/retired">
                 <P>
                   <b>Stillgelegt:</b>
                 </P>
                 <xsl:choose>
                 <xsl:when test="$featureRel/taggedValue[@tag='AAA:GueltigBis']">
                 <P STYLE="margin-left:20px">
                  Gültig bis GeoInfoDok
                  <xsl:call-template name="replace_ins">
                    <xsl:with-param name="string" select="$featureRel/taggedValue[@tag='AAA:GueltigBis'][1]"/>
                  </xsl:call-template>
                 </P>
                 </xsl:when>
                 <xsl:otherwise>
                  <P STYLE="margin-left:20px">Ja</P>
                 </xsl:otherwise>
                 </xsl:choose>
                </xsl:if>
              </xsl:if>
            </DIV>
                      <DIV>
                        <P>
                          <b>Kardinalitaet:</b>
                        </P>
                        <table>
                          <tr>
                            <td width="20">
                              <BR/>
                            </td>
                            <td>
                              <xsl:call-template name="replace_ins">
                                <xsl:with-param name="string" select="$featureRel/cardinality" />
                              </xsl:call-template>
                              <xsl:if test="$featureRel/orderIndicator = 1">
                                (geordnet)
                              </xsl:if>
                            </td>
                          </tr>
                        </table>
                      </DIV>
                      <DIV>
                        <P>
                          <b>Objektart des Relationspartners:</b>
                        </P>
                        <UL>
                          <xsl:for-each select="$featureRel/FeatureTypeIncluded">
                            <xsl:variable name="ftinc" select="." />
                            <LI>
                              <xsl:variable name="nft" select="count(/child::FC_FeatureCatalogue/child::AC_FeatureType[attribute::id=$ftinc/@idref])" />
                              <xsl:choose>
                                <xsl:when test="$nft = 0">
                                  <xsl:value-of select="$ftinc/@name" />
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:for-each select="/FC_FeatureCatalogue/AC_FeatureType[attribute::id=$ftinc/@idref]">
                                    <a><xsl:attribute name="href">#<xsl:value-of select="$ftinc/@idref" /></xsl:attribute>
                                      <xsl:value-of select="$ftinc/@name" />
                                    </a>
                                  </xsl:for-each>
                                </xsl:otherwise>
                              </xsl:choose>
                            </LI>
                            <xsl:for-each select="$featureRel/InverseRole">
                              <xsl:variable name="ir" select="." />
                              <xsl:for-each select="/FC_FeatureCatalogue/FC_RelationshipRole[attribute::id=$ir/@idref]">
                                <BR/>inverse Relationsrichtung zu
                                <a><xsl:attribute name="href">#<xsl:value-of select="$ftinc/@idref" />-<xsl:value-of select="@id" /></xsl:attribute>
                                  <xsl:value-of select="name" />
                                </a>
                              </xsl:for-each>
                            </xsl:for-each>
                          </xsl:for-each>
                        </UL>
                        <DIV>
                          <xsl:variable name="nft" select="count($featureRel/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung'])" />
                          <xsl:if test="$nft >= 1">
                            <P>
                              <b>Weitere Angaben:</b>
                            </P>
                            <TABLE>
                            <TR>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Bezeichner</TD>
                              <td width="20">
                                <BR/>
                              </td>
                              <TD>Wert</TD>
                            </TR>
                              <xsl:for-each select="$featureRel/taggedValue[@tag!='AAA:GueltigBis' and @tag!='AAA:Landnutzung']">
                                <xsl:variable name="tv" select="." />
                                <TR>
                                  <td width="20">
                                    <BR/>
                                  </td>
                                  <TD valign="top">
                                    <xsl:choose>
                                      <xsl:when test="$tv/@mode='DELETE'">
                                        <del style="background:#ffe6e6;">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="$tv/@tag"/>
                        </xsl:call-template>
                      </del>
                                      </xsl:when>
                                      <xsl:when test="$tv/@mode='INSERT'">
                                        <ins style="background:#e6ffe6;">
                        <xsl:call-template name="replace_ins">
                          <xsl:with-param name="string" select="$tv/@tag"/>
                        </xsl:call-template>
                      </ins>
                                      </xsl:when>
                                      <xsl:otherwise>
                                        <xsl:call-template name="replace_ins">
                                          <xsl:with-param name="string" select="$tv/@tag" />
                                        </xsl:call-template>
                                      </xsl:otherwise>
                                    </xsl:choose>
                                  </TD>
                              <td width="20">
                                <BR/>
                              </td>
                                  <TD valign="top">
                                    <xsl:choose>
                                      <xsl:when test="$tv/@mode='DELETE'">
                                        <del style="background:#ffe6e6;">
                        <xsl:value-of select="$tv/text()"/>
                      </del>
                                      </xsl:when>
                                      <xsl:when test="$tv/@mode='INSERT'">
                                        <ins style="background:#e6ffe6;">
                        <xsl:value-of select="$tv/text()"/>
                      </ins>
                                      </xsl:when>
                                      <xsl:otherwise>
                                        <xsl:value-of select="$tv/text()" />
                                      </xsl:otherwise>
                                    </xsl:choose>
                                  </TD>
                                </TR>
                              </xsl:for-each>
                            </TABLE>
                          </xsl:if>
                        </DIV>
                        <table>
                          <tr>
                            <td width="100%">
                              <p align="right">
                                <small><A><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id"/></xsl:attribute>zurück zu: <xsl:value-of select="$featuretype/name"/></A></small>
                              </p>
                            </td>
                            <td>
                              <BR/>
                            </td>
                          </tr>
                        </table>
                      </DIV>
                    </div>
                  </xsl:for-each>
                  <DIV>
                    <xsl:for-each select="/FC_FeatureCatalogue/FC_FeatureOperation[attribute::id=$featuretype/characterizedBy/@idref]">
                      <xsl:variable name="featureOpr" select="." />
                      <A><xsl:attribute name="name"><xsl:value-of select="$featuretype/@id" />-<xsl:value-of select="$featureOpr/@id" /></xsl:attribute>
                        <table>
                          <tr>
                            <td width="100%" bgcolor="#F0F0F0">
                              <b>
                                  Methode: 
                                  <xsl:value-of select="$featureOpr/name"/>
          </b>
                            </td>
                            <td>
                              <BR/>
                            </td>
                          </tr>
                        </table>
                      </A>
                      <xsl:variable name="nft" select="count($featureOpr/definition)" />
                      <xsl:if test="$nft >= 1">
                        <P>
                          <b>Definition:</b>
                        </P>
                        <xsl:for-each select="$featureOpr/definition">
                          <P STYLE="margin-left:20px">
                            <xsl:value-of select="." />
                          </P>
                        </xsl:for-each>
                      </xsl:if>
                      <table>
                        <tr>
                          <td width="100%">
                            <p align="right">
                              <small><A><xsl:attribute name="href">#<xsl:value-of select="$featuretype/@id"/></xsl:attribute>zurück zu: <xsl:value-of select="$featuretype/name"/></A></small>
                            </p>
                          </td>
                          <td>
                            <BR/>
                          </td>
                        </tr>
                      </table>
                    </xsl:for-each>
                  </DIV>
                </DIV>
              </div>
            </xsl:for-each>
          </xsl:if>
        </div>
      </xsl:for-each>
    </body>

    </html>
  </xsl:template>
  <xsl:template name="replace_ins">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'[[ins]]')">
        <xsl:call-template name="replace_del">
          <xsl:with-param name="string" select="substring-before($string,'[[ins]]')" />
        </xsl:call-template>
        <ins style="background:#e6ffe6;">
        <xsl:value-of select="substring-before(substring-after($string,'[[ins]]'),'[[/ins]]')"/>
      </ins>
        <xsl:call-template name="replace_ins">
          <xsl:with-param name="string" select="substring-after($string,'[[/ins]]')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replace_del">
          <xsl:with-param name="string" select="$string" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="replace_del">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'[[del]]')">
        <xsl:value-of select="substring-before($string,'[[del]]')" />
        <del style="background:#ffe6e6;">
        <xsl:value-of select="substring-before(substring-after($string,'[[del]]'),'[[/del]]')"/>
      </del>
        <xsl:call-template name="replace_del">
          <xsl:with-param name="string" select="substring-after($string,'[[/del]]')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="replace_br">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'&#xA;')">
        <xsl:value-of select="substring-before($string,'&#xA;')" />
        <br/>
        <xsl:call-template name="replace_br">
          <xsl:with-param name="string" select="substring-after($string,'&#xA;')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>