<?xml version="1.0" encoding="utf-8"?>
<!-- 
(c) 2001-2020 interactive instruments GmbH, Bonn
im Auftrag der Arbeitsgemeinschaft der Vermessungsverwaltungen der Länder der Bundesrepublik Deutschland (AdV)
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="diff-text">
		<xsl:param name="mode"/>
		<xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="$mode='DELETE'">
        <del><xsl:value-of select="$text" /></del>
      </xsl:when>
      <xsl:when test="$mode='INSERT'">
        <ins><xsl:value-of select="$text" /></ins>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="diff">
		<xsl:param name="element"/>
    <xsl:choose>
      <xsl:when test="$element/@mode='DELETE'">
        <del>
          <xsl:call-template name="replace-ins">
            <xsl:with-param name="string" select="$element" />
          </xsl:call-template>
        </del>
      </xsl:when>
      <xsl:when test="$element/@mode='INSERT'">
        <ins>
          <xsl:call-template name="replace-ins">
            <xsl:with-param name="string" select="$element" />
          </xsl:call-template>
        </ins>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replace-ins">
          <xsl:with-param name="string" select="$element" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="replace-ins">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'[[ins]]')">
        <xsl:call-template name="replace-del">
          <xsl:with-param name="string" select="substring-before($string,'[[ins]]')" />
        </xsl:call-template>
        <ins>
        <xsl:value-of select="substring-before(substring-after($string,'[[ins]]'),'[[/ins]]')"/>
      </ins>
        <xsl:call-template name="replace-ins">
          <xsl:with-param name="string" select="substring-after($string,'[[/ins]]')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replace-del">
          <xsl:with-param name="string" select="$string" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="replace-del">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'[[del]]')">
        <xsl:value-of select="substring-before($string,'[[del]]')" />
        <del>
        <xsl:value-of select="substring-before(substring-after($string,'[[del]]'),'[[/del]]')"/>
      </del>
        <xsl:call-template name="replace-del">
          <xsl:with-param name="string" select="substring-after($string,'[[/del]]')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="current">
		<xsl:param name="element"/>
    <xsl:choose>
      <xsl:when test="$element/@mode='DELETE'"></xsl:when>
      <xsl:when test="$element/@mode='INSERT'">
        <ins>
          <xsl:call-template name="replace-ins-current">
            <xsl:with-param name="string" select="$element" />
          </xsl:call-template>
        </ins>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replace-ins-current">
          <xsl:with-param name="string" select="$element" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="replace-ins-current">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'[[ins]]')">
        <xsl:call-template name="replace-del-current">
          <xsl:with-param name="string" select="substring-before($string,'[[ins]]')" />
        </xsl:call-template>
        <xsl:value-of select="substring-before(substring-after($string,'[[ins]]'),'[[/ins]]')"/>
        <xsl:call-template name="replace-ins-current">
          <xsl:with-param name="string" select="substring-after($string,'[[/ins]]')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replace-del-current">
          <xsl:with-param name="string" select="$string" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="replace-del-current">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="contains($string,'[[del]]')">
        <xsl:value-of select="substring-before($string,'[[del]]')" />
        <xsl:call-template name="replace-del-current">
          <xsl:with-param name="string" select="substring-after($string,'[[/del]]')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>