<?xml version="1.0"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- 
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 -->
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
        
        <h2>Service Creation Test</h2>
        <xsl:call-template name="ServiceRegistrationBenchMark">
          <xsl:with-param name="results" select="/tests/test/result[@type='REG']"/>
        </xsl:call-template>
      </BODY>
    </HTML>
  </xsl:template>
  
  <xsl:template name="ServiceRegistrationBenchMark">
    <xsl:param name="results"/>
    
    <xsl:for-each select="$results">
      x: <xsl:value-of select="@x-value"/> y: <xsl:value-of select="@y-value"/> 
    </xsl:for-each>

    <p/>
    <xsl:variable name="cb">http://chart.apis.google.com/chart?cht=lxy&amp;chs=400x300&amp;chxt=x,y&amp;chco=3072F3,ff0000,00aaaa&amp;chls=2,4,1&amp;chm=s,000000,0,-1,5|s,000000,1,-1,5&amp;chdl=JBoss|Dummy&amp;chdlp=r&amp;</xsl:variable>
    <xsl:variable name="cb2" select="concat($cb, 'chxr=0,0,1000|1,0,2000&amp;chds=0,1000,0,2000&amp;')"/>

    <xsl:variable name="avg100">
      <xsl:call-template name="GetAverageYValue">
        <xsl:with-param name="data" select="$results[(../parameters/parameter/@name='Total Services') and (../parameters/parameter/@value='100')]"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="avg1000">
      <xsl:call-template name="GetAverageYValue">
        <xsl:with-param name="data" select="$results[(../parameters/parameter/@name='Total Services') and (../parameters/parameter/@value='1000')]"/>
      </xsl:call-template>
    </xsl:variable>
        
    <xsl:variable name="cb3" select="concat($cb2, 'chd=t:100,1000|', $avg100, ',', $avg1000, '|100,1000|200,100')"/>
    ########### <xsl:value-of select="$cb3"></xsl:value-of>
    <p/>
    <img>
      <xsl:attribute name="src"><xsl:value-of select="$cb3"/></xsl:attribute>
    </img>
    
    <!-- 
    <br/>
    <img src="http://chart.apis.google.com/chart?cht=lxy&amp;chs=400x300&amp;chxt=x,y&amp;chxr=0,0,250|1,0,200&amp;chd=t:25,250|10,20|25,250|100,200&amp;chds=0,250,0,200&amp;chco=3072F3,ff0000,00aaaa&amp;chls=2,4,1&amp;chm=s,000000,0,-1,5|s,000000,1,-1,5&amp;chdl=JBoss|Equinox&amp;chdlp=r" alt="Chart showing Service Creation Benchmark"/>
    <br/>
    -->
  </xsl:template>
  
  <xsl:template name="GetAverageYValue">
    <xsl:param name="data"/>
    <xsl:value-of select="round(sum($data/@y-value) div count($data))"/>
  </xsl:template>
</xsl:transform>
