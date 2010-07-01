<?xml version="1.0"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <HTML>
      <head>
      </head>
      <BODY>
        <h1>Performance Test Report</h1>
        <xsl:for-each select="tests/test">
          <h2>
            <xsl:value-of select="@name" />
          </h2>
        </xsl:for-each>
      </BODY>
    </HTML>
  </xsl:template>
</xsl:transform>
